package com.aicopilot.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeExtractionService {

    public String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);

            return rawText.replaceAll("(?m)^[ \t]*\r?\n", "");
        }
    }
}