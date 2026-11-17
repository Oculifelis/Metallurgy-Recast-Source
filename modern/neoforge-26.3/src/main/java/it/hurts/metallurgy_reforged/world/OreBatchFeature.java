package it.hurts.metallurgy_reforged.world;
import it.hurts.metallurgy_reforged.content.Content;
import it.hurts.metallurgy_reforged.effect.VersionBridge;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import java.util.*;

/** Modern native vein placement with original ordered attempts, inclusive
 * heights and special spawn predicates. Existing-world retrogen is separate. */
public record OreBatchFeature(String dimension) implements Feature {
    public static final MapCodec<OreBatchFeature> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.fieldOf("dimension").forGetter(OreBatchFeature::dimension)).apply(i,OreBatchFeature::new));
    @Override public MapCodec<OreBatchFeature> codec() { return CODEC; }
    @Override public boolean place(WorldGenLevel world,ChunkGenerator chunkGenerator,RandomSource random,BlockPos origin) {
        String realm=dimension;
        if(!VersionBridge.oreRealm(world,realm)) return false;
        int chunkX=origin.getX()>>4,chunkZ=origin.getZ()>>4;
        boolean warm=false,fortress=false;
        if(realm.equals("overworld")) {
            outer:for(int x=0;x<16;x++) for(int z=0;z<16;z++) if(world.getBiome(new BlockPos(chunkX*16+x,64,chunkZ*16+z)).value().getBaseTemperature()>=0.95F) { warm=true;break outer; }
        } else if(realm.equals("nether")) fortress=VersionBridge.nearFortress(world,chunkX,chunkZ);
        boolean generated=false;
        for(var profile:OreProfiles.ALL) {
            if(!profile.dimension().equals(realm) || profile.vein_size()<=0) continue;
            var holder=Content.BLOCK_MAP.get(profile.block());if(holder==null) continue;
            Block ore=holder.get();int attempts=profile.attempts();
            if(profile.special().equals("warm_chunk") && warm) attempts=it.hurts.metallurgy_reforged.config.WorldGenerationConfig.rarity.uncommonRarity;
            if(profile.special().equals("fortress") && fortress) attempts=it.hurts.metallurgy_reforged.config.WorldGenerationConfig.rarity.commonRarity;
            if(profile.special().equals("lava_adjacent")) attempts=Math.round(attempts*1.05F);
            List<BlockReplacement> targets=new ArrayList<>();
            targets.add(new BlockReplacement(new net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest(
                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,VersionBridge.effectKey("metallurgy:ore_replaceables/"+realm))),ore.defaultBlockState()));
            if(realm.equals("overworld")) {
                targets.add(new BlockReplacement(new BlockMatchTest(Blocks.STONE),ore.defaultBlockState()));
                targets.add(new BlockReplacement(new BlockMatchTest(Blocks.DEEPSLATE),ore.defaultBlockState()));
            } else if(realm.equals("end")) targets.add(new BlockReplacement(new BlockMatchTest(Blocks.END_STONE),ore.defaultBlockState()));
            else {
                targets.add(new BlockReplacement(new BlockMatchTest(Blocks.NETHERRACK),ore.defaultBlockState()));
                for(String variant:List.of("gloomy","lively","fiery","icy")) {
                    Block block=VersionBridge.registeredBlock("netherex",variant+"_netherrack");
                    if(block!=null) targets.add(new BlockReplacement(new BlockMatchTest(block),ore.defaultBlockState()));
                }
            }
            var configuration=new OreFeature(targets,Math.max(3,profile.vein_size()));
            int height=realm.equals("nether")?128:256;
            for(int i=0;i<attempts;i++) {
                int x=chunkX*16+random.nextInt(16),y;
                int difference=profile.max_y()-profile.min_y()+1;
                if(difference>0) y=profile.min_y()+random.nextInt(difference);
                else if(difference<0 && profile.min_y()<height && profile.max_y()>0)
                    y=random.nextBoolean()?height-random.nextInt(height-profile.min_y()):1+random.nextInt(profile.max_y());
                else continue; // Reject the old zero-bound RNG configuration.
                int z=chunkZ*16+random.nextInt(16);BlockPos pos=new BlockPos(x,y,z);
                if(!profile.biomes().isEmpty() && !profile.biomes().contains(VersionBridge.biomeId(world,pos))) continue;
                if(profile.special().equals("outer_islands") && Math.abs((long)x)<700 && Math.abs((long)z)<700) continue;
                if(profile.special().equals("high_y") && y<80 && random.nextFloat()>=0.5F) continue;
                if(profile.special().equals("lava_adjacent")) {
                    boolean adjacent=false;BlockPos.MutableBlockPos cursor=pos.mutable();
                    // Preserve the source's cumulative cursor moves.
                    for(Direction direction:Direction.values()) {
                        cursor.move(direction);
                        if(cursor.getX()>>4!=chunkX || cursor.getZ()>>4!=chunkZ) continue;
                        var state=world.getBlockState(cursor);if(state.is(Blocks.LAVA)||state.is(ore)) { adjacent=true;break; }
                    }
                    if(!adjacent && !random.nextBoolean()) continue;
                }
                generated|=configuration.place(world,chunkGenerator,random,pos);
            }
        }
        return generated;
    }
}
