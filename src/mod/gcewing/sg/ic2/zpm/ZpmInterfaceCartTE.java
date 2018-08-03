package gcewing.sg.ic2.zpm;

import gcewing.sg.BaseTileInventory;
import gcewing.sg.ISGEnergySource;
import gcewing.sg.SGBaseTE;
import gcewing.sg.SGCraft;
import ic2.api.energy.prefab.BasicSource;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public final class ZpmInterfaceCartTE extends BaseTileInventory implements ISGEnergySource, IEnergySource, IInventory, ITickable {
  private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
  private final BasicSource source;

  public ZpmInterfaceCartTE() {
    this.source = new BasicSource(this, 2000000, 4);
    this.setInventorySlotContents(0, new ItemStack(SGCraft.zpm));
  }

  /* TileEntity */

  @Override
  public void readFromNBT(final NBTTagCompound compound) {
    super.readFromNBT(compound);
    this.source.readFromNBT(compound);

  }

  @Override
  public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
    super.writeToNBT(compound);
    this.source.writeToNBT(compound);

    return compound;
  }

  @Override
  public void update() {
    if (this.world == null || this.world.isRemote) {
      return;
    }

    this.source.update();
  }

  @Override
  public void onChunkUnload() {
    this.source.onChunkUnload();
  }

  @Override
  public void invalidate() {
    super.invalidate(); // this is important for mc!
    this.source.invalidate(); // notify the energy source
  }

  // Below 3 methods are to keep the server and the client TE's in sync.
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

  /* Energy */

  @Override
  public double availableEnergy() {
    return this.source.getEnergyStored();
  }

  @Override
  public double totalAvailableEnergy() {
    return this.source.getCapacity();
  }

  @Override
  public double drawEnergyDouble(double amount) {

    this.source.drawEnergy(amount);

    return amount;
  }

  @Override
  public double getOfferedEnergy() {
    return this.source.getOfferedEnergy();
  }

  @Override
  public void drawEnergy(double v) {
    this.source.drawEnergy(v);
  }

  @Override
  public int getSourceTier() {
    return this.source.getSourceTier();
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, EnumFacing enumFacing) {
    //return this.source.emitsEnergyTo(iEnergyAcceptor, enumFacing);
    return true;
  }

  /* Inventory */

  @Override
  public int getSizeInventory() {
    return 1;
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
      tag.setDouble(ZPMItem.ENERGY, this.source.getEnergyStored());
      this.source.setEnergyStored(0);
      System.out.println("Removed ZPM via Remote Slot");
    }
    return ItemStackHelper.getAndRemove(this.items, 0);
  }

  @Override
  public ItemStack decrStackSize(final int index, final int quantity) {
    final ItemStack item = ItemStackHelper.getAndRemove(this.items, 0);
    if(!item.isEmpty()) {
      this.markDirty();
    }

    NBTTagCompound tag = item.getTagCompound();
    if(tag != null && tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
      tag.setDouble(ZPMItem.ENERGY, this.source.getEnergyStored());
      System.out.println("Stored: " + this.source.getEnergyStored() + " to the ZPM before it was removed, then set SOURCE to: 0");
      this.source.setEnergyStored(0);
    }
    return item;
  }

  @Override
  public void setInventorySlotContents(final int index, final ItemStack item) {
    this.items.set(0, item);

    NBTTagCompound tag = item.getTagCompound();
    if(tag == null) {
      tag = new NBTTagCompound();
      item.setTagCompound(tag);
    }
    if(!tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
      System.out.println("Creating tag");
      tag.setDouble(ZPMItem.ENERGY, 500000);
      this.source.setCapacity(800000);
      this.source.setEnergyStored(500000);
    } else {
      System.out.println("ZPM Power detected: " + tag.getDouble(ZPMItem.ENERGY));
      this.source.setEnergyStored(tag.getDouble(ZPMItem.ENERGY));
    }
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
    //if(this.world.getTileEntity(this.pos) != this) {
    //  return false;
    //}
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
}
