package query4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlTest {

	@Test
	public void testThat_Sql_resource_succesfullyReturns_classpathResourceAsString() throws Exception {
		String expected = "select count(*) from test_table;";
		String actual = Sql.resource("sql/test_count_test_table.sql").trim();
		Assertions.assertTrue(expected.equals(actual), "Returned sql did not match expected sql.");
	}
}
