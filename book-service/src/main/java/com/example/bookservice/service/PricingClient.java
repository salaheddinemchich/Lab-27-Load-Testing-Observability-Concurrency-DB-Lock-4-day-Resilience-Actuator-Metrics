package com.example.bookservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PricingClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public PricingClient(RestTemplate rest, @Value("${pricing.base-url}") String baseUrl) {
        this.rest = rest; // Penser à configurer un RestTemplate avec timeout (voir étape suivante)
        this.baseUrl = baseUrl;
    }

    @Retry(name = "pricing")
    @CircuitBreaker(name = "pricing", fallbackMethod = "fallbackPrice")
    public double getPrice(long bookId) {
        return rest.getForObject(baseUrl + "/api/prices/" + bookId, Double.class);
    }

    // Le fallback doit avoir la même signature + l'exception
    public double fallbackPrice(long bookId, Throwable ex) {
        System.err.println("Fallback pricing activé pour le livre " + bookId + " : " + ex.getMessage());
        return 0.0; // Prix par défaut
    }
}
