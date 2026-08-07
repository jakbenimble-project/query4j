module query4j {
	exports query4j;

	requires java.base;
	requires transitive java.sql;
	requires java.naming;
	requires java.management;
	requires com.zaxxer.hikari;
}
