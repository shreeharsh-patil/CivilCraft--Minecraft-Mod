package com.civilcraftai.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.civilcraftai.CivilCraftAI;

public record ClaimBorderSyncPayload(int chunkX, int chunkZ, String townName) implements CustomPayload {
    public static final CustomPayload.Id<ClaimBorderSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(CivilCraftAI.MOD_ID, "claim_border_sync"));

    public static final PacketCodec<PacketByteBuf, ClaimBorderSyncPayload> CODEC = PacketCodec.of(
        (buf, packet) -> {
            buf.writeInt(packet.chunkX);
            buf.writeInt(packet.chunkZ);
            buf.writeString(packet.townName);
        },
        buf -> new ClaimBorderSyncPayload(buf.readInt(), buf.readInt(), buf.readString())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
