# UniSettings

Mod Minecraft qui garde les mêmes réglages dans toutes les instances où il est installé.
Comme dans un jeu PC classique, un profil de réglages vit dans le dossier utilisateur
et chaque instance le charge au lancement.

| Système | Emplacement du profil |
| --- | --- |
| Windows | `%APPDATA%\UniSettings` |
| macOS | `~/Library/Application Support/UniSettings` |
| Linux | `~/.config/unisettings` |

## Fonctionnement

- **Au lancement**, juste avant que Minecraft lise `options.txt`, le mod copie le profil dans l'instance.
  L'ancien `options.txt` de l'instance est gardé une fois sous `options.txt.unisettings-backup`.
- **Quand tu changes un réglage**, Minecraft réécrit `options.txt` et le mod met le profil à jour.
  Les lignes que l'instance ne connaît pas (par exemple les touches d'un mod absent) sont conservées.
- **Premier lancement :** s'il n'y a pas encore de profil, il est créé à partir de l'instance actuelle.

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
./gradlew build                 # compile + tests, le .jar sort dans fabric/build/libs/
./gradlew :fabric:runClient     # lance Minecraft avec le mod
```
