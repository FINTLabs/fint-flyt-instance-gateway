package no.fintlabs.gateway.instance.exception;

import no.fintlabs.gateway.instance.model.File;

public class FileUploadErrorException extends AbstractInstanceRejectedException {
    public FileUploadErrorException(File file, String errorBody) {
        super("Could not post file=" + file + " Error: " + errorBody);
    }
}