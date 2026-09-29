package com.bhavya.documentworkflowplatform.service;

import com.bhavya.documentworkflowplatform.entity.Document;
import com.bhavya.documentworkflowplatform.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // ============================================================
    // SAVE NEW DOCUMENT
    // ============================================================

    public Document saveDocument(
            String fileName,
            String fileType,
            String filePath,
            String uploadedBy) {

        Document document = new Document();

        document.setFileName(fileName);
        document.setFileType(fileType);
        document.setFilePath(filePath);
        document.setStatus("UPLOADED");
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    // ============================================================
    // UPDATE EXISTING DOCUMENT
    // ============================================================

    public Document updateDocument(Document document) {

        return documentRepository.save(document);
    }

    // ============================================================
    // GET ALL DOCUMENTS
    // ============================================================

    public List<Document> getAllDocuments() {

        return documentRepository.findAll();
    }

    // ============================================================
    // GET DOCUMENT BY ID
    // ============================================================

    public Document getDocumentById(Long id) {

        return documentRepository
                .findById(id)
                .orElse(null);
    }

    // ============================================================
    // GET DOCUMENTS BY USER
    // ============================================================

    public List<Document> getDocumentsByUser(String email) {

        return documentRepository.findByUploadedBy(email);
    }

    // ============================================================
    // DELETE DOCUMENT
    // ============================================================

    public void deleteDocument(Long id) {

        documentRepository.deleteById(id);
    }

    // ============================================================
    // UPDATE EXTRACTED TEXT
    // ============================================================

    public Document updateExtractedText(
            Long id,
            String extractedText) {

        Document document =
                documentRepository
                        .findById(id)
                        .orElse(null);

        if (document != null) {

            document.setExtractedText(extractedText);
            document.setStatus("TEXT_EXTRACTED");

            return documentRepository.save(document);
        }

        return null;
    }
}