package it.hurts.metallurgy_reforged.network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
/** Server-to-client sends that skip players whose connection never negotiated the channel (fake players, vanilla clients). */
public final class Net {
    public static boolean canReceive(ServerPlayer player,SimpleChannel channel) {return player.connection!=null && player.connection.connection!=null && player.connection.connection.channel()!=null && channel.isRemotePresent(player.connection.connection);}
    public static void send(SimpleChannel channel,ServerPlayer player,Object message) {if(canReceive(player,channel)) channel.send(PacketDistributor.PLAYER.with(()->player),message);}
    private Net() {}
}
