package com.example.ingresos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IngresoApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngresoApplication.class, args);
    }
}