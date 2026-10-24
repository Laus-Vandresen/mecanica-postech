package br.com.mecanica.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class ApiExceptionHandlerTest {

    @Test
    void falhaInesperadaNaoExpoeDetalhesInternos() {
        var response = new TestableApiExceptionHandler().handleUnexpected(
                new IllegalStateException("informacao interna confidencial"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        var problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Ocorreu um erro inesperado.", problem.getDetail());
        var properties = problem.getProperties();
        assertNotNull(properties);
        assertTrue(properties.containsKey("errorId"));
    }

    private static class TestableApiExceptionHandler extends ApiExceptionHandler {
        ResponseEntity<Object> handleUnexpected(Exception exception) {
            return handleExceptionInternal(exception, null, new HttpHeaders(),
                    HttpStatus.INTERNAL_SERVER_ERROR, new ServletWebRequest(new MockHttpServletRequest()));
        }
    }
}
