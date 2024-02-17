package com.demirsoft.apiservice.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "com.demirsoft.apiservice.inventory")
@Setter
@Getter
public class InventoryServiceProperties {
    @NotBlank
    private String grpcServerHost;

    @Min(1025)
    @Max(65536)
    private Integer grpcServerPort;

    @Min(1)
    @Max(600)
    private Integer grpcDeadline;

    @NotBlank
    private String hostUrl;

    @Min(0)
    @Max(100)
    private Integer retryCount;

    @Min(0)
    @Max(100)
    private Integer retryPeriod;

    @Min(0)
    @Max(1)
    private Double retryJitter;

}
