package no.fintlabs.gateway.instance.web;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.exception.FileUploadErrorException;
import no.fintlabs.gateway.instance.model.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class FileClient {

    private final WebClient fileWebClient;

    public FileClient(@Qualifier("fileWebClient") WebClient fileWebClient) {
        this.fileWebClient = fileWebClient;
    }

    public Mono<UUID> postFile(File file) {

        AtomicBoolean hasThrownFileUploadException = new AtomicBoolean(false);

        return fileWebClient
                .post()
                .bodyValue(file)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Could not post file=" + file + " Error: " + errorBody);
                                    if (!hasThrownFileUploadException.getAndSet(true)) {
                                        return Mono.error(new FileUploadErrorException(file, errorBody));
                                    }
                                    return Mono.error(new RuntimeException("File upload failed"));
                                });
                    } else {
                        return clientResponse.bodyToMono(UUID.class);
                    }
                })
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .doFinally(signalType -> hasThrownFileUploadException.set(false));
    }

}
