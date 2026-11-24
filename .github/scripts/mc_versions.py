"""Liste les versions de Minecraft à vérifier, à partir du manifeste officiel de Mojang.

Sortie (pour GitHub Actions) :
  all=[...]    toutes les versions "release" depuis la 1.14.4 (compilées une par une)
  smoke=[...]  un échantillon lancé pour de vrai : la dernière de chaque version majeure,
               plus quelques versions très jouées
  forge=[...]  les lancements réels avec Forge et NeoForge : pour chaque version majeure, la
               dernière version de Minecraft qui a un Forge (ou un NeoForge), avec son dernier build
Une nouvelle version de Minecraft (ou de Forge, ou de NeoForge) est donc testée automatiquement
dès sa sortie.
"""
import json
import os
import re
import urllib.request
import xml.etree.ElementTree as ElementTree

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
OLDEST = (1, 14, 4)  # premières mappings officielles de Mojang
POPULAR = {"1.16.5", "1.20.1", "1.21.1", "26.1"}

FORGE_METADATA = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml"
NEOFORGE_METADATA = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"
# NeoForge pour Minecraft 1.20.1 : c'était encore une copie de Forge, publiée sous un autre nom
NEOFORGE_1201_METADATA = "https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml"
FORGE_OLDEST = (1, 15, 2)  # Forge n'a pas Mixin avant (voir README)


def parse(version):
    try:
        return tuple(int(p) for p in version.split("."))
    except ValueError:
        return None


def loader_key(version):
    """Ordre des builds d'un loader : "47.3.10" > "47.3.9", une bêta passe avant la version finale."""
    parts = version.split("-")
    numeric = [p for p in parts if re.fullmatch(r"[\d.]+", p)]
    numbers = tuple(int(n) for p in numeric for n in p.split(".") if n)
    return numbers, len(numeric) == len(parts)


def maven_versions(url):
    try:
        with urllib.request.urlopen(url, timeout=60) as response:
            root = ElementTree.parse(response).getroot()
        return [v.text for v in root.iter("version")]
    except Exception as error:  # un dépôt en panne ne doit pas bloquer les autres tests
        print("Impossible de lire", url, ":", error)
        return []


def neoforge_minecraft(version):
    """20.4.237 -> 1.20.4 ; 21.0.1 -> 1.21 ; 26.1.0.5-beta -> 26.1 ; 26.1.1.2 -> 26.1.1"""
    numbers = [int(n) for n in re.findall(r"\d+", version.split("-")[0])]
    if len(numbers) < 3:
        return None
    if numbers[0] < 26:
        mc = "1.%d" % numbers[0] + (".%d" % numbers[1] if numbers[1] else "")
    else:
        mc = "%d.%d" % (numbers[0], numbers[1]) + (".%d" % numbers[2] if numbers[2] else "")
    return mc


def pick(builds, oldest):
    """builds : {version de Minecraft: [builds du loader]} -> les versions à lancer."""
    latest_build = {}
    for mc, versions in builds.items():
        parsed = parse(mc)
        if parsed and parsed >= oldest and versions:
            latest_build[mc] = max(versions, key=loader_key)
    latest_mc_of_minor = {}
    for mc in sorted(latest_build, key=parse):
        latest_mc_of_minor[parse(mc)[:2]] = mc
    chosen = set(latest_mc_of_minor.values()) | {mc for mc in latest_build if mc in POPULAR}
    if latest_build:
        chosen.add(min(latest_build, key=parse))
    return [(mc, latest_build[mc]) for mc in sorted(chosen, key=parse)]


with urllib.request.urlopen(MANIFEST) as response:
    manifest = json.load(response)

releases = []
for entry in manifest["versions"]:
    if entry["type"] != "release":
        continue
    parsed = parse(entry["id"])
    if parsed and parsed >= OLDEST:
        releases.append((parsed, entry["id"]))
releases.sort()

latest_of_minor = {}
for parsed, version in releases:
    latest_of_minor[parsed[:2]] = version
smoke = sorted(set(latest_of_minor.values()) | {v for _, v in releases if v in POPULAR} | {releases[0][1]},
               key=parse)

all_versions = [v for _, v in releases]
known = set(all_versions)

# Forge : les builds s'appellent "<minecraft>-<forge>", par exemple "1.20.1-47.3.0"
forge_builds = {}
for version in maven_versions(FORGE_METADATA):
    mc, _, build = version.partition("-")
    if mc in known and build:
        forge_builds.setdefault(mc, []).append(version)

# NeoForge : "20.4.237" pour Minecraft 1.20.4 (et "1.20.1-47.1.x" pour la 1.20.1)
neoforge_builds = {}
for version in maven_versions(NEOFORGE_METADATA):
    mc = neoforge_minecraft(version)
    if mc in known:
        neoforge_builds.setdefault(mc, []).append(version)
for version in maven_versions(NEOFORGE_1201_METADATA):
    if version.startswith("1.20.1-"):
        neoforge_builds.setdefault("1.20.1", []).append(version)

loaders = []
for mc, build in pick(forge_builds, FORGE_OLDEST):
    loaders.append({"loader": "forge", "mc": mc, "spec": "forge:" + build})
for mc, build in pick(neoforge_builds, (1, 20, 1)):
    loaders.append({"loader": "neoforge", "mc": mc, "spec": "neoforge:" + build})

print("Toutes :", " ".join(all_versions))
print("Lancées en vrai :", " ".join(smoke))
for entry in loaders:
    print("Lancée avec {loader} : {mc} ({spec})".format(**entry))
with open(os.environ.get("GITHUB_OUTPUT", os.devnull), "a") as out:
    out.write("all=" + json.dumps(all_versions) + "\n")
    out.write("smoke=" + json.dumps(smoke) + "\n")
    out.write("forge=" + json.dumps(loaders) + "\n")
