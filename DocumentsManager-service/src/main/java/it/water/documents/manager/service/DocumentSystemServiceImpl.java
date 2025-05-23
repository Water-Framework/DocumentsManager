package it.water.documents.manager.service;

import it.water.core.api.registry.filter.ComponentFilterBuilder;
import it.water.core.api.repository.query.Query;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.documents.manager.api.DocumentRepository;
import it.water.documents.manager.api.DocumentSystemApi;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;
import it.water.documents.manager.model.Document;
import it.water.repository.service.BaseEntitySystemServiceImpl;
import lombok.Getter;
import lombok.Setter;


/**
 * @Generated by Water Generator
 * System Service Api Class for DocumentsManager entity.
 */
@FrameworkComponent
public class DocumentSystemServiceImpl extends BaseEntitySystemServiceImpl<Document> implements DocumentSystemApi {
    @Inject
    @Getter
    @Setter
    private DocumentRepository repository;

    @Inject
    @Setter
    private DocumentRepositoryIntegrationClient documentRepositoryIntegrationClient;

    @Inject
    @Setter
    private ComponentFilterBuilder componentFilterBuilder;

    public DocumentSystemServiceImpl() {
        super(Document.class);
    }

    @Override
    public Document fetchDocumentContent(long documentId) {
        Document document = find(documentId);
        fillDocumentEntityWithContent(document);
        return document;
    }

    @Override
    public Document fetchDocumentContentByPath(String path, String fileName) {
        Document d = this.getRepository().findByPath(path, fileName);
        fillDocumentEntityWithContent(d);
        return d;
    }

    @Override
    public Document fetchDocumentContentByUID(String documentUID) {
        Query byUid = getQueryBuilderInstance().field("uid").equalTo(documentUID);
        Document document = find(byUid);
        fillDocumentEntityWithContent(document);
        return document;
    }

    private void fillDocumentEntityWithContent(Document document) {
        document.setDocumentContentInputStream(documentRepositoryIntegrationClient.fetchDocumentContent(document.getFullPath()));
    }
}