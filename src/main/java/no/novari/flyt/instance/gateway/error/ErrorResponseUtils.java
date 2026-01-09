package no.novari.flyt.instance.gateway.error;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class ErrorResponseUtils {

    static final Map<String, String> PAYLOAD_TOO_LARGE_BODY = Map.of(
            "error", "payload_too_large",
            "message", "Request payload exceeds configured limit."
    );

    static final byte[] PAYLOAD_TOO_LARGE_FALLBACK =
            "{\"error\":\"payload_too_large\",\"message\":\"Request payload exceeds configured limit.\"}"
                    .getBytes(StandardCharsets.UTF_8);

    static String resolveFullUrl(ServerWebExchange exchange) {
        return resolveFullUrl(exchange.getRequest());
    }

    static String resolveFullUrl(ServerHttpRequest request) {
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

    private ErrorResponseUtils() {
    }
}
