package com.bhavya.documentworkflowplatform.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DocumentAnalysisService {

    public String analyzeDocument(String fileName, String text) {

        if (text == null || text.isBlank()) {
            return """
                    Document Type:
                    Unknown

                    Summary:
                    No readable text was found in the document.

                    Key Information:
                    None

                    Verification:
                    The document could not be verified because no readable text was extracted.

                    Missing or Suspicious Information:
                    No readable content was available for analysis.

                    AI Status:
                    NEEDS_REVIEW
                    """;
        }

        String lowerText = text.toLowerCase();
        String lowerFileName = fileName == null
                ? ""
                : fileName.toLowerCase();

        // ---------------------------------------------------------
        // Detect document type
        // ---------------------------------------------------------

        String documentType;

        if (isResume(lowerText, lowerFileName)) {
            documentType = "Resume";
        } else if (isCertificate(lowerText, lowerFileName)) {
            documentType = "Certificate";
        } else if (isInvoice(lowerText, lowerFileName)) {
            documentType = "Invoice";
        } else if (isAgreement(lowerText, lowerFileName)) {
            documentType = "Agreement/Contract";
        } else {
            documentType = "General Document";
        }

        // ---------------------------------------------------------
        // Resume analysis
        // ---------------------------------------------------------

        if (documentType.equals("Resume")) {
            return analyzeResume(text, lowerText);
        }

        // ---------------------------------------------------------
        // Certificate analysis
        // ---------------------------------------------------------

        if (documentType.equals("Certificate")) {
            return analyzeCertificate(text, lowerText);
        }

        // ---------------------------------------------------------
        // Invoice analysis
        // ---------------------------------------------------------

        if (documentType.equals("Invoice")) {
            return analyzeInvoice(text, lowerText);
        }

        // ---------------------------------------------------------
        // Agreement analysis
        // ---------------------------------------------------------

        if (documentType.equals("Agreement/Contract")) {
            return analyzeAgreement(text, lowerText);
        }

        // ---------------------------------------------------------
        // General document
        // ---------------------------------------------------------

        return analyzeGeneralDocument(text, documentType);
    }

    // =========================================================
    // RESUME
    // =========================================================

    private String analyzeResume(String text, String lowerText) {

        List<String> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        // Email
        boolean emailPresent = Pattern
                .compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}",
                        Pattern.CASE_INSENSITIVE)
                .matcher(text)
                .find();

        if (emailPresent) {
            present.add("Email address");
        } else {
            missing.add("Email address");
        }

        // Phone
        boolean phonePresent = Pattern
                .compile("(\\+91[-\\s]?)?[6-9]\\d{9}")
                .matcher(text)
                .find();

        if (phonePresent) {
            present.add("Phone number");
        } else {
            missing.add("Phone number");
        }

        // Education
        if (containsAny(lowerText,
                "education",
                "b.tech",
                "btech",
                "bachelor",
                "degree",
                "college",
                "university")) {

            present.add("Education");
        } else {
            missing.add("Education");
        }

        // Skills
        if (containsAny(lowerText,
                "skills",
                "technical skills",
                "programming",
                "java",
                "python",
                "sql")) {

            present.add("Skills");
        } else {
            missing.add("Skills");
        }

        // Projects
        if (containsAny(lowerText,
                "projects",
                "project",
                "academic project")) {

            present.add("Projects");
        } else {
            missing.add("Projects");
        }

        // Experience / Internship
        if (containsAny(lowerText,
                "experience",
                "internship",
                "intern",
                "training")) {

            present.add("Experience/Internship");
        } else {
            missing.add("Experience/Internship");
        }

        // Name
        boolean namePresent = hasPossibleName(text);

        if (namePresent) {
            present.add("Candidate name");
        } else {
            missing.add("Candidate name");
        }

        // -----------------------------------------------------
        // Determine status
        // -----------------------------------------------------

        String status;

        if (present.size() >= 5 && missing.size() <= 1) {
            status = "VERIFIED";
        } else if (present.size() >= 3) {
            status = "NEEDS_REVIEW";
        } else {
            status = "REJECTED";
        }

        String missingInformation;

        if (missing.isEmpty()) {

            missingInformation =
                    "No major required resume section was missing.";

        } else {

            missingInformation =
                    String.join(", ", missing)
                            + " was not detected in the document.";
        }

        return """
                Document Type:
                Resume

                Summary:
                The document was identified as a resume based on its content.
                The extracted text was checked for common resume sections and contact information.

                Key Information:
                Detected sections: %s

                Verification:
                The document contains %d important resume elements.

                Missing or Suspicious Information:
                %s

                AI Status:
                %s
                """.formatted(
                String.join(", ", present),
                present.size(),
                missingInformation,
                status
        );
    }

    // =========================================================
    // CERTIFICATE
    // =========================================================

    private String analyzeCertificate(
            String text,
            String lowerText) {

        List<String> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (containsAny(lowerText,
                "certificate",
                "certification",
                "certifies",
                "awarded")) {

            present.add("Certificate heading/content");
        } else {
            missing.add("Certificate identification");
        }

        if (containsAny(lowerText,
                "name",
                "student",
                "candidate",
                "awarded to")) {

            present.add("Recipient information");
        } else {
            missing.add("Recipient information");
        }

        if (containsAny(lowerText,
                "date",
                "issued",
                "issue date")) {

            present.add("Date information");
        } else {
            missing.add("Date information");
        }

        if (containsAny(lowerText,
                "university",
                "college",
                "school",
                "institute",
                "organization")) {

            present.add("Issuing organization");
        } else {
            missing.add("Issuing organization");
        }

        String status;

        if (present.size() >= 3 && missing.size() <= 1) {
            status = "VERIFIED";
        } else if (present.size() >= 2) {
            status = "NEEDS_REVIEW";
        } else {
            status = "REJECTED";
        }

        String missingInformation =
                missing.isEmpty()
                        ? "No major certificate information was missing."
                        : String.join(", ", missing)
                        + " was not detected.";

        return """
                Document Type:
                Certificate

                Summary:
                The document was identified as a certificate based on its text.

                Key Information:
                Detected information: %s

                Verification:
                Certificate structure and common identifying information were checked.

                Missing or Suspicious Information:
                %s

                AI Status:
                %s
                """.formatted(
                String.join(", ", present),
                missingInformation,
                status
        );
    }

    // =========================================================
    // INVOICE
    // =========================================================

    private String analyzeInvoice(
            String text,
            String lowerText) {

        List<String> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (containsAny(lowerText,
                "invoice number",
                "invoice no",
                "bill no")) {

            present.add("Invoice number");
        } else {
            missing.add("Invoice number");
        }

        if (containsAny(lowerText,
                "date",
                "invoice date")) {

            present.add("Invoice date");
        } else {
            missing.add("Invoice date");
        }

        if (containsAny(lowerText,
                "total",
                "amount",
                "grand total")) {

            present.add("Amount/Total");
        } else {
            missing.add("Amount/Total");
        }

        if (containsAny(lowerText,
                "customer",
                "client",
                "bill to",
                "buyer")) {

            present.add("Customer information");
        } else {
            missing.add("Customer information");
        }

        if (containsAny(lowerText,
                "company",
                "seller",
                "vendor",
                "from")) {

            present.add("Seller information");
        } else {
            missing.add("Seller information");
        }

        String status;

        if (present.size() >= 4 && missing.size() <= 1) {
            status = "VERIFIED";
        } else if (present.size() >= 2) {
            status = "NEEDS_REVIEW";
        } else {
            status = "REJECTED";
        }

        String missingInformation =
                missing.isEmpty()
                        ? "No major invoice information was missing."
                        : String.join(", ", missing)
                        + " was not detected.";

        return """
                Document Type:
                Invoice

                Summary:
                The document was identified as an invoice based on its content.

                Key Information:
                Detected information: %s

                Verification:
                Common invoice fields were checked.

                Missing or Suspicious Information:
                %s

                AI Status:
                %s
                """.formatted(
                String.join(", ", present),
                missingInformation,
                status
        );
    }

    // =========================================================
    // AGREEMENT / CONTRACT
    // =========================================================

    private String analyzeAgreement(
            String text,
            String lowerText) {

        List<String> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (containsAny(lowerText,
                "agreement",
                "contract")) {

            present.add("Agreement/Contract identification");
        }

        if (containsAny(lowerText,
                "party",
                "between",
                "employee",
                "employer",
                "client")) {

            present.add("Parties");
        } else {
            missing.add("Parties");
        }

        if (containsAny(lowerText,
                "date",
                "effective date")) {

            present.add("Date");
        } else {
            missing.add("Date");
        }

        if (containsAny(lowerText,
                "terms",
                "conditions",
                "clause")) {

            present.add("Terms and conditions");
        } else {
            missing.add("Terms and conditions");
        }

        if (containsAny(lowerText,
                "signature",
                "signed",
                "authorized")) {

            present.add("Signature/Authorization");
        } else {
            missing.add("Signature/Authorization");
        }

        String status;

        if (present.size() >= 4 && missing.size() <= 1) {
            status = "VERIFIED";
        } else if (present.size() >= 2) {
            status = "NEEDS_REVIEW";
        } else {
            status = "REJECTED";
        }

        String missingInformation =
                missing.isEmpty()
                        ? "No major agreement information was missing."
                        : String.join(", ", missing)
                        + " was not detected.";

        return """
                Document Type:
                Agreement/Contract

                Summary:
                The document was identified as an agreement or contract.

                Key Information:
                Detected information: %s

                Verification:
                Common agreement elements were checked.

                Missing or Suspicious Information:
                %s

                AI Status:
                %s
                """.formatted(
                String.join(", ", present),
                missingInformation,
                status
        );
    }

    // =========================================================
    // GENERAL DOCUMENT
    // =========================================================

    private String analyzeGeneralDocument(
            String text,
            String documentType) {

        String status;

        if (text.length() >= 500) {
            status = "VERIFIED";
        } else if (text.length() >= 100) {
            status = "NEEDS_REVIEW";
        } else {
            status = "REJECTED";
        }

        return """
                Document Type:
                %s

                Summary:
                The document contains readable text and was processed successfully.

                Key Information:
                Approximately %d characters of readable text were detected.

                Verification:
                Basic document structure and readable content were checked.

                Missing or Suspicious Information:
                Detailed verification could not be performed because the document type could not be confidently identified.

                AI Status:
                %s
                """.formatted(
                documentType,
                text.length(),
                status
        );
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private boolean containsAny(
            String text,
            String... keywords) {

        for (String keyword : keywords) {

            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isResume(
            String text,
            String fileName) {

        int score = 0;

        if (fileName.contains("resume")
                || fileName.contains("cv")) {
            score++;
        }

        if (containsAny(text,
                "resume",
                "curriculum vitae")) {
            score++;
        }

        if (containsAny(text,
                "education",
                "skills",
                "projects",
                "experience")) {
            score++;
        }

        if (containsAny(text,
                "objective",
                "career objective",
                "professional summary")) {
            score++;
        }

        return score >= 2;
    }

    private boolean isCertificate(
            String text,
            String fileName) {

        return fileName.contains("certificate")
                || containsAny(text,
                "certificate",
                "certifies that",
                "certification");
    }

    private boolean isInvoice(
            String text,
            String fileName) {

        return fileName.contains("invoice")
                || containsAny(text,
                "invoice number",
                "invoice no",
                "bill to",
                "grand total");
    }

    private boolean isAgreement(
            String text,
            String fileName) {

        return fileName.contains("agreement")
                || fileName.contains("contract")
                || containsAny(text,
                "agreement",
                "contract",
                "terms and conditions");
    }

    private boolean hasPossibleName(String text) {

        String[] lines = text.split("\\R");

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.length() >= 3
                    && trimmed.length() <= 60
                    && trimmed.matches("[A-Za-z .]+")) {

                String lower = trimmed.toLowerCase();

                if (!lower.contains("resume")
                        && !lower.contains("education")
                        && !lower.contains("skills")
                        && !lower.contains("experience")
                        && !lower.contains("projects")
                        && !lower.contains("objective")) {

                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================
    // EXTRACT STATUS
    // =========================================================

    public String extractAiStatus(String analysis) {

        if (analysis == null || analysis.isBlank()) {
            return "NEEDS_REVIEW";
        }

        String upper = analysis.toUpperCase();

        if (upper.contains("AI STATUS:")
                && upper.contains("VERIFIED")) {
            return "VERIFIED";
        }

        if (upper.contains("AI STATUS:")
                && upper.contains("REJECTED")) {
            return "REJECTED";
        }

        if (upper.contains("AI STATUS:")
                && upper.contains("NEEDS_REVIEW")) {
            return "NEEDS_REVIEW";
        }

        return "NEEDS_REVIEW";
    }
}