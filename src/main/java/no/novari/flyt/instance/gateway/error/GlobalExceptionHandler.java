package no.novari.flyt.instance.gateway.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(DataBufferLimitException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDataBufferLimitException(
            DataBufferLimitException exception,
            ServerHttpRequest request
    ) {
        String url = ErrorResponseUtils.resolveFullUrl(request);
        log.warn("Payload too large for {} {}: {}", request.getMethod(), url, exception.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponseUtils.PAYLOAD_TOO_LARGE_BODY));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleServerWebInputException(
            ServerWebInputException exception,
            ServerHttpRequest request
    ) {
        if (exception.getCause() instanceof DataBufferLimitException) {
            return handleDataBufferLimitException((DataBufferLimitException) exception.getCause(), request);
        }

        String url = ErrorResponseUtils.resolveFullUrl(request);
        log.warn("Bad request for {} {}: {}", request.getMethod(), url, exception.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "bad_request",
                        "message", "Malformed request."
                )));
    }

}
