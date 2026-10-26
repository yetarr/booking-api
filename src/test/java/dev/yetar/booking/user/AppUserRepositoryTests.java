package dev.yetar.booking.user;

import dev.yetar.booking.ContainerizedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AppUserRepositoryTests extends ContainerizedTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savedUserCanBeReadBackWithAllFields() {
        var userSave = new AppUser(
                "sabrina@yetar.dev",
                "$2b$06$10zzJ8YALzBdxJwz9ngcf.vHSlRsmRcpxRnfX7ilhhvl7rSWmEnam"
        );

        userRepository.saveAndFlush(userSave);
        entityManager.clear();
        var userOpt = userRepository.findById(userSave.getId());

        Assertions.assertTrue(userOpt.isPresent());

        var userRead = userOpt.get();
        Assertions.assertEquals("sabrina@yetar.dev", userRead.getEmail());
        Assertions.assertEquals(
                "$2b$06$10zzJ8YALzBdxJwz9ngcf.vHSlRsmRcpxRnfX7ilhhvl7rSWmEnam",
                userRead.getPasswordHash()
        );
        Assertions.assertNotNull(userRead.getCreatedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Sabrina@yetar.dev",
            "sabrina@yetar.dev",
            " sabrina@yetar.dev",
            "sabrina@yetar.dev ",
            " sabrina@yetar.dev "
    })
    void rejectDuplicateAppUserEmail(String duplicateEmail) {
        var user1 = new AppUser(
                "sabrina@yetar.dev",
                "$2b$06$10zzJ8YALzBdxJwz9ngcf.vHSlRsmRcpxRnfX7ilhhvl7rSWmEnam"
        );
        userRepository.saveAndFlush(user1);
        entityManager.clear();

        var user2 = new AppUser(
                duplicateEmail,
                "$2b$06$10zzJ8YALzBdxJwz9ngcf.vHSlRsmRcpxRnfX7ilhhvl7rSWmEnam"
        );

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user2)
        );

        Assertions.assertTrue(e.getMessage().contains("uq_app_user_email"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void rejectBlankAppUserEmail(String blankEmail) {
        var user = new AppUser(
                blankEmail,
                "$2b$06$10zzJ8YALzBdxJwz9ngcf.vHSlRsmRcpxRnfX7ilhhvl7rSWmEnam"
        );

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user)
        );

        Assertions.assertTrue(e.getMessage().contains("ck_app_user_email_not_blank"));
    }

    @Test
    void rejectBlankAppUserPasswordHash() {
        var user = new AppUser(
                "sabrina@yetar.dev",
                ""
        );

        var e = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user)
        );

        Assertions.assertTrue(e.getMessage().contains("ck_app_user_password_not_empty"));
    }
}
