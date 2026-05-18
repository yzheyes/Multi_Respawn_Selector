# Multi Respawn Selector

Multi Respawn Selector is a Fabric mod for Minecraft Java Edition 1.20.1. It lets each player keep multiple respawn points instead of being limited to the last bed or respawn anchor they used.

When you die, the vanilla respawn button opens a respawn point selection screen. You can choose any currently valid saved bed, respawn anchor, command-added point, or the world spawn.

## Features

- Saves multiple respawn points for each player.
- Supports beds, respawn anchors, and command-added custom points.
- Shows a respawn point selection screen after death.
- Validates respawn points on the server before use.
- Removes invalid points when beds or respawn anchors are broken, missing, blocked, uncharged, or unsafe.
- Supports cross-dimension respawning.
- Consumes only one respawn anchor charge per successful anchor respawn.
- Allows renaming saved bed and respawn anchor points.
- Persists saved respawn data across server restarts.
- Includes English and Simplified Chinese localization.

## Requirements

- Minecraft Java Edition 1.20.1
- Fabric Loader
- Fabric API
- Java 17 or newer

The mod must be installed on both client and server.

## How To Use

Set respawn points normally:

- Sleep in a bed to save that bed.
- Use a charged respawn anchor to save that anchor.
- Use `/multirespawn add <name>` to save your current position as a command respawn point.

After death:

1. Click the vanilla respawn button.
2. Choose a saved respawn point from the selection screen.
3. The server validates the selected point.
4. If the point is valid, you respawn there.
5. If the point is invalid, it is removed or rejected and the list refreshes.

If there are no valid saved respawn points, use the world spawn option.

## Renaming Respawn Points

Sneak + right-click one of your saved beds or respawn anchors to open the rename screen.

You can also rename points with:

```text
/multirespawn rename <id_or_name> <new_name>
```

If the old name contains spaces, wrap it in quotes:

```text
/multirespawn rename "Main Base Bed" "Village Backup Bed"
```

## Commands

```text
/multirespawn add <name>
```

Adds your current position as a command respawn point.

```text
/multirespawn list
```

Lists your saved respawn points. The list is refreshed before display, so invalid beds and anchors are cleaned up.

```text
/multirespawn remove <id_or_name>
```

Removes one saved respawn point.

```text
/multirespawn rename <id_or_name> <new_name>
```

Renames one saved respawn point.

```text
/multirespawn clear
```

Clears all of your saved respawn points.

## Valid Respawn Points

A saved point must still be safe and usable.

Beds are invalid if:

- The bed block was broken.
- The saved position is no longer a bed.
- No safe landing spot can be found nearby.

Respawn anchors are invalid if:

- The anchor was broken.
- The saved position is no longer a respawn anchor.
- The anchor has no charge.
- No safe landing spot can be found nearby.

Command and custom points are invalid if:

- The dimension no longer exists.
- No safe landing spot can be found nearby.

## Localization

The mod includes:

- English: `en_us`
- Simplified Chinese: `zh_cn`

The displayed language follows the player's Minecraft language setting.

## Build From Source

```powershell
.\gradlew.bat build
```

The built mod jar is generated in:

```text
build/libs/
```
