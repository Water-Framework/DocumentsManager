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
    
    private Document createDocument(int seed){
        Document entity = new Document("exampleField"+seed,"fileName","uid"+seed,"contentType",0L);
        //todo add more fields here...
        return entity;
    }
}
