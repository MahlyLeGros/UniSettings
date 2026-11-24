package io.github.mahlylegros.unisettings.mixin.modern;

import io.github.mahlylegros.unisettings.UniSettings;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/**
 * Copie EXACTE de {@code mixin.OptionsMixin}, version Minecraft 26.1 et plus.
 * <p>
 * Pourquoi une copie ? Le même code Java donne deux fichiers .class différents :
 * pour 1.14.4 à 1.21.11, Loom remplace les noms de Minecraft par les noms internes de Fabric
 * (class_315, field_1897...) ; pour 26.x, Minecraft n'est plus obfusqué et les noms restent tels quels.
 * Le jar unique contient les deux, et {@link io.github.mahlylegros.unisettings.UniSettingsMixinPlugin}
 * active la bonne au lancement. Si tu modifies l'un des deux fichiers, modifie l'autre pareil.
 * <p>
 * Branche la synchro sur Minecraft : on applique le profil juste avant que le jeu lise
 * options.txt, et on met le profil à jour juste après chaque sauvegarde des réglages.
 * Ce mixin vit dans le code commun, donc il marchera aussi sur NeoForge plus tard.
 */
@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    @Final
    private File optionsFile;

    @Inject(method = "load()V", at = @At("HEAD"))
    private void unisettings$beforeLoad(CallbackInfo ci) {
        UniSettings.beforeOptionsLoad(optionsFile.toPath());
    }

    @Inject(method = "save()V", at = @At("TAIL"))
    private void unisettings$afterSave(CallbackInfo ci) {
        UniSettings.afterOptionsSave(optionsFile.toPath());
    }
}
