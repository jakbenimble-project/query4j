package query4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

public class JdbcTemplateTest {

	@Test
	public void testThat_JdbcTemplate_query_successfullyReturns_expectedValues() throws Exception {
		Db db = getJdbcTempate();
		JdbcTemplate jdbc = db.jdbc();
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		assert users.size() > 0;
		assertTrue(users.get(0).firstName.equals("olivia"));

		List<User> noResults = jdbc.query("select * from fake_users where first_name = ?", User.MAPPER, "test");
		assert noResults.size() == 0;
		destroyJdbcTemplate(db);
	}

	@Test
	public void testThat_JdbcTemplate_update_successfullyUpdatesRow() throws Exception {
		Db db = getJdbcTempate();
		JdbcTemplate jdbc = db.jdbc();

		String firstName = "olivia";
		String oldLastName = "dunham";
		String newLastName = "bishop";

		User u = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, firstName);
		System.out.println(u);
		assertTrue(u.lastName().equals(oldLastName),
				"The expected lastName value is not correct before update");

		int rowCount = jdbc.update("update fake_users set last_name = ? where first_name = ?", firstName,
				newLastName);
		User updated = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, firstName);
		System.out.println(updated);
		assertTrue(updated.lastName().equals("bishop"),
				"The expected lastName value is not correct after update");

		destroyJdbcTemplate(db);
	}

	private Db getJdbcTempate() throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		UUID uuid = UUID.randomUUID();
		ds.setURL("jdbc:h2:mem:" + uuid);
		ds.setUser("sa");
		ds.setPassword("");
		JdbcTemplate template = new JdbcTemplate(ds);

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(Sql.resource("sql/JdbcTemplateTest/01_setup_query.sql"));
		return new Db(ds, template);
	}

	private void destroyJdbcTemplate(Db db) throws Exception {
		JdbcDataSource ds = db.ds();
		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute("drop table fake_users");
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
