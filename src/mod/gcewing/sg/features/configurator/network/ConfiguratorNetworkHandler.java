package gcewing.sg.features.configurator.network;

import gcewing.sg.BaseDataChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.tileentity.data.GateAccessData;
import gcewing.sg.tileentity.data.PlayerAccessData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Optional;

public class ConfiguratorNetworkHandler extends SGChannel {

    protected static BaseDataChannel configuratorChannel;

    public ConfiguratorNetworkHandler(String name) {
        super(name);
        configuratorChannel = this;
    }

    public static void sendConfiguratorInputToServer(SGBaseTE te, int secondsToStayOpen, double ringRotationSpeed, double maxEnergyBuffer, double energyPerFuelItem, int gateOpeningsPerFuelItem,
        double distanceFactorMultiplier, double interDimensionalMultiplier, boolean oneWayTravel, boolean hasIrisUpgrade, boolean hasChevronUpgrade, int gateType, boolean reverseWormholeKills,
        boolean closeFromEitherEnd, boolean preserveInventory, boolean requiresNoPower, boolean chevronsLockOnDial, boolean returnToPreviousIrisState, boolean transientDamage,
        boolean transparency, int orientation, boolean useDHDFuelSource) {

        ChannelOutput data = configuratorChannel.openServer("ConfiguratorInput");
        writeCoords(data, te);
        data.writeInt(secondsToStayOpen);
        data.writeDouble(ringRotationSpeed);
        data.writeDouble(maxEnergyBuffer);
        data.writeDouble(energyPerFuelItem);
        data.writeInt(gateOpeningsPerFuelItem);
        data.writeDouble(distanceFactorMultiplier);
        data.writeDouble(interDimensionalMultiplier);
        data.writeBoolean(oneWayTravel);
        data.writeBoolean(hasIrisUpgrade);
        data.writeBoolean(hasChevronUpgrade);
        data.writeInt(gateType);
        data.writeBoolean(reverseWormholeKills);
        data.writeBoolean(closeFromEitherEnd);
        data.writeBoolean(preserveInventory);
        data.writeBoolean(requiresNoPower);
        data.writeBoolean(chevronsLockOnDial);
        data.writeBoolean(returnToPreviousIrisState);
        data.writeBoolean(transientDamage);
        data.writeBoolean(transparency);
        data.writeInt(orientation);
        data.writeBoolean(useDHDFuelSource);
        data.close();
    }

    @ServerMessageHandler("ConfiguratorInput")
    public void handleConfiguratorInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE te = SGBaseTE.at(player.world, pos);
        String playerName = player.getName();

        int secondsToStayOpen = data.readInt();
        double ringRotationSpeed = data.readDouble();
        double maxEnergyBuffer = data.readDouble();
        double energyPerFuelItem = data.readDouble();
        int gateOpeningsPerFuelItem = data.readInt();
        double distanceFromMultiplier = data.readDouble();
        double interDimensionalMultiplier = data.readDouble();
        boolean oneWayTravel = data.readBoolean();
        boolean hasIrisUpgrade = data.readBoolean();
        boolean hasChevronUpgrade = data.readBoolean();
        int gateType = data.readInt();
        boolean reverseWormholeKills = data.readBoolean();
        boolean closeFromEitherEnd = data.readBoolean();
        boolean preserveInventory = data.readBoolean();
        boolean requiresNoPower = data.readBoolean();
        boolean chevronsLockOnDial = data.readBoolean();
        boolean returnToPreviousIrisState = data.readBoolean();
        boolean transientDamage = data.readBoolean();
        boolean transparency = data.readBoolean();
        int orientation = data.readInt();
        boolean useDHDFuelSource = data.readBoolean();

        boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

