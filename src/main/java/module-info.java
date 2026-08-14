module query4j {
	exports query4j;
	exports query4j.domain;
	exports query4j.exceptions;

	requires java.base;
	requires transitive java.sql;
	requires java.naming;
	requires java.management;
}
