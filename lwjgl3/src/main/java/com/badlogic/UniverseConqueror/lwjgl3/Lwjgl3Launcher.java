package com.badlogic.UniverseConqueror.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.UniverseConqueror.GameLauncher;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new GameLauncher(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("UniverseConquer");

        // Vsync limits the frames per second to what your hardware can display, and helps eliminate
        // screen tearing.
        configuration.useVsync(true);

        // Set the desired window size (width, height)
        int windowWidth = 1280;  // Example width
        int windowHeight = 720;  // Example height
        configuration.setWindowedMode(windowWidth, windowHeight);

        // Set window icon
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        return configuration;
    }
}
