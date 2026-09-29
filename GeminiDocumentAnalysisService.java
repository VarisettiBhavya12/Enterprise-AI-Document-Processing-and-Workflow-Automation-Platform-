package com.bhavya.documentworkflowplatform.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiDocumentAnalysisService {

    private static final String PRIMARY_MODEL = "gemini-3.8-flash";
    private static final String FALLBACK_MODEL = "gemini-3.5-flash-lite";

    private final Client client;

    public GeminiDocumentAnalysisService() {
        this.client = new Client();
    }

    public String analyzeDocument(String fileName, String extractedText) {

        // ---------------------------------------------------------
        // 1. Check whether text was extracted
        // ---------------------------------------------------------

        if (extractedText == null || extractedText.isBlank()) {

            return """
                    Document Type:
                    Unknown

                    Summary:
                    No readable text was found in the document.

                    Key Information:
                    None

                    Verification:
                    The document could not be analyzed because no readable text was extracted.

                    Missing or Suspicious Information:
                    No readable content available.

                    AI Status:
                    NEEDS_REVIEW
                    """;
        }

        // ---------------------------------------------------------
        // 2. Limit text sent to Gemini
        // ---------------------------------------------------------

        String text = extractedText;

        if (text.length() > 12000) {
            text = text.substring(0, 12000);
        }

        // ---------------------------------------------------------
        // 3. Create prompt
        // ---------------------------------------------------------

        String prompt = """
                You are an enterprise document verification assistant.

                Analyze the following document carefully.

                Document Name:
                %s

                Document Content:
                %s

                Return the result using exactly these sections:

                Document Type:
                Summary:
                Key Information:
                Verification:
                Missing or Suspicious Information:
                AI Status:

                AI Status must be exactly one of:

                VERIFIED
                NEEDS_REVIEW
                REJECTED

                Rules:

                1. Do not invent information.
                2. Use only information present in the document.
                3. Identify the type of document.
                4. Provide a short summary.
                5. Extract important information.
                6. Mention missing or suspicious information if applicable.
                7. Use VERIFIED when the document appears complete and internally consistent.
                8. Use NEEDS_REVIEW when important information is unclear or cannot be verified.
                9. Use REJECTED only when there is clear evidence that the document is invalid or unacceptable.
                10. Do not make assumptions about information that is not present.
                """.formatted(fileName, text);

        // ---------------------------------------------------------
        // 4. Try primary Gemini model
        // ---------------------------------------------------------

        System.out.println(
                "Sending document to Gemini AI using "
                        + PRIMARY_MODEL
                        + "..."
        );

        try {

            String result = callGemini(PRIMARY_MODEL, prompt);

            if (result != null && !result.isBlank()) {

                System.out.println(
                        "Gemini primary model analysis successful."
                );

                return result;
            }

        } catch (Exception e) {

            System.out.println(
                    "========== GEMINI PRIMARY ERROR =========="
            );

            System.out.println(
                    "Model: " + PRIMARY_MODEL
            );

            System.out.println(
                    "Message: " + e.getMessage()
            );

            System.out.println(
                    "=========================================="
            );

            // -----------------------------------------------------
            // 5. If quota is exceeded (429), don't waste time
            //    retrying the same model.
            // -----------------------------------------------------

            if (isQuotaError(e)) {

                System.out.println(
                        "Gemini quota exceeded for primary model."
                );

            } else if (isServiceUnavailable(e)) {

                System.out.println(
                        "Gemini primary model temporarily unavailable."
                );

                // One short retry only for 503
                try {

                    System.out.println(
                            "Retrying primary Gemini model once..."
                    );

                    Thread.sleep(2000);

                    String retryResult =
                            callGemini(PRIMARY_MODEL, prompt);

                    if (retryResult != null
                            && !retryResult.isBlank()) {

                        System.out.println(
                                "Gemini primary retry successful."
                        );

                        return retryResult;
                    }

                } catch (Exception retryException) {

                    System.out.println(
                            "Primary retry failed: "
                                    + retryException.getMessage()
                    );
                }

            } else {

                System.out.println(
                        "Primary Gemini model failed for another reason."
                );
            }
        }

        // ---------------------------------------------------------
        // 6. Try fallback model
        // ---------------------------------------------------------

        System.out.println(
                "Trying fallback model: "
                        + FALLBACK_MODEL
        );

        try {

            String fallbackResult =
                    callGemini(FALLBACK_MODEL, prompt);

            if (fallbackResult != null
                    && !fallbackResult.isBlank()) {

                System.out.println(
                        "Gemini fallback model analysis successful."
                );

                return fallbackResult;
            }

        } catch (Exception e) {

            System.out.println(
                    "========== GEMINI FALLBACK ERROR =========="
            );

            System.out.println(
                    "Model: " + FALLBACK_MODEL
            );

            System.out.println(
                    "Message: " + e.getMessage()
            );

            System.out.println(
                    "==========================================="
            );
        }

        // ---------------------------------------------------------
        // 7. Gemini completely unavailable
        //
        // IMPORTANT:
        // We do NOT throw the exception here.
        //
        // The document must still be uploaded and saved.
        // ---------------------------------------------------------

        System.out.println(
                "Gemini analysis unavailable."
        );

        System.out.println(
                "Document will be saved with NEEDS_REVIEW status."
        );

        return createGeminiUnavailableResponse();
    }


    // =============================================================
    // Call Gemini
    // =============================================================

    private String callGemini(
            String model,
            String prompt
    ) {

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        null
                );

        if (response == null) {

            return null;
        }

        return response.text();
    }


    // =============================================================
    // Detect 429 quota error
    // =============================================================

    private boolean isQuotaError(Exception e) {

        String message =
                e.getMessage();

        if (message == null) {

            return false;
        }

        String upper =
                message.toUpperCase();

        return upper.contains("429")
                || upper.contains("TOO MANY REQUESTS")
                || upper.contains("QUOTA EXCEEDED")
                || upper.contains("FREE_TIER");
    }


    // =============================================================
    // Detect 503 error
    // =============================================================

    private boolean isServiceUnavailable(Exception e) {

        String message =
                e.getMessage();

        if (message == null) {

            return false;
        }

        String upper =
                message.toUpperCase();

        return upper.contains("503")
                || upper.contains("SERVICE UNAVAILABLE")
                || upper.contains("HIGH DEMAND");
    }


    // =============================================================
    // Fallback response
    // =============================================================

    private String createGeminiUnavailableResponse() {

        return """
                Document Type:
                Unknown

                Summary:
                The document was uploaded successfully, but Gemini AI analysis is temporarily unavailable.

                Key Information:
                Unable to determine because the AI service is temporarily unavailable.

                Verification:
                The document could not be verified by Gemini at this time.

                Missing or Suspicious Information:
                AI analysis could not be completed because the Gemini service or quota was unavailable.

                AI Status:
                NEEDS_REVIEW
                """;
    }


    // =============================================================
    // Extract AI status
    // =============================================================

    public String extractAiStatus(String analysis) {

        if (analysis == null || analysis.isBlank()) {

            return "NEEDS_REVIEW";
        }

        String upper =
                analysis.toUpperCase();

        if (upper.contains("AI STATUS: VERIFIED")) {

            return "VERIFIED";
        }

        if (upper.contains("AI STATUS: REJECTED")) {

            return "REJECTED";
        }

        if (upper.contains("AI STATUS: NEEDS_REVIEW")) {

            return "NEEDS_REVIEW";
        }

        return "NEEDS_REVIEW";
    }
}