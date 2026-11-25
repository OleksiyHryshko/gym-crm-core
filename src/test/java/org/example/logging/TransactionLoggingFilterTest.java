package org.example.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLoggingFilterTest {

    private final TransactionLoggingFilter filter = new TransactionLoggingFilter();

    @Test
    void maskQueryString_ShouldMaskPasswordAndKeepUsername() {
        String masked = filter.maskQueryString("username=John.Doe&password=mySecret123");
        assertEquals("?username=John.Doe&password=***", masked);
    }

    @Test
    void maskQueryString_ShouldMaskMultipleSensitiveParamsCaseInsensitively() {
        String masked = filter.maskQueryString("oldPassword=abc&newPassword=xyz&trainerName=Bob");
        assertEquals("?oldPassword=***&newPassword=***&trainerName=Bob", masked);
    }

    @Test
    void maskQueryString_WhenNullOrEmpty_ShouldReturnEmptyString() {
        assertEquals("", filter.maskQueryString(null));
        assertEquals("", filter.maskQueryString(""));
    }

    @Test
    void maskSensitiveData_ShouldMaskPasswordsInJsonBody() {
        String body = "{\"userName\":\"John.Doe\",\"oldPassword\":\"secret1\",\"newPassword\":\"secret2\"}";
        String masked = filter.maskSensitiveData(body);
        assertEquals("{\"userName\":\"John.Doe\",\"oldPassword\":\"***\",\"newPassword\":\"***\"}", masked);
    }

    @Test
    void doFilter_ShouldSetTransactionIdHeaderAndClearMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        request.setQueryString("username=John.Doe&password=secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader("X-Transaction-Id"));
        assertNull(MDC.get(TransactionLoggingFilter.TRANSACTION_ID_KEY));
    }
}