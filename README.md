# saga-grpc-async
- 3 micro service
  - 1 api, 1 controller, 1 postgre, 1 redis, 1 kafka
  order -> payment -> stock -> delivery
  persist each task to atomic saga log
  a service will read and perform events from the saga log

- each controller auto scale according to active request count
- kafka cluster
- redis cluster
- spring, grpc, postgre, redis async
- grpc server host:port'u kubernetes service discovery'den al
- grpc response client'ta islenemediyse, server'da da rollback
- service1 grpc to service2 grpc to service3 (trace the call)
- await termination of grpc channels at the client side
- await termination of grpc server at the server side
- create app specific exceptions
- make grpc classes a java module and import from the microservices by building it into local maven repo
- add grpc exception handling, timeout and retries
- use async grpc
- test grpc timeout on failure
- make spring rest api async with webflux
- make all business classes extend from an interface
- diger servisler icin proxy yarat (feign gibi)
- move toCompletableFuture to a Utility class.
- GreeterGrpcServerManager can be a Utility class
- rollback: all async: success, error, success, error:
  on any error, rollback all
  for error state responses, the server might or might not rollback
= implement a deadletter system for each microservice
= implement using quarqus
- max retries'i properties'den al
- jsend return et
- use springboot starter for @GrpcService @GrpcAdvice etc (yidongnan/grpc-spring-boot-starter)
- add circuit breaker for grpc calls
- implement tracing

# after implementation check, implement and document the following
- unit tests
- add project to git
- implement redis cluster load balancing
- integration tests
  - test rest controller using Spring MvcTest mock
- load balancing mechanisms
- delete unnecessary comments
- Autowired'lari constructor initialize'a cevir
- use service names instead of ips (service discovery)
- documentation
  - draw high level architecture
  - define resillience mechannisms, grpc retry, linkerd
  - implement swagger
  - folder structure: 
    project -> service1
            -> service2
               grpc
                 proto
               controllers
               config
