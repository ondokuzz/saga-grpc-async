# saga-grpc-async
- Saga pattern implementation with Spring Webflux and Grpc.
- 3 micro services
  - api (saga coordinator), payment and inventory services.
  - create order -> process payment & drop amount from inventory.
                    rollback both if any step fails.
  - all operations (rest and grpc) will be async.

# todos:
- move order coordinator to its own service
- add more services, make some of them grpc and others kafka
  * api -> async rest: order -> async grpc: payment
  *                          -> async grpc: inventory
  *                          -> async kafka: delivery
  *                          -> async kafka: e-mail
  *                          -> implement transaction log (on postgre)
  *                          -> retry failed transactions: order retry service (use state pattern for order state processing)
    

- kubernetes: auto scale according to active request count
- implement load balancing for grpc calls (linkerd sidecar)
- create kafka cluster
- create redis cluster for order history retrieval
- create service specific exceptions
- make grpc classes a java module and import from the microservices by building it into local maven repo
- add grpc exception handling, timeout and retries
- return jsend format from rest apis
- use springboot starter for @GrpcService @GrpcAdvice etc (yidongnan/grpc-spring-boot-starter)
- add circuit breaker for calls to other services
- implement tracing
- add unit and integration tests
- re-implement using quarqus

# after implementation check, implement and document the following
- create test and production clusters
- use service names instead of ips (service discovery)
- documentation
  - draw high level architecture
  - create swagger
