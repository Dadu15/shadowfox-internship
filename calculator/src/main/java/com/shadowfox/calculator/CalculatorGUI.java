package com.shadowfox.calculator;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Tier 1 (Grounded Upgrade) for Task 1: takes the console logic and puts a
 * Swing UI in front of it. This introduces event-driven programming (we no
 * longer ask for input in a fixed order - we wait for whichever button the
 * user clicks) and basic layout management (BorderLayout + GridLayout).
 *
 * The display accepts a full expression (digits, + - * / ^, parentheses)
 * and evaluates it via {@link ExpressionParser} when "=" is pressed, so
 * operator precedence (BODMAS/PEMDAS) works exactly like the Tier 2 console
 * "Expression Mode" - the GUI is just a different front end onto the same
 * evaluation engine.
 */
public class CalculatorGUI extends JFrame {

    private final JTextField display = new JTextField();
    private final ExpressionParser parser = new ExpressionParser();

    public CalculatorGUI() {
        super("ShadowFox Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setupDisplay();
        add(display, BorderLayout.NORTH);
        add(buildButtonPanel(), BorderLayout.CENTER);

        setSize(340, 420);
        setLocationRelativeTo(null);
    }

    private void setupDisplay() {
        display.setEditable(true);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("Monospaced", Font.PLAIN, 24));
        display.setPreferredSize(new Dimension(0, 50));
    }

    private JPanel buildButtonPanel() {
        String[][] rows = {
                {"(", ")", "C", "\u2190"},   // \u2190 = backspace arrow
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "\u221A", "+"},   // \u221A = sqrt
                {"^", "=", "", ""}
        };

        JPanel panel = new JPanel(new GridLayout(rows.length, 4, 6, 6));
        for (String[] row : rows) {
            for (String label : row) {
                if (label.isEmpty()) {
                    panel.add(new JLabel()); // filler cell to keep the grid aligned
                    continue;
                }
                JButton button = new JButton(label);
                button.setFont(new Font("SansSerif", Font.PLAIN, 18));
                button.addActionListener(e -> handleButton(label));
                panel.add(button);
            }
        }
        return panel;
    }

    private void handleButton(String label) {
        switch (label) {
            case "C" -> display.setText("");
            case "\u2190" -> backspace();
            case "=" -> evaluateDisplay();
            case "\u221A" -> wrapWithSqrt();
            default -> display.setText(display.getText() + label);
        }
    }

    private void backspace() {
        String text = display.getText();
        if (!text.isEmpty()) {
            display.setText(text.substring(0, text.length() - 1));
        }
    }

    /** Wraps the current expression in a sqrt() call by inserting "sqrt(" style handling. */
    private void wrapWithSqrt() {
        // Simple UX choice: sqrt applies to whatever is currently typed.
        String text = display.getText();
        try {
            BigDecimal value = text.isBlank() ? BigDecimal.ZERO : parser.evaluate(text);
            CalculatorEngine engine = new CalculatorEngine();
            display.setText(engine.sqrt(value).toPlainString());
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void evaluateDisplay() {
        String expression = display.getText();
        if (expression.isBlank()) {
            return;
        }
        try {
            BigDecimal result = parser.evaluate(expression);
            display.setText(result.stripTrailingZeros().toPlainString());
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Calculation Error", JOptionPane.ERROR_MESSAGE);
        display.setText("");
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new CalculatorGUI().setVisible(true));
    }
}
