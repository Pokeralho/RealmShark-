# Changelog

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
