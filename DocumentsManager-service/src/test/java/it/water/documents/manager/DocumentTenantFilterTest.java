package it.water.documents.manager;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.model.Role;
import it.water.core.api.model.User;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.core.testing.utils.security.TestSecurityContext;
import it.water.documents.manager.api.DocumentApi;
import it.water.documents.manager.api.DocumentRepository;
import it.water.documents.manager.api.DocumentSystemApi;
import it.water.documents.manager.model.Document;
import it.water.repository.entity.model.exceptions.EntityNotFound;
import it.water.repository.entity.model.exceptions.NoResultException;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Multitenancy "Tassello 4" — behavioral enforcement tests for the tenant filter implemented in
 * {@code BaseEntityServiceImpl} (Api layer) on the {@code Document} entity, which is a
 * {@code TenantResource} (single company, nullable {@code companyId} = global) and is ALSO an
 * {@code OwnedResource} (via {@code SharedEntity}). See {@code the `multitenancy-knowledge` skill}
 * &sect;1/&sect;6.
 * <p>
 * Covers:
 * <ul>
 *     <li>save() auto-assigns {@code companyId} from the active company;</li>
 *     <li>find/findAll/countAll AND a tenant condition ({@code companyId = active OR companyId IS NULL})
 *     that applies REGARDLESS of {@code isAdmin()} (only gated on an active company being present);</li>
 *     <li>backward compatibility: with no active company, behaviour is unfiltered (identical to
 *     single-tenant);</li>
 *     <li>by-id enforcement: a non-admin user who OWNS a document in a different company is still
 *     denied access to it once scoped to another company — isolating the tenant dimension from the
 *     pre-existing ownership dimension (both must be satisfied).</li>
 * </ul>
 * <p>
 * Note on by-id assertions: whether the deny surfaces as {@link UnauthorizedException} (the
 * checkById permission gate in {@code PermissionManagerDefault.checkUserOwnsResource}) or as
 * {@link NoResultException} (the query-path tenant filter applied inside the self-invoked
 * {@code find(Query)}) was not disambiguated with certainty against this codebase's exact
 * interceptor/self-invocation wiring; both are treated as valid "denied" outcomes here, matching
 * the task's own tolerance. See the test-engineer memory notes for a flagged follow-up.
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentTenantFilterTest implements Service {

    //declared as boxed Long (not primitive long) to avoid a JUnit5 assertEquals(long,long) vs
    //assertEquals(Object,Object) overload ambiguity when compared against getCompanyId() (Long)
    private static final Long COMPANY_A = 100L;
    private static final Long COMPANY_B = 200L;

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private DocumentApi documentApi;

    @Inject
    @Setter
    private DocumentSystemApi documentSystemApi;

    @Inject
    @Setter
    private DocumentRepository documentRepository;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private RoleManager roleManager;

    @Inject
    @Setter
    private UserManager userManager;

    private long adminId;
    private User tftManagerUser;

    //fixture ids used by the filter tests (find/findAll/countAll) — accessed as admin, so ownership
    //is irrelevant (admin bypasses the owner filter but NOT the tenant filter)
    private long docFilterCompanyAId;
    private long docFilterCompanyBId;
    private long docFilterGlobalId;

    //fixture ids used by the by-id isolation test — all owned by the SAME non-admin user, so only
    //the tenant dimension varies across them
    private long ownedDocCompanyAId;
    private long ownedDocCompanyBId;
    private long ownedDocGlobalId;

    @BeforeAll
    void beforeAll() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        adminId = userManager.findUser("admin").getId();
        Role documentsManagerRole = roleManager.getRole(Document.DEFAULT_MANAGER_ROLE);
        Assertions.assertNotNull(documentsManagerRole);
        tftManagerUser = userManager.addUser("tftManager", "name", "lastname", "tftmanager@a.com", "TempPassword1_", "salt", false);
        roleManager.addRole(tftManagerUser.getId(), documentsManagerRole);
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    /**
     * Removes every document seeded by this test (path prefix "tft-") via the SystemApi (which bypasses
     * the tenant filter). Keeps the shared in-memory DB footprint neutral for the other test classes in
     * this module — notably the pagination-sensitive Karate CRUD test, whose findAll CONTAINS assertion
     * would otherwise push the just-created document off the returned page.
     */
    @AfterAll
    void afterAll() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        documentSystemApi.findAll(null, -1, -1, null).getResults().stream()
                .filter(d -> d.getPath() != null && d.getPath().startsWith("tft-"))
                .map(Document::getId)
                .forEach(id -> documentSystemApi.remove(id));
    }

    /**
     * Seeds three documents directly via the SystemApi (bypasses auto-assign and permissions),
     * one per company plus a global (null companyId) one, so the filter tests below can control
     * exactly which company each row belongs to.
     */
    @Test
    @Order(1)
    void seedFilterFixtureDocuments_viaSystemApi_bypassesAutoAssign() {
        Document docA = new Document("tft-path-companyA", "tft-file-companyA.txt", "tft-uid-companyA", "text/plain", 0L);
        docA.setCompanyId(COMPANY_A);
        Document savedA = documentSystemApi.save(docA);
        docFilterCompanyAId = savedA.getId();
        Assertions.assertEquals(COMPANY_A, savedA.getCompanyId());

        Document docB = new Document("tft-path-companyB", "tft-file-companyB.txt", "tft-uid-companyB", "text/plain", 0L);
        docB.setCompanyId(COMPANY_B);
        Document savedB = documentSystemApi.save(docB);
        docFilterCompanyBId = savedB.getId();
        Assertions.assertEquals(COMPANY_B, savedB.getCompanyId());

        Document docGlobal = new Document("tft-path-global", "tft-file-global.txt", "tft-uid-global", "text/plain", 0L);
        //companyId left null => global/unassigned instance
        Document savedGlobal = documentSystemApi.save(docGlobal);
        docFilterGlobalId = savedGlobal.getId();
        Assertions.assertNull(savedGlobal.getCompanyId());
    }

    /**
     * A2: admin scoped to company A must see the company-A doc and the global doc, but NOT the
     * company-B doc. The tenant filter applies to admins too (only ownership is bypassed for admin).
     */
    @Test
    @Order(2)
    void findAll_adminScopedToActiveCompany_returnsOwnAndGlobalOnly() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(adminId, "admin", true, COMPANY_A));
        try {
            Query filter = filterOnIds(docFilterCompanyAId, docFilterCompanyBId, docFilterGlobalId);
            PaginableResult<Document> result = documentApi.findAll(filter, -1, -1, null);
            Set<Long> ids = idsOf(result);
            Assertions.assertTrue(ids.contains(docFilterCompanyAId), "own-company doc must be visible");
            Assertions.assertTrue(ids.contains(docFilterGlobalId), "global doc must be visible");
            Assertions.assertFalse(ids.contains(docFilterCompanyBId), "other-company doc must NOT be visible");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * D: backward compatibility — with no active company on the SecurityContext, the tenant filter
     * must not apply at all, exactly like single-tenant behaviour today.
     */
    @Test
    @Order(3)
    void findAll_noActiveCompany_backwardCompatibleReturnsAll() {
        //non-scoped admin context (activeCompanyId == null)
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Query filter = filterOnIds(docFilterCompanyAId, docFilterCompanyBId, docFilterGlobalId);
        PaginableResult<Document> result = documentApi.findAll(filter, -1, -1, null);
        Set<Long> ids = idsOf(result);
        Assertions.assertTrue(ids.containsAll(List.of(docFilterCompanyAId, docFilterCompanyBId, docFilterGlobalId)),
                "with no active company, the tenant filter must not apply (backward compatible)");
    }

    /**
     * A2 (countAll variant): the tenant-scoped result set size must reflect the filter too.
     */
    @Test
    @Order(4)
    void countAll_adminScopedToActiveCompany_excludesOtherCompany() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(adminId, "admin", true, COMPANY_A));
        try {
            Query filter = filterOnIds(docFilterCompanyAId, docFilterCompanyBId, docFilterGlobalId);
            long count = documentApi.countAll(filter);
            Assertions.assertEquals(2, count,
                    "countAll must reflect the tenant-scoped result set (companyA + global, not companyB)");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * A2 (single find(Query) variant): the other-company doc must not be reachable via a direct
     * id-based filter either, once scoped.
     */
    @Test
    @Order(5)
    void find_singleEntity_adminScopedToActiveCompany_excludesOtherCompany() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(adminId, "admin", true, COMPANY_A));
        try {
            Query filterOwn = documentRepository.getQueryBuilderInstance().createQueryFilter("id=" + docFilterCompanyAId);
            Document own = documentApi.find(filterOwn);
            Assertions.assertNotNull(own, "own-company doc must be findable when scoped to that company");

            Query filterOther = documentRepository.getQueryBuilderInstance().createQueryFilter("id=" + docFilterCompanyBId);
            Assertions.assertThrows(NoResultException.class, () -> documentApi.find(filterOther),
                    "other-company doc must not be findable when scoped to a different company");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * A4: save() under an active company auto-assigns companyId, mirroring how ownerUserId is
     * auto-assigned for OwnedResource.
     */
    @Test
    @Order(6)
    void save_underActiveCompany_autoAssignsCompanyId() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(adminId, "admin", true, COMPANY_A));
        try {
            Document newDoc = new Document("tft-path-autoassign", "tft-file-autoassign.txt", "tft-uid-autoassign", "text/plain", 0L);
            Document saved = documentApi.save(newDoc);
            Assertions.assertEquals(COMPANY_A, saved.getCompanyId(),
                    "save() under an active company must auto-assign companyId");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * Seeds three documents all owned by the SAME non-admin user (tftManagerUser), one per company
     * plus a global one, so the by-id test below isolates the tenant dimension from ownership
     * (ownership matches in every case; only companyId varies).
     */
    @Test
    @Order(7)
    void seedOwnedFixtureDocuments_forByIdTenantIsolation() {
        long ownerId = tftManagerUser.getId();

        Document ownedA = new Document("tft-owned-companyA", "tft-owned-companyA.txt", "tft-owned-uid-companyA", "text/plain", ownerId);
        ownedA.setOwnerUserId(ownerId);
        ownedA.setCompanyId(COMPANY_A);
        ownedDocCompanyAId = documentSystemApi.save(ownedA).getId();

        Document ownedB = new Document("tft-owned-companyB", "tft-owned-companyB.txt", "tft-owned-uid-companyB", "text/plain", ownerId);
        ownedB.setOwnerUserId(ownerId);
        ownedB.setCompanyId(COMPANY_B);
        ownedDocCompanyBId = documentSystemApi.save(ownedB).getId();

        Document ownedGlobal = new Document("tft-owned-global", "tft-owned-global.txt", "tft-owned-uid-global", "text/plain", ownerId);
        ownedGlobal.setOwnerUserId(ownerId);
        ownedDocGlobalId = documentSystemApi.save(ownedGlobal).getId();

        Assertions.assertTrue(ownedDocCompanyAId > 0 && ownedDocCompanyBId > 0 && ownedDocGlobalId > 0);
    }

    /**
     * A5: a non-admin user, scoped to company A, can find/remove their OWN documents in company A
     * and in the global (company-less) scope, but is DENIED find/remove on their OWN document that
     * belongs to company B — isolating the tenant check from the (already-satisfied) ownership check.
     */
    @Test
    @Order(8)
    void byId_nonAdminSameOwner_crossTenantFindAndRemove_denied() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(
                tftManagerUser.getId(), tftManagerUser.getUsername(), false, COMPANY_A));
        try {
            Assertions.assertDoesNotThrow(() -> documentApi.find(ownedDocCompanyAId),
                    "owner accessing their own same-company doc must succeed");
            Assertions.assertDoesNotThrow(() -> documentApi.find(ownedDocGlobalId),
                    "owner accessing their own global (company-less) doc must succeed");

            assertDeniedCrossTenant(() -> documentApi.find(ownedDocCompanyBId),
                    "owner must be denied find() on their own doc when it belongs to a different company than the active one");
            assertDeniedCrossTenant(() -> documentApi.remove(ownedDocCompanyBId),
                    "owner must be denied remove() on their own doc when it belongs to a different company than the active one");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
        //defense-in-depth: confirm the cross-tenant doc was NOT actually removed by the denied attempt
        Assertions.assertNotNull(documentSystemApi.find(ownedDocCompanyBId),
                "the cross-tenant doc must still exist after the denied remove() attempt");
    }

    /**
     * Accepts either {@link UnauthorizedException} (the by-id permission/tenant gate) or
     * {@link NoResultException} (the query-path tenant filter) as a valid "denied" outcome — see
     * the class javadoc for why both are tolerated here.
     */
    private void assertDeniedCrossTenant(Runnable action, String message) {
        try {
            action.run();
            Assertions.fail(message + " (no exception was thrown)");
        } catch (UnauthorizedException | NoResultException | EntityNotFound e) {
            //expected: any of these confirms the cross-tenant row was not accessible/removable
        }
    }

    private Query filterOnIds(Long... ids) {
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        return documentRepository.getQueryBuilderInstance().createQueryFilter("id IN (" + sb + ")");
    }

    private Set<Long> idsOf(PaginableResult<Document> result) {
        return result.getResults().stream().map(Document::getId).collect(Collectors.toSet());
    }
}
