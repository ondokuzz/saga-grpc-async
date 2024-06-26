package com.demirsoft.apiservice.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "com.demirsoft.apiservice.payment")
@Setter
@Getter
public class PaymentServiceProperties {
    @NotBlank
    private String grpcServerHost;

    @Min(1025)
    @Max(65536)
    private Integer grpcServerPort;

    @Min(1)
    @Max(600)
    private Integer grpcDeadline;
}
