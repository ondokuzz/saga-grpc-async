package com.demirsoft.apiservice.api;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demirsoft.apiservice.api.grpc.GreeterGrpcClient;
import com.demirsoft.apiservice.api.model.Site;
import com.demirsoft.greeter.Saga.HelloReply;
import com.google.common.util.concurrent.ListenableFuture;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ApiController {

    private final Logger logger = LogManager.getLogger(getClass());

    private final KafkaTemplate<String, Site> kafka;
    private final GreeterGrpcClient greeterGrpcClient;

    ApiController(GreeterGrpcClient greeterGrpcClient, KafkaTemplate<String, Site> kafka) {
        this.greeterGrpcClient = greeterGrpcClient;
        this.kafka = kafka;

    }

    @PostMapping("/sites")
    public ResponseEntity<String> addSite(@RequestBody Site site)
            throws InterruptedException, ExecutionException, TimeoutException {
        // CompletableFuture<SendResult<String, Site>> f = kafka.send("site",
        // site).orTimeout(5000, TimeUnit.MILLISECONDS);
        // String result = f.get(5000,
        // TimeUnit.MILLISECONDS).getRecordMetadata().toString();
        logger.info("addSite called via logger!!!");
        logger.info(site);

        return ResponseEntity.ok(site.toString());
    }

    @GetMapping("/sites")
    public Flux<Integer> getSites() throws InterruptedException, ExecutionException, TimeoutException {

        logger.info("getSites called via logger 222!!!");
        System.out.println("getSites called sout");
        return Flux.<Integer, Integer>generate(
                () -> 0,
                (s, sink) -> {
                    sink.next(s + 1);
                    return s + 1;
                },
                (s) -> System.out.println(s))
                .delayElements(Duration.ofSeconds(1)).log();

    }

    @GetMapping("/sites/grpc")
    public Mono<Site> getSitesViaGrpc() {
        return greeterGrpcClient.greet("murat2");

    }

}
