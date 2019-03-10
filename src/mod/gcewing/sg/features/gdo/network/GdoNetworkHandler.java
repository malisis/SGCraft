package gcewing.sg.features.gdo.network;

import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class GdoNetworkHandler extends SGChannel {

    protected static BaseDataChannel gdoChannel;

    public GdoNetworkHandler(String name) {
        super(name);
        gdoChannel = this;
    }

    public static void sendGdoInputToServer(SGBaseTE te, int function) {
        ChannelOutput data = gdoChannel.openServer("GdoInput");
        writeCoords(data, te);
        data.writeInt(function);
        data.close();
    }

    @ServerMessageHandler("GdoInput")
    public void handleGdoInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        int setting = data.readInt();
        SGBaseTE localGate = SGBaseTE.at(player.world, pos);

        if (!SGCraft.hasPermission(player, "sgcraft.gui.gdo")) {
            System.err.println("SGCraft - Hacked Client detected!");
            return;
        }

        boolean canEditLocal = localGate.getWorld().isBlockModifiable(player, localGate.getPos());
        boolean canEditRemote = false;


        if (SGCraft.hasPermission(player, "sgcraft.gui.gdo") && canEditLocal) {
            if (localGate != null) {
                if (setting == 1) localGate.openIris();
                if (setting == 2) localGate.closeIris();
                if (setting == 3) localGate.disconnect(player);

                if (setting == 4 || setting == 5 || setting == 6) {
                    if (localGate.isConnected()) {
                        SGBaseTE remoteGate = localGate.getConnectedStargateTE();
                        canEditRemote = remoteGate.getWorld().isBlockModifiable(player, remoteGate.getPos());

                        if (canEditRemote) {
                            if (setting == 4) remoteGate.openIris();
                            if (setting == 5) remoteGate.closeIris();
                        }
                    }
                }
            }
        }
    }
}
