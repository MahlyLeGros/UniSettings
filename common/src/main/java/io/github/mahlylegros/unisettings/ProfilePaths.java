package io.github.mahlylegros.unisettings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Emplacement du profil partagé, comme pour un jeu PC classique :
 * <ul>
 *   <li>Windows : %APPDATA%\UniSettings</li>
 *   <li>macOS : ~/Library/Application Support/UniSettings</li>
 *   <li>Linux : $XDG_CONFIG_HOME/unisettings, sinon ~/.config/unisettings</li>
 * </ul>
 */
public final class ProfilePaths {

    private ProfilePaths() {}

    public static Path profileDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Path home = Paths.get(System.getProperty("user.home"));

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            Path base = (appData != null && !appData.trim().isEmpty()) ? Paths.get(appData) : home.resolve("AppData").resolve("Roaming");
            return base.resolve("UniSettings");
        }
        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Application Support").resolve("UniSettings");
        }
        String xdg = System.getenv("XDG_CONFIG_HOME");
        Path base = (xdg != null && !xdg.trim().isEmpty()) ? Paths.get(xdg) : home.resolve(".config");
        return base.resolve("unisettings");
    }
}
