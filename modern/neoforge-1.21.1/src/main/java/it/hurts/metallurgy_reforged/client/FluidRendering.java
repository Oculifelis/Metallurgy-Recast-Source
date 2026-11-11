package it.hurts.metallurgy_reforged.client;
import it.hurts.metallurgy_reforged.fluid.FluidCatalog;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** Each fluid has its own coloured still and flow textures (drawn untinted); the still one repeats every 4 blocks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="metallurgy",value=net.neoforged.api.distmarker.Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class FluidRendering {
    public static ResourceLocation still(FluidCatalog.Definition definition) {return ResourceLocation.fromNamespaceAndPath("metallurgy","blocks/fluid/"+definition.block()+"_still");}
    public static ResourceLocation flow(FluidCatalog.Definition definition) {return ResourceLocation.fromNamespaceAndPath("metallurgy","blocks/fluid/"+definition.block()+"_flow");}
    public static IClientFluidTypeExtensions extensions(FluidCatalog.Definition definition) {
        return new IClientFluidTypeExtensions() {
            public ResourceLocation getStillTexture() {return still(definition);}
            public ResourceLocation getStillTexture(net.minecraft.world.level.material.FluidState state,net.minecraft.world.level.BlockAndTintGetter getter,net.minecraft.core.BlockPos pos) {return FluidTextures.still(getStillTexture(),pos);}
            public ResourceLocation getFlowingTexture() {return flow(definition);}
            public int getTintColor() {return 0xFFFFFFFF;}
            public ResourceLocation getRenderOverlayTexture(net.minecraft.client.Minecraft minecraft) {return definition.tar()?ResourceLocation.fromNamespaceAndPath("metallurgy","textures/effects/fluid_tar.png"):null;}
        };
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        for(var entry:it.hurts.metallurgy_reforged.fluid.FluidContent.ENTRIES.values()) event.registerFluidType(extensions(entry.definition),entry.type.get());
    }
    private FluidRendering() {}
}
