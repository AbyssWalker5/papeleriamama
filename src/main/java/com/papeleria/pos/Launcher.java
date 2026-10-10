package com.papeleria.pos;

/**
 * Launcher class for the fat JAR distribution.
 * JavaFX requires this indirection when running from a shaded JAR
 * because it checks that the main class extends Application.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
