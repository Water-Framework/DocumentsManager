package it.water.documents.manager.service.repository;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.documents.manager.api.DocumentRepository;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;
import it.water.documents.manager.model.Document;
import it.water.documents.manager.model.exception.NoDocumentRepositoryClientInstalled;
import it.water.repository.jpa.WaterJpaRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkComponent
public class DocumentRepositoryImpl extends WaterJpaRepositoryImpl<Document> implements DocumentRepository {

    private static final String DOCUMENT_PERSISTENCE_UNIT = "document-persistence-unit";
    private static Logger logger = LoggerFactory.getLogger(DocumentRepositoryImpl.class);

    @Inject
    @Setter
    private DocumentRepositoryIntegrationClient documentRepositoryIntegrationClient;

    public DocumentRepositoryImpl() {
        super(Document.class, DOCUMENT_PERSISTENCE_UNIT);
    }

    //Overriding in order to integrate with documental repository
    @Override
    public Document persist(Document entity, Runnable runnable) {
        this.checkDocumentRepositoryComponentNotNull();
        logger.debug("Persisting document {}", entity);
        return this.tx(Transactional.TxType.REQUIRED, entityManager -> {
            Document savedDocument = super.persist(entity, runnable);
            this.documentRepositoryIntegrationClient.saveOrUpdateDocument(savedDocument.getPath(), savedDocument.getDocumentContentInputStream());
            return savedDocument;
        });
    }

    //Overriding in order to integrate with documental repository
    @Override
    public Document update(Document entity, Runnable runnable) {
        this.checkDocumentRepositoryComponentNotNull();
        logger.debug("Updating document {}", entity);
        return this.tx(Transactional.TxType.REQUIRED, entityManager -> {
            Document savedDocument = super.update(entity, runnable);
            this.documentRepositoryIntegrationClient.saveOrUpdateDocument(savedDocument.getPath(), savedDocument.getDocumentContentInputStream());
            return savedDocument;
        });
    }

    //Overriding in order to integrate with documental repository
    @Override
    public void remove(long id, Runnable runnable) {
        this.checkDocumentRepositoryComponentNotNull();
        logger.debug("Removing document {}", id);
        this.txExpr(Transactional.TxType.REQUIRED, entityManager -> {
            Document document = this.find(id);
            super.remove(id, runnable);
            this.documentRepositoryIntegrationClient.removeDocument(document.getPath());
        });
    }

    void checkDocumentRepositoryComponentNotNull() {
        //if (documentRepositoryIntegrationClient == null)
          //  throw new NoDocumentRepositoryClientInstalled();
    }
}
