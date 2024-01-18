package com.demirsoft.apiservice.api.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.NonNull;

import com.demirsoft.apiservice.api.grpc.GreeterGrpcClient;
import com.demirsoft.apiservice.api.model.Site;
import com.demirsoft.greeter.GreeterGrpc;
import com.demirsoft.greeter.GreeterGrpc.GreeterBlockingStub;
import com.demirsoft.greeter.GreeterGrpc.GreeterStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class ApiConfiguration {
    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private AppProperties appProperties;

    @Bean
    ProducerFactory<String, Site> createKafkaClientProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-kraft:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // TODO: value -> producer ve consumer'da json type mappings
        configProps.put(JsonSerializer.TYPE_MAPPINGS, "site:com.demirsoft.apiservice.api.model.Site");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    KafkaTemplate<String, Site> createKafkaClient(@NonNull ProducerFactory<String, Site> producerFactory) {
        return new KafkaTemplate<>(producerFactory);

    }

    @Bean
    ManagedChannel createGrpcChannel() {
        logger.info("creating grpc channel for {} {}", appProperties.getGrpcServerHost(),
                appProperties.getGrpcServerPort());

        return ManagedChannelBuilder.forAddress(appProperties.getGrpcServerHost(), appProperties.getGrpcServerPort())
                .usePlaintext()
                .build();
    }

    @Bean
    GreeterBlockingStub createBlockingGrpc(ManagedChannel channel) {
        return GreeterGrpc.newBlockingStub(channel);

    }

    @Bean
    GreeterStub createAsyncGrpc(ManagedChannel channel) {
        return GreeterGrpc.newStub(channel);

    }

    @Bean
    GreeterGrpcClient createGrpcClient(GreeterStub asynGreeterStub) {
        logger.info("creating grpc client");
        return new GreeterGrpcClient(asynGreeterStub);
    }
}
