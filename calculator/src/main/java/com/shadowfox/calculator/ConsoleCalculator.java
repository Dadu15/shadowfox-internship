package com.shadowfox.calculator;

import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Baseline build for Task 1: Enhanced Console-Based Calculator.
 *
 * Covers:
 *  - Basic arithmetic (+, -, *, /) using BigDecimal (no floating point drift)
 *  - Scientific operations: square root, exponentiation
 *  - Unit conversions: temperature and currency
 *  - Robust exception handling: divide-by-zero, non-numeric input, negative
 *    sqrt, unknown units, etc. never crash the app - they print a friendly
 *    message and let the user try again
 *  - A "run again?" loop so the session doesn't exit after one calculation
 *  - Tier 2: an "Expression Mode" that evaluates a full expression like
 *    "5 + 3 * 2" with correct operator precedence
 */
public class ConsoleCalculator {

    private final Scanner scanner = new Scanner(System.in);
    private final CalculatorEngine engine = new CalculatorEngine();
    private final ExpressionParser parser = new ExpressionParser();

    public void run() {
        System.out.println("=========================================");
        System.out.println("   ShadowFox Enhanced Console Calculator");
        System.out.println("=========================================");

        boolean keepGoing = true;
        while (keepGoing) {
            printMenu();
            String choice = readLine("Enter choice: ").trim();

            try {
                switch (choice) {
                    case "1" -> doBasicOperation("+");
                    case "2" -> doBasicOperation("-");
                    case "3" -> doBasicOperation("*");
                    case "4" -> doBasicOperation("/");
                    case "5" -> doSquareRoot();
                    case "6" -> doExponentiation();
                    case "7" -> doTemperatureConversion();
                    case "8" -> doCurrencyConversion();
                    case "9" -> doExpressionMode();
                    case "0" -> keepGoing = false;
                    default -> System.out.println("Unrecognized option. Please choose a number from the menu.");
                }
            } catch (ArithmeticException e) {
                System.out.println("Math error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                // Last-resort safety net so a single bad interaction never
                // kills the whole session.
                System.out.println("Something went wrong: " + e.getMessage());
            }

            if (keepGoing) {
                System.out.println();
            }
        }

        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("[1] Add            [2] Subtract");
        System.out.println("[3] Multiply       [4] Divide");
        System.out.println("[5] Square Root    [6] Exponentiation");
        System.out.println("[7] Temperature Conversion");
        System.out.println("[8] Currency Conversion");
        System.out.println("[9] Expression Mode (e.g. \"5 + 3 * 2\")");
        System.out.println("[0] Exit");
    }

    private void doBasicOperation(String op) {
        BigDecimal a = readNumber("Enter first number: ");
        BigDecimal b = readNumber("Enter second number: ");
        BigDecimal result = switch (op) {
            case "+" -> engine.add(a, b);
            case "-" -> engine.subtract(a, b);
            case "*" -> engine.multiply(a, b);
            case "/" -> engine.divide(a, b);
            default -> throw new IllegalStateException("Unreachable");
        };
        System.out.println("Result: " + result);
    }

    private void doSquareRoot() {
        BigDecimal a = readNumber("Enter a number: ");
        System.out.println("Result: " + engine.sqrt(a));
    }

    private void doExponentiation() {
        BigDecimal base = readNumber("Enter base: ");
        BigDecimal exponent = readNumber("Enter exponent: ");
        System.out.println("Result: " + engine.power(base, exponent));
    }

    private void doTemperatureConversion() {
        BigDecimal value = readNumber("Enter temperature value: ");
        CalculatorEngine.TemperatureUnit from = readTemperatureUnit("Convert from (C/F/K): ");
        CalculatorEngine.TemperatureUnit to = readTemperatureUnit("Convert to (C/F/K): ");
        BigDecimal result = engine.convertTemperature(value, from, to);
        System.out.println("Result: " + result);
    }

    private CalculatorEngine.TemperatureUnit readTemperatureUnit(String prompt) {
        String input = readLine(prompt).trim().toUpperCase();
        return switch (input) {
            case "C", "CELSIUS" -> CalculatorEngine.TemperatureUnit.CELSIUS;
            case "F", "FAHRENHEIT" -> CalculatorEngine.TemperatureUnit.FAHRENHEIT;
            case "K", "KELVIN" -> CalculatorEngine.TemperatureUnit.KELVIN;
            default -> throw new IllegalArgumentException("Unit must be C, F, or K.");
        };
    }

    private void doCurrencyConversion() {
        BigDecimal amount = readNumber("Enter amount: ");
        System.out.println("Supported currencies: " + CurrencyRates.supportedCodes());
        String from = readLine("Convert from (e.g. USD): ").trim();
        String to = readLine("Convert to (e.g. INR): ").trim();
        BigDecimal result = engine.convertCurrency(amount, from, to);
        System.out.println("Result: " + result.setScale(2, java.math.RoundingMode.HALF_UP) + " " + to.toUpperCase());
    }

    private void doExpressionMode() {
        String expression = readLine("Enter expression (e.g. \"(4 + 2) * 3 - 1\"): ");
        BigDecimal result = parser.evaluate(expression);
        System.out.println("Result: " + result);
    }

    /** Reads a BigDecimal robustly - never throws InputMismatchException, retries on bad input. */
    private BigDecimal readNumber(String prompt) {
        while (true) {
            String raw = readLine(prompt).trim();
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException e) {
                System.out.println("\"" + raw + "\" is not a valid number. Please try again.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine();
        } catch (java.util.NoSuchElementException | IllegalStateException e) {
            // Input stream closed unexpectedly - treat as exit rather than crashing.
            throw new InputMismatchException("Input stream closed.");
        }
    }
}
