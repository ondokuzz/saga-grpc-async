# saga-grpc-async
- Saga pattern implementation with Spring Webflux and Grpc.
- 3 micro services
  ~~~
  - api -> order -> payment
                 -> inventory
  
  - create order: process payment & drop requested product count from inventory.
                  rollback both operations if any of them fails.

  - all operations (rest and grpc) will be async.
  ~~~

# todos:
- implement transaction log (on postgre) and retry on failures
- move order coordinator to its own service and call it from api service
- add more services, make some of them rest, some of them grpc and others kafka
  ~~~
  api -(async rest)-> order -(async grpc)-> payment
                            -(async kafka)-> inventory
                            -(async kafka)-> delivery
                            -(async kafka)-> e-mail
  order retry service:  retry failed transactions
                        (use state pattern for order state processing)
  ~~~
- kubernetes: auto scale according to active request count
- implement load balancing for grpc calls (linkerd sidecar)
- create kafka cluster
- create redis cluster for read operations
- create service specific exceptions
- add unit and integration tests
- make grpc classes java module and import from the microservices by building it into local maven repo
- add grpc exception handling, timeout and retries
- return jsend format from rest apis
- use springboot starter for @GrpcService @GrpcAdvice etc (yidongnan/grpc-spring-boot-starter)
- add circuit breaker for calls to other services
- implement tracing
- re-implement using quarqus

# after implementation check, implement and document the following
- create test and production environments
- use service names instead of ips (service discovery)
- documentation
  - draw high level architecture
  - create swagger
