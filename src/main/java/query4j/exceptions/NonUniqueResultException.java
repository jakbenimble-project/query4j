package query4j.exceptions;

public class NonUniqueResultException extends QueryException {
	public NonUniqueResultException(String message) {
		super(message);
	}

	public NonUniqueResultException(Throwable cause) {
		super(cause);
	}

	public NonUniqueResultException(String message, Throwable cause) {
		super(message, cause);
	}
}
