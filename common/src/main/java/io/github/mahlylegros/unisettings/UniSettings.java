package io.github.mahlylegros.unisettings;

import io.github.mahlylegros.unisettings.platform.Services;

/**
 * Point d'entrée commun à tous les loaders. Toute la logique de synchro vivra ici (étape 3),
 * pour que l'ajout d'un autre loader ne demande qu'un petit module de plus.
 */
public final class UniSettings {

    private UniSettings() {}

    public static void init() {
        Constants.LOG.info("UniSettings chargé sur {}. Instance : {}. Profil partagé : {}",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getGameDirectory(),
                ProfilePaths.profileDirectory());
    }
}
