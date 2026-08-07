package query4j.exceptions;

public class NoResultException extends QueryException {
	public NoResultException(String message) {
		super(message);
	}

	public NoResultException(Throwable cause) {
		super(cause);
	}

	public NoResultException(String message, Throwable cause) {
		super(message, cause);
	}
}
