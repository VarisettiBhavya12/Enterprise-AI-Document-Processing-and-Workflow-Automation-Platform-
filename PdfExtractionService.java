package com.bhavya.documentworkflowplatform.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class PdfExtractionService {

    public String extractText(String filePath) throws IOException {

        File file = new File(filePath);

        try (PDDocument document = Loader.loadPDF(file)) {

            // ------------------------------------------------
            // STEP 1: Try normal PDF text extraction
            // ------------------------------------------------

            PDFTextStripper stripper = new PDFTextStripper();

            String extractedText = stripper.getText(document);

            if (extractedText != null && !extractedText.isBlank()) {

                System.out.println(
                        "PDF text extracted successfully using PDFBox."
                );

                return extractedText;
            }

            // ------------------------------------------------
            // STEP 2: No text found -> use OCR
            // ------------------------------------------------

            System.out.println(
                    "No selectable text found. Starting OCR..."
            );

            PDFRenderer renderer = new PDFRenderer(document);

            Tesseract tesseract = new Tesseract();

            // IMPORTANT:
            // Change this path if your tessdata folder
            // is installed somewhere else.
            tesseract.setDatapath(
                    "C:\\Program Files\\Tesseract-OCR\\tessdata"
            );

            tesseract.setLanguage("eng");

            StringBuilder ocrText = new StringBuilder();

            for (int page = 0; page < document.getNumberOfPages(); page++) {

                System.out.println(
                        "Running OCR on page " + (page + 1)
                );

                BufferedImage image =
                        renderer.renderImageWithDPI(page, 300);

                String pageText;

                try {

                    pageText = tesseract.doOCR(image);

                } catch (TesseractException e) {

                    throw new IOException(
                            "OCR failed on page " + (page + 1),
                            e
                    );
                }

                if (pageText != null && !pageText.isBlank()) {

                    ocrText.append(pageText);
                    ocrText.append("\n");
                }
            }

            return ocrText.toString();
        }
    }
}