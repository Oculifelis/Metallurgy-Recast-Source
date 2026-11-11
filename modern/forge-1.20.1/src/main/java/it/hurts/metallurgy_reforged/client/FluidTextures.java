package it.hurts.metallurgy_reforged.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fluid still textures that repeat every 4 blocks instead of every block. Next to
 * {@code <still>.png} put 16 sprites {@code <still>_<x>_<z>.png} (x, z = 0..3, cut from one seamless 64x64 surface and
 * animated in step); return {@link #still} from {@code IClientFluidTypeExtensions#getStillTexture(FluidState,
 * BlockAndTintGetter, BlockPos)} and each block shows the sprite for its position. Without them the plain still
 * texture is used.
 */
public final class FluidTextures {
    private FluidTextures() {
    }

    private static final Map<ResourceLocation, ResourceLocation[]> PARTS = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    public static ResourceLocation still(ResourceLocation still, BlockPos pos) {
        ResourceLocation part = PARTS.computeIfAbsent(still, FluidTextures::parts)
                [(Math.floorMod(pos.getX(), 4) << 2) | Math.floorMod(pos.getZ(), 4)];
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(part);
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? still : part;
    }

    private static ResourceLocation[] parts(ResourceLocation still) {
        ResourceLocation[] parts = new ResourceLocation[16];
        for (int i = 0; i < 16; i++) {
            parts[i] = new ResourceLocation(still.getNamespace(), still.getPath() + "_" + (i >> 2) + "_" + (i & 3));
        }
        return parts;
    }
}
