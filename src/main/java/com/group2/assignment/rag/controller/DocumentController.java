package com.group2.assignment.rag.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.service.PdfChunkingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/courses/{courseId}")
@RequiredArgsConstructor
public class DocumentController {

    private final CourseRepository courseRepository;
    private final PdfChunkingService pdfChunkingService;

    /**
     * Hiển thị trang quản lý tài liệu và trực quan hóa các đoạn chunks
     */
    @GetMapping("/documents")
    public String showDocumentsPage(@PathVariable Long courseId, Model model, HttpSession session) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

        List<Document> documents = pdfChunkingService.getDocumentsByCourse(courseId);
        List<Chunk> chunks = pdfChunkingService.getChunksByCourse(courseId);

        User currentUser = getCurrentUser(session);
        boolean isPro = currentUser != null && currentUser.isPro();
        int currentDocCount = documents.size();
        int maxDocsFree = PdfChunkingService.MAX_DOCS_FREE;
        boolean isDocLimitReached = !isPro && currentDocCount >= maxDocsFree;

        model.addAttribute("course", course);
        model.addAttribute("documents", documents);
        model.addAttribute("chunks", chunks);
        model.addAttribute("totalDocuments", currentDocCount);
        model.addAttribute("totalChunks", chunks.size());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isPro", isPro);
        model.addAttribute("currentDocCount", currentDocCount);
        model.addAttribute("maxDocsFree", maxDocsFree);
        model.addAttribute("isDocLimitReached", isDocLimitReached);

        return "rag/documents";
    }

    /**
     * Upload file PDF giáo trình và thực hiện trích xuất, cắt chunks
     */
    @PostMapping({"/documents/upload", "/upload"})
    public String uploadPdfDocument(@PathVariable Long courseId,
                                    @RequestParam("file") MultipartFile file,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(session);
            Document doc = pdfChunkingService.processPdf(courseId, file, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tải lên và xử lý tài liệu \"" + doc.getFileName() + "\" thành công (" + doc.getTotalChunks() + " đoạn chunks, " + doc.getTotalPages() + " trang)!");
        } catch (Exception e) {
            log.error("Lỗi khi tải lên tài liệu cho môn học {}: {}", courseId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/documents";
    }

    /**
     * Xóa tài liệu và toàn bộ các chunks liên quan
     */
    @PostMapping("/documents/{docId}/delete")
    public String deleteDocument(@PathVariable Long courseId,
                                 @PathVariable Long docId,
                                 RedirectAttributes redirectAttributes) {
        try {
            pdfChunkingService.deleteDocument(docId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài liệu và các đoạn chunks thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi xóa tài liệu {}: {}", docId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài liệu: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/documents";
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(AuthController.SESSION_USER);
    }
}
