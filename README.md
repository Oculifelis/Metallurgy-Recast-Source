# Metallurgy: Recast

Source code of Metallurgy: Recast, a continuation of [Metallurgy 4: Reforged](https://github.com/Davoleo/Metallurgy-4-Reforged)
for modern Minecraft, with Metallurgy Chests merged in: dozens of metals, ores, alloys, tools, armour, machines, fluids
and gadgets.

The modern ports live in `modern/`, one Gradle project per target (`forge-1.20.1`, `neoforge-1.21.1`,
`neoforge-26.1.2`). Build one with `./gradlew build` inside its folder. The original Minecraft 1.12 source and its history
are in the [original repository](https://github.com/Davoleo/Metallurgy-4-Reforged).

The NeoForge projects resolve pinned optional APIs from public Maven. See
[`modern/BUILDING.md`](modern/BUILDING.md) for the versions and offline overrides.
Server checks live in `modern/verification/<target>/` and run with `-Pverification`.

Licence: GPL-3.0 (`LICENSE`), as the original. Metallurgy 4: Reforged by Davoleo, Matpac and contributors,
based on Metallurgy 4 by ShadowClaimer and its team; see `modern/NOTICE.md`. Modern port by Oculifelis.
