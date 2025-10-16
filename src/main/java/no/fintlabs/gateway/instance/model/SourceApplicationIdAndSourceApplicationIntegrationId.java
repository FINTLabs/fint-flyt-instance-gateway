package no.fintlabs.gateway.instance.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class SourceApplicationIdAndSourceApplicationIntegrationId {
    final Long sourceApplicationId;
    final String sourceApplicationIntegrationId;
}
