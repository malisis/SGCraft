package gcewing.sg.network;

import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.features.configurator.client.gui.ConfiguratorScreen;
import gcewing.sg.features.gdo.client.gui.GdoScreen;
import gcewing.sg.features.pdd.client.gui.PddScreen;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class GuiNetworkHandler extends SGChannel {

    protected static BaseDataChannel guiChannel;

    public GuiNetworkHandler(String name) {
        super(name);
        guiChannel = this;
    }

    public static void sendGuiRequestToServer(SGBaseTE te, EntityPlayer player, int guiType) {
        ChannelOutput data = guiChannel.openServer("requestGUI");
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
        ChannelOutput data = guiChannel.openPlayer(player,"OpenGUI");
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
        boolean canAccessLocal = data.readBoolean();
        boolean canAccessRemote = data.readBoolean();

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
                new GdoScreen(player, player.world, isAdmin, r_connected, r_hasIrisUpgrade, r_hasChevronUpgrade, r_isIrisClosed, r_gateType, r_address, canAccessLocal, canAccessRemote).display();
            }
        }
        if (guiType == 3) {
            new PddScreen(player, player.world, isAdmin).display();
        }
    }
}
