package io.github.mahlylegros.unisettings;

import io.github.mahlylegros.unisettings.platform.Services;

import java.nio.file.Path;

/**
 * Point d'entrée commun à tous les loaders. Le mixin {@code OptionsMixin} appelle
 * {@link #beforeOptionsLoad} et {@link #afterOptionsSave} ; le reste est dans {@link ProfileSync}.
 * Une erreur de synchro est écrite dans les logs mais ne doit jamais empêcher le jeu de démarrer.
 */
public final class UniSettings {

    private static final ProfileSync SYNC = new ProfileSync(ProfilePaths.profileDirectory());

    private UniSettings() {}

    public static void init() {
        Constants.LOG.info("UniSettings chargé sur {}. Instance : {}. Profil partagé : {}",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getGameDirectory(),
                SYNC.profileOptions());
    }

    public static void beforeOptionsLoad(Path instanceOptions) {
        try {
            SYNC.applyProfileTo(instanceOptions);
        } catch (Exception e) {
            Constants.LOG.error("Impossible d'appliquer le profil UniSettings", e);
        }
    }

    public static void afterOptionsSave(Path instanceOptions) {
        try {
            SYNC.saveToProfile(instanceOptions);
        } catch (Exception e) {
            Constants.LOG.error("Impossible de mettre à jour le profil UniSettings", e);
        }
    }
}
