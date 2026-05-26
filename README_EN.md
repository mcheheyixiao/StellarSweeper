# StellarSweeper

中文说明: [README.md](README.md)

StellarSweeper is an Architectury multi-loader Minecraft mod for server-side dropped-item cleanup.

Supported modules in this repository:
- `common`
- `fabric`
- `neoforge`

Target Minecraft version:
- `1.21.1` (Mojmap)

## Features

- Scheduled automatic dropped-item cleanup.
- Threshold-based warning checks.
- Per-player scan range (horizontal radius + Y min/max).
- Safe deduplication across overlapping player scan areas.
- Manual cleanup and preview commands.
- Multi-list cleanup targets (`cleanupLists` + `currentCleanupList`).
- Clickable confirm flow for OP2 players when threshold is reached.
- Per-world `doEntityDrops` rule handling during cleanup.
- Localization via standard lang JSON files:
  - `assets/stellarsweeper/lang/en_us.json`
  - `assets/stellarsweeper/lang/zh_cn.json`

## Command Overview

Root command:
- `/sweep`

All subcommands require permission level OP2:

- `/sweep`
- `/sweep help`
- `/sweep run`
- `/sweep preview`
- `/sweep reload`
- `/sweep save`
- `/sweep toggle auto`
- `/sweep toggle threshold`
- `/sweep set interval <ticks>`
- `/sweep set threshold <count>`
- `/sweep set radius <blocks>`
- `/sweep set y <min> <max>`
- `/sweep list`
- `/sweep list <listName>`
- `/sweep lists`
- `/sweep list-create <listName>`
- `/sweep list-delete <listName>`
- `/sweep list-use <listName>`
- `/sweep add`
- `/sweep add <itemId>`
- `/sweep remove <itemId>`
- `/sweep confirm <requestId> yes`
- `/sweep confirm <requestId> no`

## Threshold Prompt Rules

When threshold checks trigger:

- OP2 players receive clickable confirm buttons.
- Non-OP players receive plain text (no clickable buttons).
- If no OP2 players are online:
  - no clickable request is created,
  - no threshold message is broadcast,
  - server log records the event.

## Cleanup Safety Rules

- If no players are online, `/sweep run` does not clean anything and returns `sweep.none`.
- Cleanup does not switch to global world scan when executed from console.
- Cleanup checks `GameRules.RULE_DOENTITYDROPS` per world:
  - worlds with disabled drops are skipped,
  - skipped worlds are included in the cleanup report.

## Configuration

Config file path:
- `config/stellarsweeper.json`

Key fields:
- `enableAutoCleanup`
- `enableThresholdCheck`
- `cleanupInterval`
- `thresholdCheckInterval`
- `warningCooldown`
- `cleanRadius`
- `yMin`
- `yMax`
- `itemThreshold`
- `language` (kept for compatibility, not used to force server language)
- `currentCleanupList`
- `cleanupLists`

Example values are generated automatically on first load if the file does not exist.

## Build

From repository root:

```bash
./gradlew build
./gradlew :fabric:build
./gradlew :neoforge:build
```

## Notes

- No custom items are registered.
- No `cleanup_hoe` item is included.
- No GUI is implemented in this version.
