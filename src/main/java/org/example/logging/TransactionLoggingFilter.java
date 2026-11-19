package org.example.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    public static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader("X-Transaction-Id", transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            String requestBody = getBodyAsString(wrappedRequest.getContentAsByteArray());
            String responseBody = getBodyAsString(wrappedResponse.getContentAsByteArray());

            String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
            log.info("REST Call | {} {}{} | Status: {} | Duration: {}ms | RequestBody: {} | ResponseBody: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    query,
                    wrappedResponse.getStatus(),
                    durationMs,
                    maskSensitiveData(requestBody),
                    maskSensitiveData(responseBody));

            wrappedResponse.copyBodyToResponse();
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private String getBodyAsString(byte[] content) {
        if (content == null || content.length == 0) {
            return "<empty>";
        }
        return new String(content, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
    }

    private String maskSensitiveData(String payload) {
        return payload.replaceAll("(\"(?:password|oldPassword|newPassword)\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
    }
}