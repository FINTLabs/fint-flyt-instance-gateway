package no.fintlabs.gateway.instance;

public enum ErrorCode {
    GENERAL_SYSTEM_ERROR,
    INSTANCE_VALIDATION_ERROR,
    NO_INTEGRATION_FOUND_ERROR,
    INTEGRATION_DEACTIVATED_ERROR;

    private static final String ERROR_PREFIX = "FINT_FLYT_INSTANCE_GATEWAY_";

    public String getCode() {
        return ERROR_PREFIX + name();
    }

}
