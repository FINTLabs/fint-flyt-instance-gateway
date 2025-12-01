package no.novari.flyt.instance.gateway.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class ArchiveCaseIdRequestParams {
    private final Long sourceApplicationId;
    private final String sourceApplicationInstanceId;
}
