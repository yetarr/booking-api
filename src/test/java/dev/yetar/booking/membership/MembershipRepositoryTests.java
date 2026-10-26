package dev.yetar.booking.membership;

import dev.yetar.booking.ContainerizedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class MembershipRepositoryTests extends ContainerizedTest {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savedMembershipCanBeReadBackWithAllFields() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");
        var membershipSave = new Membership(userId, tenantId, Role.OWNER);

        membershipRepository.saveAndFlush(membershipSave);
        entityManager.clear();
        var membershipOpt = membershipRepository.findById(new MembershipId(userId, tenantId));

        Assertions.assertTrue(membershipOpt.isPresent());

        var membershipRead = membershipOpt.get();
        Assertions.assertEquals(Role.OWNER, membershipRead.getRole());
        Assertions.assertNotNull(membershipRead.getCreatedAt());
    }

    @Test
    void rejectDuplicateMembership() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        membershipRepository.saveAndFlush(
                new Membership(userId, tenantId, Role.OWNER)
        );
        entityManager.clear();

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> membershipRepository.saveAndFlush(
                        new Membership(userId, tenantId, Role.OWNER)
                )
        );

        Assertions.assertTrue(e.getMessage().contains("pk_membership"));
    }

    @Test
    void roleIsStoredAsItsName() {
        var userId = insertAppUser("sabrina@yetar.dev");
        var tenantId = insertTenant("Sabrina");

        membershipRepository.saveAndFlush(
                new Membership(userId, tenantId, Role.OWNER)
        );

        var role = template.queryForObject(
                "SELECT role FROM membership WHERE user_id = ? AND tenant_id = ?",
                String.class,
                userId,
                tenantId
        );

        Assertions.assertEquals("OWNER", role);
    }
}