        if (SGCraft.hasPermission(player, "sgcraft.configurator") && te.allowAdminAccess(playerName) || isPermissionsAdmin) {
            if (SGCraft.hasPermission(player, "sgcraft.configurator.secondsToStayOpen")) te.secondsToStayOpen = secondsToStayOpen;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.ringRotationSpeed")) te.ringRotationSpeed = ringRotationSpeed;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.maxEnergyBuffer")) te.maxEnergyBuffer = maxEnergyBuffer;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.energyPerFuelItem")) te.energyPerFuelItem = energyPerFuelItem;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.gateOpeningsPerFuelItem")) te.gateOpeningsPerFuelItem = gateOpeningsPerFuelItem;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.distanceFactorMultiplier")) te.distanceFactorMultiplier = distanceFromMultiplier;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.interDimensionalMultiplier")) te.interDimensionMultiplier = interDimensionalMultiplier;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.oneWayTravel")) te.oneWayTravel = oneWayTravel;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.hasIrisUpgrade")) te.hasIrisUpgrade = hasIrisUpgrade;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.hasChevronUpgrade")) te.hasChevronUpgrade = hasChevronUpgrade;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.gateType")) te.gateType = gateType;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.reverseWormholeKills")) te.reverseWormholeKills = reverseWormholeKills;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.closeFromEitherEnd")) te.closeFromEitherEnd = closeFromEitherEnd;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.preserveInventory")) te.preserveInventory = preserveInventory;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.noPowerRequired")) te.requiresNoPower = requiresNoPower;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.chevronsLockOnDial")) te.chevronsLockOnDial = chevronsLockOnDial;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.returnToPreviousIrisState")) te.returnToPreviousIrisState = returnToPreviousIrisState;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.transientDamage")) te.transientDamage = transientDamage;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.transparency")) te.transparency = transparency;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.orientation")) te.gateOrientation = orientation;
            if (SGCraft.hasPermission(player, "sgcraft.configurator.useDHDFuelSource")) te.useDHDFuelSource = useDHDFuelSource;
            player.sendMessage(new TextComponentString("Changes Saved!"));
            te.markForUpdate();
        }
    }

    public static void sendGateAddressAccessInputToServer(SGBaseTE te, String address, boolean defaultAllowIncoming, boolean defaultAllowOutgoing, boolean allowIncoming, boolean allowOutgoing) {
        ChannelOutput data = configuratorChannel.openServer("GateAddressAccessInput");
        writeCoords(data, te);
        data.writeUTF(address);
        data.writeBoolean(defaultAllowIncoming);
        data.writeBoolean(defaultAllowOutgoing);
        data.writeBoolean(allowIncoming);
        data.writeBoolean(allowOutgoing);
        data.close();
    }

    @ServerMessageHandler("GateAddressAccessInput")
    public void handleGateAddressAccessInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE te = SGBaseTE.at(player.world, pos);

        String address = data.readUTF();
        String playerName = player.getName();
        boolean defaultAllowIncoming = data.readBoolean();
        boolean defaultAllowOutgoing = data.readBoolean();
        boolean allowIncoming = data.readBoolean();
        boolean allowOutgoing = data.readBoolean();

        if (te == null) {
            System.err.println("SGCraft - HandleGAA Exception - SGBaseTE disappeared");
            return;
        }

        boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

        if (SGCraft.hasPermission(player, "sgcraft.configurator") && te.allowAdminAccess(playerName) || isPermissionsAdmin) {
            if (address.isEmpty()) { // indicates the user clicked the bottom save button
                te.defaultAllowIncoming = defaultAllowIncoming;
                te.defaultAllowOutgoing = defaultAllowOutgoing;
            }

            if (!address.isEmpty()) {
                te.setAllowIncomingAddress(address, allowIncoming);
                te.setAllowOutgoingAddress(address, allowOutgoing);
            }
        }

        te.markForUpdate();
    }

    public static void sendPlayerAccessInputToServer(SGBaseTE te, String playerName, boolean defaultAllowAccess, boolean defaultAllowIris, boolean defaultAllowAdmin, boolean allowAccess, boolean allowIris, boolean allowAdmin) {
        ChannelOutput data = configuratorChannel.openServer("PlayerAccessInput");
        writeCoords(data, te);
        data.writeUTF(playerName);
        data.writeBoolean(defaultAllowAccess);
        data.writeBoolean(defaultAllowIris);
        data.writeBoolean(defaultAllowAdmin);
        data.writeBoolean(allowAccess);
        data.writeBoolean(allowIris);
        data.writeBoolean(allowAdmin);
        data.close();
    }

    @ServerMessageHandler("PlayerAccessInput")
    public void handlePlayerAccessInputFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE te = SGBaseTE.at(player.world, pos);

        String playerName = data.readUTF();
        boolean defaultAllowAccess = data.readBoolean();
        boolean defaultAllowIris = data.readBoolean();
        boolean defaultAllowAdmin = data.readBoolean();
        boolean allowAccess = data.readBoolean();
        boolean allowIris = data.readBoolean();
        boolean allowAdmin = data.readBoolean();

        if (te == null) {
            System.err.println("SGCraft - HandleGAA Exception - SGBaseTE disappeared");
            return;
        }

        if (SGCraft.hasPermission(player, "sgcraft.configurator") && te.allowAdminAccess(playerName)) {
            if (playerName.isEmpty()) {
                te.defaultAllowGateAccess = defaultAllowAccess;
                te.defaultAllowIrisAccess = defaultAllowIris;
                te.defaultAllowAdminAccess = defaultAllowAdmin;
            }

            te.setAllowGateAccessAccess(playerName, allowAccess);
            te.setAllowAccessToIrisController(playerName, allowIris);
            te.setAllowAccessAdmin(playerName, allowAdmin);

            te.markForUpdate();
        }
    }

    public static void sendGAAEntryUpdateToServer(SGBaseTE te, String oldAddress, String newAddress, int function) {
        ChannelOutput data = configuratorChannel.openServer("GAAInputEntry");
        writeCoords(data, te);
        data.writeUTF(oldAddress);
        data.writeUTF(newAddress);
        data.writeInt(function);
        data.close();
    }

    @ServerMessageHandler("GAAInputEntry")
    public void handleGAAEntryUpdateFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE localGate = SGBaseTE.at(player.world, pos);
        String playerName = player.getName();
        String oldAddress = data.readUTF();
        String newAddress = data.readUTF();
        int function = data.readInt();

        if (localGate == null) {
            System.err.println("SGCraft - HandleGAA Exception - SGBaseTE disappeared");
            return;
        }
        boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

        if (SGCraft.hasPermission(player, "sgcraft.configurator") && localGate.allowAdminAccess(playerName) || isPermissionsAdmin) {
            if (localGate.getGateAccessData() != null) {
                if (oldAddress.isEmpty() && function == 1) {
                    localGate.getGateAccessData().add(new GateAccessData(newAddress, true, true));
                }

                if (localGate.getGateAccessData() != null) {
                    Optional<GateAccessData> gateAccessEntry = localGate.getGateAccessData().stream().filter(g -> g.getAddress().equalsIgnoreCase(oldAddress)).findFirst();
                    if (gateAccessEntry.isPresent()) {
                        if (!oldAddress.isEmpty() && !newAddress.isEmpty() && function == 2) {
                            gateAccessEntry.get().setAddress(newAddress);
                        }

                        if (!oldAddress.isEmpty() && function == 3) {
                            // Delete
                            localGate.getGateAccessData().remove(gateAccessEntry.get());
                        }
                    }
                }

                localGate.markChanged();
            } else {
                System.out.println("Exception in handleGAAEntryUpdateFromClient Handler");
            }
        }
    }

    public static void sendPAEntryUpdateToServer(SGBaseTE te, String oldName, String newName, int function) {
        ChannelOutput data = configuratorChannel.openServer("PAInputEntry");
        writeCoords(data, te);
        data.writeUTF(oldName);
        data.writeUTF(newName);
        data.writeInt(function);
        data.close();
    }

    @ServerMessageHandler("PAInputEntry")
    public void handlePAEntryUpdateFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        SGBaseTE localGate = SGBaseTE.at(player.world, pos);
        String playerName = player.getName();
        String oldName = data.readUTF();
        String newName = data.readUTF();
        int function = data.readInt();

        if (localGate == null) {
            System.err.println("SGCraft - HandleGAA Exception - SGBaseTE disappeared");
            return;
        }

        boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

        if (SGCraft.hasPermission(player, "sgcraft.configurator") && localGate.allowAdminAccess(playerName) || isPermissionsAdmin) {
            if (localGate.getPlayerAccessData() != null) {
                if (oldName.isEmpty() && function == 1) {
                    localGate.getPlayerAccessData().add(new PlayerAccessData(newName, true, true, player.getName().equalsIgnoreCase(newName)));
                }

                if (localGate.getPlayerAccessData() != null) {
                    Optional<PlayerAccessData> playerAccessEntry = localGate.getPlayerAccessData().stream().filter(g -> g.getPlayerName().equalsIgnoreCase(oldName)).findFirst();
                    if (playerAccessEntry.isPresent()) {
                        if (!oldName.isEmpty() && !newName.isEmpty() && function == 2) {
                            playerAccessEntry.get().setPlayerName(newName);
                        }

                        if (!oldName.isEmpty() && function == 3) {
                            // Delete
                            localGate.getPlayerAccessData().remove(playerAccessEntry.get());
                        }
                    }
                }

                localGate.markChanged();
            } else {
                System.out.println("Exception in handlePAEntryUpdateFromClient Handler");
                // Todo: throw exception
            }
        }
    }
}
