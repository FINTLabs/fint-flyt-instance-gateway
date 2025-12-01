package no.novari.flyt.instance.gateway.exception;

import lombok.Getter;

@Getter
public abstract class AbstractInstanceRejectedException extends RuntimeException {
    public AbstractInstanceRejectedException(String message) {
        super(message);
    }
}