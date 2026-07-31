package com.shadowfox.contacts;

/**
 * Entry point. Run with no arguments for the console baseline, or "--gui"
 * to launch the Tier 1 Swing dashboard directly.
 *
 *   java -jar shadowfox-contacts.jar          -> console mode
 *   java -jar shadowfox-contacts.jar --gui    -> GUI mode
 */
public class Main {
    public static void main(String[] args) {
        boolean guiRequested = args.length > 0 && args[0].equalsIgnoreCase("--gui");

        if (guiRequested) {
            ContactGUI.launch();
        } else {
            new ConsoleContactApp().run();
        }
    }
}
