package query4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import query4j.exceptions.NoResultException;
import query4j.exceptions.NonUniqueResultException;
import query4j.exceptions.QueryException;

public class JdbcTemplate {
	private final DataSource ds;

	public JdbcTemplate(DataSource ds) {
		this.ds = ds;
	}

	public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
		try (Connection conn = ds.getConnection()) {
			PreparedStatement ps = prepareStatement(conn, sql, params);

			try (ResultSet rs = ps.executeQuery()) {
				List<T> results = new ArrayList<>();
				while (rs.next())
					results.add(mapper.map(rs));
				return results;
			}
		} catch (SQLException sqle) {
			throw new QueryException(sqle);
		}
	}

	public int update(String sql, Object... params) {
		try (Connection conn = ds.getConnection()) {
			PreparedStatement ps = prepareStatement(conn, sql, params);
			return ps.executeUpdate();
		} catch (SQLException sqle) {
			throw new QueryException(sqle);
		}
	}

	public <K> K insert(String sql, Class<K> keyType, Object... params) {
		throw new UnsupportedOperationException("Method call 'insert' not yet implemented.");
	}

	public <T> Optional<T> queryOptional(String sql, RowMapper<T> mapper, Object... params) {
		try (Connection conn = ds.getConnection()) {
			PreparedStatement ps = prepareStatement(conn, sql, params);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return Optional.empty();
				T result = mapper.map(rs);
				if (rs.next())
					throw new NonUniqueResultException("queryOptional returned more than one row");
				return Optional.of(result);
			}
		} catch (SQLException sqle) {
			throw new QueryException(sqle);
		}
	}

	public <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
		try (Connection conn = ds.getConnection()) {
			PreparedStatement ps = prepareStatement(conn, sql, params);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new NoResultException("queryOne returned no rows");
				T result = mapper.map(rs);
				if (rs.next())
					throw new NonUniqueResultException("queryOne returned more than one row");
				return result;
			}
		} catch (SQLException sqle) {
			throw new QueryException(sqle);
		}
	}

	private void bind(PreparedStatement ps, Object[] params) throws SQLException {
		for (int i = 0; i < params.length; i++)
			ps.setObject(i + 1, params[i]);
	}

	private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		bind(ps, params);
		return ps;
	}
}
