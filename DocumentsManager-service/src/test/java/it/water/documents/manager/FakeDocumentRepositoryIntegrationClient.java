package it.water.documents.manager;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@FrameworkComponent
public class FakeDocumentRepositoryIntegrationClient implements DocumentRepositoryIntegrationClient {

    @Override
    public void saveOrUpdateDocument(String path, InputStream sourceFile) {
        //do nothing
    }

    @Override
    public void addFolder(String path) {
        //do nothing
    }

    @Override
    public InputStream fetchDocumentContent(String path) {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void emptyFolder(String path) {
        //do nothing
    }

    @Override
    public void removeFolder(String path) {
        //do nothing
    }

    @Override
    public void renameFolder(String oldPath, String path) {
        //do nothing
    }

    @Override
    public void removeDocument(String path) {
        //do nothing
    }
}
