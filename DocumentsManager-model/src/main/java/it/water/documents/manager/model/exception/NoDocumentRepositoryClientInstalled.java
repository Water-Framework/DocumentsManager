package it.water.documents.manager.model.exception;

public class NoDocumentRepositoryClientInstalled extends RuntimeException {
    public NoDocumentRepositoryClientInstalled() {
        super("No document repository client installed!");
    }
}
