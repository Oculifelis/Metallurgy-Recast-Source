package it.hurts.metallurgy_reforged.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

/**
 * Fluid still textures that repeat every 4 blocks instead of every block. Next to
 * {@code <still>.png} put 16 sprites {@code <still>_<x>_<z>.png} (x, z = 0..3, cut from one seamless 64x64 surface and
 * animated in step); each block then shows the sprite for its position. Without them the plain still texture is used.
 */
public final class FluidTextures {
    private FluidTextures() {
    }

    /** A fluid model with the 4-block still repeat, untinted. */
    public static FluidModel.Unbaked model(Identifier still, Identifier flow) {
        return new FluidModel.Unbaked(new Material(still), new Material(flow), null, FluidTintSources.constant(0xFFFFFFFF),
                macroStill(still));
    }

    /** Draws the fluid as vanilla does, moving the still sprite's UVs onto the sprite for the block's position. */
    public static CustomFluidRenderer macroStill(Identifier still) {
        Identifier[] parts = new Identifier[16];
        for (int i = 0; i < 16; i++) {
            parts[i] = still.withPath(still.getPath() + "_" + (i >> 2) + "_" + (i & 3));
        }
        return (fluidRenderer, fluidState, getter, pos, output, blockState) -> {
            TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
            TextureAtlasSprite part = atlas.getSprite(parts[(Math.floorMod(pos.getX(), 4) << 2) | Math.floorMod(pos.getZ(), 4)]);
            TextureAtlasSprite base = atlas.getSprite(still);
            if (part == atlas.missingSprite() || base == atlas.missingSprite()) {
                return false;
            }
            FluidRenderer.Output moved = layer -> new Moved(output.getBuilder(layer), base, part);
            fluidRenderer.tesselate(getter, pos, moved, blockState, fluidState);
            return true;
        };
    }

    /** Passes vertices through, mapping UVs inside {@code from} to the same spot in {@code to}. */
    private record Moved(VertexConsumer out, TextureAtlasSprite from, TextureAtlasSprite to) implements VertexConsumer {
        private static final float EPS = 1.0E-6F;

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords,
                              float nx, float ny, float nz) {
            if (u >= from.getU0() - EPS && u <= from.getU1() + EPS && v >= from.getV0() - EPS && v <= from.getV1() + EPS) {
                u = to.getU0() + (u - from.getU0()) / (from.getU1() - from.getU0()) * (to.getU1() - to.getU0());
                v = to.getV0() + (v - from.getV0()) / (from.getV1() - from.getV0()) * (to.getV1() - to.getV0());
            }
            out.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            out.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            out.setColor(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            out.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            out.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            out.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            out.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            out.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            out.setLineWidth(width);
            return this;
        }
    }
}
