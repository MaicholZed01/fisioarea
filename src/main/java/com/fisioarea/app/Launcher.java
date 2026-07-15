package com.fisioarea.app;

/**
 * Launcher non-JavaFX usato per gli eseguibili creati con jpackage.
 *
 * Lasciare MainApp come classe JavaFX principale, ma usare questo launcher
 * come main class nei pacchetti .exe/.dmg evita problemi di avvio quando
 * l'app viene distribuita come applicazione desktop.
 */
public final class Launcher {

    private Launcher() {
        // Utility class.
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
