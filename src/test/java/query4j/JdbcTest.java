package query4j;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import query4j.exceptions.NoResultException;
import query4j.exceptions.NonUniqueResultException;
import query4j.exceptions.QueryException;

public class JdbcTest {

	@Test
	public void jdbc_query_successfullyReturns_expectedValues() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		assertFalse(users.isEmpty(), "Users value is empty");
		assertEquals("olivia", users.get(0).firstName, "user firstName does not match");

		List<User> noResults = jdbc.query("select * from fake_users where first_name = ?", User.MAPPER, "test");
		assertTrue(noResults.isEmpty(), "noResults value contains a value");
	}

	@Test
	public void jdbc_update_successfullyUpdatesRow() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();

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
	public void jdbc_queryOne_successfullyReturnsOneRow() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "peter";
		User user = jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, userName);
		assertEquals(userName, user.firstName(), "Expected user does not match");
	}

	@Test
	public void jdbc_queryOne_throws_NoResultException_forNoMatch() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "test";
		assertThrows(NoResultException.class, () -> {
			jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, userName);
		}, "queryOne did not throw NoResultException");
	}

	@Test
	public void jdbc_queryOne_throws_NonUniqueResultException_forMultipleMatches() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "bishop";
		assertThrows(NonUniqueResultException.class, () -> {
			jdbc.queryOne("select * from fake_users where last_name = ?", User.MAPPER, userName);
		}, "queryOne did not throw NonUniqueResultException");
	}

	@Test
	public void jdbc_queryOptional_successfullyReturnsEmptyOptional_When_NoRows() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "test";
		Optional<User> user = jdbc.queryOptional("select * from fake_users where first_name = ?", User.MAPPER,
				userName);
		assertTrue(user.isEmpty(), "Returned Optional<User> is not empty");
	}

	@Test
	public void jdbc_queryOptional_successfullyReturnsOneRow() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "walter";
		User user = jdbc.queryOptional("select * from fake_users where first_name = ?", User.MAPPER, userName)
				.get();
		assertEquals(userName, user.firstName(), "Expected user does not match");
	}

	@Test
	public void jdbc_queryOptional_throws_NonUniqueResultException_forMultipleMatches() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		String userName = "bishop";
		assertThrows(NonUniqueResultException.class, () -> {
			jdbc.queryOne("select * from fake_users where last_name = ?", User.MAPPER, userName);
		}, "queryOptional did not throw NonUniqueResultException");
	}

	@Test
	public void jdbc_queryValue_successfullyReturnsOneRow() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		Long count = jdbc.queryValue("select count(*) from fake_users", Long.class);
		assertEquals(4, count, "Expected user count does not match");
	}

	@Test
	public void jdbc_queryValue_throws_NoResultException_forNoMatch() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		assertThrows(NoResultException.class, () -> {
			jdbc.queryValue("select first_name from fake_users where first_name = ?",
					Long.class, "test");
		}, "queryValue did not throw NoResultException");
	}

	@Test
	public void jdbc_queryValue_throws_NonUniqueResultException_forMultipleMatches() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		assertThrows(NonUniqueResultException.class, () -> {
			jdbc.queryValue("select first_name from fake_users", String.class);
		}, "queryOne did not throw NonUniqueResultException");
	}

	@Test
	public void jdbc_queryValue_throws_QueryException_for_scalarTypeMismatch() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		assertThrows(QueryException.class, () -> {
			jdbc.queryValue("select count(first_name) from fake_users", User.class);
		}, "queryOne did not throw QueryException");
	}

	@Test
	public void jdbc_batchUpdate_successfully_updatesAllRows() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		List<User> users = jdbc.query("select * from fake_users", User.MAPPER);
		List<Object[]> updateUsersList = users.stream()
				.map(u -> new Object[] { u.firstName() + "@fbi.gov", u.firstName() })
				.toList();
		int[] results = jdbc.batchUpdate("update fake_users set email = ? where first_name = ?",
				updateUsersList);
		assertEquals(4, results.length, "Updated row count does not match expected");
	}

	@Test
	public void jdbc_insert_throws_QueryException_when_noIdIsReturned() throws Exception {
		Jdbc jdbc = getJdbcKeepOpen();
		assertThrows(QueryException.class, () -> {
			jdbc.insert("insert into fake_users (first_name, last_name, email) values (?, ?, ?)",
					Long.class, "william", "bell", "bill@massivdynamic.com");
		}, "insert did not throw a QueryException for operation that did not return an ID");
	}

	@Test
	public void jdbc_insert_successfullyReturns_id() throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		UUID uuid = UUID.randomUUID();
		ds.setURL("jdbc:h2:mem:" + uuid);
		ds.setUser("sa");
		ds.setPassword("");
		Jdbc jdbc = new Jdbc(ds);
		String createTable = "create table if not exists fake_users_with_id (id int primary key auto_increment, first_name varchar(25), last_name varchar(25), email varchar(25))";

		Long id = 0L;

		try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(createTable);
			id = jdbc.insert(
					"insert into fake_users_with_id (first_name, last_name, email) values (?, ?, ?)",
					Long.class, "william",
					"bell",
					"belly@massivedynamic.com");
		}

		assertTrue(id > 0, "insert operation did not return a valid ID");
	}

	@Test
	public void jdbc_methodCalls_throwQueryException_onDatabaseEmpty()
			throws Exception {
		Jdbc jdbc = getJdbcAutoClose();
		assertThrows(QueryException.class, () -> {
			jdbc.query("select * from fake_users where first_name = ?", User.MAPPER);
		});
		assertThrows(QueryException.class, () -> {
			jdbc.queryOne("select * from fake_users where first_name = ?", User.MAPPER, "peter");
		});
		assertThrows(QueryException.class, () -> {
			jdbc.update("update fake_users set last_name = ? where first_name = ?", "foo", "peter");
		});
		assertThrows(QueryException.class, () -> {
			jdbc.queryOptional("select first_name where last_name = ?", User.MAPPER, "olivia");
		});
		assertThrows(QueryException.class, () -> {
			jdbc.queryValue("select first_name where last_name = ?", User.class, "olivia");
		});
		assertThrows(QueryException.class, () -> {
			jdbc.batchUpdate("insert into fake_users (first_name, last_name, email) values (?, ?, ?)",
					List.of(
							new Object[] { "test", "one", "t1@example.com" },
							new Object[] { "test", "two", "t2@example.com" }));
		});
	}

	private Jdbc getJdbc(boolean autoClose) throws Exception {
		JdbcDataSource ds = new JdbcDataSource();
		UUID uuid = UUID.randomUUID();
		if (autoClose)
			ds.setURL("jdbc:h2:mem:" + uuid);
		else
			ds.setURL("jdbc:h2:mem:" + uuid + ";DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		Jdbc jdbc = new Jdbc(ds);

		try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(Sql.resource("sql/JdbcTest/01_setup_query.sql"));
		}
		return jdbc;
	}

	private Jdbc getJdbcKeepOpen() throws Exception {
		return getJdbc(false);
	}

	private Jdbc getJdbcAutoClose() throws Exception {
		return getJdbc(true);
	}

	record User(String firstName, String lastName, String email) {
		static final RowMapper<User> MAPPER = rs -> new User(
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getString("email"));
	}
}
