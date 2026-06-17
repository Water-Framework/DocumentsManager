package it.water.documents.manager.model.exception;

import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.validation.ValidationError;

import java.util.Collections;

/**
 * #19 (Unrestricted file upload): raised when an uploaded document is rejected by the
 * upload-time validation, either because it exceeds the configured maximum size
 * (property {@code water.documents.upload.max.size}) or because its real, content-sniffed
 * MIME type is not in the configured allow-list (property {@code water.documents.upload.allowed.mime}).
 *
 * <p>It extends {@link ValidationException} on purpose: the shared REST exception mapper
 * ({@code it.water.service.rest.GenericExceptionMapperProvider}) maps {@code ValidationException}
 * to HTTP 422 (Unprocessable Entity). A dedicated 413 (Payload Too Large) status would require
 * editing that shared mapper in the Rest module (out of scope for this module-local fix), so an
 * oversize or disallowed upload is surfaced to the client as <b>HTTP 422</b>.
 */
public class InvalidDocumentUploadException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidDocumentUploadException(String field, String invalidValue, String reason) {
        super(Collections.singletonList(new ValidationError(reason, field, invalidValue)));
    }
}
