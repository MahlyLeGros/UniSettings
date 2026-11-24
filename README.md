# UniSettings

Mod Minecraft qui garde les mêmes réglages dans toutes les instances où il est installé.
Comme dans un jeu PC classique, un profil de réglages vit dans le dossier utilisateur
et chaque instance le charge au lancement.

| Système | Emplacement du profil |
| --- | --- |
| Windows | `%APPDATA%\UniSettings` |
| macOS | `~/Library/Application Support/UniSettings` |
| Linux | `~/.config/unisettings` |

**Statut :** squelette (le mod se charge et affiche l'emplacement du profil dans les logs, sans rien synchroniser encore).

## Versions

- Minecraft 1.21.1
- Fabric (NeoForge prévu plus tard)
- Java 21

## Structure

- `common/` : toute la logique, sans rien de propre à un loader.
- `fabric/` : le petit module qui branche `common` sur Fabric.

Pour ajouter un loader plus tard (NeoForge, Forge), on ajoute un module à côté de `fabric/`.
La structure vient du [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).

## Compiler et tester

```bash
./gradlew build                 # le .jar sort dans fabric/build/libs/
./gradlew :fabric:runClient     # lance Minecraft avec le mod
```
