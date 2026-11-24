package io.github.mahlylegros.unisettings.platform;

import io.github.mahlylegros.unisettings.Constants;
import io.github.mahlylegros.unisettings.platform.services.IPlatformHelper;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Trouve l'implémentation propre au loader (Fabric, NeoForge...) des interfaces de {@code services}.
 * Chaque loader déclare la sienne dans META-INF/services.
 */
public final class Services {

    /** Les implémentations de chaque loader, au cas où ServiceLoader ne les trouve pas.
     *  Déclaré avant PLATFORM : sinon il vaudrait encore null quand PLATFORM est calculé. */
    private static final String[] KNOWN_IMPLEMENTATIONS = {
            "io.github.mahlylegros.unisettings.platform.ForgePlatformHelper",
            "io.github.mahlylegros.unisettings.platform.FabricPlatformHelper"
    };

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {}

    public static <T> T load(Class<T> clazz) {
        // On cherche d'abord avec le chargeur de classes du mod lui-même : sous Forge 1.15/1.16,
        // le chargeur "par défaut" du thread ne voit pas les fichiers du mod.
        // Écrit sans findFirst() (Java 9+) pour rester compatible avec Java 8 (Minecraft 1.14 à 1.16).
        Iterator<T> services = ServiceLoader.load(clazz, Services.class.getClassLoader()).iterator();
        if (!services.hasNext()) {
            services = ServiceLoader.load(clazz).iterator();
        }
        if (!services.hasNext()) {
            // Dernier recours (Forge 1.15) : le chargeur de classes ne sait pas lire META-INF/services,
            // on essaie donc directement les implémentations connues.
            T known = loadKnown(clazz);
            if (known != null) {
                return known;
            }
            throw new NullPointerException("Failed to load service for " + clazz.getName());
        }
        final T loadedService = services.next();
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    private static <T> T loadKnown(Class<T> clazz) {
        for (String name : KNOWN_IMPLEMENTATIONS) {
            try {
                Class<?> implementation = Class.forName(name, true, Services.class.getClassLoader());
                if (clazz.isAssignableFrom(implementation)) {
                    return clazz.cast(implementation.getDeclaredConstructor().newInstance());
                }
            } catch (Throwable ignored) {
                // Pas ce loader-là
            }
        }
        return null;
    }
}
