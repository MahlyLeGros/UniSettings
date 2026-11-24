package io.github.mahlylegros.unisettings.platform;

import io.github.mahlylegros.unisettings.Constants;
import io.github.mahlylegros.unisettings.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

/**
 * Trouve l'implémentation propre au loader (Fabric, NeoForge...) des interfaces de {@code services}.
 * Chaque loader déclare la sienne dans META-INF/services.
 */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {}

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
