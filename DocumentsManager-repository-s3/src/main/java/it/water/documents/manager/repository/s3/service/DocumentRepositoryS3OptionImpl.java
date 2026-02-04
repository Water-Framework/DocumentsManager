package it.water.documents.manager.repository.s3.service;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.documents.manager.repository.s3.api.DocumentRepositoryS3Option;
import lombok.Setter;

import static it.water.documents.manager.repository.s3.config.DocumentRepositoryS3Constant.*;

/** implementation of document repository options  that retrive information from application properties  */
@FrameworkComponent(services = DocumentRepositoryS3Option.class)
public class DocumentRepositoryS3OptionImpl implements DocumentRepositoryS3Option {


    @Inject
    @Setter
    private ApplicationProperties applicationProperties;


    @Override
    public String getEndpoint() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_ENDPOINT,
                DEFAULT_STORAGE_ENDPOINT
        );
    }

    @Override
    public String getAccessKey() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_ACCESS_KEY,
                DEFAULT_STORAGE_KEY
        );
    }

    @Override
    public String getSecretKey() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_SECRET_KEY,
                DEFAULT_STORAGE_KEY
        );
    }

    @Override
    public String getRegion() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_REGION,
                DEFAULT_REGION
        );
    }

    @Override
    public String getBucket() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_BUCKET,
                DEFAULT_BUCKET
        );
    }

    @Override
    public boolean isPathStyleEnabled() {
        return applicationProperties.getPropertyOrDefault(
                PROP_S3_PATH_STYLE,
                DEFAULT_PATH_STYLE_ENABLED
        );
    }
}