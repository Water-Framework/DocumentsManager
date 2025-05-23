package it.water.documents.manager.api;

import it.water.core.api.service.BaseEntitySystemApi;
import it.water.documents.manager.model.Document;

/**
 * @Generated by Water Generator
 * This interface defines the internally exposed methods for the entity and allows interaction with it bypassing permission system.
 * The main goals of DocumentsManagerSystemApi is to validate the entity and pass it to the persistence layer.
 */
public interface DocumentSystemApi extends BaseEntitySystemApi<Document> {

    /**
     * Retrieve document content
     * Input Stream field will be filled with the file content
     *
     * @param documentId
     * @return
     */
    Document fetchDocumentContent(long documentId);

    /**
     * Retrieve document content by path
     * Input Stream field will be filled with the file content
     *
     * @param path
     * @return
     */
    Document fetchDocumentContentByPath(String path, String fileName);

    /**
     * Retrieve document content by document uid
     * Input Stream field will be filled with the file content
     *
     * @param documentUID
     * @return
     */
    Document fetchDocumentContentByUID(String documentUID);

}