package com.mediqai.ai.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    private static final int MAX_RETRIES = 3;

    /**
     * Model chính
     */
    private static final String PRIMARY_MODEL =
            "gemini-3.5-flash-lite";

    /**
     * Model dự phòng
     */
    private static final String FALLBACK_MODEL =
            "gemini-3.1-flash-lite";

    /**
     * Gửi prompt tới Gemini.
     *
     * Flow:
     *
     * 3.5 Flash-Lite
     *       |
     *      503
     *       ↓
     * 3.1 Flash-Lite
     *       |
     *      503
     *       ↓
     * Retry + Backoff
     */
    public String generateContent(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                }
                        )
                }
        );

        try {

            // =========================
            // MODEL CHÍNH
            // =========================

            return callGemini(
                    PRIMARY_MODEL,
                    requestBody
            );

        } catch (
                HttpServerErrorException.ServiceUnavailable e
        ) {

            // =========================
            // PRIMARY 503
            // =========================

            System.out.println(
                    "\n========== GEMINI PRIMARY 503 =========="
            );

            System.out.println(
                    "Model: " + PRIMARY_MODEL
            );

            System.out.println(
                    "Response: "
                            + e.getResponseBodyAsString()
            );

            System.out.println(
                    "Chuyển sang model dự phòng: "
                            + FALLBACK_MODEL
            );

            System.out.println(
                    "========================================\n"
            );

            // =========================
            // FALLBACK MODEL
            // =========================

            return callFallbackModel(
                    FALLBACK_MODEL,
                    requestBody
            );

        } catch (HttpClientErrorException.TooManyRequests e) {

            // =========================
            // PRIMARY 429
            // =========================

            System.out.println(
                    "\n========== GEMINI PRIMARY 429 =========="
            );

            System.out.println(
                    "Model: " + PRIMARY_MODEL
            );

            System.out.println(
                    "Response: "
                            + e.getResponseBodyAsString()
            );

            System.out.println(
                    "Gemini đang giới hạn request."
            );

            System.out.println(
                    "Thử lại model chính..."
            );

            System.out.println(
                    "========================================\n"
            );

            return retryPrimaryModel(
                    PRIMARY_MODEL,
                    requestBody
            );
        }
    }

    /**
     * Thử model dự phòng.
     *
     * Nếu fallback cũng 503:
     *
     * retry 1
     * retry 2
     * retry 3
     *
     * với exponential backoff.
     */
    private String callFallbackModel(
            String model,
            Map<String, Object> requestBody
    ) {

        for (
                int attempt = 0;
                attempt <= MAX_RETRIES;
                attempt++
        ) {

            try {

                return callGemini(
                        model,
                        requestBody
                );

            } catch (
                    HttpServerErrorException.ServiceUnavailable e
            ) {

                System.out.println(
                        "\n========== GEMINI FALLBACK 503 =========="
                );

                System.out.println(
                        "Model: " + model
                );

                System.out.println(
                        "Retry: "
                                + (attempt + 1)
                                + "/"
                                + MAX_RETRIES
                );

                System.out.println(
                        "Response: "
                                + e.getResponseBodyAsString()
                );

                System.out.println(
                        "=========================================\n"
                );

                if (attempt == MAX_RETRIES) {

                    throw new RuntimeException(
                            "Các model Gemini hiện đều "
                                    + "đang quá tải. "
                                    + "Vui lòng thử lại sau ít phút."
                    );
                }

                waitBeforeRetry(attempt);

            } catch (
                    HttpClientErrorException.TooManyRequests e
            ) {

                System.out.println(
                        "\n========== GEMINI FALLBACK 429 =========="
                );

                System.out.println(
                        "Model: " + model
                );

                System.out.println(
                        "Response: "
                                + e.getResponseBodyAsString()
                );

                System.out.println(
                        "=========================================\n"
                );

                if (attempt == MAX_RETRIES) {

                    throw new RuntimeException(
                            "Gemini đang giới hạn số lượng "
                                    + "request. Vui lòng thử lại sau."
                    );
                }

                waitBeforeRetry(attempt);

            } catch (HttpClientErrorException e) {

                // 400 / 401 / 403 / ...
                throw new RuntimeException(
                        "Gemini từ chối request: "
                                + e.getResponseBodyAsString(),
                        e
                );
            }
        }

        throw new RuntimeException(
                "Không thể nhận phản hồi từ Gemini"
        );
    }

    /**
     * Retry model chính khi gặp 429.
     */
    private String retryPrimaryModel(
            String model,
            Map<String, Object> requestBody
    ) {

        for (
                int attempt = 0;
                attempt <= MAX_RETRIES;
                attempt++
        ) {

            try {

                return callGemini(
                        model,
                        requestBody
                );

            } catch (
                    HttpClientErrorException.TooManyRequests e
            ) {

                System.out.println(
                        "\n========== GEMINI RETRY 429 =========="
                );

                System.out.println(
                        "Model: " + model
                );

                System.out.println(
                        "Retry: "
                                + (attempt + 1)
                                + "/"
                                + MAX_RETRIES
                );

                System.out.println(
                        "======================================\n"
                );

                if (attempt == MAX_RETRIES) {

                    throw new RuntimeException(
                            "Gemini đang quá tải request. "
                                    + "Vui lòng thử lại sau ít phút."
                    );
                }

                waitBeforeRetry(attempt);

            } catch (
                    HttpServerErrorException.ServiceUnavailable e
            ) {

                // Nếu trong quá trình retry primary
                // lại gặp 503 thì chuyển fallback ngay.

                System.out.println(
                        "\n========== GEMINI RETRY 503 =========="
                );

                System.out.println(
                        "Model: " + model
                );

                System.out.println(
                        "Chuyển sang model dự phòng: "
                                + FALLBACK_MODEL
                );

                System.out.println(
                        "======================================\n"
                );

                return callFallbackModel(
                        FALLBACK_MODEL,
                        requestBody
                );

            } catch (HttpClientErrorException e) {

                throw new RuntimeException(
                        "Gemini từ chối request: "
                                + e.getResponseBodyAsString(),
                        e
                );
            }
        }

        throw new RuntimeException(
                "Không thể nhận phản hồi từ Gemini"
        );
    }

    /**
     * Gọi trực tiếp Gemini API.
     */
    private String callGemini(
            String model,
            Map<String, Object> requestBody
    ) {

        Map<String, Object> response =
                restClient.post()
                        .uri(
                                "/v1beta/models/"
                                        + model
                                        + ":generateContent"
                        )
                        .header(
                                "x-goog-api-key",
                                apiKey
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

        return extractResponse(response);
    }

    /**
     * Exponential Backoff + Jitter.
     *
     * attempt 0 → ~1s
     * attempt 1 → ~2s
     * attempt 2 → ~4s
     */
    private void waitBeforeRetry(int attempt) {

        long baseDelay =
                1000L * (1L << attempt);

        long jitter =
                ThreadLocalRandom.current()
                        .nextLong(200, 800);

        long delay =
                baseDelay + jitter;

        System.out.println(
                "Gemini đang bận. Retry lần "
                        + (attempt + 1)
                        + " sau "
                        + delay
                        + "ms..."
        );

        try {

            Thread.sleep(delay);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Request Gemini bị gián đoạn",
                    e
            );
        }
    }

    /**
     * Đọc text từ response Gemini.
     */
    @SuppressWarnings("unchecked")
    private String extractResponse(
            Map<String, Object> response
    ) {

        if (response == null) {

            throw new RuntimeException(
                    "Gemini không trả về dữ liệu"
            );
        }

        try {

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>)
                            response.get("candidates");

            if (
                    candidates == null
                            || candidates.isEmpty()
            ) {

                throw new RuntimeException(
                        "Gemini không trả về candidate"
                );
            }

            Map<String, Object> content =
                    (Map<String, Object>)
                            candidates
                                    .get(0)
                                    .get("content");

            if (content == null) {

                throw new RuntimeException(
                        "Gemini không trả về content"
                );
            }

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>)
                            content.get("parts");

            if (
                    parts == null
                            || parts.isEmpty()
            ) {

                throw new RuntimeException(
                        "Gemini không trả về parts"
                );
            }

            Object text =
                    parts.get(0).get("text");

            if (text == null) {

                throw new RuntimeException(
                        "Gemini không trả về text"
                );
            }

            return text.toString();

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Không thể đọc response từ Gemini",
                    e
            );
        }
    }
}