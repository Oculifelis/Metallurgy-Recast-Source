package it.hurts.metallurgy_reforged.client;
import it.hurts.metallurgy_reforged.fluid.FluidCatalog;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** Each fluid has its own coloured still and flow textures (drawn untinted); the still one repeats every 4 blocks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="metallurgy",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class FluidRendering {
    public static Identifier still(FluidCatalog.Definition definition) {return Identifier.fromNamespaceAndPath("metallurgy","blocks/fluid/"+definition.block()+"_still");}
    public static Identifier flow(FluidCatalog.Definition definition) {return Identifier.fromNamespaceAndPath("metallurgy","blocks/fluid/"+definition.block()+"_flow");}
    public static IClientFluidTypeExtensions extensions(FluidCatalog.Definition definition) {
        return new IClientFluidTypeExtensions() {
            public Identifier getStillTexture() {return still(definition);}
            public Identifier getFlowingTexture() {return flow(definition);}
            public int getTintColor() {return 0xFFFFFFFF;}
            public Identifier getRenderOverlayTexture(net.minecraft.client.Minecraft minecraft) {return definition.tar()?Identifier.fromNamespaceAndPath("metallurgy","textures/effects/fluid_tar.png"):null;}
        };
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        for(var entry:it.hurts.metallurgy_reforged.fluid.FluidContent.ENTRIES.values()) event.registerFluidType(extensions(entry.definition),entry.type.get());
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void models(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
        for(var entry:it.hurts.metallurgy_reforged.fluid.FluidContent.ENTRIES.values()) event.register(FluidTextures.model(still(entry.definition),flow(entry.definition)),entry.still,entry.flowing);
    }
    private FluidRendering() {}
}
