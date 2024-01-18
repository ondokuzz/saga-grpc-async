package com.demirsoft.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "com.demirsoft.controller")
public class AppProperties {
    @NotBlank
    private String grpcServerHost;
    @Min(1025)
    @Max(65536)
    private Integer grpcServerPort;

    public String getGrpcServerHost() {
        return grpcServerHost;
    }

    public Integer getGrpcServerPort() {
        return grpcServerPort;
    }

    public void setGrpcServerHost(String grpcServerHost) {
        this.grpcServerHost = grpcServerHost;
    }

    public void setGrpcServerPort(Integer grpcServerPort) {
        this.grpcServerPort = grpcServerPort;
    }
}
