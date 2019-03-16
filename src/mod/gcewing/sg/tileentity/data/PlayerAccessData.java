package gcewing.sg.tileentity.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import gcewing.sg.SGCraft;
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
    private static final String ACCESS = "access";
    private static final String IRIS = "iris";
    private static final String ADMIN = "admin";

    private String playerName;
    private boolean access;
    private boolean iris;
    private boolean admin;


    public PlayerAccessData(final String playerName, final boolean access, boolean iris, boolean admin) {
        this.playerName = playerName;
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

    public static void updatePlayer(EntityPlayer player, NBTTagCompound compound, String oldPlayerName, String newPlayerName, boolean newAccess, boolean newIris, boolean newAdmin, int command) {
        checkNotNull(compound);
        final List<PlayerAccessData> playerAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(PLAYERACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound accessCompound = list.getCompoundTagAt(i);

            final String compoundEntryPlayerName = accessCompound.getString(PLAYER);
            if (compoundEntryPlayerName.equalsIgnoreCase(oldPlayerName)) {
                accessCompound.setString(PLAYER, newPlayerName);
                accessCompound.setBoolean(ACCESS, newAccess);
                accessCompound.setBoolean(IRIS, newIris);
                accessCompound.setBoolean(ADMIN, newIris);
            }

            if (command == -1) { // -1 means delete the entry, so skip re-adding it to the list.
                //playerAccessData.add(new PlayerAccessData(accessCompound.getString(PLAYER), accessCompound.getUniqueId(UUID), accessCompound.getBoolean(ACCESS), accessCompound.getBoolean(IRIS), accessCompound.getBoolean(ADMIN)));
            }
        }

        if (command == 1) { // Indicates new request.
            playerAccessData.add(new PlayerAccessData(newPlayerName, newAccess, newIris, newAdmin));
        }

        writeAddresses(compound, playerAccessData);
    }

    public static List<PlayerAccessData> getPlayerAccessList(final NBTTagCompound compound) {
        checkNotNull(compound);

        if (!compound.hasKey(PlayerAccessData.PLAYERACCESSLIST)) {
            // Todo remove this after testing.....
            System.out.println("getPlayerAccessList: TE missing PLAYERACCESSLIST, adding default for testing purposes");
            List<PlayerAccessData> genericAddressList = Lists.newArrayList();
            genericAddressList.add(new PlayerAccessData( "Dockter", true, true, true));
            genericAddressList.add(new PlayerAccessData( "Wifee", true, true, true));
            genericAddressList.add(new PlayerAccessData( "ceredwin", true, true, false));
            genericAddressList.add(new PlayerAccessData( "TB", true, true, false));

            PlayerAccessData.writeAddresses(compound, genericAddressList);
        }

        final List<PlayerAccessData> playerAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(PLAYERACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);

            final String name = addressCompound.getString(PLAYER);
            final boolean access = addressCompound.getBoolean(ACCESS);
            final boolean iris = addressCompound.getBoolean(IRIS);
            final boolean admin = addressCompound.getBoolean(ADMIN);

            playerAccessData.add(new PlayerAccessData(name, access, iris, admin));
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
