package com.group2.assignment.quiz.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfTextExtractorTest {

    @Test
    @DisplayName("Trích xuất text chính xác từ trang 2 của file PDF 2 trang")
    void testExtractSpecificPageRange() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 1: Introduction to Spring Framework IoC Container");
                cs.endText();
            }

            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 2: Dependency Injection is the core pattern of Spring");
                cs.endText();
            }

            doc.save(baos);
        }

        MockMultipartFile mockFile = new MockMultipartFile("slide.pdf", "slide.pdf", "application/pdf", baos.toByteArray());

        PdfTextExtractor extractor = new PdfTextExtractor();
        String extractedPage2 = extractor.extractText(mockFile, 2, 2);

        assertTrue(extractedPage2.contains("Dependency Injection"));
        assertFalse(extractedPage2.contains("Introduction to Spring Framework"));
    }
}
