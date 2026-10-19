package dev.yetar.booking.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
class MembershipSchemaTests extends ContainerizedTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "ADMIN", "owner"})
    void rejectsInvalidMembershipRoles(String invalidRole) {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> template.update(
                        "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                        userId,
                        tenantId,
                        invalidRole
                )
        );

        Assertions.assertTrue(e.getMessage().contains("ck_membership_role_is_valid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"OWNER", "STAFF", "CUSTOMER"})
    void acceptValidMembershipRoles(String validRole) {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        Assertions.assertDoesNotThrow(
                () -> template.update(
                        "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                        userId,
                        tenantId,
                        validRole
                )
        );
    }

    @Test
    void deletingUserRemovesTheirMemberships() {
        var user1Id = insertAppUser("sabrina1@yetar.dev");
        var user2Id = insertAppUser("sabrina2@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        insertMembership(user1Id, tenantId, "CUSTOMER");
        insertMembership(user2Id, tenantId, "CUSTOMER");

        var membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE tenant_id = ?",
                Integer.class,
                tenantId
        );

        Assertions.assertEquals(2, membershipCount);

        Assertions.assertDoesNotThrow(
                () -> template.update(
                        "DELETE FROM app_user WHERE id = ?",
                        user1Id
                )
        );

        membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ?",
                Integer.class,
                user1Id
        );

        Assertions.assertEquals(0, membershipCount);

        membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ?",
                Integer.class,
                user2Id
        );

        Assertions.assertEquals(1, membershipCount);

        var tenantCount = template.queryForObject(
                "SELECT count(*) FROM tenant WHERE id = ?",
                Integer.class,
                tenantId
        );

        Assertions.assertEquals(1, tenantCount);
    }

    @Test
    void deletingTenantRemovesItsMemberships() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenant1Id = insertTenant("Sabrina");
        var tenant2Id = insertTenant("Carpenter");

        insertMembership(userId, tenant1Id, "CUSTOMER");
        insertMembership(userId, tenant2Id, "CUSTOMER");

        var membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ?",
                Integer.class,
                userId
        );

        Assertions.assertEquals(2, membershipCount);

        Assertions.assertDoesNotThrow(
                () -> template.update(
                        "DELETE FROM tenant WHERE id = ?",
                        tenant1Id
                )
        );

        membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE tenant_id = ?",
                Integer.class,
                tenant1Id
        );

        Assertions.assertEquals(0, membershipCount);

        membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE tenant_id = ?",
                Integer.class,
                tenant2Id
        );

        Assertions.assertEquals(1, membershipCount);

        var userCount = template.queryForObject(
                "SELECT count(*) FROM app_user WHERE id = ?",
                Integer.class,
                userId
        );

        Assertions.assertEquals(1, userCount);
    }

    @Test
    void rejectsDuplicateMemberships() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        insertMembership(userId, tenantId, "CUSTOMER");

        var e = Assertions.assertThrows(
                DuplicateKeyException.class,
                () -> template.update(
                        "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                        userId,
                        tenantId,
                        "CUSTOMER"
                )
        );

        Assertions.assertTrue(e.getMessage().contains("pk_membership"));
    }

    @Test
    void rejectsMembershipOfNonExistingUser() {
        var tenantId = insertTenant("Sabrina");
        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> template.update(
                        "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                        UUID.randomUUID(),
                        tenantId,
                        "CUSTOMER"
                )
        );

        Assertions.assertTrue(e.getMessage().contains("fk_membership_user"));
    }

    @Test
    void rejectsMembershipOfNonExistingTenant() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> template.update(
                        "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                        userId,
                        UUID.randomUUID(),
                        "CUSTOMER"
                )
        );

        Assertions.assertTrue(e.getMessage().contains("fk_membership_tenant"));
    }

    @Test
    void acceptUserWithDifferentTenantMemberships() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenant1Id = insertTenant("Sabrina");
        var tenant2Id = insertTenant("Carpenter");

        insertMembership(userId, tenant1Id, "CUSTOMER");
        insertMembership(userId, tenant2Id, "CUSTOMER");

        var membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ?",
                Integer.class,
                userId
        );

        Assertions.assertEquals(2, membershipCount);
    }

    @Test
    void acceptUserWithDifferentRolesInDifferentTenantMemberships() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenant1Id = insertTenant("Sabrina");
        var tenant2Id = insertTenant("Carpenter");

        insertMembership(userId, tenant1Id, "CUSTOMER");
        insertMembership(userId, tenant2Id, "STAFF");

        var membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ? AND tenant_id = ? AND role = 'CUSTOMER'",
                Integer.class,
                userId,
                tenant1Id
        );

        Assertions.assertEquals(1, membershipCount);

        membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE user_id = ? AND tenant_id = ? AND role = 'STAFF'",
                Integer.class,
                userId,
                tenant2Id
        );

        Assertions.assertEquals(1, membershipCount);

    }

    @Test
    void acceptTenantMembershipWithDifferentUsers() {
        var user1Id = insertAppUser("sabrina1@yetar.dev");
        var user2Id = insertAppUser("sabrina2@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        insertMembership(user1Id, tenantId, "CUSTOMER");
        insertMembership(user2Id, tenantId, "CUSTOMER");

        var membershipCount = template.queryForObject(
                "SELECT count(*) FROM membership WHERE tenant_id = ?",
                Integer.class,
                tenantId
        );

        Assertions.assertEquals(2, membershipCount);
    }
}
