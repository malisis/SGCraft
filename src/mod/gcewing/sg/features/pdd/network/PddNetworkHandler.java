package gcewing.sg.features.pdd.network;

import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.features.pdd.AddressData;
import gcewing.sg.features.pdd.client.gui.PddScreen;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class PddNetworkHandler extends SGChannel {

    protected static BaseDataChannel pddChannel;

    public PddNetworkHandler(String name) {
        super(name);
        pddChannel = this;
    }

    public static void updatePddList(EntityPlayer player) {
        ChannelOutput data = pddChannel.openPlayer(player,"UpdatePdd");
        data.writeBoolean(true);
        data.close();
    }

    @ClientMessageHandler("UpdatePdd")
    public void handleUpdatePddListRequest(EntityPlayer player, ChannelInput data) {
        boolean update = data.readBoolean();
        if (Minecraft.getMinecraft().currentScreen instanceof PddScreen) {
            PddScreen screen = (PddScreen) Minecraft.getMinecraft().currentScreen;
            screen.delayedUpdate();
        }
    }

    public static void sendPddInputToServer(SGBaseTE te, int function, String address) {
        ChannelOutput data = pddChannel.openServer("PddInput");
        writeCoords(data, te);
        data.writeInt(function);
        data.writeUTF(address);
        data.close();
    }

    @ServerMessageHandler("PddInput") public void handlePddInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        int setting = data.readInt();
        SGBaseTE localGate = SGBaseTE.at(player.world, pos);
        String address = data.readUTF();

        if (!SGCraft.hasPermission(player, "sgcraft.gui.pdd")) {
            System.err.println("SGCraft - Hacked Client detected!");
            return;
        }

        if (setting == 1) { // Connect / Dial / Double Click
            if (!localGate.isConnected()) {
                localGate.connect(address, player);
            }
        }
        if (setting == 2) {
            if (localGate.isConnected()) {
                localGate.disconnect(player);
            }
        }
    }

    public static void sendPddEntryUpdateToServer(String name, String address, int index, int unid, boolean locked) {
        ChannelOutput data = pddChannel.openServer("PddInputEntry");
        data.writeUTF(name);
        data.writeUTF(address);
        data.writeInt(index);
        data.writeInt(unid);
        data.writeBoolean(locked);
        data.close();
    }

    @ServerMessageHandler("PddInputEntry")
    public void handlePddEntryUpdateFromClient(EntityPlayer player, ChannelInput data) {
        String name = data.readUTF();
        String address = data.readUTF();
        int index = data.readInt();
        int unid = data.readInt();
        boolean locked = data.readBoolean();

        if (!SGCraft.hasPermission(player, "sgcraft.gui.pdd.edit")) {
            System.err.println("SGCraft - Hacked Client detected!");
            return;
        }

        final ItemStack stack = player.getHeldItemMainhand();
        if (stack != null) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound != null) {
                AddressData.updateAddress(player, compound, unid, name, address, index, locked);
                stack.setTagCompound(compound);
                player.inventoryContainer.detectAndSendChanges();
                PddNetworkHandler.updatePddList(player);
            }
        }
    }
}
