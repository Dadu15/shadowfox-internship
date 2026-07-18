package com.shadowfox.calculator;

/**
 * Entry point. Run with no arguments for the console baseline, or
 * "--gui" to launch the Tier 1 Swing upgrade directly.
 *
 *   java -jar shadowfox-calculator.jar          -> console mode
 *   java -jar shadowfox-calculator.jar --gui    -> GUI mode
 */
public class Main {
    public static void main(String[] args) {
        boolean guiRequested = args.length > 0 && args[0].equalsIgnoreCase("--gui");

        if (guiRequested) {
            CalculatorGUI.launch();
        } else {
            new ConsoleCalculator().run();
        }
    }
}
