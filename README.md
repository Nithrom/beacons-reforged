# Beacons Reforged

Fabric mod for **Minecraft 26.3+** that makes beacons **portable** and, in full mode, much more powerful.

📦 Download: [Modrinth](https://modrinth.com/mod/beacons-reforged)

## How it works
1. Place a beacon on a pyramid (3x3 to 9x9) and open its menu.
2. Press **Consume pyramid** and confirm: the pyramid turns into dirt and the beacon stores its level.
3. Break the beacon and take it with you. **Right-click the air** with it in your hand to choose powers.
4. Press **C** and put the beacon in its slot, or **shift-click** it in your inventory. Its powers apply while it's equipped.
5. Quick swap: with another beacon in your hand, **sneak + right-click the air** to swap it with the equipped one.

## Modes (`config/beaconsreforged.json` → `mode`)
- **vanilla**: the beacon works like the normal one (primary + secondary), just portable.
- **full** (default):
  - Pyramid size unlocks powers.
  - The pyramid material gives points: iron 2 · gold 3 · emerald 4 · diamond 5 · netherite 8.
  - Paying in the beacon permanently raises each effect's max level (I–V).
  - Extra powers: **Luck** (extra Fortune and Looting), **Reach** (blocks and attacks), and a **Haste** that speeds up mining without speeding up the swing animation (only with the right tool).

## Config
`mode`, `consumedBlock`, `showConsumeConfirmation`, `keepBeaconOnDeath`, `luckTotalCap`,
`hasteMiningMultipliers`, `maxLevelPerEffect`.
Editable in game; the server sends its config to clients when they join.

## Compatibility
Shares the Relics screen (key C) with [Conduit Reforged](https://github.com/Nithrom/Conduit-Reforged) and [Totem Slot](https://github.com/Nithrom/Totem-Slot).

## Requirements
- Fabric Loader + Fabric API
- Required on both client and server

## Building
This mod depends on [Relic Slots](https://github.com/Nithrom/Relic-Slots), which is bundled into the jar.
Clone it next to this repo, in a folder named `CereWare Slots`, then run: ./gradlew build
Requires JDK 25.

## License
LGPL-3.0-only (see `LICENSE` and `COPYING`). © CereWare
