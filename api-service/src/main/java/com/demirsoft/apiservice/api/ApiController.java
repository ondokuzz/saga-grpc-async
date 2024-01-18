package com.demirsoft.apiservice.api;

import java.time.ZonedDateTime;
import java.util.Date;
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

import io.grpc.stub.StreamObserver;

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
    public List<Site> getSites() throws InterruptedException, ExecutionException, TimeoutException {

        logger.info("getSites called via logger 222!!!");
        System.out.println("getSites called sout");
        return List.<Site>of(new Site("murat", Date.from(ZonedDateTime.now().toInstant())),
                new Site("demirsoy", Date.from(ZonedDateTime.now().plusMonths(1).toInstant())));
    }

    @GetMapping("/sites/grpc")
    public List<Site> getSitesViaGrpc() {
        StreamObserver<HelloReply> response = greeterGrpcClient.greet("murat2");

        try {
            synchronized (response) {
                logger.info("getSites waiting for completion!!!");
                response.wait(20000);
                logger.info("getSites notified for completion !!!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return List.of();
    }

}
