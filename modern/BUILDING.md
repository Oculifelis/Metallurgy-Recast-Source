# Building Metallurgy Recast

Each version directory has its own Gradle wrapper. Use Java 17 for Forge
1.20.1, Java 21 for NeoForge 1.21.1, and Java 25 for NeoForge 26.1.2, 26.2 and 26.3.

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

## Newer NeoForge targets

The 26.2 and 26.3 trees keep version 1.4.4 and share common gameplay data with the other targets. Their native checks are a separate verification mod loaded only by runGameTestServer. The ordinary build excludes it. Run build and runGameTestServer through the shared job runner. Core validation passes on both targets: 32 required GameTests on 26.2, and the same suite plus a native cooking-fuel regression on 26.3 (33 tests). Both normal dedicated servers reach Ready and stop gracefully. The only named host diagnostic is an unavailable Windows Perflib counter catalogue; no game or data errors were accepted. Full source commits and SHA-256 values are written beside each accepted local artifact.

No matching Hephaestus or Hephaestus Tools build is catalogued for either target, so those native adapters are omitted. The Thaumaturge adapter retains the pinned 1.1.0 compile API; no matching 26.2/26.3 Thaumaturge runtime is catalogued, so this optional integration is untested on the new targets. Core tests run without those optional mods.

The newer APIs are kept in target-local overlays; common source files and gameplay values are unchanged. The 26.3 worldgen registry uses native feature codecs and flattened feature data. Cooking fuels use native components and providers that read the same live server settings. Per-target EnderIO recipe configuration directories are independent; optional adapters still need matching dependencies for runtime coverage.
