package gcewing.sg.features.zpm;

import static gcewing.sg.BaseUtils.min;

import gcewing.sg.BaseTileInventory;
import gcewing.sg.SGCraft;
import gcewing.sg.interfaces.ISGEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ZpmConsoleTE extends BaseTileInventory implements ISGEnergySource, IEnergyStorage, IInventory, ITickable {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    // Todo: specify console buffer
    private EnergyStorage storage = new EnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

    @CapabilityInject(IEnergyStorage.class)
    static Capability<IItemHandler> ENERGY = null;

    public static final int firstZpmSlot = 0;
    public static final int numZpmSlots = 1;
    public static final int numSlots = numZpmSlots; // future usage > 1
    public boolean loaded = false;
    private int update = 0;

    public double energyPerSGEnergyUnit = 0;  //  ??


    public ZpmConsoleTE() {}


    /* TileEntity */

    @Override
    public void readContentsFromNBT(NBTTagCompound nbttagcompound) {
        super.readContentsFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("capacity")) {
            int capacity = nbttagcompound.getInteger("capacity");
            int energy = nbttagcompound.getInteger("energy");
            this.storage = new EnergyStorage(capacity, capacity, capacity, energy);
        }


        // Check if Admin is trying to update all the DHD's with new values.
        if (SGCraft.forceRFCfgUpdate) {
            // energyMax = SGCraft.Ic2MaxEnergyBuffer;
            // energyPerSGEnergyUnit = SGCraft.Ic2euPerSGEnergyUnit;
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        return compound;
    }


    @Override
    public void invalidate() {
        super.invalidate(); // this is important for mc!
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbttagcompound) {
        super.writeContentsToNBT(nbttagcompound);
        nbttagcompound.setInteger("capacity", this.storage.getMaxEnergyStored());
        nbttagcompound.setInteger("energy", this.storage.getEnergyStored());
    }

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        if (update++ > 20) {
            update = 0;
        }
    }

    @Override
    @Nonnull
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        final NBTTagCompound result = new NBTTagCompound();
        this.writeToNBT(result);
        return result;
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet) {
        final NBTTagCompound tag = packet.getNbtCompound();
        this.readFromNBT(tag);
    }

    /* Inventory */

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    protected IInventory getInventory() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        for(final ItemStack item : this.items) {
            if(!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        return this.items.get(0);
    }

    @Override
    public ItemStack removeStackFromSlot(final int index) {
        final ItemStack item = ItemStackHelper.getAndSplit(this.items, 0, 1);
        NBTTagCompound tag = item.getTagCompound();

        if(tag == null) {
            tag = new NBTTagCompound();
            item.setTagCompound(tag);
        }

        if(tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
            tag.setDouble(ZPMItem.ENERGY, this.storage.getEnergyStored());
            tag.setBoolean(ZPMItem.LOADED, false);
            this.storage.extractEnergy(this.storage.getEnergyStored(), false); // Empty the storage when the ZPM is removed.
        }
        this.loaded = false;
        return ItemStackHelper.getAndRemove(this.items, 0);
    }

    @Override
    public ItemStack decrStackSize(final int index, final int quantity) {
        final ItemStack item = ItemStackHelper.getAndRemove(this.items, 0);
        if(!item.isEmpty()) {
            this.markDirty();
        }

        NBTTagCompound tag = item.getTagCompound();

        if(tag == null) {
            tag = new NBTTagCompound();
            item.setTagCompound(tag);
        }

        if(tag != null && tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
            tag.setDouble(ZPMItem.ENERGY, this.storage.getEnergyStored());
            tag.setBoolean(ZPMItem.LOADED, false);
            this.storage.extractEnergy(this.storage.getEnergyStored(), false); // Empty the storage when the ZPM is removed.
        }
        this.loaded = false;
        return item;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack item) {
        this.items.set(0, item);

        if (this.getEnergyStored() == 0) {
            NBTTagCompound tag = item.getTagCompound();

            if (tag == null) {
                tag = new NBTTagCompound();
                item.setTagCompound(tag);
            }

            tag.setBoolean(ZPMItem.LOADED, true);

            if (!tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
                tag.setDouble(ZPMItem.ENERGY, Integer.MAX_VALUE);
                item.setTagCompound(tag);
            }

            this.storage.receiveEnergy((int) tag.getDouble(ZPMItem.ENERGY), false);
        }
    }

    public static boolean isValidFuelItem(ItemStack stack) {
        return stack != null && stack.getItem() == SGCraft.zpm && stack.getCount() > 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(final EntityPlayer player) {
    }

    @Override
    public void closeInventory(final EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack item) {
        return item.getItem() instanceof ZPMItem;
    }

    @Override
    public int getField(final int id) {
        return 0;
    }

    @Override
    public void setField(final int id, final int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public String getName() {
        return "container.zpm_console";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("ZPM Console");
    }

    public static ZpmConsoleTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof ZpmConsoleTE ? (ZpmConsoleTE) te : null;
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability.equals(CapabilityEnergy.ENERGY) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing))
            return CapabilityEnergy.ENERGY.cast(this);

        return super.getCapability(capability, facing);
    }

    //------------------------ IEnergyStorage ---------------------------

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int result = storage.receiveEnergy(maxReceive, simulate);
        markChanged();

        return result;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int result = storage.extractEnergy(maxExtract, simulate);
        markChanged();

        return result;
    }

    @Override
    public int getEnergyStored() {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return storage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return false;
        //return storage.canReceive();
    }

    @Override
    public double availableEnergy() {
        double available = storage.getEnergyStored();
        //if (debugOutput)
        //    System.out.printf("SGCraft: PowerTE: %s SGU available\n", available);
        return available;
    }

    public double totalAvailableEnergy() {
        return storage.getEnergyStored();
    }


    public double drawEnergyDouble(double request) {
        double available = storage.getEnergyStored();
        double supply = min(request, available);

        this.extractEnergy((int) supply, false);
        markChanged();

        return supply;
    }
}
