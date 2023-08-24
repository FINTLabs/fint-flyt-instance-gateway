package no.fintlabs.gateway.instance;

import lombok.Getter;

@Getter
public abstract class AbstractInstanceRejectedException extends RuntimeException {
    public AbstractInstanceRejectedException(String message) {
        super(message);
    }
}