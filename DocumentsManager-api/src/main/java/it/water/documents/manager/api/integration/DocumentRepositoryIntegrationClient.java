package it.water.documents.manager.api.integration;

import java.io.InputStream;

public interface DocumentRepositoryIntegrationClient {
    /**
     * Adds new file into the specified path (The resource is identified by the pair resourceClassName-resourceId)
     *
     * @param path
     * @param sourceFile
     */
    void saveOrUpdateDocument(String path, InputStream sourceFile);

    /**
     * Adds a folder with specified name
     *
     * @param path
     */
    void addFolder(String path);


    /**
     * Retrieve document content by path
     *
     * @param path
     * @return
     */
    InputStream fetchDocumentContent(String path);


    /**
     * Removes all documents inside a folder
     *
     * @param path
     */
    void emptyFolder(String path);

    /**
     * Removes folder
     *
     * @param path
     */
    void removeFolder(String path);

    /**
     * Renames a folder
     *
     * @param oldPath
     * @param path
     */
    void renameFolder(String oldPath, String path);

    /**
     * Remove document
     *
     * @param path
     */
    void removeDocument(String path);

}
