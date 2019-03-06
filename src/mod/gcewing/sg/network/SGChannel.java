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
        int function = data.readInt();
        SGBaseTE te = SGBaseTE.at(player.world, pos);

        if (te != null) {
            if (function == 1) { // Local Open Iris
                te.openIris();
            } else if (function == 2) { // Local Close Iris
                te.closeIris();
            } else if (function == 3) { // Local Disconnect Wormhole
                te.disconnect(player);
            } else if (function == 4) { // Remote Open Iris
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.openIris();
                }
            } else if (function == 5) { // Remote Close Iris
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.closeIris();
                }
            } else if (function == 6) { // Remote Disconnect (Not implemented on GUI)
                if (te.isConnected()) {
                    SGBaseTE remoteGate = te.getConnectedStargateTE();
                    remoteGate.disconnect();
                }
            } else if (function == 7) { // Test button functionality (varies)
                //te.startDiallingStargate("ZFDDUR8", te, true, false);
                //te.connect("PFKCMK3", player,true, false);
                te.connect("ZFDDUR8", player,false, false);
            }
        }
    }
}
