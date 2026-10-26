package dev.yetar.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "logging.level.org.hibernate.SQL = DEBUG"
})
public abstract class ContainerizedTest {

    @Autowired
    protected JdbcTemplate template;

    protected UUID insertAppUser(String email) {
        var id = UUID.randomUUID();
        template.update(
                "INSERT INTO app_user (id, email, password_hash) VALUES (?, ?, ?)",
                id,
                email,
                "$2b$06$6gUPz0kHTUTixj.nkoZ7V.OjCWBeZnBFt.rnecl0p24NqFAt.ev3."
        );

        return id;
    }

    protected UUID insertTenant(String name) {
        var id = UUID.randomUUID();
        template.update(
                "INSERT INTO tenant (id, name) VALUES (?, ?)",
                id,
                name
        );

        return id;
    }

    protected void insertMembership(UUID userId, UUID tenantId, String role) {
        template.update(
                "INSERT INTO membership (user_id, tenant_id, role) VALUES (?, ?, ?)",
                userId,
                tenantId,
                role
        );
    }
}
