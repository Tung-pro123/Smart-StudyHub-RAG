import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class CreatePdfs {

    public static void main(String[] args) throws Exception {
        new File("test-fixtures").mkdirs();

        createPdf("test-fixtures/slide-spring-jpa-core.pdf",
                "HSF302 - JPA and Spring Boot Architecture Guide",
                new String[]{
                        "Chapter 1: Entity Lifecycle in Java Persistence API (JPA)\n" +
                        "In JPA and Hibernate, an entity can exist in four primary lifecycle states: New/Transient, Managed/Persistent, Detached, and Removed.\n" +
                        "1. New/Transient: The entity has just been instantiated using the Java new keyword and is not associated with any EntityManager persistence context. It has no primary key identifier in the database.\n" +
                        "2. Managed/Persistent: The entity is associated with an active EntityManager session. Any modifications to its fields are tracked by Hibernate dirty checking mechanism and flushed to the database on commit.\n" +
                        "3. Detached: The entity has a primary key representation in the database, but its EntityManager context was closed or cleared.\n" +
                        "4. Removed: The entity has been marked for deletion via entityManager.remove() and will be deleted from the database table upon transaction commit.",

                        "Chapter 2: Spring Data JPA Repositories and Query Generation\n" +
                        "Spring Data JPA simplifies the data access layer by automatically implementing interface methods extending JpaRepository.\n" +
                        "- Derived Query Methods: Method names like findByUsernameIgnoreCase or findAllByCourseIdOrderByUploadedAtDesc are parsed into SQL queries at application startup.\n" +
                        "- Custom JPQL: The @Query annotation allows declaring custom queries against JPA entities rather than underlying database tables.\n" +
                        "- Transaction Management: Service methods annotated with @Transactional ensure atomic operations with automatic rollback on unhandled RuntimeException."
                });

        createPdf("test-fixtures/slide-spring-security.pdf",
                "HSF302 - Spring Security 6 Architecture & BCrypt Password Hashing",
                new String[]{
                        "Chapter 1: Spring Security Filter Chain and Authentication Architecture\n" +
                        "Spring Security operates through a series of servlet filters known as the SecurityFilterChain.\n" +
                        "When an HTTP request enters the web application, it passes through security filters including SecurityContextHolderFilter, CsrfFilter, and UsernamePasswordAuthenticationFilter.\n" +
                        "AuthenticationProvider validates user credentials using UserDetailsService and BCryptPasswordEncoder.\n" +
                        "Session management handles JSESSIONID cookie creation and persists authentication in the HttpSession."
                });

        createPdf("test-fixtures/slide-microservices-rag.pdf",
                "HSF302 - Retrieval Augmented Generation (RAG) Architecture",
                new String[]{
                        "Chapter 1: RAG Architecture and Vector Search Overview\n" +
                        "Retrieval-Augmented Generation (RAG) grounds Large Language Models on verified course textbook documents to eliminate hallucinations.\n" +
                        "1. Document Chunking: Lecture slides and PDF textbooks are extracted using Apache PDFBox and partitioned into semantic chunks of 300 to 500 words with sentence boundaries preserved.\n" +
                        "2. Semantic Retrieval: Cosine similarity and keyword overlap score relevant chunks against the student query.\n" +
                        "3. Context Injection: Top matching chunks are injected into the Gemini System Prompt with strict citation constraints including source file name and page numbers."
                });

        createPdf("test-fixtures/slide-docker-deployment.pdf",
                "HSF302 - Docker Containerization and Cloud Deployment",
                new String[]{
                        "Chapter 1: Containerizing Spring Boot Applications with Docker\n" +
                        "Containerization packages the Spring Boot application JAR, OpenJDK runtime, and system dependencies into an immutable Docker image.\n" +
                        "Multi-stage builds separate Maven compilation from runtime images to reduce artifact size down to under 200MB.\n" +
                        "Environment variables configure external database credentials and centralized AI API keys securely in production."
                });

        System.out.println("Successfully generated 4 test PDF files in test-fixtures/");
    }

    private static void createPdf(String filePath, String title, String[] pagesContent) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < pagesContent.length; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    cs.newLineAtOffset(45, 740);
                    cs.showText(title + " - Page " + (i + 1));
                    cs.newLineAtOffset(0, -25);

                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.setLeading(15.0f);

                    String content = pagesContent[i];
                    String[] lines = content.split("\n");
                    for (String line : lines) {
                        for (String wrapped : wrapLine(line, 80)) {
                            cs.showText(wrapped);
                            cs.newLine();
                        }
                    }
                    cs.endText();
                }
            }
            doc.save(filePath);
        }
    }

    private static java.util.List<String> wrapLine(String text, int maxChars) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            list.add("");
            return list;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String w : words) {
            if (current.length() + w.length() + 1 > maxChars) {
                list.add(current.toString());
                current = new StringBuilder(w);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(w);
            }
        }
        if (current.length() > 0) {
            list.add(current.toString());
        }
        return list;
    }
}

