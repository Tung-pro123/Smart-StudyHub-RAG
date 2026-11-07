package com.group2.assignment.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.dto.TaskToggleResultDto;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.entity.StudyTask;
import com.group2.assignment.planner.repository.StudyPlanRepository;
import com.group2.assignment.planner.repository.StudyTaskRepository;
import com.group2.assignment.planner.service.StudyPlannerService;
import com.group2.assignment.settings.service.SystemAiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyPlannerServiceSquadTest {

    @Mock
    private ChunkRepository chunkRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private StudyTaskRepository studyTaskRepository;
    @Mock
    private SystemAiKeyService systemAiKeyService;

    private StudyPlannerService studyPlannerService;

    @BeforeEach
    void setUp() {
        studyPlannerService = new StudyPlannerService(
                chunkRepository,
                courseRepository,
                studyPlanRepository,
                studyTaskRepository,
                new ObjectMapper(),
                systemAiKeyService
        );
    }

    @Test
    @DisplayName("Gói SQUAD phân công thành viên cho từng nhiệm vụ")
    void squadPlanAssignsMembersToTasks() {
        User owner = User.builder()
                .id(1L)
                .username("squad_owner")
                .role("ROLE_STUDENT")
                .subscriptionTier("SQUAD")
                .build();
        Course course = Course.builder()
                .id(10L)
                .code("HSF302")
                .name("JPA and Spring Framework")
                .user(owner)
                .build();
        StudyPlanRequestDto dto = StudyPlanRequestDto.builder()
                .courseId(10L)
                .targetGrade("A")
                .squadMembers(List.of("an", "binh"))
                .build();

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(chunkRepository.findAllByCourseId(10L)).thenReturn(List.of(
                Chunk.builder().content("JPA and Spring Framework course material").build()
        ));
        when(systemAiKeyService.getApiKey()).thenReturn(null);
        when(studyPlanRepository.save(any(StudyPlan.class))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            plan.setId(5L);
            return plan;
        });

        studyPlannerService.generateStudyPlan(dto, owner);

        ArgumentCaptor<List<StudyTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(studyTaskRepository).saveAll(tasksCaptor.capture());
        List<StudyTask> tasks = tasksCaptor.getValue();
        assertEquals(4, tasks.size());
        assertTrue(tasks.stream().allMatch(task -> List.of("an", "binh").contains(task.getAssignee())));

        ArgumentCaptor<StudyPlan> planCaptor = ArgumentCaptor.forClass(StudyPlan.class);
        verify(studyPlanRepository).save(planCaptor.capture());
        assertEquals("A", planCaptor.getValue().getTargetGrade());
    }

    @Test
    @DisplayName("Mục tiêu điểm không hợp lệ bị từ chối")
    void invalidTargetGradeIsRejected() {
        User owner = User.builder()
                .id(1L)
                .username("squad_owner")
                .role("ROLE_STUDENT")
                .subscriptionTier("SQUAD")
                .build();
        StudyPlanRequestDto dto = StudyPlanRequestDto.builder()
                .courseId(10L)
                .targetGrade("Z")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> studyPlannerService.generateStudyPlan(dto, owner));
    }

    @Test
    @DisplayName("Untick nhiệm vụ đưa kế hoạch từ COMPLETED về ACTIVE")
    void untickTaskResetsCompletedStatus() {
        User owner = User.builder()
                .id(1L)
                .username("squad_owner")
                .role("ROLE_STUDENT")
                .subscriptionTier("SQUAD")
                .build();
        Course course = Course.builder()
                .id(10L)
                .code("HSF302")
                .name("JPA and Spring Framework")
                .user(owner)
                .build();
        StudyPlan plan = StudyPlan.builder()
                .id(5L)
                .course(course)
                .status("COMPLETED")
                .progressPercent(100)
                .build();
        StudyTask task = StudyTask.builder()
                .id(7L)
                .plan(plan)
                .weekNumber(1)
                .dayOfWeek("Thứ 3")
                .taskTitle("Ôn tập")
                .isCompleted(true)
                .build();

        when(studyTaskRepository.findById(7L)).thenReturn(Optional.of(task));
        when(studyTaskRepository.save(any(StudyTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studyTaskRepository.countByPlanId(5L)).thenReturn(2L);
        when(studyTaskRepository.countByPlanIdAndIsCompletedTrue(5L)).thenReturn(1L);
        when(studyPlanRepository.findById(5L)).thenReturn(Optional.of(plan));
        when(studyPlanRepository.save(any(StudyPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskToggleResultDto result = studyPlannerService.toggleTaskCompletion(7L, owner);

        assertEquals(false, result.isCompleted());
        assertEquals(50, result.getProgressPercent());
        assertEquals("ACTIVE", result.getPlanStatus());
    }
}
