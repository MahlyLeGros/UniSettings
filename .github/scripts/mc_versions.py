"""Liste les versions de Minecraft à vérifier, à partir du manifeste officiel de Mojang.

Sortie (pour GitHub Actions) :
  all=[...]    toutes les versions "release" depuis la 1.14.4 (compilées une par une)
  smoke=[...]  un échantillon lancé pour de vrai : la dernière de chaque version majeure,
               plus quelques versions très jouées
Une nouvelle version de Minecraft est donc testée automatiquement dès sa sortie.
"""
import json
import os
import urllib.request

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
OLDEST = (1, 14, 4)  # premières mappings officielles de Mojang
POPULAR = {"1.16.5", "1.20.1", "1.21.1", "26.1"}


def parse(version):
    try:
        return tuple(int(p) for p in version.split("."))
    except ValueError:
        return None


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
print("Toutes :", " ".join(all_versions))
print("Lancées en vrai :", " ".join(smoke))
with open(os.environ.get("GITHUB_OUTPUT", os.devnull), "a") as out:
    out.write("all=" + json.dumps(all_versions) + "\n")
    out.write("smoke=" + json.dumps(smoke) + "\n")
