package no.fintlabs.web;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class FileClient {

    private final WebClient fileWebClient;

    public FileClient(@Qualifier("fileWebClient") WebClient fileWebClient) {
        this.fileWebClient = fileWebClient;
    }

    public Mono<UUID> postFile(File file) {
        return fileWebClient
                .post()
                .bodyValue(file)
                .retrieve()
                .bodyToMono(UUID.class)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .doOnError(
                        e -> log.error("Could not post file=" + file, e)
                );
    }

}
