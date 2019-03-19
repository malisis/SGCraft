//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.ic2.zpm;

import gcewing.sg.BaseContainer;
import gcewing.sg.tileentity.DHDTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZpmInterfaceCartContainer extends BaseContainer {

    static final int numFuelSlotColumns = 2;
    static final int zpmSlotsX = 120;
    static final int zpmSlotsY = 54;
    static final int playerSlotsX = 48;
    static final int playerSlotsY = 124;

    ZpmInterfaceCartTE te;
    
    public static ZpmInterfaceCartContainer create(EntityPlayer player, World world, BlockPos pos) {
        ZpmInterfaceCartTE te =ZpmInterfaceCartTE.at(world, pos);
        if (te != null)
            return new ZpmInterfaceCartContainer(player, te);
        else
            return null;
    }
    
    public ZpmInterfaceCartContainer(EntityPlayer player, ZpmInterfaceCartTE te) {
        super(ZPMInterfaceCartScreen.guiWidth, ZPMInterfaceCartScreen.guiHeight);
        this.te = te;

        addZpmSlots();
        addPlayerSlots(player, playerSlotsX, playerSlotsY);
    }
    
    void addZpmSlots() {
        int b = ZpmInterfaceCartTE.firstZpmSlot;
        int n = ZpmInterfaceCartTE.numZpmSlots;
        for (int i = 0; i < n; i++) {
            int row = i / numFuelSlotColumns;
            int col = i % numFuelSlotColumns;
            int x = zpmSlotsX + col * 18;
            int y = zpmSlotsY + row * 18;
            addSlotToContainer(new ZpmInterfaceCartSlot(te, b + i, x, y));
        }
    }


    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = DHDTE.firstFuelSlot;
        range.numSlots = DHDTE.numFuelSlots;

        return range;
    }
}
