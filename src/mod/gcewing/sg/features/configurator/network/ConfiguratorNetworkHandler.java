package gcewing.sg.features.configurator.network;

import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class ConfiguratorNetworkHandler extends SGChannel {

    protected static BaseDataChannel configuratorChannel;

    public ConfiguratorNetworkHandler(String name) {
        super(name);
        configuratorChannel = this;
    }

    public static void sendConfiguratorInputToServer(SGBaseTE te, int setting, int a, boolean b, double c) {
        ChannelOutput data = configuratorChannel.openServer("ConfiguratorInput");
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
        } else if (setting == 16 && SGCraft.hasPermission(player, "sgcraft.configurator.noPowerRequired")) { // No Input Power Required
            te.requiresNoPower = b;
        } else if (setting == 17 && SGCraft.hasPermission(player, "sgcraft.configurator.chevronsLockOnDial")) { // Chevrons lock when dialed
            te.chevronsLockOnDial = b;
        } else if (setting == 18 && SGCraft.hasPermission(player, "sgcraft.configurator.returnToPreviousIrisState")) {
            te.returnToPreviousIrisState = b;
        } else if (setting == 19 && SGCraft.hasPermission(player, "sgcraft.configurator.transientDamage")) {
            te.transientDamage = b;
        } else if (setting == 20 && SGCraft.hasPermission(player, "sgcraft.configurator.transparency")) {
            te.transparency = b;
        }

        if (setting == 20) { // Always the last packet to refresh the TE
            player.sendMessage(new TextComponentString("Changes Saved!"));
            te.markForUpdate(); // Force Client to Update but only after the last packet
        }
    }
}
