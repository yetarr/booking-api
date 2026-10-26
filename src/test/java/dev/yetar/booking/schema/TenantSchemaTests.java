package dev.yetar.booking.schema;

import dev.yetar.booking.ContainerizedTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
class TenantSchemaTests extends ContainerizedTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void rejectsBlankTenantName(String blankName) {
        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> template.update("INSERT INTO tenant (id, name) VALUES (?, ?)", UUID.randomUUID(), blankName)
        );
        Assertions.assertTrue(e.getMessage().contains("ck_tenant_name_not_blank"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Sabrina", "sabrina", " sabrina", "sabrina ", " sabrina "})
    void rejectsDuplicateTenantNameIgnoringCaseAndWhitespace(String duplicateName) {
        template.update("INSERT INTO tenant (id, name) VALUES (?, ?)", UUID.randomUUID(), "Sabrina");
        var e = Assertions.assertThrows(
                DuplicateKeyException.class,
                () -> template.update("INSERT INTO tenant (id, name) VALUES (?, ?)", UUID.randomUUID(), duplicateName)
        );
        Assertions.assertTrue(e.getMessage().contains("uq_tenant_name"));
    }
}
