package io.github.mahlylegros.unisettings.platform;

import io.github.mahlylegros.unisettings.platform.services.IPlatformHelper;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Forge et NeoForge ont la même classe {@code FMLPaths} pour trouver le dossier de l'instance,
 * mais pas dans le même package. On la cherche donc par son nom (réflexion), ce qui permet
 * d'avoir un seul jar pour les deux loaders et pour toutes les versions.
 */
public class ForgePlatformHelper implements IPlatformHelper {

    private static final String NEOFORGE_PATHS = "net.neoforged.fml.loading.FMLPaths";
    private static final String FORGE_PATHS = "net.minecraftforge.fml.loading.FMLPaths";

    private static Path gameDirectory;

    @Override
    public String getPlatformName() {
        return exists(NEOFORGE_PATHS) ? "NeoForge" : "Forge";
    }

    @Override
    public Path getGameDirectory() {
        return gameDirectory();
    }

    /** Le options.txt de l'instance. */
    public static Path optionsFile() {
        return gameDirectory().resolve("options.txt");
    }

    public static synchronized Path gameDirectory() {
        if (gameDirectory == null) {
            gameDirectory = findGameDirectory();
        }
        return gameDirectory;
    }

    private static Path findGameDirectory() {
        for (String className : new String[]{NEOFORGE_PATHS, FORGE_PATHS}) {
            try {
                Class<?> paths = Class.forName(className);
                Object gameDir = paths.getField("GAMEDIR").get(null);
                Method get = paths.getMethod("get");
                Object path = get.invoke(gameDir);
                if (path instanceof Path) {
                    return ((Path) path).toAbsolutePath();
                }
            } catch (Throwable ignored) {
                // Pas ce loader-là, on essaie le suivant
            }
        }
        // Dernier recours : le dossier courant, qui est le dossier de l'instance avec la plupart des launchers
        return Paths.get("").toAbsolutePath();
    }

    private static boolean exists(String className) {
        try {
            Class.forName(className, false, ForgePlatformHelper.class.getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
