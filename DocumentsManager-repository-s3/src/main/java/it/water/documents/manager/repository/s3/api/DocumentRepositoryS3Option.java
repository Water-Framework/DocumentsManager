package it.water.documents.manager.repository.s3.api;

import it.water.core.api.service.Service;

/** necessary method in order to retrive options for  set S3 repository */
public interface DocumentRepositoryS3Option extends Service {

    String getEndpoint();

    String getAccessKey();

    String getSecretKey();

    String getRegion();

    String getBucket();

    boolean isPathStyleEnabled();


}
