package no.novari.flyt.instance.gateway.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestSizeLimitFilter implements WebFilter {

    private static final Map<String, String> PAYLOAD_TOO_LARGE_BODY = Map.of(
            "error", "payload_too_large",
            "message", "Request payload exceeds configured limit."
    );

    private static final byte[] PAYLOAD_TOO_LARGE_FALLBACK =
            "{\"error\":\"payload_too_large\",\"message\":\"Request payload exceeds configured limit.\"}"
                    .getBytes(StandardCharsets.UTF_8);

    private final long maxInMemoryBytes;
    private final ObjectMapper objectMapper;

    public RequestSizeLimitFilter(
            @Value("${spring.http.codec.max-in-memory-size}") DataSize maxInMemorySize,
            ObjectMapper objectMapper
    ) {
        this.maxInMemoryBytes = maxInMemorySize.toBytes();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long contentLength = exchange.getRequest().getHeaders().getContentLength();
        if (contentLength > 0 && maxInMemoryBytes > 0 && contentLength > maxInMemoryBytes) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = response.bufferFactory().wrap(serializeBody());
            return response.writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    private byte[] serializeBody() {
        try {
            return objectMapper.writeValueAsBytes(PAYLOAD_TOO_LARGE_BODY);
        } catch (JsonProcessingException e) {
            return PAYLOAD_TOO_LARGE_FALLBACK;
        }
    }
}
