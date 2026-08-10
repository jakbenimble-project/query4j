package query4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Sql {

	private Sql() {
	}

	public static String resource(String name) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		return new String(is.readAllBytes(), StandardCharsets.UTF_8);
	}
}
