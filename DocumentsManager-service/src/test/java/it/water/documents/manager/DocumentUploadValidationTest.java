package it.water.documents.manager;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.Service;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.documents.manager.api.DocumentApi;
import it.water.documents.manager.model.Document;
import it.water.documents.manager.model.exception.InvalidDocumentUploadException;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Security fix #19 — Unrestricted file upload: size and MIME-type validation.
 *
 * <p>These tests exercise {@code DocumentServiceImpl.validateAndBoundUploadContent()} through
 * the public {@link DocumentApi} layer (the same entry point a REST caller uses), exactly as
 * the existing {@link DocumentApiTest} does.  Apache Tika is NOT mocked — the real content-
 * sniffing logic runs so that MIME detection is exercised end-to-end.
 *
 * <p>Test-order range: 40–49 (does not overlap with existing orders 1–32 in DocumentApiTest).
 *
 * <p>Seed range for Document UIDs / paths: 900–909 (does not collide with seeds used in
 * DocumentApiTest: 0–10, 101, 201, 301, 401, 501–505).
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentUploadValidationTest implements Service {

    // -----------------------------------------------------------------------
    // Minimal byte fixtures for real Tika detection
    // -----------------------------------------------------------------------

    /**
     * Minimal 8-byte PNG signature (IHDR chunk would follow in a real PNG, but Tika detects
     * the type purely from the magic bytes at the start — the full chunk structure is not
     * required for detection by tika-core).
     * Expected Tika detection: {@code image/png}.
     */
    private static final byte[] PNG_MAGIC =
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    /**
     * Minimal PDF header string.  Tika detects {@code application/pdf} from the {@code %PDF}
     * magic at offset 0.
     * Expected Tika detection: {@code application/pdf}.
     */
    private static final byte[] PDF_MAGIC =
            "%PDF-1.4\n%This is a minimal PDF for unit testing\n".getBytes(StandardCharsets.US_ASCII);

    /**
     * Plain ASCII text.  With no binary magic bytes Tika falls back to {@code text/plain}.
     * Expected Tika detection: {@code text/plain}.
     */
    private static final byte[] TEXT_PLAIN_BYTES =
            "Hello, Water Framework unit test.".getBytes(StandardCharsets.UTF_8);

    /**
     * Minimal HTML document.  Tika detects {@code text/html} from the {@code <!DOCTYPE html>}
     * declaration.  {@code text/html} is NOT in the default allow-list, so this must be rejected.
     * Expected Tika detection: {@code text/html}.
     */
    private static final byte[] HTML_BYTES =
            "<!DOCTYPE html><html><head><title>t</title></head><body>x</body></html>"
                    .getBytes(StandardCharsets.UTF_8);

    // -----------------------------------------------------------------------
    // Property key constants (mirrors DocumentServiceImpl package-private constants)
    // -----------------------------------------------------------------------
    private static final String PROP_UPLOAD_MAX_SIZE = "water.documents.upload.max.size";
    private static final long DEFAULT_UPLOAD_MAX_SIZE = 10_485_760L;

    // -----------------------------------------------------------------------
    // Injected components (same pattern as DocumentApiTest)
    // -----------------------------------------------------------------------

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private DocumentApi documentApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private ApplicationProperties applicationProperties;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeAll
    void beforeAll() {
        // Run all upload-validation tests as admin so permission checks do not interfere.
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    @AfterEach
    void afterEach() {
        // Restore the max-size property to the default after every test so that any
        // test that lowers the limit does not pollute subsequent tests.
        Properties restore = new Properties();
        restore.put(PROP_UPLOAD_MAX_SIZE, String.valueOf(DEFAULT_UPLOAD_MAX_SIZE));
        applicationProperties.loadProperties(restore);

        // Ensure we stay as admin for every test.
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    // -----------------------------------------------------------------------
    // Test 1 — Allowed MIME (PNG) within size: save succeeds
    // -----------------------------------------------------------------------

    /**
     * #19-1: A Document whose content is recognised by Tika as {@code image/png} (which is in the
     * default allow-list) and whose size is well within the 10 MiB limit must be saved without
     * throwing any exception.
     *
     * <p>Fixture: 8-byte PNG magic signature — the smallest payload that Tika reliably detects as PNG.
     */
    @Test
    @Order(40)
    void save_allowedMimePng_withinSizeLimit_succeeds() {
        Document doc = createDocumentWithContent(900, PNG_MAGIC);

        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "save() with an image/png payload within size limit must succeed");

        Assertions.assertTrue(saved.getId() > 0,
                "Saved document must have a positive id");
    }

    // -----------------------------------------------------------------------
    // Test 2 — Allowed MIME (PDF) within size: save succeeds
    // -----------------------------------------------------------------------

    /**
     * #19-2: A Document with a minimal PDF header (Tika: {@code application/pdf}) must be accepted
     * by save().
     */
    @Test
    @Order(41)
    void save_allowedMimePdf_withinSizeLimit_succeeds() {
        Document doc = createDocumentWithContent(901, PDF_MAGIC);

        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "save() with an application/pdf payload within size limit must succeed");

        Assertions.assertTrue(saved.getId() > 0,
                "Saved document must have a positive id");
    }

    // -----------------------------------------------------------------------
    // Test 3 — Allowed MIME (text/plain) within size: save succeeds
    // -----------------------------------------------------------------------

    /**
     * #19-3: A Document with plain ASCII text (Tika: {@code text/plain}) must be accepted by save().
     */
    @Test
    @Order(42)
    void save_allowedMimeTextPlain_withinSizeLimit_succeeds() {
        Document doc = createDocumentWithContent(902, TEXT_PLAIN_BYTES);

        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "save() with a text/plain payload within size limit must succeed");

        Assertions.assertTrue(saved.getId() > 0,
                "Saved document must have a positive id");
    }

    // -----------------------------------------------------------------------
    // Test 4 — Disallowed MIME (text/html): save throws InvalidDocumentUploadException
    // -----------------------------------------------------------------------

    /**
     * #19-4: A Document whose content Tika detects as {@code text/html} (not in the default
     * allow-list {@code application/pdf,image/png,image/jpeg,text/plain}) must be REJECTED.
     *
     * <p>The thrown exception must be {@link InvalidDocumentUploadException} (a subtype of
     * {@link ValidationException} → HTTP 422).  The check is on the real exception type so that
     * any regression that changes the exception type is caught.
     */
    @Test
    @Order(43)
    void save_disallowedMimeHtml_throwsInvalidDocumentUploadException() {
        Document doc = createDocumentWithContent(903, HTML_BYTES);

        // InvalidDocumentUploadException extends ValidationException — assert the specific subtype.
        Exception thrown = Assertions.assertThrows(ValidationException.class,
                () -> documentApi.save(doc),
                "save() with a text/html payload must throw ValidationException (InvalidDocumentUploadException)");

        Assertions.assertInstanceOf(InvalidDocumentUploadException.class, thrown,
                "The exception must be specifically InvalidDocumentUploadException, not a generic ValidationException");
    }

    // -----------------------------------------------------------------------
    // Test 5 — Disallowed MIME on update: update throws InvalidDocumentUploadException
    // -----------------------------------------------------------------------

    /**
     * #19-5: The same MIME rejection must apply on update().  This test saves a valid document
     * first (no content), then calls update() with a disallowed HTML payload attached.
     */
    @Test
    @Order(44)
    void update_disallowedMimeHtml_throwsInvalidDocumentUploadException() {
        // First save a document WITHOUT content so it persists cleanly.
        Document doc = createDocumentNoContent(904);
        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "Initial save without content must succeed");

        // Attach disallowed content for the update attempt.
        saved.setDocumentContentInputStream(new ByteArrayInputStream(HTML_BYTES));

        Assertions.assertThrows(InvalidDocumentUploadException.class,
                () -> documentApi.update(saved),
                "update() with a text/html payload must throw InvalidDocumentUploadException");
    }

    // -----------------------------------------------------------------------
    // Test 6 — Oversize content: save throws InvalidDocumentUploadException
    // -----------------------------------------------------------------------

    /**
     * #19-6: When the configured max size is set to a very small value (4 bytes) and the payload
     * carries more bytes than that, save() must throw {@link InvalidDocumentUploadException}.
     *
     * <p>Strategy: lower {@code water.documents.upload.max.size} to {@code 4} via
     * {@link ApplicationProperties#loadProperties}, then send 8 bytes of valid PNG magic.  The PNG
     * magic itself is 8 bytes > 4 bytes limit, so the size check fires before MIME detection.
     * The property is restored to the default in {@link #afterEach()}.
     *
     * <p>Note: the exact bytes of the payload do not matter for the size check — we use the PNG magic
     * bytes because they are a real valid array already defined as a constant.  Any 8-byte payload
     * would trigger the oversize rejection when the limit is 4 bytes.
     */
    @Test
    @Order(45)
    void save_oversizeContent_throwsInvalidDocumentUploadException() {
        // Lower the max size to 4 bytes so that our 8-byte PNG payload exceeds it.
        final long tinyLimit = 4L;
        Properties shrink = new Properties();
        shrink.put(PROP_UPLOAD_MAX_SIZE, String.valueOf(tinyLimit));
        applicationProperties.loadProperties(shrink);

        // PNG_MAGIC is 8 bytes — exceeds the 4-byte limit.
        Document doc = createDocumentWithContent(905, PNG_MAGIC);

        Assertions.assertThrows(InvalidDocumentUploadException.class,
                () -> documentApi.save(doc),
                "save() with a payload larger than the configured max size must throw InvalidDocumentUploadException");

        // afterEach() restores the property.
    }

    // -----------------------------------------------------------------------
    // Test 7 — Oversize on update: update throws InvalidDocumentUploadException
    // -----------------------------------------------------------------------

    /**
     * #19-7: Oversize rejection also applies on update().  Same property-lowering strategy as
     * test #19-6.
     */
    @Test
    @Order(46)
    void update_oversizeContent_throwsInvalidDocumentUploadException() {
        // Save without content first.
        Document doc = createDocumentNoContent(906);
        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "Initial save without content must succeed");

        // Lower the limit to 4 bytes, then attach an 8-byte PNG payload.
        final long tinyLimit = 4L;
        Properties shrink = new Properties();
        shrink.put(PROP_UPLOAD_MAX_SIZE, String.valueOf(tinyLimit));
        applicationProperties.loadProperties(shrink);

        saved.setDocumentContentInputStream(new ByteArrayInputStream(PNG_MAGIC));

        Assertions.assertThrows(InvalidDocumentUploadException.class,
                () -> documentApi.update(saved),
                "update() with a payload larger than the configured max size must throw InvalidDocumentUploadException");
    }

    // -----------------------------------------------------------------------
    // Test 8 — No content: save and update are unaffected (regression)
    // -----------------------------------------------------------------------

    /**
     * #19-8 (regression): When a Document has no content (i.e., {@code documentContentInputStream}
     * is {@code null}), save() and update() must complete without any upload-validation error.
     * This guards against the early-return {@code null} guard in
     * {@code validateAndBoundUploadContent} being accidentally removed.
     */
    @Test
    @Order(47)
    void save_noContent_succeedsWithoutValidationError() {
        Document doc = createDocumentNoContent(907);

        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "save() with no content must succeed without any upload-validation error");

        Assertions.assertTrue(saved.getId() > 0,
                "Saved document must have a positive id");

        // Verify update also works without content.
        int versionBeforeUpdate = saved.getEntityVersion();
        saved.setFileName("updated-fileName-907.txt");
        Document updated = Assertions.assertDoesNotThrow(() -> documentApi.update(saved),
                "update() with no content must succeed without any upload-validation error");

        Assertions.assertTrue(updated.getEntityVersion() > versionBeforeUpdate,
                "Updated document version must increment (no-content update still persists)");
    }

    // -----------------------------------------------------------------------
    // Test 9 — Allowed MIME via update: update replaces content with valid PNG, succeeds
    // -----------------------------------------------------------------------

    /**
     * #19-9: update() with an allowed-type payload (PNG) within the size limit must succeed and
     * must not overwrite the bounded bytes with the original stream (the implementation replaces
     * the stream with a fresh ByteArrayInputStream of the already-bounded bytes).
     */
    @Test
    @Order(48)
    void update_allowedMimePng_withinSizeLimit_succeeds() {
        // Save without content first.
        Document doc = createDocumentNoContent(908);
        Document saved = Assertions.assertDoesNotThrow(() -> documentApi.save(doc),
                "Initial save without content must succeed");

        // Attach valid PNG content for the update.
        saved.setDocumentContentInputStream(new ByteArrayInputStream(PNG_MAGIC));

        Document updated = Assertions.assertDoesNotThrow(() -> documentApi.update(saved),
                "update() with an image/png payload within size limit must succeed");

        Assertions.assertEquals(2, updated.getEntityVersion(),
                "Updated document must be at version 2");
    }

    // -----------------------------------------------------------------------
    // Test 10 — Exception message contains field name (API contract for HTTP 422 body)
    // -----------------------------------------------------------------------

    /**
     * #19-10: The {@link InvalidDocumentUploadException} thrown on MIME rejection must carry a
     * validation error referencing the {@code contentType} field (as set by the constructor in
     * {@code DocumentServiceImpl}).  This validates the HTTP 422 response body structure.
     */
    @Test
    @Order(49)
    void save_disallowedMime_exceptionContainsFieldName() {
        Document doc = createDocumentWithContent(909, HTML_BYTES);

        InvalidDocumentUploadException thrown = Assertions.assertThrows(
                InvalidDocumentUploadException.class,
                () -> documentApi.save(doc),
                "save() with a disallowed MIME type must throw InvalidDocumentUploadException");

        // ValidationException.getViolations() is the standard Water accessor.
        // The field name must be "contentType" as set by DocumentServiceImpl.
        boolean fieldPresent = thrown.getViolations() != null
                && thrown.getViolations().stream()
                .anyMatch(v -> "contentType".equals(v.getField()));
        Assertions.assertTrue(fieldPresent,
                "InvalidDocumentUploadException must carry a validation error for the 'contentType' field");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a Document with the given seed, attaching {@code content} as the upload stream.
     * The seed is used to generate unique {@code path} and {@code uid} values so that no
     * UNIQUE constraint violation occurs across test methods.
     */
    private Document createDocumentWithContent(int seed, byte[] content) {
        Document doc = new Document(
                "uploadTest" + seed,         // path  (unique per seed)
                "file" + seed + ".bin",      // fileName
                "upload-uid-" + seed,        // uid   (unique per seed)
                "application/octet-stream",  // declared contentType (intentionally generic — Tika overrides)
                0L                           // ownerUserId
        );
        doc.setDocumentContentInputStream(new ByteArrayInputStream(content));
        return doc;
    }

    /**
     * Creates a Document with the given seed but WITHOUT any content (documentContentInputStream == null).
     * Used to verify that the null-content guard in validateAndBoundUploadContent is effective.
     */
    private Document createDocumentNoContent(int seed) {
        return new Document(
                "uploadTest" + seed,
                "file" + seed + ".bin",
                "upload-uid-" + seed,
                "application/octet-stream",
                0L
        );
    }
}
