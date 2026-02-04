package it.water.documents.manager.repository.s3.config;


public class DocumentRepositoryS3Constant {

    private DocumentRepositoryS3Constant() {}


    public static final String PROP_S3_ENDPOINT = "it.water.storage.endpoint";
    public static final String PROP_S3_ACCESS_KEY = "it.water.storage.access-key";
    public static final String PROP_S3_SECRET_KEY = "it.water.storage.secret-key";
    public static final String PROP_S3_REGION = "it.water.storage.region";
    public static final String PROP_S3_BUCKET = "it.water.storage.bucket";
    public static final String PROP_S3_PATH_STYLE = "it.water.storage.path-style-enabled";

    // DEFAULTS VALUES
    public static final String DEFAULT_STORAGE_ENDPOINT = "http://localhost:9000";
    public  static final String DEFAULT_STORAGE_KEY = "";
    public static final String DEFAULT_BUCKET = "bucket";
    public  static final String DEFAULT_REGION = "us-east-1";
    public static final boolean DEFAULT_PATH_STYLE_ENABLED = false;
}
