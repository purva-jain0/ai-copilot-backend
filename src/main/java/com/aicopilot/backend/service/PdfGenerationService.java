package com.aicopilot.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationService {

    @SuppressWarnings("unchecked")
    public byte[] generateAtsResume(Map<String, Object> profileData) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Industry standard 1-inch margins (72 points) for ATS readability
        Document document = new Document(PageSize.A4, 72, 72, 72, 72);
        PdfWriter.getInstance(document, out);
        document.open();

        // Standard ATS-Safe Fonts (Helvetica is highly machine-readable)
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font contactFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);

        // --- 1. HEADER (Contact Info) ---
        String name = (String) profileData.getOrDefault("candidateName", "Professional Candidate");
        Paragraph nameParagraph = new Paragraph(name.toUpperCase(), nameFont);
        nameParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(nameParagraph);

        // Dynamically pull contact info if AI extracts it, otherwise leave a standard placeholder format
        String contactInfoStr = (String) profileData.getOrDefault("contactInfo", "City, State | email@example.com | LinkedIn | GitHub");
        Paragraph contactInfo = new Paragraph(contactInfoStr, contactFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        contactInfo.setSpacingAfter(10f);
        document.add(contactInfo);

        // --- 2. PROFESSIONAL SUMMARY ---
        String summary = (String) profileData.get("summary");
        if (summary != null && !summary.trim().isEmpty()) {
            addSectionHeader(document, "PROFESSIONAL SUMMARY", headerFont);
            document.add(new Paragraph(summary, normalFont));
            document.add(new Chunk("\n"));
        }

        // --- 3. TECHNICAL SKILLS ---
        Map<String, List<String>> skills = (Map<String, List<String>>) profileData.get("skills");
        if (skills != null && !skills.isEmpty()) {
            addSectionHeader(document, "TECHNICAL SKILLS", headerFont);

            // Dynamically loop through whatever skill categories the AI found
            for (Map.Entry<String, List<String>> entry : skills.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    // Capitalize the first letter of the category (e.g., "languages" -> "Languages")
                    String category = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                    String skillList = String.join(", ", entry.getValue());
                    document.add(new Paragraph(category + ": " + skillList, normalFont));
                }
            }
            document.add(new Chunk("\n"));
        }

        // --- 4. EXPERIENCE ---
        List<Map<String, String>> experienceList = (List<Map<String, String>>) profileData.get("experience");
        if (experienceList != null && !experienceList.isEmpty()) {
            addSectionHeader(document, "EXPERIENCE", headerFont);

            for (Map<String, String> exp : experienceList) {
                String role = exp.getOrDefault("role", "Title");
                String company = exp.getOrDefault("company", "Company");

                Paragraph expTitle = new Paragraph(role + " | " + company, subHeaderFont);
                document.add(expTitle);

                String description = exp.get("description");
                if (description != null && !description.isEmpty()) {
                    // Split by periods or semicolons to create dynamic bullet points
                    String[] bullets = description.split("\\. |; ");
                    for (String bullet : bullets) {
                        addBulletPoint(document, bullet.replace(".", "").trim() + ".", normalFont);
                    }
                }
                document.add(new Chunk("\n"));
            }
        }

        // --- 5. PROJECTS ---
        List<Map<String, String>> projectList = (List<Map<String, String>>) profileData.get("projects");
        if (projectList != null && !projectList.isEmpty()) {
            addSectionHeader(document, "PROJECTS", headerFont);

            for (Map<String, String> proj : projectList) {
                String title = proj.getOrDefault("title", "Project Title");
                String techStack = proj.getOrDefault("techStack", "");

                String headerText = title + (techStack.isEmpty() ? "" : " | " + techStack);
                Paragraph projTitle = new Paragraph(headerText, subHeaderFont);
                document.add(projTitle);

                String description = proj.get("description");
                if (description != null && !description.isEmpty()) {
                    String[] bullets = description.split("\\. |; ");
                    for (String bullet : bullets) {
                        addBulletPoint(document, bullet.replace(".", "").trim() + ".", normalFont);
                    }
                }
                document.add(new Chunk("\n"));
            }
        }

        // --- 6. EDUCATION ---
        List<Map<String, String>> educationList = (List<Map<String, String>>) profileData.get("education");
        if (educationList != null && !educationList.isEmpty()) {
            addSectionHeader(document, "EDUCATION", headerFont);

            for (Map<String, String> edu : educationList) {
                String institution = edu.getOrDefault("institution", "Institution Name");
                String degree = edu.getOrDefault("degree", "Degree");
                String year = edu.getOrDefault("year", "");

                document.add(new Paragraph(institution, subHeaderFont));
                document.add(new Paragraph(degree, normalFont));
                if (!year.isEmpty()) {
                    Paragraph yearParagraph = new Paragraph(year, italicFont);
                    yearParagraph.setSpacingAfter(5f);
                    document.add(yearParagraph);
                }
            }
        }

        document.close();
        return out.toByteArray();
    }

    // Helper method to draw standard ATS section dividers
    private void addSectionHeader(Document document, String title, Font font) throws Exception {
        Paragraph header = new Paragraph(title, font);
        header.setSpacingBefore(5f);
        header.setSpacingAfter(2f);
        document.add(header);

        LineSeparator ls = new LineSeparator();
        ls.setLineColor(Color.BLACK);
        ls.setLineWidth(1f);
        document.add(new Chunk(ls));
        document.add(new Chunk("\n"));
    }

    // Helper method for standard ATS bullet points
    private void addBulletPoint(Document document, String text, Font font) throws Exception {
        Paragraph bullet = new Paragraph("\u2022 " + text, font);
        bullet.setIndentationLeft(15f);
        bullet.setSpacingAfter(3f);
        document.add(bullet);
    }
}