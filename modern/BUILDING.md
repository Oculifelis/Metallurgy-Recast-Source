# Building Metallurgy Recast

Each version directory has its own Gradle wrapper. Use Java 17 for Forge
1.20.1, Java 21 for NeoForge 1.21.1, and Java 25 for NeoForge 26.1.2.

The NeoForge targets resolve their optional compile APIs from Curse Maven.
The coordinates are pinned to the same unmodified releases used by this port:

| Target | Dependency | CurseForge file |
| --- | --- | --- |
| 26.1.2 | Hephaestus Tools 1.0.8 | [9202105](https://www.curseforge.com/minecraft/mc-mods/hephaestus-tools/files/9202105) |
| 26.1.2 | Thaumaturge 1.1.0 | [9428045](https://www.curseforge.com/minecraft/mc-mods/thaumaturge/files/9428045) |
| 1.21.1 | Thaumaturge 1.1.0 | [9452543](https://www.curseforge.com/minecraft/mc-mods/thaumaturge/files/9452543) |

These are compile-only dependencies. Their implementations and assets stay out
of the mod JAR and source repository. Normal gameplay still loads each adapter
only when its optional mod is installed.

The harvest-tier adapter reads the enum exposed by Hephaestus Tools 1.0.8 and
the data definitions used by later releases. The latter map is read again after
a datapack reload. The optional minimum remains 1.0.8.

For an offline build, an unmodified local file may be supplied with
`-PhephaestusToolsJar` or `-PthaumaturgeJar`. The existing environment overrides
`HEPHAESTUS_TOOLS_JAR`, `THAUMATURGE_JAR` (26.1.2) and
`THAUMATURGE_JAR_1_21_1` are also supported.
