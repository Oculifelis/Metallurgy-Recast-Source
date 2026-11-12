package it.hurts.metallurgy_reforged.compat.hephaestus;

import com.titammods.hephaestus_tools.tools.stat.HarvestTier;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/** Hephaestus 1.0.8 has enum tiers; later releases load their tier definitions from data. */
final class HarvestTiers {
    private HarvestTiers() { }

    private static final Supplier<Stream<HarvestTier>> TIERS = reader();

    static int requiredLevel(BlockState state) {
        return TIERS.get().filter(tier -> tier.canHarvest(state))
                .mapToInt(HarvestTier::miningLevel).min().orElse(6);
    }

    private static Supplier<Stream<HarvestTier>> reader() {
        HarvestTier[] constants = HarvestTier.class.getEnumConstants();
        if (constants != null) return () -> Arrays.stream(constants);
        try {
            Method definitions = Class.forName("com.titammods.hephaestus_tools.materials.tier.BuiltInTiers")
                    .getMethod("definitions");
            Method of = HarvestTier.class.getMethod("of", Identifier.class);
            // Read the current map on every call so a datapack reload keeps its effect.
            return () -> {
                try {
                    Map<?, ?> current = (Map<?, ?>) definitions.invoke(null);
                    return current.keySet().stream().map(id -> {
                        try {
                            return (HarvestTier) of.invoke(null, id);
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException("Cannot read Hephaestus harvest tier " + id, e);
                        }
                    });
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot read Hephaestus tier definitions", e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported Hephaestus harvest tier API", e);
        }
    }
}
