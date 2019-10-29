//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.ic2.zpm.modulehub;

import gcewing.sg.SGCraft;
import gcewing.sg.client.gui.SGScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.text.DecimalFormat;

public class ZpmModuleHubScreen extends SGScreen {

    static String screenTitle = I18n.format("tile.sgcraft:zpm_interface_cart.name");
    static final int guiWidth = 256;
    static final int guiHeight = 208;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

    ZpmModuleHubTE te;

    public static ZpmModuleHubScreen create(EntityPlayer player, World world, BlockPos pos) {
        ZpmModuleHubTE te = ZpmModuleHubTE.at(world, pos);
        if (te != null)
            return new ZpmModuleHubScreen(player, te);
        else
            return null;
    }

    public ZpmModuleHubScreen(EntityPlayer player, ZpmModuleHubTE te) {
        super(new ZpmModuleHubContainer(player, te), guiWidth, guiHeight);
        this.te = te;
    }
    
    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/zpm_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);

        int cx = xSize / 2;
        setTextColor(0x004c66);
        drawCenteredString(screenTitle, cx, 8);
        drawString("ZPM", 120, 45);
        drawString(I18n.format("sgcraft.gui.zpmConsole.label.availablePower")+": " + dFormat.format(te.availableEnergy()), 60, 100);
    }
}
