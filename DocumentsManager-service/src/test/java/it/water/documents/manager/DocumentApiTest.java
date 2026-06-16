package it.water.documents.manager;

import it.water.core.api.model.PaginableResult;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.Role;
import it.water.core.api.role.RoleManager;
import it.water.core.api.user.UserManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.service.Service;
import it.water.core.api.permission.PermissionManager;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;

import it.water.core.testing.utils.junit.WaterTestExtension;

import it.water.documents.manager.api.*;
import it.water.documents.manager.model.*;

import it.water.repository.entity.model.exceptions.NoResultException;
import lombok.Setter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for DocumentsManager Services.
 * 
 * Please use DocumentsManagerRestTestApi for ensuring format of the json response
 

 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentApiTest implements Service {
    
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
    private DocumentRepository documentRepository;
    
    @Inject
    @Setter
    //default permission manager in test environment;
    private PermissionManager permissionManager;

    @Inject
    @Setter
    //test role manager
    private UserManager userManager;
    
    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    //admin user
    @SuppressWarnings("unused")
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User documentsmanagerManagerUser;
    private it.water.core.api.model.User documentsmanagerViewerUser;
    private it.water.core.api.model.User documentsmanagerEditorUser;

    private Role documentsmanagerManagerRole;
    private Role documentsmanagerViewerRole;
    private Role documentsmanagerEditorRole;
    
    @BeforeAll
    void beforeAll() {
        //getting user
        documentsmanagerManagerRole = roleManager.getRole(Document.DEFAULT_MANAGER_ROLE);
        documentsmanagerViewerRole = roleManager.getRole(Document.DEFAULT_VIEWER_ROLE);
        documentsmanagerEditorRole = roleManager.getRole(Document.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(documentsmanagerManagerRole);
        Assertions.assertNotNull(documentsmanagerViewerRole);
        Assertions.assertNotNull(documentsmanagerEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        documentsmanagerManagerUser = userManager.addUser("manager", "name", "lastname", "manager@a.com","TempPassword1_","salt", false);
        documentsmanagerViewerUser = userManager.addUser("viewer", "name", "lastname", "viewer@a.com","TempPassword1_","salt", false);
        documentsmanagerEditorUser = userManager.addUser("editor", "name", "lastname", "editor@a.com","TempPassword1_","salt", false);
        //starting with admin permissions
        roleManager.addRole(documentsmanagerManagerUser.getId(), documentsmanagerManagerRole);
        roleManager.addRole(documentsmanagerViewerUser.getId(), documentsmanagerViewerRole);
        roleManager.addRole(documentsmanagerEditorUser.getId(), documentsmanagerEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }
    /**
     * Testing basic injection of basic component for documentsmanager entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.documentApi = this.componentRegistry.findComponent(DocumentApi.class, null);
        Assertions.assertNotNull(this.documentApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(DocumentSystemApi.class, null));
        this.documentRepository = this.componentRegistry.findComponent(DocumentRepository.class, null);
        Assertions.assertNotNull(this.documentRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        Document entity = createDocument(0);
        entity = this.documentApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("exampleField0", entity.getPath());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.documentRepository.getQueryBuilderInstance().createQueryFilter("path=exampleField0");
        Document entity = this.documentApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setPath("exampleFieldUpdated");
        entity = this.documentApi.update(entity);
        Assertions.assertEquals("exampleFieldUpdated", entity.getPath());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.documentRepository.getQueryBuilderInstance().createQueryFilter("path=exampleFieldUpdated");
        Document errorEntity = this.documentApi.find(q);
        Assertions.assertEquals("exampleFieldUpdated", errorEntity.getPath());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.documentApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<Document> all = this.documentApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1,all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            Document u = createDocument(i);
            this.documentApi.save(u);
        }
        PaginableResult<Document> paginated = this.documentApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.documentApi.findAll(null, 7, 2, null);
        Assertions.assertEquals(3, paginated.getResults().size());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<Document> paginated = this.documentApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.documentApi.remove(entity.getId());
        });
        Assertions.assertEquals(0,this.documentApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        Document entity = createDocument(1);
        this.documentApi.save(entity);
        Document duplicated = this.createDocument(1);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class,() -> this.documentApi.save(duplicated));
        Document secondEntity = createDocument(2);
        this.documentApi.save(secondEntity);
        entity.setPath("exampleField2");
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class,() -> this.documentApi.update(entity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        Document newEntity = new Document("<script>function(){alert('ciao')!}</script>","fileName","uid","contentType",0L);
        Assertions.assertThrows(ValidationException.class,() -> this.documentApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerManagerUser,runtime);
        final Document entity = createDocument(101);
        Document savedEntity = Assertions.assertDoesNotThrow(() -> this.documentApi.save(entity));
        savedEntity.setPath("newSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.documentApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.documentApi.find(savedEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.documentApi.remove(savedEntity.getId()));

    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerViewerUser,runtime);
        final Document entity = createDocument(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.documentApi.save(entity));
        //viewer can search
        Assertions.assertEquals(0, this.documentApi.findAll(null, -1, -1, null).getResults().size());
    }

    @Order(12)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser,runtime);
        final Document entity = createDocument(301);
        Document savedEntity = Assertions.assertDoesNotThrow(() -> this.documentApi.save(entity));
        savedEntity.setPath("editorNewSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.documentApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.documentApi.find(savedEntity.getId()));
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.documentApi.remove(savedEntityId));
    }
    
    @Order(13)
    @Test
    void ownedResourceShouldBeAccessedOnlyByOwner() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        final Document entity = createDocument(401);
        //saving as editor
        Document savedEntity = Assertions.assertDoesNotThrow(() -> this.documentApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.documentApi.find(savedEntity.getId()));
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerManagerUser, runtime);
        //find an owned entity with different user from the creator should raise an unauthorized exception
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(NoResultException.class,() -> this.documentApi.find(savedEntityId));
    }

    // =========================================================
    // #18 — fetchDocumentContent ownership + path-traversal tests
    // =========================================================

    /**
     * #18: fetchDocumentContent succeeds for the owner of the document.
     * ownerUserId is explicitly set to the logged editor user's id before save.
     */
    @Test
    @Order(20)
    void fetchDocumentContent_asOwner_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        long editorId = runtime.getSecurityContext().getLoggedEntityId();
        Document doc = createDocument(501);
        doc.setOwnerUserId(editorId);
        Document saved = Assertions.assertDoesNotThrow(() -> this.documentApi.save(doc));
        long savedId = saved.getId();
        // owner must be able to fetch their own document content
        Assertions.assertDoesNotThrow(() -> this.documentApi.fetchDocumentContent(savedId));
    }

    /**
     * #18: fetchDocumentContent throws UnauthorizedException when a non-owner, non-admin user
     * tries to access a document owned by a different user.
     */
    @Test
    @Order(21)
    void fetchDocumentContent_asNonOwner_throwsUnauthorized() {
        // Save as editor with explicit ownerUserId
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        long editorId = runtime.getSecurityContext().getLoggedEntityId();
        Document doc = createDocument(502);
        doc.setOwnerUserId(editorId);
        Document saved = Assertions.assertDoesNotThrow(() -> this.documentApi.save(doc));
        long savedId = saved.getId();

        // Switch to manager — different user, NOT the owner and NOT sharing
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerManagerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.documentApi.fetchDocumentContent(savedId));
    }

    /**
     * #18: fetchDocumentContent succeeds for admin regardless of ownerUserId.
     */
    @Test
    @Order(22)
    void fetchDocumentContent_asAdmin_bypass_succeeds() {
        // Save as editor with explicit ownerUserId
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        long editorId = runtime.getSecurityContext().getLoggedEntityId();
        Document doc = createDocument(503);
        doc.setOwnerUserId(editorId);
        Document saved = Assertions.assertDoesNotThrow(() -> this.documentApi.save(doc));
        long savedId = saved.getId();

        // Admin can access any document
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertDoesNotThrow(() -> this.documentApi.fetchDocumentContent(savedId));
    }

    /**
     * #18: fetchDocumentContentByUID succeeds for the owner.
     */
    @Test
    @Order(23)
    void fetchDocumentContentByUID_asOwner_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        long editorId = runtime.getSecurityContext().getLoggedEntityId();
        Document doc = createDocument(504);
        doc.setOwnerUserId(editorId);
        Document saved = Assertions.assertDoesNotThrow(() -> this.documentApi.save(doc));
        String uid = saved.getUid();
        Assertions.assertDoesNotThrow(() -> this.documentApi.fetchDocumentContentByUID(uid));
    }

    /**
     * #18: fetchDocumentContentByUID throws UnauthorizedException for a non-owner.
     */
    @Test
    @Order(24)
    void fetchDocumentContentByUID_asNonOwner_throwsUnauthorized() {
        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerEditorUser, runtime);
        long editorId = runtime.getSecurityContext().getLoggedEntityId();
        Document doc = createDocument(505);
        doc.setOwnerUserId(editorId);
        Document saved = Assertions.assertDoesNotThrow(() -> this.documentApi.save(doc));
        String uid = saved.getUid();

        TestRuntimeInitializer.getInstance().impersonate(documentsmanagerManagerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.documentApi.fetchDocumentContentByUID(uid));
    }

    /**
     * #18: fetchDocumentContentByPath with ".." in path must throw WaterRuntimeException (path traversal).
     */
    @Test
    @Order(25)
    void fetchDocumentContentByPath_dotDotInPath_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("../etc", "passwd"));
    }

    /**
     * #18: fetchDocumentContentByPath with ".." in fileName must throw WaterRuntimeException.
     */
    @Test
    @Order(26)
    void fetchDocumentContentByPath_dotDotInFileName_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("validPath", "../secret.txt"));
    }

    /**
     * #18: a leading "/" is a legal rooted path in this module (documents are stored under rooted
     * paths by convention, e.g. "/myPath"), so it must NOT be rejected as a traversal attempt — only
     * ".."/null-byte/backslash/drive-prefix are. A fileName, however, must never contain a path
     * separator: it names a single file, so "sub/evil.txt" must throw WaterRuntimeException.
     */
    @Test
    @Order(27)
    void fetchDocumentContentByPath_fileNameWithSeparator_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("/myPath", "sub/evil.txt"));
    }

    /**
     * #18: fetchDocumentContentByPath with backslash-absolute path (leading "\") must throw WaterRuntimeException.
     */
    @Test
    @Order(28)
    void fetchDocumentContentByPath_backslashAbsolutePath_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("\\windows\\system32", "file.dll"));
    }

    /**
     * #18: fetchDocumentContentByPath with a Windows drive prefix must throw WaterRuntimeException.
     */
    @Test
    @Order(29)
    void fetchDocumentContentByPath_windowsDrivePrefix_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("C:/Windows/System32", "file.dll"));
    }

    /**
     * #18: fetchDocumentContentByPath with a null byte in path must throw WaterRuntimeException.
     */
    @Test
    @Order(30)
    void fetchDocumentContentByPath_nullByteInPath_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        // null byte is the classic null-byte injection
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("valid\0path", "file.txt"));
    }

    /**
     * #18: fetchDocumentContentByPath with a null byte in fileName must throw WaterRuntimeException.
     */
    @Test
    @Order(31)
    void fetchDocumentContentByPath_nullByteInFileName_throwsWaterRuntimeException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(WaterRuntimeException.class,
                () -> this.documentApi.fetchDocumentContentByPath("validPath", "file\0.txt"));
    }

    /**
     * #18: fetchDocumentContentByPath with null path is safe (validateStorageComponent returns without
     * throwing when value is null — the null check is at the start of validateStorageComponent).
     * The call will then proceed to the repository and may throw a different exception; we just
     * ensure WaterRuntimeException is NOT thrown for null path alone.
     */
    @Test
    @Order(32)
    void fetchDocumentContentByPath_nullPath_doesNotThrowTraversalException() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        // null path: validateStorageComponent returns safely; subsequent DB call may throw a
        // different exception (NoResultException / NullPointerException from the repository layer)
        // — we assert it is NOT a traversal-rejection WaterRuntimeException with "illegal path component"
        try {
            this.documentApi.fetchDocumentContentByPath(null, "file.txt");
        } catch (WaterRuntimeException e) {
            Assertions.assertFalse(e.getMessage() != null && e.getMessage().contains("illegal path component"),
                    "null path must not be rejected as a path-traversal attempt");
        } catch (Exception ignored) {
            // other exceptions (NoResultException etc.) are acceptable
        }
    }

    private Document createDocument(int seed){
        Document entity = new Document("exampleField"+seed,"fileName","uid"+seed,"contentType",0L);
        //todo add more fields here...
        return entity;
    }
}
