# ShadowFox Java Internship — Task 1: Enhanced Console-Based Calculator

Beginner-level task, built all the way through baseline → Tier 1 → Tier 2.

## What's included

| Layer | Description |
|---|---|
| **Baseline** | Console calculator: `+ - * /`, square root, exponentiation, temperature conversion (C/F/K), currency conversion. All arithmetic uses `BigDecimal`, never `double`, so there's no floating-point drift. |
| **Tier 1 (Grounded Upgrade)** | A Swing GUI (`CalculatorGUI`) with a text-based display and clickable buttons — event-driven instead of prompt-driven. |
| **Tier 2 (Creative Upgrade)** | `ExpressionParser` — a Shunting Yard implementation that evaluates a full expression like `5 + 3 * 2` or `(4 + 2) ^ 2 / 3` with correct operator precedence, instead of one operation at a time. Both the console's "Expression Mode" and the GUI's `=` button use it. |

## Project layout

```
shadowfox-calculator/
├── pom.xml
├── README.md
├── LEARNINGS.md
└── src/main/java/com/shadowfox/calculator/
    ├── Main.java              # entry point (console or --gui)
    ├── ConsoleCalculator.java # baseline CLI menu loop
    ├── CalculatorGUI.java     # Tier 1 Swing GUI
    ├── ExpressionParser.java  # Tier 2 Shunting Yard parser
    ├── CalculatorEngine.java  # core math (BigDecimal) + unit conversions
    └── CurrencyRates.java     # fixed demo FX rate table
```

## How to run

**Requires JDK 17+** (built and tested on JDK 21).

### Option A — Maven
```bash
mvn clean package
java -jar target/shadowfox-calculator.jar          # console mode
java -jar target/shadowfox-calculator.jar --gui     # GUI mode
```

### Option B — plain javac (no Maven needed)
```bash
mkdir out
javac -d out $(find src/main/java -name "*.java")
java -cp out com.shadowfox.calculator.Main          # console mode
java -cp out com.shadowfox.calculator.Main --gui     # GUI mode
```

No database, no external services, and no API keys are required — everything
runs standalone.

## Design notes / self-audit results

- **`0.1 + 0.2` test**: passes — returns exactly `0.3` because every value is
  a `BigDecimal`, not a `double`.
- **Crash test**: entering non-numeric text (e.g. `"abc"`) is caught by
  `NumberFormatException` and the user is re-prompted; the app never dies.
- **Divide by zero, negative sqrt, out-of-range temperature (below absolute
  zero), unsupported currency codes**: all throw a specific exception with a
  human-readable message, caught centrally in `ConsoleCalculator.run()`, so
  one bad interaction never kills the session.
- **Looping**: the console shows the menu again after every operation until
  the user picks `0` to exit.
- **Currency rates** are a fixed demo table (`CurrencyRates.java`), not a
  live feed — swapping it for a real FX API (e.g. `HttpClient` + JSON
  parsing) would be the natural next iteration, mirroring the ISBN-lookup
  upgrade in the Library Management task.

## Pitch (per the Innovation Pitch protocol)

For the Tier 2 upgrade I used the **Shunting Yard algorithm** rather than a
recursive-descent parser: it's iterative (no stack-overflow risk on deeply
nested expressions) and cleanly splits "determine order of operations" from
"do the arithmetic," so each stage (`tokenize` → `toPostfix` →
`evaluatePostfix`) is independently testable.
