package gcewing.sg.features.ic2.zpm;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ZpmInterfaceCartSlot extends Slot {

    public ZpmInterfaceCartSlot(IInventory inv, int i, int x, int y) {
        super(inv, i, x, y);
    }

    public boolean isItemValid(ItemStack stack) {
        return ZpmInterfaceCartTE.isValidFuelItem(stack);
    }

}
