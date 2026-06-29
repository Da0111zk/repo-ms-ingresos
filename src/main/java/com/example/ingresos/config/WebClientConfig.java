package com.example.ingresos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ms.productos.url}")
    private String productosUrl;

    @Value("${ms.proveedores.url}")
    private String proveedoresUrl;

    @Value("${ms.kardex.url}")
    private String kardexUrl;

    @Value("${ms.bodega.url}")
    private String bodegaUrl;

    @Bean(name = "webClientProductos")
    public WebClient webClientProductos(WebClient.Builder builder) {
        return builder.baseUrl(productosUrl).build();
    }

    @Bean(name = "webClientProveedores")
    public WebClient webClientProveedores(WebClient.Builder builder) {
        return builder.baseUrl(proveedoresUrl).build();
    }

    @Bean(name = "webClientKardex")
    public WebClient webClientKardex(WebClient.Builder builder) {
        return builder.baseUrl(kardexUrl).build();
    }

    @Bean(name = "webClientBodega")
    public WebClient webClientBodega(WebClient.Builder builder) {
        return builder.baseUrl(bodegaUrl).build();
}
}