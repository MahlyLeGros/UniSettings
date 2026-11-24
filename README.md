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

Fabric (NeoForge prévu plus tard). **Un seul jar pour Minecraft 1.14.4 et toutes les versions
suivantes**, y compris 26.x : `unisettings-fabric-<version>.jar`.

Comment c'est possible ? Minecraft a changé en 26.1 : avant, Mojang brouillait ("obfusquait") le code
du jeu et Fabric lui donnait des noms internes, stables d'une version à l'autre ; depuis 26.1, le code
n'est plus brouillé et garde ses vrais noms. Le jar contient donc les deux variantes du petit bout
de code qui se branche sur Minecraft, et choisit la bonne au lancement
(voir `UniSettingsMixinPlugin`).

Avant la 1.14.4, Fabric n'existe pas officiellement (et Mojang ne publie pas ses noms de code),
donc le mod ne remonte pas plus loin.

À chaque modification, GitHub Actions :
- compile le mod contre **chaque** version de Minecraft depuis la 1.14.4 (la liste vient de Mojang,
  donc une nouvelle version est testée automatiquement dès sa sortie) ;
- vérifie que la variante 1.14.4-1.21.11 vise bien les mêmes noms internes partout ;
- lance le vrai jeu dans un écran virtuel sur la dernière version de chaque série
  (1.14.4, 1.15.2, 1.16.5, …, 1.21.11, 26.x) avec le jar unique, et vérifie que le mod choisit la
  bonne variante et que le profil est bien chargé.

Si une future version de Minecraft changeait le code visé par le mod, le jeu démarre quand même :
le mod ne fait simplement rien (et la CI le signale).

## Structure

- `common/` : toute la logique, sans rien de propre à un loader.
- `fabric/` : le petit module qui branche `common` sur Fabric.

Pour ajouter un loader plus tard (NeoForge, Forge), on ajoute un module à côté de `fabric/`.
La structure vient du [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).

## Compiler et tester

Il faut Java 25 pour compiler (le jar produit, lui, marche avec Java 8 et plus).

```bash
./gradlew build                          # passe 1 (1.14.4 à 1.21.11) + tests
./gradlew :fabric:build -Pmc=26.1        # passe 2 (26.x)
./gradlew universalJar                   # le jar unique, dans build/libs/
./gradlew :fabric:runClient -Pmc=1.20.1  # lance Minecraft 1.20.1 avec le mod
```
