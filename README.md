# Mob-Swap-on-Hit

A chaotic Minecraft 1.21.1 Fabric mod where hitting a mob can swap your positions or spawn a tiny, harmless mini-copy of that mob!

## Mechanics

Whenever a non-spectator player hits a non-mini mob on the server (subject to a 15-tick per-player cooldown):

- **Position Swap (25% chance):** The player and the mob swap positions and facing angles, accompanied by portal particles, Enderman teleport sounds, and an action-bar notification.
- **Mini Mob Spawn (25% chance):** If there are fewer than 6 mini mobs nearby (within a 32x16x32 area), a scaled-down (`0.45x`) harmless copy of the mob spawns nearby with `4.0` max health, `0.0` attack damage, `0.0` follow range, a `"Mini <Mob>"` name tag, villager particles, and an XP orb pickup sound.
- Normal melee damage still registers on every hit.

## Requirements

- **Minecraft:** 1.21.1
- **Mod Loader:** Fabric Loader (`0.16.10` or newer)
- **Dependencies:** Fabric API
- **Java:** Java 21

## Download

1. Open the **Actions** tab in this GitHub repository.
2. Select the latest successful **Build** workflow run on `main`.
3. Under **Artifacts**, download **mob-swap-on-hit-jars**.
4. Extract the `.zip` and place the mod `.jar` (not `-sources.jar`) into your `.minecraft/mods` folder alongside Fabric API.
