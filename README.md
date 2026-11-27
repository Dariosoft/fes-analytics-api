# Analytics API

Owns order timeline projections and sales metrics for the seller panel. It consumes domain events
from Kafka and is never the source of truth for orders, payments, products or accounts.

## Implementation

Every implementation in this repository uses only these two skills:

- `spring-boot-project-creator`, Layered option. Place code under
  `com.friendlyeshop.analytics` in `controller/`, `service/`, `repository/`, `model/`,
  `model/dto/`, `config/`, and `exception/`. Do not use the DDD option of that skill, and do not
  bootstrap a new project with Spring Initializr when changing this service.
- `clean-code-guard` after every non-trivial production code change, before the work is considered
  done.

```bash
./mvnw test
docker build -t friendly-e-shop/analytics-api:dev .
```
