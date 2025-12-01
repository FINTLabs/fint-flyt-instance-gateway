package no.novari.flyt.instance.gateway.exception;

import lombok.Getter;
import no.novari.flyt.instance.gateway.model.File;

@Getter
public class FileUploadException extends RuntimeException {

    private final File file;

    public FileUploadException(File file, String postResponse) {
        super("Could not post file=" + file + ". POST response='" + postResponse + "'");
        this.file = file;
    }
}