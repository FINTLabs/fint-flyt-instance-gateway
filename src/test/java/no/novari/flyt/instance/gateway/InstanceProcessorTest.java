package no.novari.flyt.instance.gateway;

import no.novari.flyt.instance.gateway.exception.FileUploadException;
import no.novari.flyt.instance.gateway.kafka.InstanceReceivalErrorEventProducerService;
import no.novari.flyt.instance.gateway.kafka.IntegrationRequestProducerService;
import no.novari.flyt.instance.gateway.kafka.ReceivedInstanceEventProducerService;
import no.novari.flyt.instance.gateway.model.File;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import no.novari.flyt.instance.gateway.validation.InstanceValidationService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InstanceProcessorTest {

    @Mock
    private IntegrationRequestProducerService integrationRequestProducerService;

    @Mock
    private InstanceValidationService instanceValidationService;

    @Mock
    private ReceivedInstanceEventProducerService receivedInstanceEventProducerService;

    @Mock
    private InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;

    @Mock
    private SourceApplicationAuthorizationService sourceApplicationAuthorizationService;

    @Mock
    private FileClient fileClient;

    @Mock
    private Function<Object, Optional<String>> sourceApplicationIntegrationIdFunction;

    @Mock
    private Function<Object, Optional<String>> sourceApplicationInstanceIdFunction;

    @Mock
    private InstanceMapper<Object> instanceMapper;

    @Mock
    private InstanceObject instanceObject;

    private InstanceProcessor<Object> instanceProcessor;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        instanceProcessor = new InstanceProcessor<>(
                integrationRequestProducerService,
                instanceValidationService,
                receivedInstanceEventProducerService,
                instanceReceivalErrorEventProducerService,
                sourceApplicationAuthorizationService,
                fileClient,
                sourceApplicationIntegrationIdFunction,
                sourceApplicationInstanceIdFunction,
                instanceMapper
        );
    }

    @Test
    void shouldProcessInstanceAndPostFileSuccessfully() {
        when(sourceApplicationIntegrationIdFunction.apply(any())).thenReturn(Optional.of("integrationId"));
        when(sourceApplicationInstanceIdFunction.apply(any())).thenReturn(Optional.of("instanceId"));
        when(instanceValidationService.validate(any())).thenReturn(Optional.empty());
        when(fileClient.postFile(any())).thenReturn(Mono.just(UUID.randomUUID()));

        when(instanceMapper.map(any(), any(), any())).thenAnswer(
                invocation -> {
                    Function<File, Mono<UUID>> postFile = invocation.getArgument(2);
                    return postFile.apply(File.builder().build())
                            .map(next -> instanceObject);
                }
        );

        Mono<ResponseEntity<Object>> result = instanceProcessor.processInstance(authentication, new Object());

        StepVerifier.create(result)
                .expectNext(ResponseEntity.accepted().build())
                .verifyComplete();

        verify(fileClient).postFile(any());
        verify(receivedInstanceEventProducerService).publish(any(), any());
    }

    @Test
    void shouldProcessInstanceAndPostMultipleFilesSuccessfully() {
        when(sourceApplicationIntegrationIdFunction.apply(any())).thenReturn(Optional.of("integrationId"));
        when(sourceApplicationInstanceIdFunction.apply(any())).thenReturn(Optional.of("instanceId"));
        when(instanceValidationService.validate(any())).thenReturn(Optional.empty());
        when(fileClient.postFile(any())).thenReturn(Mono.just(UUID.randomUUID()));

        when(instanceMapper.map(any(), any(), any())).thenAnswer(
                invocation -> {
                    Function<File, Mono<UUID>> postFile = invocation.getArgument(2);
                    return postFile.apply(File.builder().build())
                            .flatMap(next -> postFile.apply(File.builder().build()))
                            .flatMap(next -> postFile.apply(File.builder().build()))
                            .map(next -> instanceObject);
                }
        );

        Mono<ResponseEntity<Object>> result = instanceProcessor.processInstance(authentication, new Object());

        StepVerifier.create(result)
                .expectNext(ResponseEntity.accepted().build())
                .verifyComplete();

        verify(fileClient, times(3)).postFile(any());
        verify(receivedInstanceEventProducerService).publish(any(), any());
    }

    @Test
    void shouldProcessInstanceAndHandleSecondFileFailure() {
        when(sourceApplicationIntegrationIdFunction.apply(any())).thenReturn(Optional.of("integrationId"));
        when(sourceApplicationInstanceIdFunction.apply(any())).thenReturn(Optional.of("instanceId"));
        when(instanceValidationService.validate(any())).thenReturn(Optional.empty());

        when(fileClient.postFile(any()))
                .thenReturn(Mono.just(UUID.randomUUID()))
                .thenReturn(Mono.error(new FileUploadException(File.builder().build(), "File upload failed"))); // Second file upload fails

        when(instanceMapper.map(any(), any(), any())).thenAnswer(
                invocation -> {
                    Function<File, Mono<UUID>> postFile = invocation.getArgument(2);
                    return postFile.apply(File.builder().build())
                            .flatMap(next -> postFile.apply(File.builder().build()))
                            .map(next -> instanceObject);
                }
        );

        Mono<ResponseEntity<Object>> result = instanceProcessor.processInstance(authentication, new Object());

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().is5xxServerError() &&
                                response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                                Objects.requireNonNull(response.getBody()).toString().contains("File upload failed"))
                .verifyComplete();

        verify(fileClient, times(2)).postFile(any());
        verify(receivedInstanceEventProducerService, never()).publish(any(), any());
    }

}
