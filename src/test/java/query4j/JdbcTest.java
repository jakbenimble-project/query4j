package query4j;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

public class JdbcTemplateTest {

	@Test
	public void testThat_JdbcTemplate_query_successfullyReturns_expectedValues() throws Exception {
		Db db = getJdbcTemplate();
		JdbcTemplate jdbc = db.jdbc();
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		assertFalse(users.isEmpty(), "Users value is empty");
		assertEquals("olivia", users.get(0).firstName, "user firstName does not match");

		List<User> noResults = jdbc.query("select * from fake_users where first_name = ?", User.MAPPER, "test");
		assertTrue(noResults.isEmpty(), "noResults value contains a value");
	}

	@Test
	public void testThat_JdbcTemplate_update_successfullyUpdatesRow() throws Exception {
		Db db = getJdbcTemplate();
		JdbcTemplate jdbc = db.jdbc();

		String firstName = "olivia";
		String oldLastName = "dunham";
		String newLastName = "bishop";

		User u = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, firstName);
		assertTrue(oldLastName.equals(u.lastName()),
				"The expected lastName value is not correct before update");

		int rowCount = jdbc.update("update fake_users set last_name = ? where first_name = ?", newLastName,
				firstName);
		assertEquals(1, rowCount, "update call did not return a value");

		User updated = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, firstName);
		assertTrue(newLastName.equals(updated.lastName()),
				"The expected lastName value is not correct after update");

	}

	private Db getJdbcTemplate() throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		UUID uuid = UUID.randomUUID();
		ds.setURL("jdbc:h2:mem:" + uuid + ";DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		JdbcTemplate template = new JdbcTemplate(ds);

		try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(Sql.resource("sql/JdbcTemplateTest/01_setup_query.sql"));
		}
		return new Db(ds, template);
	}

	record User(String firstName, String lastName, String email) {
		static final RowMapper<User> MAPPER = rs -> new User(
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getString("email"));
	}

	record Db(JdbcDataSource ds, JdbcTemplate jdbc) {
	}
}
