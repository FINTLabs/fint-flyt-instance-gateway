package no.fintlabs.gateway.instance.exception;

import lombok.Getter;

@Getter
public abstract class AbstractInstanceRejectedException extends RuntimeException {
    public AbstractInstanceRejectedException(String message) {
        super(message);
    }
}