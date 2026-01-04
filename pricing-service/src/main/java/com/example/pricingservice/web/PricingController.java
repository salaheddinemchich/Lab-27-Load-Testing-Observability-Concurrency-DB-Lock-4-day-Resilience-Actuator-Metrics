package com.example.pricingservice.web;

import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/prices")
public class PricingController {

    @GetMapping("/{bookId}")
    public double price(@PathVariable long bookId, @RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        // Simulation panne forcée
        if (fail) throw new IllegalStateException("Pricing down (forced)");

        // Simulation instabilité (30% d'échec)
        if (ThreadLocalRandom.current().nextInt(100) < 30) {
            throw new IllegalStateException("Random failure - Service Unstable");
        }

        return 50.0 + (bookId % 10) * 5.0;
    }
}
