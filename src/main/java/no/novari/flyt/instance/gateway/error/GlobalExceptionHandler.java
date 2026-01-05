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

import java.net.URI;
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
        String url = resolveFullUrl(request);
        log.warn("Payload too large for {} {}: {}", request.getMethod(), url, exception.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of(
                        "error", "payload_too_large",
                        "message", "Request payload exceeds configured limit."
                )));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleServerWebInputException(
            ServerWebInputException exception,
            ServerHttpRequest request
    ) {
        if (exception.getCause() instanceof DataBufferLimitException) {
            return handleDataBufferLimitException((DataBufferLimitException) exception.getCause(), request);
        }

        String url = resolveFullUrl(request);
        log.warn("Bad request for {} {}: {}", request.getMethod(), url, exception.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "bad_request",
                        "message", "Malformed request."
                )));
    }

    private String resolveFullUrl(ServerHttpRequest request) {
        URI uri = request.getURI();
        String forwardedProto = request.getHeaders().getFirst("X-Forwarded-Proto");
        String forwardedHost = request.getHeaders().getFirst("X-Forwarded-Host");
        String forwardedPort = request.getHeaders().getFirst("X-Forwarded-Port");

        if (forwardedProto == null && forwardedHost == null && forwardedPort == null) {
            return uri.toString();
        }

        String scheme = forwardedProto != null ? forwardedProto : uri.getScheme();
        String host = forwardedHost != null ? forwardedHost : uri.getHost();
        String port = forwardedPort != null ? forwardedPort : (uri.getPort() == -1 ? null : String.valueOf(uri.getPort()));

        StringBuilder url = new StringBuilder();
        url.append(scheme != null ? scheme : "http").append("://");
        if (host != null) {
            url.append(host);
        }
        if (port != null && host != null && !host.contains(":") && !"80".equals(port) && !"443".equals(port)) {
            url.append(":").append(port);
        }
        url.append(uri.getRawPath());
        if (uri.getRawQuery() != null) {
            url.append("?").append(uri.getRawQuery());
        }
        return url.toString();
    }
}
