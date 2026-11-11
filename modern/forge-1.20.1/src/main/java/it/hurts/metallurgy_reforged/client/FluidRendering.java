package it.hurts.metallurgy_reforged.client;
import it.hurts.metallurgy_reforged.fluid.FluidCatalog;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/** Each fluid has its own coloured still and flow textures (drawn untinted); the still one repeats every 4 blocks. */
public final class FluidRendering {
    public static IClientFluidTypeExtensions extensions(FluidCatalog.Definition definition) {
        return new IClientFluidTypeExtensions() {
            public ResourceLocation getStillTexture() {return new ResourceLocation("metallurgy","blocks/fluid/"+definition.block()+"_still");}
            public ResourceLocation getStillTexture(net.minecraft.world.level.material.FluidState state,net.minecraft.world.level.BlockAndTintGetter getter,net.minecraft.core.BlockPos pos) {return FluidTextures.still(getStillTexture(),pos);}
            public ResourceLocation getFlowingTexture() {return new ResourceLocation("metallurgy","blocks/fluid/"+definition.block()+"_flow");}
            public int getTintColor() {return 0xFFFFFFFF;}
            public ResourceLocation getRenderOverlayTexture(net.minecraft.client.Minecraft minecraft) {return definition.tar()?new ResourceLocation("metallurgy","textures/effects/fluid_tar.png"):null;}
        };
    }
    private FluidRendering() {}
}
