package io.github.mahlylegros.unisettings.forge;

import io.github.mahlylegros.unisettings.Constants;
import io.github.mahlylegros.unisettings.UniSettings;

/**
 * Point d'entrée Forge ET NeoForge. La classe porte les deux annotations {@code @Mod} :
 * chaque loader cherche la sienne (Forge : {@code net.minecraftforge...},
 * NeoForge : {@code net.neoforged...}) et ignore l'autre.
 * <p>
 * Le vrai travail (charger le profil avant que Minecraft lise options.txt) est fait par
 * {@link io.github.mahlylegros.unisettings.forge.mixin.OptionsMixin}, qui agit bien avant
 * que cette classe soit créée.
 */
@net.minecraftforge.fml.common.Mod(Constants.MOD_ID)
@net.neoforged.fml.common.Mod(Constants.MOD_ID)
public class UniSettingsForge {

    public UniSettingsForge() {
        UniSettings.init();
        if (!UniSettings.isOptionsHooked()) {
            // Minecraft a déjà lu options.txt à ce moment-là : si le mixin n'a rien fait,
            // c'est que ce Forge est trop ancien pour les mixins (Forge 1.14.4, ou 1.15.2 avant 31.2.44).
            Constants.LOG.warn("UniSettings n'a pas pu se brancher sur les réglages de Minecraft. "
                    + "Mets à jour Forge/NeoForge : le profil partagé n'est pas utilisé dans cette instance.");
        }
    }
}
