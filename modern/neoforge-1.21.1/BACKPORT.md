# Thaumaturge compat on 1.21.1

Ported from the 26.1.2 compat for Thaumaturge 1.1.0 (1.21.1 build). Same items, caps, rods, clusters,
gadgets, research, scans, tuning and recipes as 26.1.2.

## What differs
- The Thaumaturge API on 1.21.1 uses the older names (getVis, getMaxVis, addVis, getParts) and ResourceLocation.
- Enchantability comes from getEnchantmentValue overrides instead of item properties; the values are the same
  (thaumium knuckles 15, void gauntlet 10, thaumium buckler 20, void buckler 22).
- Recipe JSON uses the 1.21.1 ingredient objects; the optional pack uses pack_format 48.
- Cap and rod art is still made at load from the installed Thaumaturge jar.

## Cut
Nothing from the Thaumaturge compat. The other 26.1.2-only extras (Hephaestus Tools traits and the rest) are not
part of this port.

## Build
`-PthaumaturgeJar=<path to thaumaturge-1.21.1-NeoForge-1.1.0.jar>` (or THAUMATURGE_JAR_1_21_1). Nothing of
Thaumaturge is bundled. At runtime Thaumaturge needs Curios 9+ and Lithostitched 1.8+.
