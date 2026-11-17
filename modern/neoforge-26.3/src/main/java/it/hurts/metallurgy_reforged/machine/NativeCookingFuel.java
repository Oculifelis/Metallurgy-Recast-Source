package it.hurts.metallurgy_reforged.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native cooking fuel values read the same live server configuration as the older furnace hook. */
public record NativeCookingFuel(String fuel) implements ContextIntProvider {
    public static final java.util.Set<String> FUELS=java.util.Set.of("tar","charcoal_block","thermite_dust","ignatius_fuel");
    private static final Codec<String> NAME=Codec.STRING.validate(s->FUELS.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown Metallurgy fuel: "+s));
    public static final MapCodec<NativeCookingFuel> CODEC=RecordCodecBuilder.mapCodec(i->i.group(NAME.fieldOf("fuel").forGetter(NativeCookingFuel::fuel)).apply(i,NativeCookingFuel::new));
    private static final DeferredRegister<MapCodec<? extends ContextIntProvider>> TYPES=DeferredRegister.create(Registries.CONTEXT_INT_PROVIDER_TYPE,"metallurgy");
    static { TYPES.register("cooking_fuel",()->CODEC); }
    public static void register(IEventBus bus) {TYPES.register(bus);}
    @Override public MapCodec<NativeCookingFuel> codec() {return CODEC;}
    @Override public void validate(ValidationContext context) {}
    @Override public int getIntUnsafe(LootContext context) {
        return switch(fuel) {
            case "tar"->800;
            case "charcoal_block"->16000;
            case "thermite_dust"->it.hurts.metallurgy_reforged.config.GadgetsConfig.ThermiteDust.thermiteFuelValue*200;
            case "ignatius_fuel"->it.hurts.metallurgy_reforged.config.GadgetsConfig.IgnatiusFuel.ignatiusFuelValue*200;
            default->throw new IllegalStateException("Unknown Metallurgy fuel: "+fuel);
        };
    }
}
