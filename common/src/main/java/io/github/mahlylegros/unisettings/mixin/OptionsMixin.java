package io.github.mahlylegros.unisettings.mixin;

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
 * Branche la synchro sur Minecraft : on applique le profil juste avant que le jeu lise
 * options.txt, et on met le profil à jour juste après chaque sauvegarde des réglages.
 * Ce mixin vit dans le code commun, donc il marchera aussi sur NeoForge plus tard.
 * <p>
 * Version Minecraft 1.14.4 à 1.21.11. Une copie identique existe dans {@code mixin.modern}
 * pour 26.x : si tu modifies ce fichier, modifie l'autre pareil.
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
