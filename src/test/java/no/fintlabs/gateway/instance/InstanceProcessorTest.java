package no.fintlabs.gateway.instance;

import no.fintlabs.gateway.instance.exception.FileUploadException;
import no.fintlabs.gateway.instance.kafka.InstanceReceivalErrorEventProducerService;
import no.fintlabs.gateway.instance.kafka.IntegrationRequestProducerService;
import no.fintlabs.gateway.instance.kafka.ReceivedInstanceEventProducerService;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.validation.InstanceValidationService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.*;

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

    @InjectMocks
    private InstanceProcessor<Object> instanceProcessor;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authentication = mock(Authentication.class);
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

        verify(fileClient, times(1)).postFile(any());
        verify(receivedInstanceEventProducerService, times(1)).publish(any(), any());
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
        verify(receivedInstanceEventProducerService, times(1)).publish(any(), any());
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
