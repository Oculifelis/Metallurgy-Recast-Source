package it.hurts.metallurgy_reforged.world;
import it.hurts.metallurgy_reforged.fluid.FluidContent;
import it.hurts.metallurgy_reforged.effect.VersionBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/** Original20-in-500 attempts and0..255 sampling, with the target's native
 * lake carving/terrain validation instead of copying old generator internals. */
public final class TarLakeFeature implements Feature {
    public static final MapCodec<TarLakeFeature> CODEC=MapCodec.unit(new TarLakeFeature());
    @Override public MapCodec<TarLakeFeature> codec() {return CODEC;}
    @Override public boolean place(WorldGenLevel world,ChunkGenerator chunkGenerator,RandomSource random,BlockPos origin) {
        if(!java.util.Arrays.asList(it.hurts.metallurgy_reforged.config.WorldGenerationConfig.tarLakeDimensionWhiteList).contains(VersionBridge.dimensionId(world.getLevel())) || random.nextInt(500)+1>it.hurts.metallurgy_reforged.config.WorldGenerationConfig.tarLakePercentage) return false;
        BlockPos probe=origin.offset(8+random.nextInt(16),random.nextInt(256)-origin.getY(),8+random.nextInt(16));
        while(probe.getY()>0 && world.isEmptyBlock(probe)) probe=probe.below();
        if(probe.getY()<=4) return false;
        var fluid=FluidContent.ENTRIES.get("molten_tar").block.get().defaultBlockState();
        var configuration=new LakeFeature(Holder.direct(new net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider(fluid)),Holder.direct(new net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider(Blocks.AIR.defaultBlockState())),
            BlockPredicate.alwaysTrue(),
            BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.FEATURES_CANNOT_REPLACE)),
            BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)));
        return configuration.place(world,chunkGenerator,random,probe.offset(-8,0,-8));
    }
}
