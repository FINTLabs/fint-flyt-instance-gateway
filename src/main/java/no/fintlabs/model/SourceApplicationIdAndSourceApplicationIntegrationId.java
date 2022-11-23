package no.fintlabs.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceApplicationIdAndSourceApplicationIntegrationId {
    final Long sourceApplicationId;
    final String sourceApplicationIntegrationId;
}
