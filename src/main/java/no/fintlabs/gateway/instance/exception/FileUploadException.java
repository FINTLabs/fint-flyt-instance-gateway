package no.fintlabs.gateway.instance.exception;

import no.fintlabs.gateway.instance.model.File;

public class FileUploadException extends AbstractInstanceRejectedException {
    public FileUploadException(File file, String errorBody) {
        super("Could not post file=" + file + " Error: " + errorBody);
    }
}