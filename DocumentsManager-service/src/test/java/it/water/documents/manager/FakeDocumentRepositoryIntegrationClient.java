package it.water.documents.manager;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkComponent
public class FakeDocumentRepositoryIntegrationClient implements DocumentRepositoryIntegrationClient {

    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();
    private final Set<String> folders = new ConcurrentHashSet<>();

    @Override
    public void addNewFile(String path, InputStream sourceFile) {
        if (sourceFile == null)
            return;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = sourceFile.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            storage.put(path, buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document at path: " + path, e);
        }
    }

    @Override
    public void updateFile(String path, InputStream sourceFile) {
        addNewFile(path, sourceFile);
    }

    @Override
    public void moveFile(String oldPath, String newPath, String fileName) {
        String oldFullPath = oldPath + "/" + fileName;
        String newFullPath = newPath + "/" + fileName;
        byte[] fileContent = storage.get(oldFullPath);
        if (fileContent != null) {
            storage.put(newFullPath, fileContent);
        }
    }

    @Override
    public void renameFile(String path, String oldFileName, String newFileName) {
        String oldFullPath = path + "/" + oldFileName;
        String newFullPath = path + "/" + newFileName;
        byte[] fileContent = storage.get(oldFullPath);
        storage.put(newFullPath, fileContent);
    }

    @Override
    public void deleteFile(String path, String fileName) {
        storage.remove(path + "/" + fileName);
    }

    @Override
    public void moveFolder(String oldPath, String newPath) {
        folders.remove(oldPath);
        folders.add(newPath);
    }

    @Override
    public void addFolder(String path, String folderName) {
        String fullPath = path + "/" + folderName;
        folders.add(fullPath);
    }

    @Override
    public InputStream fetchDocumentContent(String path) {
        byte[] data = storage.get(path);
        return data != null ? new ByteArrayInputStream(data) : new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void emptyFolder(String path) {
        storage.keySet().removeIf(key -> key.startsWith(path));
    }

    @Override
    public void removeFolder(String path) {
        folders.remove(path);
    }

    @Override
    public void renameFolder(String path, String oldName, String newName) {
        String oldPath = path + "/" + oldName;
        String newPath = path + "/" + newName;
        folders.remove(oldPath);
        folders.add(newPath);
    }

}
