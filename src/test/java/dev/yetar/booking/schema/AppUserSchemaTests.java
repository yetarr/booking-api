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
class AppUserSchemaTests extends ContainerizedTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void rejectsBlankAppUserEmail(String blankEmail) {
        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAppUser(blankEmail)
        );
        Assertions.assertTrue(e.getMessage().contains("ck_app_user_email_not_blank"));
    }

    @Test
    void acceptAppUsersWithDifferentEmails() {
        var user1Id = insertAppUser("sabrina@yetar.dev");
        var user2Id = insertAppUser("carpenter@yetar.dev");

        var userCount = template.queryForObject(
                "SELECT count(*) FROM app_user WHERE id IN (?, ?)",
                Integer.class,
                user1Id,
                user2Id
        );

        Assertions.assertEquals(2, userCount);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Sabrina@yetar.dev",
            "sabrina@yetar.dev",
            " sabrina@yetar.dev",
            "sabrina@yetar.dev ",
            " sabrina@yetar.dev "
    })
    void rejectsDuplicateTenantNameIgnoringCaseAndWhitespace(String duplicateEmail) {
        insertAppUser(duplicateEmail);
        var e = Assertions.assertThrows(
                DuplicateKeyException.class,
                () -> insertAppUser(duplicateEmail)
        );
        Assertions.assertTrue(e.getMessage().contains("uq_app_user_email"));
    }

    @Test
    void rejectsEmptyAppUserPasswordHash() {
        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> template.update(
                        "INSERT INTO app_user (id, email, password_hash) VALUES (?, ?, ?)",
                        UUID.randomUUID(),
                        "test@yetar.dev",
                        ""
                )
        );
        Assertions.assertTrue(e.getMessage().contains("ck_app_user_password_not_empty"));
    }
}
