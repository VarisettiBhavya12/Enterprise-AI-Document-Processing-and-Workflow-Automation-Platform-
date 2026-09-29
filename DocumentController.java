package com.bhavya.documentworkflowplatform.controller;

import com.bhavya.documentworkflowplatform.entity.Document;
import com.bhavya.documentworkflowplatform.service.DocumentService;
import com.bhavya.documentworkflowplatform.service.FileStorageService;
import com.bhavya.documentworkflowplatform.service.GeminiDocumentAnalysisService;
import com.bhavya.documentworkflowplatform.service.PdfExtractionService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final GeminiDocumentAnalysisService documentAnalysisService;

    public DocumentController(
            DocumentService documentService,
            FileStorageService fileStorageService,
            PdfExtractionService pdfExtractionService,
            GeminiDocumentAnalysisService documentAnalysisService) {

        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.pdfExtractionService = pdfExtractionService;
        this.documentAnalysisService = documentAnalysisService;
    }

    // ============================================================
    // UPLOAD DOCUMENT
    // ============================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {

        try {

            // Validate file
            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Please select a file.");
            }

            // ====================================================
            // SAVE PHYSICAL FILE
            // ====================================================

            String filePath =
                    fileStorageService.saveFile(file);

            // ====================================================
            // SAVE DOCUMENT INFORMATION
            // ====================================================

            Document document =
                    documentService.saveDocument(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            filePath,
                            email
                    );

            // ====================================================
            // PDF PROCESSING
            // ====================================================

            if (file.getContentType() != null &&
                    file.getContentType()
                            .equalsIgnoreCase("application/pdf")) {

                // =================================================
                // EXTRACT TEXT USING PDFBOX
                // =================================================

                String extractedText =
                        pdfExtractionService
                                .extractText(filePath);

                System.out.println(
                        "PDF text extracted successfully using PDFBox."
                );

                // Save extracted text
                document.setExtractedText(
                        extractedText
                );

                document.setStatus(
                        "TEXT_EXTRACTED"
                );

                // =================================================
                // GEMINI AI ANALYSIS
                // =================================================

                String aiResult =
                        documentAnalysisService
                                .analyzeDocument(
                                        file.getOriginalFilename(),
                                        extractedText
                                );

                System.out.println(
                        "Gemini AI document analysis completed."
                );

                // =================================================
                // SAVE AI ANALYSIS
                // =================================================

                document.setAiAnalysis(
                        aiResult
                );

                // =================================================
                // EXTRACT AI STATUS
                // =================================================

                document.setAiStatus(
                        documentAnalysisService
                                .extractAiStatus(
                                        aiResult
                                )
                );

                // =================================================
                // UPDATE DOCUMENT STATUS
                // =================================================

                document.setStatus(
                        "AI_ANALYZED"
                );

                // =================================================
                // UPDATE SAME DOCUMENT IN DATABASE
                // =================================================

                document =
                        documentService
                                .updateDocument(
                                        document
                                );
            }

            // Return document
            return ResponseEntity.ok(
                    document
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Document processing failed: "
                                    + e.getMessage()
                    );
        }
    }

    // ============================================================
    // GET ALL DOCUMENTS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {

        return ResponseEntity.ok(
                documentService.getAllDocuments()
        );
    }

    // ============================================================
    // GET DOCUMENT BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(
            @PathVariable Long id) {

        Document document =
                documentService.getDocumentById(id);

        if (document == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                document
        );
    }

    // ============================================================
    // GET DOCUMENTS BY USER
    // ============================================================

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Document>>
    getDocumentsByUser(
            @PathVariable String email) {

        return ResponseEntity.ok(
                documentService
                        .getDocumentsByUser(email)
        );
    }

    // ============================================================
    // OPEN PDF
    // ============================================================

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> openDocument(
            @PathVariable Long id) {

        try {

            Document document =
                    documentService
                            .getDocumentById(id);

            if (document == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            String filePath =
                    document.getFilePath();

            if (filePath == null ||
                    filePath.isBlank()) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            Path path =
                    Paths.get(filePath);

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists() ||
                    !resource.isReadable()) {

                System.out.println(
                        "File not found: "
                                + filePath
                );

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    document.getFileName() +
                                    "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    // ============================================================
    // DELETE DOCUMENT
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id) {

        Document document =
                documentService
                        .getDocumentById(id);

        if (document == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        documentService.deleteDocument(
                id
        );

        return ResponseEntity.ok(
                "Document deleted successfully."
        );
    }
}