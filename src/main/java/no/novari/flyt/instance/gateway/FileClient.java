package no.novari.flyt.instance.gateway;

import no.novari.flyt.instance.gateway.exception.FileUploadException;
import no.novari.flyt.instance.gateway.model.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Service
class FileClient {

    private final WebClient fileWebClient;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration MIN_BACKOFF = Duration.ofSeconds(1);

    FileClient(@Qualifier("fileWebClient") WebClient fileWebClient) {
        this.fileWebClient = fileWebClient;
    }

    Mono<UUID> postFile(File file) {

        return fileWebClient
                .post()
                .bodyValue(file)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new FileUploadException(file, errorBody)));
                    } else {
                        return clientResponse.bodyToMono(UUID.class);
                    }
                })
                .retryWhen(Retry.backoff(MAX_ATTEMPTS, MIN_BACKOFF)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                );
    }

}
