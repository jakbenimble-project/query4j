package query4j.domain;

import java.time.Instant;

public interface Auditable {
	Instant createdAt();

	Instant updatedAt();
}
