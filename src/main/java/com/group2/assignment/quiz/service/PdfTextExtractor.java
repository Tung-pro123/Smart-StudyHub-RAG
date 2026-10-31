package com.group2.assignment.quiz.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        return extractText(file, null, null);
    }

    public String extractText(MultipartFile file, Integer fromPage, Integer toPage) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được rỗng!");
        }
        return extractFromBytes(file.getBytes(), fromPage, toPage);
    }

    public String extractText(File file, Integer fromPage, Integer toPage) throws IOException {
        if (file == null || !file.exists() || file.length() == 0) {
            throw new IllegalArgumentException("File tài liệu trên hệ thống không tồn tại hoặc rỗng!");
        }
        try (PDDocument document = PDDocument.load(file)) {
            return extractFromDocument(document, fromPage, toPage);
        }
    }

    private String extractFromBytes(byte[] bytes, Integer fromPage, Integer toPage) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return extractFromDocument(document, fromPage, toPage);
        }
    }

    private String extractFromDocument(PDDocument document, Integer fromPage, Integer toPage) throws IOException {
        int totalPages = document.getNumberOfPages();
        if (totalPages == 0) {
            throw new IllegalArgumentException("Tài liệu PDF không chứa trang nào!");
        }

        int start = (fromPage != null && fromPage > 0) ? Math.min(fromPage, totalPages) : 1;
        int end = (toPage != null && toPage > 0) ? Math.min(toPage, totalPages) : totalPages;

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(start);
        stripper.setEndPage(end);

        String text = stripper.getText(document);
        if (text == null || text.trim().isBlank()) {
            throw new IllegalArgumentException("Không thể trích xuất văn bản từ trang " + start + " đến " + end + " (PDF chỉ có ảnh scan hoặc trang trắng)!");
        }

        return text.trim();
    }
}
