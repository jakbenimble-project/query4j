# Welcome to Query4J

## Goals
- Framework-agnostic
- SQL-first API
- JDBC with minimal abstraction
- Java records as data models
- Explicit SQL over generated SQL
- Small dependency footprint
- Transaction-friendly
- Predictable performance

## Explicit non-goals
- ORM
- entity state tracking
- lazy loading
- query DSL
- automatic schema generation
- relationship management
- criteria builders
- JPQL equivalent

## Philosophy

Query4J is not an ORM.

If you know SQL, you already know Query4J.

The framework never generates SQL, never hides database behavior, and never attempts to synchronize object graphs. It exists to remove JDBC boilerplate, not SQL.

## Quick Start

Models are simply Java records:

```java
public record User(String firstName, String lastName, String email) {}
```

Assuming that your table looks like this:

```sql
create table users (
  first_name varchar(20),
  last_name varchar(20),
  email varchar(20)
);
```

We can add a mapper directly to the User record:

```java
public record User(String firstName, String lastName, String email) {
    static final RowMapper<User> MAPPER = rs ->
        new User(
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email")
        );
}
```

Now, we can run a basic query:

```java
// Create a JDBC DataSource (Hikari, H2 JdbcDataSource, etc)...

JdbcTemplate jdbc = new JdbcTemplate(dataSource);
List<User> users = jdbc.query("select * from users", User.MAPPER);

for (User u : users)
    System.out.println(u.email());
```

Just need one?
```java
// Table contains the row:
// ("olivia", "dunham", "olivia@example.org")

User user = jdbc.queryOne("select * from users where first_name = ?", User.MAPPER, "olivia");
assert user.lastName().equals("dunham");
```

| **NOTE:** |
| --- |
| If `queryOne()` returns zero results, it will throw a `NoResultException`.  If more than one result, it will throw a `NonUniqueResultException`. |

Not sure if you have a result?

```java
Optional<User> user = jdbc.queryOptional("select * from users where first_name = ?", User.MAPPER, "olivia");
```

| **NOTE:** |
| --- |
| `queryOptional()` returns `Optional.empty()` for no results and `NonUniqueResultException` for more than one. |


