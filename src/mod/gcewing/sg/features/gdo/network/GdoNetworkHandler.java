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

        boolean playerAccessLocalControl = true;
        boolean playerAccessRemoteControl = true;

        boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

        if (SGCraft.hasPermission(player, "sgcraft.gui.gdo") || isPermissionsAdmin) {
            if (localGate != null) {
                    if (!localGate.allowAccessToIrisController(player.getName())) {
                        playerAccessLocalControl = false;
                    }
                    if (playerAccessLocalControl || isPermissionsAdmin) {
                        if (setting == 1) {
                            localGate.openIris();
                        }
                        if (setting == 2) {
                            localGate.closeIris();
                        }
                    }

                if (setting == 3) localGate.disconnect(player);

                if (setting == 4 || setting == 5 || setting == 6) {
                    if (localGate.isConnected()) {
                        SGBaseTE remoteGate = localGate.getConnectedStargateTE();
                        if (remoteGate != null) {
                            if (!remoteGate.allowAccessToIrisController(player.getName())) {
                                playerAccessRemoteControl = false;
                            }
                            if (playerAccessRemoteControl || isPermissionsAdmin) {
                                if (setting == 4) {
                                    remoteGate.openIris();
                                }
                                if (setting == 5) {
                                    remoteGate.closeIris();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
