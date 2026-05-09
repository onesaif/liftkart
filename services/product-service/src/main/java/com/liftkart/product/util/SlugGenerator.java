package com.liftkart.product.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.UUID;

@Component
public class SlugGenerator {

    public String generate(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-");
    }

    public String generateUnique(String input) {
        return generate(input) + "-" +
                UUID.randomUUID().toString().substring(0, 8);
    }
}