package it.water.documents.manager.repository.s3;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.Service;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.documents.manager.repository.s3.api.DocumentRepositoryS3Option;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Properties;

import static it.water.documents.manager.repository.s3.config.DocumentRepositoryS3Constant.*;

/**
 * Test class for DocumentRepositoryS3Option.
 * Tests the S3 repository options service that retrieves configuration from application properties.
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentRepositoryS3OptionTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private DocumentRepositoryS3Option documentRepositoryS3Option;

    @Inject
    @Setter
    private ApplicationProperties applicationProperties;

    /**
     * Test that DocumentRepositoryS3Option component is correctly instantiated.
     */
    @Test
    @Order(1)
    void componentShouldBeInstantiatedCorrectly() {
        Assertions.assertNotNull(componentRegistry);
        Assertions.assertNotNull(documentRepositoryS3Option);
        Assertions.assertNotNull(applicationProperties);
        DocumentRepositoryS3Option foundComponent = componentRegistry.findComponent(DocumentRepositoryS3Option.class, null);
        Assertions.assertNotNull(foundComponent);
    }

    /**
     * Test default values are returned when no properties are configured.
     */
    @Test
    @Order(2)
    void shouldReturnDefaultValuesWhenPropertiesNotSet() {
        Assertions.assertEquals(DEFAULT_STORAGE_ENDPOINT, documentRepositoryS3Option.getEndpoint());
        Assertions.assertEquals(DEFAULT_STORAGE_KEY, documentRepositoryS3Option.getAccessKey());
        Assertions.assertEquals(DEFAULT_STORAGE_KEY, documentRepositoryS3Option.getSecretKey());
        Assertions.assertEquals(DEFAULT_REGION, documentRepositoryS3Option.getRegion());
        Assertions.assertEquals(DEFAULT_BUCKET, documentRepositoryS3Option.getBucket());
        Assertions.assertEquals(DEFAULT_PATH_STYLE_ENABLED, documentRepositoryS3Option.isPathStyleEnabled());
    }

    /**
     * Test custom endpoint value is returned when configured.
     */
    @Test
    @Order(3)
    void shouldReturnCustomEndpointWhenConfigured() {
        String customEndpoint = "http://custom-s3-endpoint:9000";
        Properties props = new Properties();
        props.put(PROP_S3_ENDPOINT, customEndpoint);
        applicationProperties.loadProperties(props);
        Assertions.assertEquals(customEndpoint, documentRepositoryS3Option.getEndpoint());
    }

    /**
     * Test custom access key value is returned when configured.
     */
    @Test
    @Order(4)
    void shouldReturnCustomAccessKeyWhenConfigured() {
        String customAccessKey = "myAccessKey123";
        Properties props = new Properties();
        props.put(PROP_S3_ACCESS_KEY, customAccessKey);
        applicationProperties.loadProperties(props);
        Assertions.assertEquals(customAccessKey, documentRepositoryS3Option.getAccessKey());
    }

    /**
     * Test custom secret key value is returned when configured.
     */
    @Test
    @Order(5)
    void shouldReturnCustomSecretKeyWhenConfigured() {
        String customSecretKey = "mySecretKey456";
        Properties props = new Properties();
        props.put(PROP_S3_SECRET_KEY, customSecretKey);
        applicationProperties.loadProperties(props);
        Assertions.assertEquals(customSecretKey, documentRepositoryS3Option.getSecretKey());
    }

    /**
     * Test custom region value is returned when configured.
     */
    @Test
    @Order(6)
    void shouldReturnCustomRegionWhenConfigured() {
        String customRegion = "eu-west-1";
        Properties props = new Properties();
        props.put(PROP_S3_REGION, customRegion);
        applicationProperties.loadProperties(props);
        Assertions.assertEquals(customRegion, documentRepositoryS3Option.getRegion());
    }

    /**
     * Test custom bucket value is returned when configured.
     */
    @Test
    @Order(7)
    void shouldReturnCustomBucketWhenConfigured() {
        String customBucket = "my-custom-bucket";
        Properties props = new Properties();
        props.put(PROP_S3_BUCKET, customBucket);
        applicationProperties.loadProperties(props);
        Assertions.assertEquals(customBucket, documentRepositoryS3Option.getBucket());
    }

    /**
     * Test custom path style enabled value is returned when configured.
     */
    @Test
    @Order(8)
    void shouldReturnCustomPathStyleEnabledWhenConfigured() {
        Properties props = new Properties();
        props.put(PROP_S3_PATH_STYLE, true);
        applicationProperties.loadProperties(props);
        Assertions.assertTrue(documentRepositoryS3Option.isPathStyleEnabled());
    }

    /**
     * Test all S3 configuration properties together.
     */
    @Test
    @Order(9)
    void shouldReturnAllCustomValuesWhenAllPropertiesConfigured() {
        String customEndpoint = "http://minio:9000";
        String customAccessKey = "accessKey";
        String customSecretKey = "secretKey";
        String customRegion = "ap-southeast-1";
        String customBucket = "documents-bucket";
        boolean customPathStyle = true;

        Properties props = new Properties();
        props.put(PROP_S3_ENDPOINT, customEndpoint);
        props.put(PROP_S3_ACCESS_KEY, customAccessKey);
        props.put(PROP_S3_SECRET_KEY, customSecretKey);
        props.put(PROP_S3_REGION, customRegion);
        props.put(PROP_S3_BUCKET, customBucket);
        props.put(PROP_S3_PATH_STYLE, customPathStyle);
        applicationProperties.loadProperties(props);

        Assertions.assertEquals(customEndpoint, documentRepositoryS3Option.getEndpoint());
        Assertions.assertEquals(customAccessKey, documentRepositoryS3Option.getAccessKey());
        Assertions.assertEquals(customSecretKey, documentRepositoryS3Option.getSecretKey());
        Assertions.assertEquals(customRegion, documentRepositoryS3Option.getRegion());
        Assertions.assertEquals(customBucket, documentRepositoryS3Option.getBucket());
        Assertions.assertTrue(documentRepositoryS3Option.isPathStyleEnabled());
    }
}
