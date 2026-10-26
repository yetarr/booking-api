package dev.yetar.booking.tenant;

import dev.yetar.booking.ContainerizedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TenantRepositoryTests extends ContainerizedTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savedTenantCanBeReadBackWithAllFields() {
        var tenantSave = new Tenant("Sabrina");

        tenantRepository.saveAndFlush(tenantSave);
        entityManager.clear();
        var tenantOpt = tenantRepository.findById(tenantSave.getId());

        Assertions.assertTrue(tenantOpt.isPresent());

        var tenantRead = tenantOpt.get();
        Assertions.assertEquals("Sabrina", tenantRead.getName());
        Assertions.assertNotNull(tenantRead.getCreatedAt());
    }

    @Test
    void rejectDuplicateTenantName() {
        var tenant1 = new Tenant("Sabrina");
        tenantRepository.saveAndFlush(tenant1);
        entityManager.clear();

        var tenant2 = new Tenant("Sabrina");

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> tenantRepository.saveAndFlush(tenant2)
        );

        Assertions.assertTrue(e.getMessage().contains("uq_tenant_name"));
    }
}
