# saga-grpc-async
- 3 micro service
  - 1 controller, 1 postgre, 1 redis
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
- make grpc classes a java module and import from the microservices

# after implementation check, implement and document the following
- unit tests
- add project to git
- integration tests
  - test rest controller using Spring MvcTest mock
- load balancing mechanisms
- documentation
  - draw high level architecture
  - folder structure: 
    project -> service1
            -> service2
               grpc
                 proto
               controllers
               config
