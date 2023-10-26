package no.fintlabs.gateway.instance.exception;

import lombok.Getter;
import no.fintlabs.gateway.instance.model.File;

@Getter
public class FileUploadException extends RuntimeException {

    private final File file;
    public FileUploadException(File file, String postResponse) {
        super("Could not post file=" + file + ". POST response='" + postResponse + "'");
        this.file = file;
    }
}