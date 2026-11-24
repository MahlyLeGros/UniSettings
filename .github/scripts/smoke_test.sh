#!/usr/bin/env bash
# Lance le vrai Minecraft (avec Fabric, Forge ou NeoForge) dans un écran virtuel et vérifie
# que UniSettings charge bien le profil partagé dans l'instance.
#
#   smoke_test.sh <version de Minecraft> <jar du mod> [fabric|forge|neoforge] [version pour portablemc]
#
# Exemples : smoke_test.sh 1.20.1 unisettings-fabric.jar
#            smoke_test.sh 1.20.1 unisettings-forge-neoforge.jar forge forge:1.20.1-47.4.10
set -euo pipefail

MC="$1"
JAR="$(realpath "$2")"
LOADER="${3:-fabric}"
SPEC="${4:-fabric:$MC}"
WORK="${RUNNER_TEMP:-/tmp}/smoke-$LOADER-$MC"
LOG="$WORK/game.log"
rm -rf "$WORK"
mkdir -p "$WORK/game/mods" "$WORK/xdg/unisettings"
cp "$JAR" "$WORK/game/mods/"

# Le profil partagé (sous Linux : $XDG_CONFIG_HOME/unisettings/options.txt)
export XDG_CONFIG_HOME="$WORK/xdg"
printf 'fov:0.75\nlang:fr_fr\nunisettingsSmokeTest:ok\n' > "$XDG_CONFIG_HOME/unisettings/options.txt"
# Une instance qui a déjà ses propres réglages, différents
printf 'fov:0.0\nlang:en_us\n' > "$WORK/game/options.txt"

export LIBGL_ALWAYS_SOFTWARE=1
xvfb-run -a -s "-screen 0 1280x720x24" \
  portablemc --main-dir "$WORK/main" --work-dir "$WORK/game" \
  start "$SPEC" -u UniSettingsCI > "$LOG" 2>&1 &
GAME=$!

# Ce qu'on attend dans le journal du jeu. Fabric annonce aussi quelle variante du mixin il a choisie.
expect=("depuis le profil" "UniSettings charg")
if [ "$LOADER" = fabric ]; then
  expect+=("variante")
fi
found_all() {
  for text in "${expect[@]}"; do
    grep -q "$text" "$LOG" || return 1
  done
}

ok=""
for _ in $(seq 1 150); do   # jusqu'à 12 minutes et demie (téléchargement et installation compris)
  if found_all; then
    ok=1
    break
  fi
  if ! kill -0 "$GAME" 2>/dev/null; then
    break
  fi
  sleep 5
done
sleep 5
pkill -P "$GAME" -f java 2>/dev/null || true
pkill -f "$WORK/game" 2>/dev/null || true
kill "$GAME" 2>/dev/null || true

echo "----- Lignes UniSettings / erreurs du journal -----"
grep -aiE "unisettings|mixin.*(error|fail)|exception" "$LOG" | head -50 || true

if [ -z "$ok" ]; then
  echo "----- Fin du journal -----"
  tail -80 "$LOG"
  echo "ÉCHEC : Minecraft $MC ($SPEC) n'a pas chargé le profil UniSettings"
  exit 1
fi
if [ "$LOADER" = fabric ]; then
  case "$MC" in
    1.*) expected="variante 1.14.4-1.21.11" ;;
    *)   expected="variante 26.x" ;;
  esac
  if ! grep -q "$expected" "$LOG"; then
    echo "ÉCHEC : le mod n'a pas choisi la bonne variante (attendu : $expected)"
    exit 1
  fi
fi
if ! grep -q "unisettingsSmokeTest:ok" "$WORK/game/options.txt" && ! grep -q "fov:0.75" "$WORK/game/options.txt"; then
  echo "ÉCHEC : le options.txt de l'instance ne contient pas les réglages du profil"
  cat "$WORK/game/options.txt"
  exit 1
fi
echo "OK : Minecraft $MC ($SPEC) a chargé le profil UniSettings"
