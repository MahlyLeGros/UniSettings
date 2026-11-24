"""portablemc 4.4.1 ne connaît pas la variable {ROOT} (dossier de Minecraft) utilisée par les
installateurs récents de NeoForge et Forge (26.x). On l'ajoute à la volée, avec {LIBRARY_DIR},
avant de lancer les tests en jeu. Sans effet si portablemc la gère déjà.
"""
import pathlib

import portablemc.forge as forge

path = pathlib.Path(forge.__file__)
code = path.read_text()
anchor = 'info.variables["MINECRAFT_JAR"] = str(self._jar_path.absolute())'
extra = (
    '\n        info.variables.setdefault("ROOT", str(self.context.libraries_dir.parent.absolute()))'
    '\n        info.variables.setdefault("LIBRARY_DIR", str(self.context.libraries_dir.absolute()))'
)
if anchor in code and '"ROOT"' not in code:
    path.write_text(code.replace(anchor, anchor + extra))
    print("portablemc corrigé :", path)
else:
    print("portablemc : rien à corriger")
