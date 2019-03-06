//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.client.gui;

import gcewing.sg.util.SGAddressing;
import gcewing.sg.container.SGBaseContainer;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.SGCraft;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class SGBaseScreen extends SGScreen {

    static String screenTitle = "Stargate Address";
    static final int guiWidth = 256;
    static final int guiHeight = 208; //92;
    static final int fuelGaugeWidth = 16;
    static final int fuelGaugeHeight = 34;
    static final int fuelGaugeX = 214;
    static final int fuelGaugeY = 84;
    static final int fuelGaugeU = 0;
    static final int fuelGaugeV = 208;
    
    private SGBaseTE te;
    private String address;
    private String formattedAddress;
    private boolean addressValid;
    
    public static SGBaseScreen create(EntityPlayer player, World world, BlockPos pos) {
        SGBaseTE te = SGBaseTE.at(world, pos);
        if (te != null)
            return new SGBaseScreen(player, te);
        else
            return null;
    }

    private SGBaseScreen(EntityPlayer player, SGBaseTE te) {
        super(new SGBaseContainer(player, te), guiWidth, guiHeight);
        this.te = te;
        this.te.markBlockForUpdate();
        getAddress();
        if (this.addressValid) {
            //System.out.printf("SGBaseScreen: Copying address %s to clipboard\n", formattedAddress);
            if (SGCraft.saveAddressToClipboard) {
                setClipboardString(this.formattedAddress);
            }
        }
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/sg_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        int cx = this.xSize / 2;
        if (this.addressValid)
            drawAddressSymbols(cx, 22, this.address);
        setTextColor(0x004c66);
        drawCenteredString(this.screenTitle, cx, 8);
        drawCenteredString(this.formattedAddress, cx, 72);
        if (this.te.numCamouflageSlots > 0)
            drawCenteredString("Base Camouflage", 92, 92);
    }

    private void getAddress() {
        // Fix if a gate is constructed but the base block is removed, then replaced, the home address isn't getting properly set again.
        if (this.te.homeAddress != null) {
            this.address = this.te.homeAddress;
            this.formattedAddress = SGAddressing.formatAddress(this.address, "-", "-");
            this.addressValid = true;
        } else {
            this.address = "";
            this.formattedAddress = this.te.addressError;
            this.addressValid = false;
        }
    }
}
