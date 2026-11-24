package io.github.mahlylegros.unisettings.platform.services;

import java.nio.file.Path;

/** Ce dont le code commun a besoin de la part du loader. */
public interface IPlatformHelper {

    /** Nom du loader, par exemple "Fabric". */
    String getPlatformName();

    /** Dossier de l'instance (celui qui contient options.txt). */
    Path getGameDirectory();
}
