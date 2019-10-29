//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.ic2.zpm.modulehub;

import gcewing.sg.BaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZpmHubContainer extends BaseContainer {

    static final int numFuelSlotColumns = 3;
    static final int zpmSlotsX = 120;
    static final int zpmSlotsY = 54;
    static final int playerSlotsX = 48;
    static final int playerSlotsY = 124;

    ZpmHubTE te;

    public static ZpmHubContainer create(EntityPlayer player, World world, BlockPos pos) {
        ZpmHubTE te = ZpmHubTE.at(world, pos);
        if (te != null)
            return new ZpmHubContainer(player, te);
        else
            return null;
    }

    public ZpmHubContainer(EntityPlayer player, ZpmHubTE te) {
        super(ZpmHubScreen.guiWidth, ZpmHubScreen.guiHeight);
        this.te = te;

        addZpmSlots();
        addPlayerSlots(player, playerSlotsX, playerSlotsY);
    }

    void addZpmSlots() {
        int b = ZpmHubTE.firstZpmSlot;
        int n = ZpmHubTE.numZpmSlots;
        for (int i = 0; i < n; i++) {
            int row = i / numFuelSlotColumns;
            int col = i % numFuelSlotColumns;
            int x = zpmSlotsX + col * 18;
            int y = zpmSlotsY + row * 18;
            addSlotToContainer(new ZpmHubSlot(te, b + i, x, y));
        }
    }

    @Override // Shift-Click Inventory
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        ItemStack stack = slot.getStack();
        if (slot != null && slot.getHasStack()) {
            SlotRange destRange = transferSlotRange(index, stack);
            if (destRange != null) {
                if (index >= destRange.numSlots) {
                    result = stack.copy();
                    if (!mergeItemStackIntoRange(stack, destRange))
                        return ItemStack.EMPTY;
                    if (stack.getCount() == 0)
                        slot.putStack(ItemStack.EMPTY);
                    else
                        slot.onSlotChanged();
                } else {
                    player.inventory.addItemStackToInventory(te.decrStackSize(index, 1));
                }
            }
        }
        return result;
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = 0;
        range.numSlots = 1;

        return range;
    }
}
