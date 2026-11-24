#!/usr/bin/env bash
# Lance le vrai Minecraft (avec Fabric) dans un écran virtuel et vérifie que UniSettings
# charge bien le profil partagé dans l'instance.
#
#   smoke_test.sh <version de Minecraft> <jar du mod>
set -euo pipefail

MC="$1"
JAR="$(realpath "$2")"
WORK="${RUNNER_TEMP:-/tmp}/smoke-$MC"
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
  start "fabric:$MC" -u UniSettingsCI > "$LOG" 2>&1 &
GAME=$!

ok=""
for _ in $(seq 1 120); do   # jusqu'à 10 minutes (téléchargement compris)
  if grep -q "depuis le profil" "$LOG" && grep -q "UniSettings charg" "$LOG"; then
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
  echo "ÉCHEC : Minecraft $MC n'a pas chargé le profil UniSettings"
  exit 1
fi
if ! grep -q "unisettingsSmokeTest:ok" "$WORK/game/options.txt" && ! grep -q "fov:0.75" "$WORK/game/options.txt"; then
  echo "ÉCHEC : le options.txt de l'instance ne contient pas les réglages du profil"
  cat "$WORK/game/options.txt"
  exit 1
fi
echo "OK : Minecraft $MC a chargé le profil UniSettings"
