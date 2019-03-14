package gcewing.sg.tileentity.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerAccessData {
    static final String PLAYERACCESSLIST = "playeraccesslist";
    private static final String PLAYER = "player";
    private static final String UUID = "uuid";
    private static final String ACCESS = "access";
    private static final String IRIS = "iris";
    private static final String ADMIN = "admin";

    private String playerName;
    private final UUID playerUUID;
    private boolean access;
    private boolean iris;
    private boolean admin;


    public PlayerAccessData(final String playerName, final UUID playerUUID, final boolean access, boolean iris, boolean admin) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.access = access;
        this.iris = iris;
        this.admin = admin;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean hasGateAccess() {
        return access;
    }

    public void setGateAccess(boolean value) {
        this.access = value;
    }

    public boolean hasIrisAccess() {
        return iris;
    }

    public void setIrisAccess(boolean value) {
        this.iris = value;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean value) {
        this.admin = value;
    }

    public static void updateAddress(EntityPlayer player, NBTTagCompound compound, UUID uuid, String newPlayer, boolean newAccess, boolean newIris, boolean newAdmin, int command) {
        checkNotNull(compound);
        final List<PlayerAccessData> playerAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(PLAYERACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound accessCompound = list.getCompoundTagAt(i);

            final UUID compoundEntryUnid = accessCompound.getUniqueId(UUID);
            if (compoundEntryUnid == uuid) {
                accessCompound.setString(PLAYER, newPlayer);
                accessCompound.setBoolean(ACCESS, newAccess);
                accessCompound.setBoolean(IRIS, newIris);
                accessCompound.setBoolean(ADMIN, newIris);
            }

            if (command == -1) { // -1 means delete the entry, so skip re-adding it to the list.
                playerAccessData.add(new PlayerAccessData(accessCompound.getString(PLAYER), accessCompound.getUniqueId(UUID), accessCompound.getBoolean(ACCESS), accessCompound.getBoolean(IRIS), accessCompound.getBoolean(ADMIN)));
            }
        }

        if (command == 1) { // Indicates new request.
            playerAccessData.add(new PlayerAccessData(newPlayer, uuid, newAccess, newIris, newAdmin));
        }

        writeAddresses(compound, playerAccessData);
    }

    public static List<PlayerAccessData> getPlayerAccessList(final NBTTagCompound compound) {
        checkNotNull(compound);

        if (!compound.hasKey(PlayerAccessData.PLAYERACCESSLIST)) {
            UUID fakeID = java.util.UUID.randomUUID();

            // Todo remove this after testing.....
            System.out.println("getPlayerAccessList: TE missing PLAYERACCESSLIST, adding default for testing purposes");
            List<PlayerAccessData> genericAddressList = Lists.newArrayList();
            genericAddressList.add(new PlayerAccessData( "TEST", fakeID, true, true, true));
            PlayerAccessData.writeAddresses(compound, genericAddressList);
        }

        final List<PlayerAccessData> playerAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(PLAYERACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);

            final String name = addressCompound.getString(PLAYER);
            final UUID uuid = addressCompound.getUniqueId(UUID);
            final boolean access = addressCompound.getBoolean(ACCESS);
            final boolean iris = addressCompound.getBoolean(IRIS);
            final boolean admin = addressCompound.getBoolean(ADMIN);

            playerAccessData.add(new PlayerAccessData(name, uuid, access, iris, admin));
        }

        return playerAccessData;
    }

    public static NBTTagCompound writeAddresses(final NBTTagCompound compound, final List<PlayerAccessData> playerAccessData) {
        checkNotNull(compound);
        checkNotNull(playerAccessData);

        if (playerAccessData.size() >= 0) {

            final NBTTagList list = new NBTTagList();

            for (final PlayerAccessData data : playerAccessData) {
                final NBTTagCompound addressCompound = new NBTTagCompound();
                addressCompound.setString(PLAYER, data.getPlayerName());
                addressCompound.setUniqueId(UUID, data.getPlayerUUID());
                addressCompound.setBoolean(ACCESS, data.hasGateAccess());
                addressCompound.setBoolean(IRIS, data.hasIrisAccess());
                addressCompound.setBoolean(ADMIN, data.isAdmin());

                list.appendTag(addressCompound);
            }

            compound.setTag(PLAYERACCESSLIST, list);
        }

        return compound;
    }
}
