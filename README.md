# Query4J

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

This project is not yet in Maven Central. Please clone and install locally:

```
git clone https://github.com/jakbenimble-project/query4j
cd query4j
mvn clean install
```

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

### query

Now, we can run a basic query:

```java
// Create a JDBC DataSource (Hikari, H2 JdbcDataSource, etc)...

Jdbc jdbc = new Jdbc(dataSource);
List<User> users = jdbc.query("select * from users", User.MAPPER);

for (User u : users)
    System.out.println(u.email());
```

### queryOne

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

### queryOptional

Not sure if you have a result?

```java
Optional<User> user = jdbc.queryOptional("select * from users where first_name = ?", User.MAPPER, "olivia");
```

| **NOTE:** |
| --- |
| `queryOptional()` returns `Optional.empty()` for no results and `NonUniqueResultException` for more than one. |

### batchUpdate

Need to update multiple rows?

```java
List<Object[]> updatedUsers = users.stream().map(
                 u -> new Object[] {
                     u.firstName() + "@fbi.gov",
                     u.firstName()
                 }).toList();  // our users list from earlier...
int[] rowCount = batchUpdate("update users set email = ? where first_name = ?", updatedUsers);

// use the rowCount for whatever...
```

### insert

Insert is an operation that expects to have a returned key type:

```sql
-- create a new table
create table fruit (
  id int primary key auto_increment,
  name varchar(20),
  type varchar(20)
);
```

```java
// Get the data source for this new table/database. Pass it to Jdbc.
Jdbc jdbc = new Jdbc(fruitDataSource);
Long id = jdbc.insert("insert into fruit (name, type) values (?, ?)", Long.class, "orange", "citrus");

// Do something with the ID that gets returned.
```

| **NOTE:** |
| --- |
| `insert()` will throw a `QueryException` in the event that it doesn't get an ID back. |

An ID can be whatever type you need (Long, Int, UUID, etc).

## Domain Helpers

These are strictly optional and are provided solely for convenience.

| Interface | Purpose |
| --- | --- |
| Auditable | Adds `Instant updatedAt` and `Instant createdAt` |
| Identifiable | Adds `UUID uuid` |
| Sequenceable | Adds `Long id` |

Use these with your records:

```java
import query4j.domain.Auditable;

public record User(String username, String password, Instant createdAt, Instant updatedAt) implements Auditable {
    static final RowMapper<User> MAPPER = rs -> new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getObject("created_at", Instant.class),
                    rs.getObject("updated_at", Instant.class)
                    );
}
```
