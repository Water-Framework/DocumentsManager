package it.water.documents.manager;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkComponent
public class FakeDocumentRepositoryIntegrationClient implements DocumentRepositoryIntegrationClient {

    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

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
        //do nothing
    }

    @Override
    public void addFolder(String path) {
        //do nothing
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
        storage.keySet().removeIf(key -> key.startsWith(path));
    }

    @Override
    public void renameFolder(String oldPath, String newPath) {
        storage.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(oldPath))
                .toList() // copy to avoid concurrent modification
                .forEach(entry -> {
                    String newKey = entry.getKey().replaceFirst(oldPath, newPath);
                    storage.put(newKey, entry.getValue());
                    storage.remove(entry.getKey());
                });
    }

}
