package dev.yetar.booking.schema;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class FlywayMigrationTests extends ContainerizedTest {

	@ParameterizedTest
	@ValueSource(strings = {"1", "2"})
	void flywaySchemaHistoryExists(String version) {
		var v1Succeeded = template.queryForObject(
				"SELECT success FROM flyway_schema_history WHERE version = '" + version + "';", Boolean.class
		);
		MatcherAssert.assertThat(v1Succeeded, Matchers.is(true));
	}

	@ParameterizedTest
	@ValueSource(strings = {"tenant", "app_user", "membership"})
	void tableTenantExists(String tableName) {
		var relationName = template.queryForObject("SELECT to_regclass('" + tableName + "');", String.class);
		MatcherAssert.assertThat(relationName, Matchers.is(tableName));
	}
}
