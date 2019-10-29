package gcewing.sg.features.ic2.zpm.modulehub;

import static gcewing.sg.BaseUtils.min;
import static gcewing.sg.features.ic2.zpm.interfacecart.ZpmInterfaceCart.ZPM_LOADED;

import gcewing.sg.BaseTileInventory;
import gcewing.sg.SGCraft;
import gcewing.sg.features.zpm.ZPMItem;
import gcewing.sg.interfaces.ISGEnergySource;
import ic2.api.energy.prefab.BasicSource;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public final class ZpmModuleHubTE extends BaseTileInventory implements ISGEnergySource, IEnergySource, IInventory, ITickable {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    public final BasicSource hubSource;
    public static final int firstZpmSlot = 0;
    public static final int numZpmSlots = 3;
    public static final int numSlots = numZpmSlots; // future usage > 1
    public int zpmSlot0Energy = 0;
    public int zpmSlot1Energy = 0;
    public int zpmSlot2Energy = 0;

    private double energyPerSGEnergyUnit = 80;
    private int update = 0;

    public ZpmModuleHubTE() {
        this.hubSource = new ZpmModuleHubBasicSource(this, Integer.MAX_VALUE, 5);
    }

    /* TileEntity */

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.hubSource.readFromNBT(compound);
        this.zpmSlot0Energy = compound.getInteger("zpmSlot0Energy");
        this.zpmSlot1Energy = compound.getInteger("zpmSlot1Energy");
        this.zpmSlot2Energy = compound.getInteger("zpmSlot2Energy");
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.hubSource.writeToNBT(compound);
        compound.setInteger("zpmSlot0Energy", zpmSlot0Energy);
        compound.setInteger("zpmSlot1Energy", zpmSlot1Energy);
        compound.setInteger("zpmSlot2Energy", zpmSlot2Energy);
        return compound;
    }

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        this.hubSource.update();
    }

    @Override
    public void onChunkUnload() {
        this.hubSource.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate(); // this is important for mc!
        this.hubSource.invalidate(); // notify the energy hubSource
    }

    @Override
    @Nonnull
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
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

    /* Energy */

    @Override
    public double availableEnergy() {
        return this.hubSource.getEnergyStored();
    }

    @Override
    public double totalAvailableEnergy() {
        return this.hubSource.getCapacity();
    }

    @Override
    public double drawEnergyDouble(double amount) {
        double available = this.hubSource.getEnergyStored();
        double supply = min(amount, available);
        this.hubSource.drawEnergy(supply * energyPerSGEnergyUnit);

        if (isTainted(this.getStackInSlot(0))) {
            world.newExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), (float)250, true, true);
        }

        markChanged();
        return amount;
    }

    @Override
    public double getOfferedEnergy() {
        return this.hubSource.getOfferedEnergy();
    }

    @Override
    public void drawEnergy(double v) {
        int zpmCount = 0;

        if (zpmSlot0Energy > 0) {
            zpmCount = zpmCount + 1;
        }
        if (zpmSlot1Energy > 0) {
            zpmCount = zpmCount + 1;
        }
        if (zpmSlot2Energy > 0) {
            zpmCount = zpmCount + 1;
        }

        double drawAmount = v / zpmCount;
        int perZpmDrawAmount = (int)(drawAmount / zpmCount);

        this.hubSource.drawEnergy(drawAmount);

        if (zpmSlot0Energy > 0) {
            this.zpmSlot0Energy = this.zpmSlot0Energy - perZpmDrawAmount;
        }
        if (zpmSlot1Energy > 0) {
            this.zpmSlot1Energy = this.zpmSlot0Energy - perZpmDrawAmount;
        }
        if (zpmSlot2Energy > 0) {
            this.zpmSlot2Energy = this.zpmSlot0Energy - perZpmDrawAmount;
        }

        if (isTainted(this.getStackInSlot(0)) || isTainted(this.getStackInSlot(1)) || isTainted(this.getStackInSlot(2))) {
            world.newExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), (float)250, true, true);
        }

        markChanged();
    }

    @Override
    public int getSourceTier() {
        return this.hubSource.getSourceTier();
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, EnumFacing enumFacing) {
        if (isTainted(this.getStackInSlot(0)) || isTainted(this.getStackInSlot(1))|| isTainted(this.getStackInSlot(2))) {
            world.newExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), (float)250, true, true);
        }

        markChanged();
        return this.hubSource.emitsEnergyTo(iEnergyAcceptor, enumFacing);
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
        return this.items.get(index);
    }

    @Override // This prevents the zpm from being input/extract from the console.
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[0];
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
            if (index == 0) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot0Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot0Energy);
            } else if (index == 1) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot1Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot1Energy);
            } else if (index == 2) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot2Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot2Energy);
            }
        }

        IBlockState other = world.getBlockState(pos).withProperty(ZPM_LOADED, false);
        world.setBlockState(pos, other, 3);

        markDirty();

        return ItemStackHelper.getAndRemove(this.items, 0);
    }

    @Override
    public ItemStack decrStackSize(final int index, final int quantity) {
        final ItemStack item = ItemStackHelper.getAndRemove(this.items, 0);

        NBTTagCompound tag = item.getTagCompound();

        if(tag == null) {
            tag = new NBTTagCompound();
            item.setTagCompound(tag);
        }

        if(tag != null && tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
            if (index == 0) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot0Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot0Energy);
            } else if (index == 1) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot1Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot1Energy);
            } else if (index == 2) {
                tag.setDouble(ZPMItem.ENERGY, this.zpmSlot2Energy);
                tag.setBoolean(ZPMItem.LOADED, false);
                this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() - this.zpmSlot2Energy);
            }
        }

        IBlockState other = world.getBlockState(pos).withProperty(ZPM_LOADED, false);
        world.setBlockState(pos, other, 3);

        markDirty();

        return item;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack item) {
        this.items.set(0, item);
        if (isValidFuelItem(item)) {
            NBTTagCompound tag = item.getTagCompound();

            if (tag == null) {
                tag = new NBTTagCompound();
                item.setTagCompound(tag);
            }

            tag.setBoolean(ZPMItem.LOADED, true);

            if (!tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
                tag.setDouble(ZPMItem.ENERGY, Integer.MAX_VALUE);
            }

            if (index == 0) {
                this.zpmSlot0Energy = (int)tag.getDouble(ZPMItem.ENERGY);
            } else if (index == 1) {
                this.zpmSlot1Energy = (int)tag.getDouble(ZPMItem.ENERGY);
            } else if (index == 2) {
                this.zpmSlot2Energy = (int)tag.getDouble(ZPMItem.ENERGY);
            }

            this.hubSource.setEnergyStored(this.hubSource.getEnergyStored() + tag.getDouble(ZPMItem.ENERGY));
        }

        if (world != null){ // This will be null both on the server AND client at time, no idea why....
            IBlockState other = world.getBlockState(pos).withProperty(ZPM_LOADED, isValidFuelItem(item));
            world.setBlockState(pos, other, 3);
        }

        markDirty();
    }

    public static boolean isValidFuelItem(ItemStack stack) {
        return stack != null && stack.getItem() == SGCraft.zpm && stack.getCount() > 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
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
        return "container.zero_point_module";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("ZPM Container");
    }

    public static ZpmModuleHubTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof ZpmModuleHubTE ? (ZpmModuleHubTE) te : null;
    }

    public boolean isTainted(ItemStack item) {
        boolean hasTaint = false;
        NBTTagList nbttaglist = item.getEnchantmentTagList();
        for (int j = 0; j < nbttaglist.tagCount(); ++j) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);
            int k = nbttagcompound.getShort("id");
            int l = nbttagcompound.getShort("lvl");
            Enchantment enchantment = Enchantment.getEnchantmentByID(k);
            if (k == 51) {
                hasTaint = true;
            }
        }
        return hasTaint;
    }

    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) { // Prevents the TE from nullifying itself when we force change the state to change it models.  Vanilla mechanics invalidate the TE.
            return true;
        } else {
            return false;
        }
    }
}
