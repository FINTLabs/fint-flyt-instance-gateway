# FINT Flyt Instance Gateway

Spring Boot library that validates and forwards FINT Flyt instances from source applications, persists attachments to the file-service, and emits instance and error events on Kafka. It is designed to be embedded in edge services that expose HTTP endpoints and map payloads into the shared `InstanceObject` model.

## Highlights

- **Reactive instance pipeline** — `InstanceProcessor` orchestrates auth, validation, integration lookup, file uploads, mapping, and publishing of `instance-received` events with flow headers.
- **Integration verification** — request/reply lookup ensures an integration exists and is active before processing; can be toggled with `novari.flyt.instance-gateway.check-integration-exists`.
- **Error surfacing** — `instance-receival-error` topic collects validation, rejection, integration, upload, and system failures with structured error codes.
- **File offloading** — OAuth2-enabled WebClient with retry/backoff uploads attachments to the file-service and records file IDs in the emitted headers.
- **Archive support** — request/reply helpers resolve archive case IDs from source instance IDs and fetch `SakResource` details.

## Architecture Overview

| Component | Responsibility |
| --- | --- |
| `InstanceProcessor` | Builds `InstanceFlowHeaders`, checks integration state, validates input, uploads files, maps payloads, and publishes `instance-received` events or error responses. |
| `InstanceProcessorFactoryService` | Creates `InstanceProcessor` instances with integration/instance ID extractors and a provided `InstanceMapper`. |
| `InstanceMapper<T>` | Maps inbound payloads to `InstanceObject` and persists attached files via a provided `persistFile` function. |
| `InstanceValidationService` / `InstanceValidationErrorMappingService` | Runs Bean Validation (including `@ValidBase64`) and converts violations to Kafka-friendly `ErrorCollection` payloads. |
| `IntegrationRequestProducerService` | Request/reply lookup for integrations; rejects deactivated/missing integrations and provisions reply topics. |
| `ReceivedInstanceEventProducerService` | Emits `instance-received` events using the InstanceFlow templates. |
| `InstanceReceivalErrorEventProducerService` | Publishes `instance-receival-error` events for validation, rejection, integration, upload, and general failures while provisioning the topic. |
| `FileClient` / `FileWebClientConfiguration` | OAuth2 WebClient with connection pooling, large payload support, and retry/backoff for posting files to the file-service. |
| `ArchiveCaseService` / `ArchiveCaseIdRequestService` / `ArchiveCaseRequestService` | Request/reply helpers that resolve archive case IDs from source application instances and fetch `SakResource` records. |

## HTTP API

This module does not ship with controllers; embed `InstanceProcessor` in your own WebFlux/Spring MVC endpoints. Typical responses from `processInstance` handlers:

| Status | Description |
| --- | --- |
| `202 Accepted` | Instance accepted and `instance-received` event published. |
| `422 Unprocessable Entity` | Validation errors, missing integrations, deactivated integrations, or domain rejections; details also published to `instance-receival-error`. |
| `500 Internal Server Error` | File upload or unexpected failures; a generic error event is emitted. |

## Kafka Integration

- Produces `instance-received` events through the InstanceFlow template using org/application/topic prefixes.
- Publishes `instance-receival-error` events with error codes (`INSTANCE_VALIDATION_ERROR`, `INSTANCE_REJECTED_ERROR`, `FILE_UPLOAD_ERROR`, `NO_INTEGRATION_FOUND_ERROR`, `INTEGRATION_DEACTIVATED_ERROR`, `GENERAL_SYSTEM_ERROR`). Topic retention/cleanup/partitions are configured via `novari.flyt.instance-gateway.kafka.topic.instance-receival-error.*`.
- Requests integrations via request/reply on the `integration` resource (`source-application-id-and-source-application-integration-id` parameter). Reply topics are provisioned with 10m retention and 15s timeout, keyed by `fint.application-id`.
- Resolves archive case IDs with `archive-instance-id` requests (10s timeout) and fetches cases from `arkiv-noark-sak-with-filtered-journalposts` (60s timeout), using `novari.kafka.application-id` for reply topic naming.

## Scheduled Tasks

None; processing is event-driven with Reactor retries for file uploads.

## Configuration

The library is typically combined with the shared `flyt-kafka`, `flyt-logging`, and `flyt-resource-server` profiles.

| Property | Description |
| --- | --- |
| `fint.application-id` | Application ID used when naming reply topics for integration lookups (required). |
| `novari.kafka.application-id` | Application ID used for archive-related reply topics (required). |
| `novari.flyt.file-service-url` | Base URL for the file-service; `/api/intern-klient/filer` is appended. |
| `novari.flyt.instance-gateway.check-integration-exists` | Enable/disable integration existence/state checks before processing (default `true`). |
| `novari.flyt.instance-gateway.kafka.topic.instance-receival-error.retention-time` | Retention for error events (default `7d`). |
| `novari.flyt.instance-gateway.kafka.topic.instance-receival-error.cleanup-frequency` | Cleanup frequency for the error topic (default `NORMAL`). |
| `novari.flyt.instance-gateway.kafka.topic.instance-receival-error.partitions` | Partition count for the error topic (default `1`). |
| `spring.security.oauth2.client.registration.file-service.*` | OAuth2 client configuration used by the file WebClient. |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Required in host services so `SourceApplicationAuthorizationService` can resolve source application IDs from JWTs. |

## Running Locally

Prerequisites:

- Java 21+
- Access to Kafka for end-to-end testing; unit tests run without external services

Useful commands:

```shell
./gradlew clean build    # compile and run tests
./gradlew test           # run the test suite
./gradlew publishToMavenLocal  # publish the library locally for consumers
```

## Deployment

Published as a library. Push releases to the FINT Labs repository with

```shell
./gradlew publish
```

Provide `REPOSILITE_USERNAME` and `REPOSILITE_PASSWORD` for authenticated publishing. Consumers depend on `no.novari:flyt-instance-gateway` with the desired version.

## Security

- Relies on the FINT OAuth2 resource server setup so `SourceApplicationAuthorizationService` can identify calling source applications.
- File uploads use the `file-service` OAuth2 client registration; ensure credentials and scopes are available in the runtime environment.
- Integration checks can be disabled for test environments via `novari.flyt.instance-gateway.check-integration-exists=false`.

## Observability & Operations

- Logging is handled by the host application; Kafka producers log failures around upload and validation flows.
- Error events on `instance-receival-error` provide the primary operational signal for rejected instances.
- Any Actuator/Prometheus exposure comes from the embedding service; this module does not add endpoints on its own.

## Development Tips

- Implement `InstanceMapper` to translate inbound DTOs to `InstanceObject` and invoke the provided file persistence hook for attachments.
- Add Bean Validation annotations (including `@ValidBase64`) to your inbound models to surface predictable validation errors.
- Keep request/reply timeouts and retention settings aligned with upstream services when adjusting Kafka topic configuration.
- Disable integration checks in local development when the integration lookup service is unavailable.

## Contributing

1. Create a topic branch for your change.
2. Run `./gradlew test` before raising a PR; consider `publishToMavenLocal` to validate consumer integration.
3. If you alter Kafka topic naming or request/reply parameters, update dependent services accordingly.
4. Add or adjust tests for new behaviours or edge cases.

FINT Flyt Instance Gateway is maintained by the FINT Flyt team. Reach out via the internal Slack channel or open an issue in this repository for questions or enhancements.
