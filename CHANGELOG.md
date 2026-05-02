# Changelog

## v1.9.4

### Added

- Added automatic ExitLag detection on Windows using a best-effort heuristic (process presence + adapter hints).
- Added automatic capture adjustments when ExitLag is detected:
  - Expanded BPF filter from `tcp/2050` to `tcp/2050 or tcp/443`.
  - Increased interface priority for tunnel/VPN-style adapters (e.g. ExitLag/Wintun/TAP) while keeping loopback/Npcap as top priority.
- Added unit tests validating ExitLag detection behavior for:
  - ExitLag inactive
  - ExitLag active
  - Other game proxies (should not trigger ExitLag mode)

### Changed

- Sniffer behavior remains unchanged when ExitLag is not detected (default filter stays `tcp/2050`).

### Validation

- `gradle test` passed.
- `gradle shadowJar` passed.

## v1.9.3

### Fixed

- Fixed loot logging so dropped bags are recorded from their spawn-time contents instead of the mutable bag state after items are picked up, left behind, or processed after a Nexus transition.
- Flushed queued loot bags before clearing instance state so pending drops keep the correct dungeon, attribution, and timing context.
- Reduced false delayed sounds from loot sharing/enchant processing by keeping alert playback in the GUI-side drop detection path.
- Corrected local-player DPS normalization by deduplicating overlapping local `EnemyHitPacket` and `DamagePacket` damage reports without discarding the player's own client-side hit calculation.
- Moved major Swing UI refresh paths onto the EDT to prevent stale or delayed tab updates.

### Added

- Added a low-latency `Sniffer` implementation using a `LinkedBlockingQueue` packet pipeline and immediate pcap capture settings.
- Added multi-interface capture behavior with adapter filtering and prioritization for loopback/Npcap setups, including ExitLag-style tunneling.
- Added best-effort ExitLag detection on Windows and automatic expansion of the capture filter to include `tcp/443` when ExitLag is active.
- Added dungeon drop alert configuration in the Security alerts UI.
- Added dungeon modifier alert configuration in the Security alerts UI.
- Added separate custom sound selectors for dungeon drop pings and dungeon modifier pings.
- Added parser helpers for portal IDs and dungeon modifier IDs so alert GUIs can be populated from the asset XML files.

### Changed

- Entity ID pings now play the entity ping sound, while configured dungeon portal pings play the dungeon drop sound.
- Dungeon modifier pings now play their own dedicated dungeon modifier sound.
- Loot GUI, dungeon statistics, and loot sharing now read bag item/enchant data from the captured drop snapshot.

### Validation

- Built with JDK 21.
- `gradlew compileJava` passed.
- `gradlew test` passed.
- `gradlew shadowJar` passed.
