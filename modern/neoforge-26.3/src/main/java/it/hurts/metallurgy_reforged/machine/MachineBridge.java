package it.hurts.metallurgy_reforged.machine;
import it.hurts.metallurgy_reforged.effect.VersionBridge;
import it.hurts.metallurgy_reforged.content.Content;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
public final class MachineBridge {
    public static int fuel(Level level,ItemStack stack,net.minecraft.world.Container container) {
        if(!(level instanceof net.minecraft.server.level.ServerLevel server) || !(container instanceof net.minecraft.world.level.block.entity.BlockEntity block))
            return stack.has(net.minecraft.core.component.DataComponents.COOKING_FUEL)?1:0;
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(server)
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,block.getBlockState())
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY,block)
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.atCenterOf(block.getBlockPos()))
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.CONTAINER,container)
            .withOptionalParameter(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK,stack.isEmpty()?null:stack)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CONTAINER_PROCESS);
        var context=new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
        return net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.getFromItem(stack,
            net.minecraft.core.component.DataComponents.COOKING_FUEL,net.minecraft.world.item.component.CookingFuel::burnTime,context,0);
    }
    public static ItemStack remainder(ItemStack stack) { return stack.getCraftingRemainder()==null?ItemStack.EMPTY:stack.getCraftingRemainder().create(); }
    public static void sound(MachineEntity machine,String suffix) {
        var sound=Content.SOUND_MAP.get((machine.alloy?"alloyer":"crusher")+"_"+suffix);
        if(sound!=null) machine.getLevel().playSound(null,machine.getBlockPos(),sound.get(),SoundSource.BLOCKS,1,1);
    }
    public static void experience(Player player,ItemStack output,int count,float amount) {
        if(player.level().isClientSide()) return;
        int xp=(int)amount;
        if(Math.random()<amount-xp) xp++;
        while(xp>0) { int split=ExperienceOrb.getExperienceValue(xp);xp-=split;player.level().addFreshEntity(new ExperienceOrb(player.level(),player.getX(),player.getY()+0.5,player.getZ()+0.5,split)); }
        output.onCraftedBy(player,count);
        net.neoforged.neoforge.event.EventHooks.firePlayerSmeltedEvent(player,output,count);
    }
    private MachineBridge() {}
}
