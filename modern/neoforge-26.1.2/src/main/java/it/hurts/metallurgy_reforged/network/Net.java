package it.hurts.metallurgy_reforged.network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
/** Server-to-client sends that skip players whose connection never negotiated the channel (fake players, vanilla clients). */
public final class Net {
    public static boolean canReceive(ServerPlayer player,CustomPacketPayload.Type<?> type) {return player.connection!=null && player.connection.hasChannel(type);}
    public static void send(ServerPlayer player,CustomPacketPayload payload) {if(canReceive(player,payload.type())) PacketDistributor.sendToPlayer(player,payload);}
    private Net() {}
}
