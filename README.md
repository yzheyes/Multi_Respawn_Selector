# Multi Respawn Selector

Fabric 1.20.1 mod that stores multiple respawn points per player and lets the player choose one from the death screen before the vanilla respawn request is processed.

## Build

Use Java 17 or Java 21 to run Gradle. The current project was verified with Microsoft JDK 21 and Gradle 8.8.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Microsoft\jdk-21.0.8.9-hotspot'
.\gradlew.bat build
```

The remapped mod jar is written to `build/libs/`.

## Gameplay Flow

- Setting a vanilla spawn point captures a mod respawn point through `ServerPlayerEntity#setSpawnPoint`.
- The death screen gets a `选择重生点` button.
- The client requests available points; the server validates and filters them.
- Choosing a point records a server-side pending choice, then the client sends the vanilla respawn packet.
- `PlayerManager#respawnPlayer` applies the pending choice to the new player instance and teleports across dimensions if needed.

## Commands

- `/multirespawn add <name>`: add the player's current position as a command respawn point.
- `/multirespawn list`: list saved points for the player.
- `/multirespawn remove <id_or_name>`: remove a saved point.
- `/multirespawn clear`: clear all saved points for the player.

## Manual Test Cases

- Save multiple overworld beds, die, and choose each one.
- Save multiple charged nether anchors, die, and confirm one charge is consumed after use.
- Die in the overworld and choose a nether anchor.
- Die in the nether and choose an overworld bed.
- Break a bed and confirm it disappears from the choice list.
- Drain an anchor and confirm it is rejected.
- Restart the server and confirm data persists.
- Test two players and confirm each list is isolated.
- Click vanilla respawn with multiple valid points and confirm the chooser opens instead.
- Confirm vanilla world spawn is still available from the chooser.

## Notes

The code targets Yarn `1.20.1+build.10`. If updating mappings or Minecraft versions, re-check the three Mixin targets in `multirespawn.mixins.json`, especially `ServerPlayerEntity#setSpawnPoint`, `ServerPlayNetworkHandler#onClientStatus`, and `PlayerManager#respawnPlayer`.
