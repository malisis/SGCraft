//------------------------------------------------------------------------------------------------
//
//   SG Craft - Packet Handling
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.network;

import gcewing.sg.BaseBlockUtils;
import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.features.configurator.client.gui.ConfiguratorScreen;
import gcewing.sg.features.gdo.client.gui.GdoScreen;
import gcewing.sg.features.pdd.client.gui.PddScreen;
import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @ServerMessageHandler("ConnectOrDisconnect") public void handleConnectOrDisconnectFromClient(EntityPlayer player, ChannelInput data) {
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

    @ServerMessageHandler("ClearAddress") public void handleClearAddressFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        DHDTE te = DHDTE.at(player.world, pos);
        if (te != null) te.clearAddress();
    }

    public static void sendUnsetSymbolToServer(DHDTE te) {
        ChannelOutput data = channel.openServer("UnsetSymbol");
        writeCoords(data, te);
        data.close();
    }

    @ServerMessageHandler("UnsetSymbol") public void handleUnsetSymbolFromClient(EntityPlayer player, ChannelInput data) {
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

    @ServerMessageHandler("EnterSymbol") public void handleEnterSymbolFromClient(EntityPlayer player, ChannelInput data) {
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

    public static void sendPddInputToServer(SGBaseTE te, int function, String address) {
        ChannelOutput data = channel.openServer("PddInput");
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

    public static void sendGdoInputToServer(SGBaseTE te, int function) {
        ChannelOutput data = channel.openServer("GdoInput");
        writeCoords(data, te);
        data.writeInt(function);
        data.close();
    }

    @ServerMessageHandler("GdoInput") public void handleGdoInputFromClient(EntityPlayer player, ChannelInput data) {
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

                if (setting == 7) { // Test button functionality (varies)
                    localGate.connect("ZFDDUR8", player);
                }
            }
        }
    }

    public static void sendGuiRequestToServer(SGBaseTE te, EntityPlayer player, int guiType) {
        ChannelOutput data = channel.openServer("requestGUI");
        writeCoords(data, te);
        data.writeInt(guiType);
        data.close();
    }

    @ServerMessageHandler("requestGUI")
    public void handleGUIRequestFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE localGate = SGBaseTE.at(player.world, pos);
        boolean canEditLocal = localGate.getWorld().isBlockModifiable(player, localGate.getPos());
        boolean canEditRemote = false;
        if (localGate.isConnected() && localGate.state == SGState.Connected) {
            SGBaseTE remoteGate = localGate.getConnectedStargateTE();
            canEditRemote = remoteGate.getWorld().isBlockModifiable(player, remoteGate.getPos());
        }
        int guiType = data.readInt();
        if (guiType == 1) {
            if (SGCraft.hasPermission(player, "sgcraft.gui.configurator")) {
                if (canEditLocal) {
                    this.openGuiAtClient(localGate, player, 1, SGCraft.hasPermission(player, "sgcraft.admin"), canEditLocal, canEditRemote);
                } else {
                    player.sendMessage(new TextComponentString("Insufficient block permissions!"));
                }
            } else {
                player.sendMessage(new TextComponentString("Insufficient permissions!  Requires 'sgcraft.gui.configurator'"));
            }
        }

        if (guiType == 2) {
            if (SGCraft.hasPermission(player, "sgcraft.gui.gdo")) {
                this.openGuiAtClient(localGate, player, 2, SGCraft.hasPermission(player, "sgcraft.admin"), canEditLocal, canEditRemote);
            } else {
                player.sendMessage(new TextComponentString("Insufficient permissions!  Requires 'sgcraft.gui.gdo'"));
            }
        }

    }

    public static void openGuiAtClient(SGBaseTE te, EntityPlayer player, int guiType, boolean isAdmin, boolean canEditLocal, boolean canEditRemote) {
        ChannelOutput data = channel.openPlayer(player,"OpenGUI");
        writeCoords(data, te);
        data.writeInt(guiType);
        data.writeBoolean(isAdmin);
        data.writeBoolean(canEditLocal);
        data.writeBoolean(canEditRemote);
        if (guiType == 2) {
            data.writeBoolean(te.isConnected() && te.state == SGState.Connected);
            if (te.isConnected() && te.state == SGState.Connected) {
                SGBaseTE remoteGate = te.getConnectedStargateTE();
                data.writeBoolean(remoteGate.hasIrisUpgrade);
                data.writeBoolean(remoteGate.hasChevronUpgrade);
                data.writeBoolean(remoteGate.irisIsClosed());
                data.writeInt(remoteGate.gateType);
                data.writeUTF(SGAddressing.formatAddress(((SGBaseTE) remoteGate).homeAddress, "-", "-"));
            }
        }
        data.close();
    }

    @ClientMessageHandler("OpenGUI")
    public void handleGuiOpenRequest(EntityPlayer player, ChannelInput data) {

        BlockPos pos = readCoords(data);
        int guiType = data.readInt();
        boolean isAdmin = data.readBoolean();
        boolean canEditLocal = data.readBoolean();
        boolean canEditRemote = data.readBoolean();
        System.out.println("Handler GUI Request: Type: "+ guiType + " isAdmin: " + isAdmin + " canEditLocal: " + canEditLocal + " canEditRemote: " + canEditRemote);
        if (guiType == 1) {
            new ConfiguratorScreen(player, player.world, isAdmin).display();
        }
        if (guiType == 2) {
            boolean r_connected = data.readBoolean();
            boolean r_hasIrisUpgrade = false;
            boolean r_hasChevronUpgrade = false;
            boolean r_isIrisClosed = false;
            int r_gateType = 1;
            String r_address = "";
            if (r_connected) {
                r_hasIrisUpgrade = data.readBoolean();
                r_hasChevronUpgrade = data.readBoolean();
                r_isIrisClosed = data.readBoolean();
                r_gateType = data.readInt();
                r_address = data.readUTF();
            }

            if (Minecraft.getMinecraft().currentScreen instanceof GdoScreen) {
                GdoScreen screen = (GdoScreen)Minecraft.getMinecraft().currentScreen;
                screen.isRemoteConnected = r_connected;
                screen.r_hasIrisUpgrade = r_hasIrisUpgrade;
                screen.r_hasChevronUpgrade = r_hasIrisUpgrade;
                screen.r_isIrisClosed = r_isIrisClosed;
                screen.r_gateType = r_gateType;
                screen.r_address = r_address;
            } else {
                new GdoScreen(player, player.world, isAdmin, r_connected, r_hasIrisUpgrade, r_hasChevronUpgrade, r_isIrisClosed, r_gateType, r_address).display();
            }
        }
        if (guiType == 3) {
            new PddScreen(player, player.world, isAdmin).display();
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

        if (setting == 1 && SGCraft.hasPermission(player, "sgcraft.configurator.secondsToStayOpen")) { // Seconds to Stay Open (int)
            te.secondsToStayOpen = a;
        } if (setting == 2 && SGCraft.hasPermission(player, "sgcraft.configurator.ringRotationSpeed")) { // Gate Rotation Speed (double)
            te.ringRotationSpeed = c;
        } else if (setting == 3 && SGCraft.hasPermission(player, "sgcraft.configurator.maxEnergyBuffer")) { // Energy Buffer Size (double)
            te.maxEnergyBuffer = c;
        } else if (setting == 4 && SGCraft.hasPermission(player, "sgcraft.configurator.energyPerFuelItem")) { // Energy Per Fuel Item (double)
            te.energyPerFuelItem = c;
        } else if (setting == 5 && SGCraft.hasPermission(player, "sgcraft.configurator.gateOpeningsPerFuelItem")) { // Gate Openings per Fuel Item (int)
            te.gateOpeningsPerFuelItem = a;
        } else if (setting == 6 && SGCraft.hasPermission(player, "sgcraft.configurator.distanceFactorMultiplier")) { // Distance Factor Multiplier (double)
            te.distanceFactorMultiplier = c;
        } else if (setting == 7 && SGCraft.hasPermission(player, "sgcraft.configurator.interDimensionalMultiplier")) { // Inter-dimensional Multiplier (double)
            te.interDimensionMultiplier = c;
        } else if (setting == 8 && SGCraft.hasPermission(player, "sgcraft.configurator.oneWayTravel")) { // One-Way Travel (boolean)
            te.oneWayTravel = b;
        } else if (setting == 9 && SGCraft.hasPermission(player, "sgcraft.configurator.hasIrisUpgrade")) { // Iris Upgrade (boolean)
            te.hasIrisUpgrade = b;
        } else if (setting == 10 && SGCraft.hasPermission(player, "sgcraft.configurator.hasChevronUpgrade")) { // Chevron Upgrade (boolean)
            te.hasChevronUpgrade = b;
        } else if (setting == 11 && SGCraft.hasPermission(player, "sgcraft.configurator.gateType")) { //Pegasus Gate Type
            te.gateType = a;
        } else if (setting == 12 && SGCraft.hasPermission(player, "sgcraft.configurator.reverseWormholeKills")) { // Reverse Wormhold Kills
            te.reverseWormholeKills = b;
        } else if (setting == 13 && SGCraft.hasPermission(player, "sgcraft.configurator.acceptIncomingConnections")) { // Gate accepts incoming connections
            te.acceptIncomingConnections = b;
        } else if (setting == 14 && SGCraft.hasPermission(player, "sgcraft.configurator.closeFromEitherEnd")) { // Close from Either End
            te.closeFromEitherEnd = b;
        } else if (setting == 15 && SGCraft.hasPermission(player, "sgcraft.configurator.preserveInventory")) { // Preserve Inventory on Iris Death
            te.preserveInventory = b;
        } else if (setting == 16) { // No Input Power Required
            te.requiresNoPower = b;
        } else if (setting == 17 && SGCraft.hasPermission(player, "sgcraft.configurator.chevronsLockOnDial")) { // Chevrons lock when dialed
            te.chevronsLockOnDial = b;
        } else if (setting == 18 && SGCraft.hasPermission(player, "sgcraft.configurator.returnToPreviousIrisState")) {
            te.returnToPreviousIrisState = b;
        }

        if (setting == 18) { // Always the last packet to refresh the TE
            te.markForUpdate(); // Force Client to Update but only after the last packet
        }
    }
}
