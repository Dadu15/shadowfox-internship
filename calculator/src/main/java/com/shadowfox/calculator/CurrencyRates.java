package com.shadowfox.calculator;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Fixed demo exchange rates, expressed as "1 USD = X of this currency".
 * These are illustrative snapshot values, NOT live rates. A natural
 * "Tier 2" extension of Task 3 (Library Management System / API
 * integration) would be swapping this for a real FX API call.
 */
public final class CurrencyRates {

    private static final Map<String, BigDecimal> RATES_PER_USD = Map.of(
            "USD", new BigDecimal("1.00"),
            "EUR", new BigDecimal("0.92"),
            "GBP", new BigDecimal("0.79"),
            "INR", new BigDecimal("86.50"),
            "JPY", new BigDecimal("156.50"),
            "AUD", new BigDecimal("1.52"),
            "CAD", new BigDecimal("1.37")
    );

    private CurrencyRates() {
    }

    public static BigDecimal rateToUsd(String code) {
        BigDecimal rate = RATES_PER_USD.get(code.toUpperCase());
        if (rate == null) {
            throw new IllegalArgumentException(
                    "Unsupported currency code: " + code + ". Supported: " + RATES_PER_USD.keySet());
        }
        return rate;
    }

    public static java.util.Set<String> supportedCodes() {
        return RATES_PER_USD.keySet();
    }
}
