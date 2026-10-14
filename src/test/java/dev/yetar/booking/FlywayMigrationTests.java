package dev.yetar.booking;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class FlywayMigrationTests {

	@Autowired
	private JdbcTemplate template;

	@Test
	void flywaySchemaHistoryExists() {
		var v1Succeeded = template.queryForObject("SELECT success FROM flyway_schema_history WHERE version = '1';", Boolean.class);
		MatcherAssert.assertThat(v1Succeeded, Matchers.is(true));
	}

	@Test
	void tableTenantExists() {
		var relationName = template.queryForObject("SELECT to_regclass('tenant');", String.class);
		MatcherAssert.assertThat(relationName, Matchers.is("tenant"));
	}

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
	@ValueSource(strings = {"Sabrina", "sabrina", " sabrina", "sabrina "})
	void rejectsDuplicateTenantNameIgnoringCaseAndWhitespace(String duplicateName) {
		template.update("INSERT INTO tenant (id, name) VALUES (?, ?)", UUID.randomUUID(), "Sabrina");
		var e = Assertions.assertThrows(
				DuplicateKeyException.class,
				() -> template.update("INSERT INTO tenant (id, name) VALUES (?, ?)", UUID.randomUUID(), duplicateName)
		);
		Assertions.assertTrue(e.getMessage().contains("uq_tenant_name"));
	}
}
