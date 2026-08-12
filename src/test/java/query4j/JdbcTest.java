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

public class JdbcTest {

	@Test
	public void testThat_Jdbc_query_successfullyReturns_expectedValues() throws Exception {
		Jdbc jdbc = getJdbc();
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		assertFalse(users.isEmpty(), "Users value is empty");
		assertEquals("olivia", users.get(0).firstName, "user firstName does not match");

		List<User> noResults = jdbc.query("select * from fake_users where first_name = ?", User.MAPPER, "test");
		assertTrue(noResults.isEmpty(), "noResults value contains a value");
	}

	@Test
	public void testThat_Jdbc_update_successfullyUpdatesRow() throws Exception {
		Jdbc jdbc = getJdbc();

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

	@Test
	public void testThat_Jdbc_query_and_update_methodCalls_throwQueryException() throws Exception {

	}

	private Jdbc getJdbc() throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		UUID uuid = UUID.randomUUID();
		ds.setURL("jdbc:h2:mem:" + uuid + ";DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		Jdbc jdbc = new Jdbc(ds);

		try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(Sql.resource("sql/JdbcTest/01_setup_query.sql"));
		}
		return jdbc;
	}

	record User(String firstName, String lastName, String email) {
		static final RowMapper<User> MAPPER = rs -> new User(
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getString("email"));
	}
}
