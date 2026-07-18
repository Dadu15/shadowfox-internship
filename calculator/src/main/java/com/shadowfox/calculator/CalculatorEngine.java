package com.shadowfox.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Core arithmetic/scientific engine for the calculator.
 *
 * Design note: everything is done with {@link BigDecimal} instead of
 * double/float. Floating point types cannot represent most decimal
 * fractions exactly (0.1 + 0.2 != 0.3 in binary floating point), which is
 * unacceptable for anything resembling a financial or scientific tool.
 * BigDecimal trades a little performance for exactness and predictable
 * rounding, which is the correct trade-off here.
 */
public class CalculatorEngine {

    /** Precision used for operations that cannot be exact (sqrt, division). */
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero is undefined.");
        }
        return a.divide(b, MC);
    }

    /** Square root. Throws for negative input since results would be imaginary. */
    public BigDecimal sqrt(BigDecimal a) {
        if (a.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("Cannot take the square root of a negative number.");
        }
        return a.sqrt(MC);
    }

    /**
     * Exponentiation, a^b.
     * BigDecimal natively only supports integer exponents (via pow(int)), so
     * whole-number exponents stay perfectly precise. Fractional exponents fall
     * back to double math (via Math.pow) since there is no exact decimal
     * representation for irrational results anyway.
     */
    public BigDecimal power(BigDecimal base, BigDecimal exponent) {
        if (exponent.stripTrailingZeros().scale() <= 0) {
            int exp = exponent.intValueExact();
            if (exp >= 0) {
                return base.pow(exp, MC);
            } else {
                return BigDecimal.ONE.divide(base.pow(-exp, MC), MC);
            }
        }
        double result = Math.pow(base.doubleValue(), exponent.doubleValue());
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new ArithmeticException("Result is not a real number.");
        }
        return new BigDecimal(result, MC);
    }

    // ---------------------------------------------------------------
    // Unit conversions
    // ---------------------------------------------------------------

    public enum TemperatureUnit { CELSIUS, FAHRENHEIT, KELVIN }

    public BigDecimal convertTemperature(BigDecimal value, TemperatureUnit from, TemperatureUnit to) {
        if (from == to) {
            return value;
        }
        // Normalize to Celsius first, then convert to the target unit.
        BigDecimal celsius;
        switch (from) {
            case CELSIUS -> celsius = value;
            case FAHRENHEIT -> celsius = value.subtract(new BigDecimal("32"))
                    .multiply(new BigDecimal("5"))
                    .divide(new BigDecimal("9"), MC);
            case KELVIN -> celsius = value.subtract(new BigDecimal("273.15"));
            default -> throw new IllegalArgumentException("Unknown unit: " + from);
        }

        if (to == TemperatureUnit.KELVIN && celsius.compareTo(new BigDecimal("-273.15")) < 0) {
            throw new ArithmeticException("Temperature is below absolute zero.");
        }

        return switch (to) {
            case CELSIUS -> celsius;
            case FAHRENHEIT -> celsius.multiply(new BigDecimal("9"))
                    .divide(new BigDecimal("5"), MC)
                    .add(new BigDecimal("32"));
            case KELVIN -> celsius.add(new BigDecimal("273.15"));
        };
    }

    /**
     * Currency conversion using fixed demo rates (relative to USD).
     * In a production system these would come from a live FX API rather
     * than a hardcoded table - this is intentionally kept simple for the
     * baseline build. See the README for how this could be swapped out.
     */
    public BigDecimal convertCurrency(BigDecimal amount, String fromCode, String toCode) {
        BigDecimal fromRate = CurrencyRates.rateToUsd(fromCode);
        BigDecimal toRate = CurrencyRates.rateToUsd(toCode);
        BigDecimal usdAmount = amount.divide(fromRate, MC);
        return usdAmount.multiply(toRate, MC);
    }
}
