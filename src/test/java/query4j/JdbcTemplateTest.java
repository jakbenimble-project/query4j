package query4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JdbcTemplateTest {

	JdbcTemplate jdbc;
	JdbcDataSource ds;

	@BeforeEach
	void init() throws Exception {
		ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		jdbc = new JdbcTemplate(ds);

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(Sql.resource("sql/JdbcTemplateTest/01_setup_query.sql"));
	}

	@Test
	public void testThat_JdbcTemplate_query_successfullyReturns_expectedValues() throws Exception {
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		assert users.size() > 0;
		assertTrue(users.get(0).firstName.equals("olivia"));

		List<User> noResults = jdbc.query("select * from fake_users where first_name = ?", User.MAPPER, "test");
		assert noResults.size() == 0;
	}

	@Test
	public void testThat_JdbcTemplate_update_successfullyUpdatesRow() throws Exception {
		User u = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, "olivia");
		assertTrue(u.lastName().equals("dunham"));
		String sql = "update fake_users set last_name = ? where first_name = ?";
		jdbc.update(sql, "olivia", "bishop");
		u = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, "olivia");
		assertTrue(u.lastName().equals("bishop"));
	}

	@AfterEach
	void destroy() throws Exception {
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
}
