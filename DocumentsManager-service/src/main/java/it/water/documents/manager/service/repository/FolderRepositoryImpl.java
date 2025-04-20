package it.water.documents.manager.service.repository;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.documents.manager.api.FolderRepository;
import it.water.documents.manager.api.integration.DocumentRepositoryIntegrationClient;
import it.water.documents.manager.model.Folder;
import it.water.repository.jpa.WaterJpaRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkComponent
public class FolderRepositoryImpl extends WaterJpaRepositoryImpl<Folder> implements FolderRepository {

    private static final String DOCUMENT_PERSISTENCE_UNIT = "document-persistence-unit";
    private static Logger logger = LoggerFactory.getLogger(FolderRepositoryImpl.class);

    @Inject
    @Setter
    private DocumentRepositoryIntegrationClient documentRepositoryIntegrationClient;

    public FolderRepositoryImpl() {
        super(Folder.class, DOCUMENT_PERSISTENCE_UNIT);
    }

    //Overriding in order to integrate with documental repository
    @Override
    public Folder persist(Folder entity, Runnable runnable) {
        logger.debug("Persisting folder {}", entity);
        return this.tx(Transactional.TxType.REQUIRED, entityManager -> {
            Folder savedFolder = super.persist(entity, runnable);
            this.documentRepositoryIntegrationClient.addFolder(savedFolder.getPath());
            return savedFolder;
        });
    }

    //Overriding in order to integrate with documental repository
    @Override
    public Folder update(Folder entity, Runnable runnable) {
        logger.debug("Updating folder {}", entity);
        return this.tx(Transactional.TxType.REQUIRED, entityManager -> {
            String oldPath = find(entity.getId()).getPath();
            Folder savedDocument = super.update(entity, runnable);
            this.documentRepositoryIntegrationClient.renameFolder(oldPath, entity.getPath());
            return savedDocument;
        });
    }

    //Overriding in order to integrate with documental repository
    @Override
    public void remove(long id, Runnable runnable) {
        logger.debug("Removing folder {}", id);
        this.txExpr(Transactional.TxType.REQUIRED, entityManager -> {
            Folder folder = this.find(id);
            super.remove(id, runnable);
            this.documentRepositoryIntegrationClient.removeFolder(folder.getPath());
        });
    }
}
