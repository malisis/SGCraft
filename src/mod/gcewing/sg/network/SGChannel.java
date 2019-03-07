//------------------------------------------------------------------------------------------------
//
//   SG Craft - Packet Handling
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.network;

import gcewing.sg.BaseBlockUtils;
import gcewing.sg.BaseDataChannel;
import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;

public class SGChannel extends BaseDataChannel {

    protected static BaseDataChannel channel;
    
    public SGChannel(String name) {
        super(name);
        channel = this;
//         for (Object h : handlers)
//             System.out.printf("SGChannel: handlers include %s\n", h);
    }

    public static void sendConnectOrDisconnectToServer(SGBaseTE te, String address) {
        ChannelOutput data = channel.openServer("ConnectOrDisconnect");
        writeCoords(data, te);
        data.writeUTF(address);
        data.close();
    }
    
    @ServerMessageHandler("ConnectOrDisconnect")
    public void handleConnectOrDisconnectFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        String address = data.readUTF();
        SGBaseTE te = SGBaseTE.at(player.world, pos);
        if (te != null) {
            //DHDTE dhd = te.getLinkedControllerTE();
            //dhd.immediateDialDHD = true;
            //System.out.println("Set Auto Dial true");
            te.connectOrDisconnect(address, player);
        }
    }
    
    public static void sendClearAddressToServer(DHDTE te) {
        ChannelOutput data = channel.openServer("ClearAddress");
        writeCoords(data, te);
        data.close();
    }
    
    @ServerMessageHandler("ClearAddress")
    public void handleClearAddressFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        DHDTE te = DHDTE.at(player.world, pos);
        if (te != null)
            te.clearAddress();
    }

    public static void sendUnsetSymbolToServer(DHDTE te) {
        ChannelOutput data = channel.openServer("UnsetSymbol");
        writeCoords(data, te);
        data.close();
    }

    @ServerMessageHandler("UnsetSymbol")
    public void handleUnsetSymbolFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        if (player.world.isBlockLoaded(pos)) {
            DHDTE te = DHDTE.at(player.world, pos);
            if (te != null) {
                te.unsetSymbol(player);
            }
        }
    }

    public static void sendEnterSymbolToServer(DHDTE te, char symbol) {
        ChannelOutput data = channel.openServer("EnterSymbol");
        writeCoords(data, te);
        data.writeChar(symbol);
        data.close();
    }

    @ServerMessageHandler("EnterSymbol")
    public void handleEnterSymbolFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        char symbol = data.readChar();
        if (player.world.isBlockLoaded(pos)) {
            DHDTE te = DHDTE.at(player.world, pos);
            if (te != null) {
                te.enterSymbol(player, symbol);
            }
        }
    }
    
    public static void writeCoords(ChannelOutput data, TileEntity te) {
        BaseBlockUtils.writeBlockPos(data, te.getPos());
    }
    
    public BlockPos readCoords(ChannelInput data) {
        return BaseBlockUtils.readBlockPos(data);
    }

    public static void sendGdoInputToServer(SGBaseTE te, int function) {
        ChannelOutput data = channel.openServer("GdoInput");
        writeCoords(data, te);
        data.writeInt(function);
        data.close();
    }

    @ServerMessageHandler("GdoInput")
    public void handleGdoInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        int setting = data.readInt();
        SGBaseTE te = SGBaseTE.at(player.world, pos);

        if (te != null) {
            if (setting == 1) { // Local Open Iris
                te.openIris();
            } else if (setting == 2) { // Local Close Iris
                te.closeIris();
            } else if (setting == 3) { // Local Disconnect Wormhole
                te.disconnect(player);
            } else if (setting == 4) { // Remote Open Iris
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.openIris();
                }
            } else if (setting == 5) { // Remote Close Iris
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.closeIris();
                }
            } else if (setting == 6) { // Remote Disconnect (Not implemented on GUI)
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.disconnect();
                }
            } else if (setting == 7) { // Test button functionality (varies)
                //te.startDiallingStargate("ZFDDUR8", te, true, false);
                //te.connect("PFKCMK3", player,true, false);
                te.connect("ZFDDUR8", player,true, false);
            }
        }
    }

    public static void sendConfiguratorInputToServer(SGBaseTE te, int setting, int a, boolean b, double c) {
        ChannelOutput data = channel.openServer("ConfiguratorInput");
        writeCoords(data, te);
        data.writeInt(setting);
        data.writeInt(a);
        data.writeBoolean(b);
        data.writeDouble(c);
        data.close();
    }

    @ServerMessageHandler("ConfiguratorInput")
    public void handleConfiguratorInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        int setting = data.readInt();
        SGBaseTE te = SGBaseTE.at(player.world, pos);
        int a = data.readInt();
        boolean b = data.readBoolean();
        double c = data.readDouble();

        if (setting <= 0) {
            System.err.println("Cannot process ConfiguratorInput packet, setting value is at or below zero!");
            return;
        }

        System.out.println("Server: Configurator - " + setting + " | " + a + " | " + b + " | " + c);

        if (setting == 1) { // Seconds to Stay Open (int)
            te.secondsToStayOpen = a;
        } if (setting == 2) { // Gate Rotation Speed (double)
            te.ringRotationSpeed = c;
        } else if (setting == 3) { // Energy Buffer Size (double)
            te.maxEnergyBuffer = c;
        } else if (setting == 4) { // Energy Per Fuel Item (double)
            te.energyPerFuelItem = c;
        } else if (setting == 5) { // Gate Openings per Fuel Item (int)
            te.gateOpeningsPerFuelItem = a;
        } else if (setting == 6) { // Distance Factor Multiplier (double)
            te.distanceFactorMultiplier = c;
        } else if (setting == 7) { // Inter-dimensional Multiplier (double)
            te.interDimensionMultiplier = c;
        } else if (setting == 8) { // One-Way Travel (boolean)
            te.oneWayTravel = b;
        } else if (setting == 9) { // Iris Upgrade (boolean)
            te.hasIrisUpgrade = b;
        } else if (setting == 10) { // Chevron Upgrade (boolean)
            te.hasChevronUpgrade = b;
        } else if (setting == 11) { //Pegasus Gate Type
            te.gateType = a;
        } else if (setting == 12) { // Reverse Wormhold Kills
            te.reverseWormholeKills = b;
        } else if (setting == 13) { // Can be Dialed
            //Todo: build this.
        } else if (setting == 14) { // Close from Either End
            te.closeFromEitherEnd = b;
        } else if (setting == 15) { // Preserve Inventory on Iris Death
            te.preserveInventory = b;
        } else if (setting == 16) { // No Input Power Required
            //Todo: build this
        }

        te.markForUpdate(); // Force Client to Update
    }

}
