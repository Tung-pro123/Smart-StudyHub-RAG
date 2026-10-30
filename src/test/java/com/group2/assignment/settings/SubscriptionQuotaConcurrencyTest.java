package com.group2.assignment.settings;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.settings.service.SubscriptionQuotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SubscriptionQuotaConcurrencyTest {
    @Autowired UserRepository users;
    @Autowired SubscriptionQuotaService quota;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void concurrentWritesOnlyUseTheLastAvailableFreeQueryOnce() throws Exception {
        String name = "quota-" + UUID.randomUUID().toString().substring(0, 8);
        User user = users.saveAndFlush(User.builder().username(name).email(name + "@example.com")
                .fullName("Quota Test").password("test-hash").dailyRagCount(49)
                .lastRagResetDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))).build());
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch loaded = new CountDownLatch(4);
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 4; i++) {
                results.add(executor.submit(() -> {
                    return new TransactionTemplate(transactions).execute(status -> {
                        users.findById(user.getId()).orElseThrow();
                        loaded.countDown();
                        try {
                            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }
                        return quota.recordSuccessfulRagQuery(user.getId());
                    });
                }));
            }
            assertThat(loaded.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int accepted = 0;
            for (Future<Boolean> result : results) if (result.get(15, TimeUnit.SECONDS)) accepted++;
            assertThat(accepted).isEqualTo(1);
            assertThat(users.findById(user.getId()).orElseThrow().getDailyRagCount()).isEqualTo(50);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            users.deleteById(user.getId());
        }
    }
}
