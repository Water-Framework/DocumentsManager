package it.water.documents.manager;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.model.Role;
import it.water.core.api.permission.PermissionManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.documents.manager.api.FolderApi;
import it.water.documents.manager.api.FolderRepository;
import it.water.documents.manager.api.FolderSystemApi;
import it.water.documents.manager.model.Folder;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.repository.entity.model.exceptions.NoResultException;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for DocumentsManager Services.
 * <p>
 * Please use DocumentsManagerRestTestApi for ensuring format of the json response
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FolderApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private FolderApi folderApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private FolderRepository folderRepository;

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
    private it.water.core.api.model.User folderManagerUser;
    private it.water.core.api.model.User folderViewerUser;
    private it.water.core.api.model.User folderEditorUser;

    private Role folderManagerRole;
    private Role folderViewerRole;
    private Role folderEditorRole;

    @BeforeAll
    void beforeAll() {
        //getting user
        folderManagerRole = roleManager.getRole(Folder.DEFAULT_MANAGER_ROLE);
        folderViewerRole = roleManager.getRole(Folder.DEFAULT_VIEWER_ROLE);
        folderEditorRole = roleManager.getRole(Folder.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(folderManagerRole);
        Assertions.assertNotNull(folderViewerRole);
        Assertions.assertNotNull(folderEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        folderManagerUser = userManager.addUser("folderManager", "name", "lastname", "folderManager@a.com", "TempPassword1_", "salt", false);
        folderViewerUser = userManager.addUser("folderViewer", "name", "lastname", "folderViewer@a.com", "TempPassword1_", "salt", false);
        folderEditorUser = userManager.addUser("folderEditor", "name", "lastname", "folderEditor@a.com", "TempPassword1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(folderManagerUser.getId(), folderManagerRole);
        roleManager.addRole(folderViewerUser.getId(), folderViewerRole);
        roleManager.addRole(folderEditorUser.getId(), folderEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    /**
     * Testing basic injection of basic component for documentsmanager entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.folderApi = this.componentRegistry.findComponent(FolderApi.class, null);
        Assertions.assertNotNull(this.folderApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(FolderSystemApi.class, null));
        this.folderRepository = this.componentRegistry.findComponent(FolderRepository.class, null);
        Assertions.assertNotNull(this.folderRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        Folder entity = createFolder(0);
        entity = this.folderApi.save(entity);
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
        Query q = this.folderRepository.getQueryBuilderInstance().createQueryFilter("path=exampleField0");
        Folder entity = this.folderApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setPath("exampleFieldUpdated");
        entity = this.folderApi.update(entity);
        Assertions.assertEquals("exampleFieldUpdated", entity.getPath());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.folderRepository.getQueryBuilderInstance().createQueryFilter("path=exampleFieldUpdated");
        Folder errorEntity = this.folderApi.find(q);
        Assertions.assertEquals("exampleFieldUpdated", errorEntity.getPath());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.folderApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<Folder> all = this.folderApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1, all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            Folder u = createFolder(i);
            this.folderApi.save(u);
        }
        PaginableResult<Folder> paginated = this.folderApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.folderApi.findAll(null, 7, 2, null);
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
        PaginableResult<Folder> paginated = this.folderApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.folderApi.remove(entity.getId());
        });
        Assertions.assertEquals(0, this.folderApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        Folder entity = createFolder(1);
        this.folderApi.save(entity);
        Folder duplicated = this.createFolder(1);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.folderApi.save(duplicated));
        Folder secondEntity = createFolder(2);
        this.folderApi.save(secondEntity);
        entity.setPath("exampleField2");
        entity.setName("exampleField2");
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.folderApi.update(entity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        Folder newEntity = new Folder("<script>function(){alert('ciao')!}</script>", "name",0L);
        Assertions.assertThrows(ValidationException.class, () -> this.folderApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(folderManagerUser, runtime);
        final Folder entity = createFolder(101);
        Folder savedEntity = Assertions.assertDoesNotThrow(() -> this.folderApi.save(entity));
        savedEntity.setPath("newSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.folderApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.folderApi.find(savedEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.folderApi.remove(savedEntity.getId()));

    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(folderViewerUser, runtime);
        final Folder entity = createFolder(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.folderApi.save(entity));
    }

    @Order(12)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(folderEditorUser, runtime);
        final Folder entity = createFolder(301);
        Folder savedEntity = Assertions.assertDoesNotThrow(() -> this.folderApi.save(entity));
        savedEntity.setPath("editorNewSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.folderApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.folderApi.find(savedEntity.getId()));
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.folderApi.remove(savedEntityId));
    }

    @Order(13)
    @Test
    void ownedResourceShouldBeAccessedOnlyByOwner() {
        TestRuntimeInitializer.getInstance().impersonate(folderEditorUser, runtime);
        final Folder entity = createFolder(401);
        //saving as editor
        Folder savedEntity = Assertions.assertDoesNotThrow(() -> this.folderApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.folderApi.find(savedEntity.getId()));
        TestRuntimeInitializer.getInstance().impersonate(folderManagerUser, runtime);
        //find an owned entity with different user from the creator should raise an unauthorized exception
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(NoResultException.class, () -> this.folderApi.find(savedEntityId));
    }

    private Folder createFolder(int seed) {
        Folder entity = new Folder("exampleField" + seed,"exampleField" + seed, 0L);
        return entity;
    }
}
