package query4j;

import java.time.Instant;

public interface Auditable {
	Instant createdAt();

	Instant updatedAt();
}
