package com.demirsoft.apiservice.api.services.inventory;

import java.net.URI;
import java.time.Duration;

import javax.annotation.Nonnull;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.demirsoft.apiservice.api.config.InventoryServiceProperties;
import com.demirsoft.apiservice.api.services.InventoryServiceUnreachableException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class InventoryServiceImpl implements InventoryService {
    private static final String INVENTORY_BASE_URI = "/inventory";

    private static final String INVENTORY_QUERY_AVAILABILITY_URI = "/availability";
    private static final String INVENTORY_QUERY_AVAILABILITY_PARAM = "amount";

    private static final String INVENTORY_QUERY_DROP_URI = "/drop";
    private static final String INVENTORY_QUERY_DROP_PARAM = "amount";

    private static final String INVENTORY_QUERY_ROLLBACK_URI = "/rollback";
    private static final String INVENTORY_QUERY_ROLLBACK_PARAM = "amount";

    private final InventoryServiceProperties inventoryServiceProperties;
    private final WebClient webClient;

    public InventoryServiceImpl(
            InventoryServiceProperties inventoryServiceProperties,
            WebClient webClient) {
        this.inventoryServiceProperties = inventoryServiceProperties;
        this.webClient = webClient;
    }

    @Override
    public Mono<InventoryResponse> drop(@Nonnull InventoryRequest inventoryRequest) {
        return webClient.get()
                .uri(uriBuilder -> builDropUri(
                        uriBuilder,
                        inventoryRequest.productId(),
                        inventoryRequest.productCount()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(InventoryResponse.class)
                .retryWhen(buildBackoffRetry());
    }

    @Override
    public Mono<InventoryResponse> rollback(@Nonnull InventoryRequest inventoryRequest) {
        return webClient.get()
                .uri(uriBuilder -> builRollbackUri(
                        uriBuilder,
                        inventoryRequest.productId(),
                        inventoryRequest.productCount()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(InventoryResponse.class)
                .retryWhen(buildBackoffRetry());
    }

    @Override
    public Boolean isProductAvailable(@Nonnull InventoryRequest inventoryRequest) {

        return webClient.get()
                .uri(uriBuilder -> builProductAvailableUri(
                        uriBuilder,
                        inventoryRequest.productId(),
                        inventoryRequest.productCount()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .retryWhen(buildBackoffRetry())
                .block();
    }

    private Retry buildBackoffRetry() {
        Integer retryCount = inventoryServiceProperties.getRetryCount();
        Integer retryPeriod = inventoryServiceProperties.getRetryPeriod();
        Double retryJitter = inventoryServiceProperties.getRetryJitter();
        String hostUrl = inventoryServiceProperties.getHostUrl();

        return Retry.backoff(retryCount,
                Duration.ofSeconds(retryPeriod))
                .jitter(retryJitter)
                .onRetryExhaustedThrow(
                        (retryBackoffSpec, retrySignal) -> {
                            throw new InventoryServiceUnreachableException(hostUrl);
                        });
    }

    private URI builProductAvailableUri(UriBuilder uriBuilder, Integer productId, Integer productCount) {
        return uriBuilder.pathSegment(
                INVENTORY_BASE_URI,
                productId.toString(),
                INVENTORY_QUERY_AVAILABILITY_URI)
                .queryParam(INVENTORY_QUERY_AVAILABILITY_PARAM, productCount)
                .build();
    }

    private URI builDropUri(UriBuilder uriBuilder, Integer productId, Integer productCount) {
        return uriBuilder.pathSegment(
                INVENTORY_BASE_URI,
                productId.toString(),
                INVENTORY_QUERY_DROP_URI)
                .queryParam(INVENTORY_QUERY_DROP_PARAM, productCount)
                .build();
    }

    private URI builRollbackUri(UriBuilder uriBuilder, Integer productId, Integer productCount) {
        return uriBuilder.pathSegment(
                INVENTORY_BASE_URI,
                productId.toString(),
                INVENTORY_QUERY_ROLLBACK_URI)
                .queryParam(INVENTORY_QUERY_ROLLBACK_PARAM, productCount)
                .build();
    }

}
