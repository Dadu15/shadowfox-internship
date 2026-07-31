package com.shadowfox.calculator;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Tier 2 (Creative Upgrade) for Task 1: lets a user type a full expression
 * such as "5 + 3 * 2" or "(4 + 2) ^ 2 / 3" and evaluates it with correct
 * operator precedence (BODMAS/PEMDAS), instead of one operation at a time.
 *
 * Implementation: the Shunting Yard algorithm (Dijkstra).
 *   1. Tokenize the raw string into numbers, operators, and parentheses.
 *   2. Convert the infix token stream to postfix (Reverse Polish Notation)
 *      using an operator stack, respecting precedence and associativity.
 *   3. Evaluate the postfix stream with a simple value stack.
 *
 * Why Shunting Yard over a recursive-descent parser: it's iterative (no risk
 * of stack overflow on deeply nested input), and it cleanly separates
 * "figuring out order of operations" from "doing the arithmetic", which
 * makes each stage easy to unit test independently.
 */
public class ExpressionParser {

    private final CalculatorEngine engine = new CalculatorEngine();

    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "+", 1,
            "-", 1,
            "*", 2,
            "/", 2,
            "^", 3
    );

    private static final Map<String, Boolean> RIGHT_ASSOCIATIVE = Map.of(
            "+", false,
            "-", false,
            "*", false,
            "/", false,
            "^", true
    );

    public BigDecimal evaluate(String expression) {
        List<String> tokens = tokenize(expression);
        List<String> postfix = toPostfix(tokens);
        return evaluatePostfix(postfix);
    }

    // ---------------------------------------------------------------
    // Step 1: Tokenizer
    // ---------------------------------------------------------------
    List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        int len = expression.length();

        while (i < len) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                int start = i;
                while (i < len && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(expression.substring(start, i));
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '(' || c == ')') {
                // Handle unary minus/plus by rewriting "-3" as "0 - 3" style
                // negative literal, e.g. leading minus or minus after '(' / operator.
                boolean isUnary = (c == '-' || c == '+') &&
                        (tokens.isEmpty() || isOperatorToken(tokens.get(tokens.size() - 1)) || "(".equals(tokens.get(tokens.size() - 1)));
                if (isUnary) {
                    int start = i;
                    i++; // consume sign
                    while (i < len && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        i++;
                    }
                    String numberToken = expression.substring(start, i);
                    if (numberToken.equals("-") || numberToken.equals("+")) {
                        throw new IllegalArgumentException("Invalid expression near position " + start);
                    }
                    tokens.add(numberToken);
                    continue;
                }
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + i);
        }

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty expression.");
        }
        return tokens;
    }

    private boolean isOperatorToken(String token) {
        return PRECEDENCE.containsKey(token);
    }

    // ---------------------------------------------------------------
    // Step 2: Infix -> Postfix (Shunting Yard)
    // ---------------------------------------------------------------
    List<String> toPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (PRECEDENCE.containsKey(token)) {
                while (!operators.isEmpty()
                        && PRECEDENCE.containsKey(operators.peek())
                        && (PRECEDENCE.get(operators.peek()) > PRECEDENCE.get(token)
                            || (PRECEDENCE.get(operators.peek()).equals(PRECEDENCE.get(token)) && !RIGHT_ASSOCIATIVE.get(token)))) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses.");
                }
                operators.pop(); // discard the "("
            } else {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }

        while (!operators.isEmpty()) {
            String op = operators.pop();
            if (op.equals("(") || op.equals(")")) {
                throw new IllegalArgumentException("Mismatched parentheses.");
            }
            output.add(op);
        }

        return output;
    }

    // ---------------------------------------------------------------
    // Step 3: Evaluate postfix (RPN)
    // ---------------------------------------------------------------
    BigDecimal evaluatePostfix(List<String> postfix) {
        Deque<BigDecimal> stack = new ArrayDeque<>();

        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(new BigDecimal(token));
            } else {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Malformed expression.");
                }
                BigDecimal b = stack.pop();
                BigDecimal a = stack.pop();
                stack.push(applyOperator(token, a, b));
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Malformed expression.");
        }
        return stack.pop();
    }

    private BigDecimal applyOperator(String op, BigDecimal a, BigDecimal b) {
        return switch (op) {
            case "+" -> engine.add(a, b);
            case "-" -> engine.subtract(a, b);
            case "*" -> engine.multiply(a, b);
            case "/" -> engine.divide(a, b);
            case "^" -> engine.power(a, b);
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }

    private boolean isNumber(String token) {
        if (token.isEmpty()) return false;
        int start = (token.charAt(0) == '-' || token.charAt(0) == '+') ? 1 : 0;
        if (start == token.length()) return false;
        boolean seenDigit = false;
        boolean seenDot = false;
        for (int i = start; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isDigit(c)) {
                seenDigit = true;
            } else if (c == '.' && !seenDot) {
                seenDot = true;
            } else {
                return false;
            }
        }
        return seenDigit;
    }
}
