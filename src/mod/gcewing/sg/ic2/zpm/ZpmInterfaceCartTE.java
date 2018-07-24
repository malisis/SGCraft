package gcewing.sg.ic2.zpm;

import gcewing.sg.ISGEnergySource;
import gcewing.sg.SGCraft;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

public class ZpmInterfaceCartTE extends TileEntity implements IEnergySource, ISGEnergySource,IInventory {
  private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

  public ZpmInterfaceCartTE() {
    this.setInventorySlotContents(0, new ItemStack(SGCraft.zpm));
  }

  @Override
  public double getOfferedEnergy() {
    double offeredEnergy = 0d;
    for(final ItemStack item : this.items) {
      final double availableEnergy = this.availableEnergy(item);
      System.out.println("ZPM Power Available: " + availableEnergy);
      offeredEnergy = availableEnergy;

      /*
      if(availableEnergy == Double.MAX_VALUE) {
        offeredEnergy = Double.MAX_VALUE;
      } else {
        final double incremented = offeredEnergy + availableEnergy;
        if(incremented == Double.MIN_VALUE) {
          offeredEnergy = Double.MAX_VALUE;
        } else {
          offeredEnergy = incremented;
        }
      }*/
    }

    return offeredEnergy;
  }

  @Override
  public void drawEnergy(double v) {
    drawEnergyDouble(v);
  }

  @Override
  public double availableEnergy() {
    return this.getOfferedEnergy();
  }

  @Override
  public double totalAvailableEnergy() {
    return this.getOfferedEnergy();
  }

  @Override
  public double drawEnergyDouble(final double value) {
    double energy = 0;
    for(final ItemStack item : this.items) {
      energy = this.availableEnergy(item);
      if(energy > 0d) {
        final NBTTagCompound tag = item.getTagCompound();
        if(tag != null) {
          tag.setDouble(ZPMItem.ENERGY, Math.max(0, energy - value));
        }
      }
    }
    return Math.min(value, energy);
  }

  @Override
  public int getSourceTier() {
    int tier = 3;
    //if(!this.items.get(0).isEmpty()) tier++; // LV
    //if(!this.items.get(1).isEmpty()) tier++; // MV
    //if(!this.items.get(2).isEmpty()) tier++; // HV
    return tier;
  }

  @Override
  public boolean emitsEnergyTo(final IEnergyAcceptor acceptor, final EnumFacing direction) {
    for(final ItemStack item : this.items) {
      final double energy = this.availableEnergy(item);
      if(energy > 0d) {
        System.out.println("Emit True");
        return true;
      }
    }
    System.out.println("Emit false");
    return false;
  }

  private double availableEnergy(final ItemStack item) {
    if(item.hasTagCompound()) {
      final NBTTagCompound tag = item.getTagCompound();
      if(tag != null) {
        return tag.getDouble(ZPMItem.ENERGY);
      }
    }
    return 0d;
  }

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
    return this.items.get(index);
  }

  @Override
  public ItemStack decrStackSize(final int index, final int quantity) {
    final ItemStack item = ItemStackHelper.getAndSplit(this.items, index, quantity);
    if(!item.isEmpty()) {
      this.markDirty();
    }
    return item;
  }

  @Override
  public ItemStack removeStackFromSlot(final int index) {
    return ItemStackHelper.getAndRemove(this.items, index);
  }

  @Override
  public void setInventorySlotContents(final int index, final ItemStack item) {
    this.items.set(index, item);

    NBTTagCompound tag = item.getTagCompound();
    if(tag == null) {
      tag = new NBTTagCompound();
      item.setTagCompound(tag);
    }
    if(!tag.hasKey(ZPMItem.ENERGY, 99 /* number */)) {
      tag.setDouble(ZPMItem.ENERGY, Integer.MAX_VALUE);
    }
  }

  @Override
  public int getInventoryStackLimit() {
    return 1;
  }

  @Override
  public boolean isUsableByPlayer(final EntityPlayer player) {
    if(this.world.getTileEntity(this.pos) != this) {
      return false;
    }
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
}
