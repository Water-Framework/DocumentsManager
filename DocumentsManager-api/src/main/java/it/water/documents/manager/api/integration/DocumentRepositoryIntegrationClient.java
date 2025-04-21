package it.water.documents.manager.api.integration;

import java.io.InputStream;

public interface DocumentRepositoryIntegrationClient {
    /**
     * Adds new file into the specified path (The resource is identified by the pair resourceClassName-resourceId)
     *
     * @param path
     * @param sourceFile
     */
    void addNewFile(String path, InputStream sourceFile);

    /**
     * @param path
     * @param sourceFile
     */
    void updateFile(String path, InputStream sourceFile);

    /**
     *
     * @param oldPath
     * @param newPath
     * @param fileName
     */
    void moveFile(String oldPath, String newPath,String fileName);

    /**
     *
     * @param path
     * @param oldFileName
     * @param newFileName
     */
    void renameFile(String path,String oldFileName, String newFileName);

    /**
     * @param path
     * @param fileName
     */
    void deleteFile(String path, String fileName);

    /**
     * Adds a folder with specified name
     *
     * @param path
     */
    void addFolder(String path,String folderName);


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
     *
     * @param path
     * @param oldName
     * @param newName
     */
    void renameFolder(String path,String oldName,String newName);

    /**
     * @param oldPath
     * @param newPath
     */
    void moveFolder(String oldPath, String newPath);

}
