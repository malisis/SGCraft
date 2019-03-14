package gcewing.sg.tileentity.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GateAccessData {
    static final String GATEACCESSLIST = "gateaccesslist";
    public static final String ADDRESS = "address";
    private static final String IACCESS = "iaccess";
    private static final String OACCESS = "oaccess";

    private String address;
    private boolean iaccess;
    private boolean oaccess;


    public GateAccessData(final String address, final boolean iaccess, boolean oaccess) {
        this.address = address;
        this.iaccess = iaccess;
        this.oaccess = oaccess;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean hasIncomingAccess() {
        return iaccess;
    }

    public void setIncomingAccess(boolean value) {
        this.iaccess = value;
    }

    public boolean hasOutgoingAccess() {
        return oaccess;
    }

    public void setOutgoingAccess(boolean value) {
        this.oaccess = value;
    }

    public static void updateAddress(NBTTagCompound compound, String address, boolean iaccess, boolean oaccess, int command) {
        checkNotNull(compound);
        final List<GateAccessData> gateAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(GATEACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound gateCompound = list.getCompoundTagAt(i);

            final String compoundEntryAddress = gateCompound.getString(ADDRESS);
            if (compoundEntryAddress.equalsIgnoreCase(address)) {
                gateCompound.setString(ADDRESS, address);
                gateCompound.setBoolean(IACCESS, iaccess);
                gateCompound.setBoolean(OACCESS, oaccess);
            }

            if (command == -1) { // -1 means delete the entry, so skip re-adding it to the list.
                gateAccessData.add(new GateAccessData(gateCompound.getString(ADDRESS), gateCompound.getBoolean(IACCESS), gateCompound.getBoolean(OACCESS)));
            }
        }

        if (command == 1) { // Indicates new request.
            gateAccessData.add(new GateAccessData(address, iaccess, oaccess));
        }

        writeAddresses(compound, gateAccessData);
    }

    public static List<GateAccessData> getGateAccessList(final NBTTagCompound compound) {
        checkNotNull(compound);

        if (!compound.hasKey(GateAccessData.GATEACCESSLIST)) {
            // Todo remove this after testing.....
            System.out.println("getGateAccessList: TE missing GATEACCESSLIST, adding default for testing purposes");
            List<GateAccessData> genericAddressList = Lists.newArrayList();
            genericAddressList.add(new GateAccessData( "T9FH-3VW-VL", true, true));
            GateAccessData.writeAddresses(compound, genericAddressList);
        }

        final List<GateAccessData> gateAccessData = new ArrayList<>();
        final NBTTagList list = compound.getTagList(GATEACCESSLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound addressCompound = list.getCompoundTagAt(i);

            final String address = addressCompound.getString(ADDRESS);
            final boolean iaccess = addressCompound.getBoolean(IACCESS);
            final boolean oaccess = addressCompound.getBoolean(OACCESS);

            gateAccessData.add(new GateAccessData(address, iaccess, oaccess));
        }

        return gateAccessData;
    }

    public static NBTTagCompound writeAddresses(final NBTTagCompound compound, final List<GateAccessData> gateAccessData) {
        checkNotNull(compound);
        checkNotNull(gateAccessData);

        if (gateAccessData.size() == 0) {
            List<GateAccessData> genericAddressList = Lists.newArrayList();
        }

        if (gateAccessData.size() >= 0) {
            final NBTTagList list = new NBTTagList();

            for (final GateAccessData data : gateAccessData) {
                final NBTTagCompound addressCompound = new NBTTagCompound();
                addressCompound.setString(ADDRESS, data.getAddress());
                addressCompound.setBoolean(IACCESS, data.hasIncomingAccess());
                addressCompound.setBoolean(OACCESS, data.hasOutgoingAccess());

                list.appendTag(addressCompound);
            }

            compound.setTag(GATEACCESSLIST, list);
        }

        return compound;
    }
}
