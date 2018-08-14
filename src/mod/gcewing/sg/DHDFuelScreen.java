//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import org.lwjgl.opengl.*;

import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import java.text.DecimalFormat;

public class DHDFuelScreen extends SGScreen {

    static String screenTitle = "Stargate Controller";
    static final int guiWidth = 256;
    static final int guiHeight = 208;
    static final int fuelGaugeWidth = 16;
    static final int fuelGaugeHeight = 34;
    static final int fuelGaugeX = 214;
    static final int fuelGaugeY = 84;
    static final int fuelGaugeU = 0;
    static final int fuelGaugeV = 208;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###");

    DHDTE te;
    
    public static DHDFuelScreen create(EntityPlayer player, World world, BlockPos pos) {
        DHDTE te = DHDTE.at(world, pos);
        if (te != null)
            return new DHDFuelScreen(player, te);
        else
            return null;
    }

    public DHDFuelScreen(EntityPlayer player, DHDTE te) {
        super(new DHDFuelContainer(player, te), guiWidth, guiHeight);
        this.te = te;
    }
    
    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/dhd_fuel_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        drawFuelGauge();
        int cx = xSize / 2;
        setTextColor(0x004c66);
        drawCenteredString(screenTitle, cx, 8);

        int naquadahUnits = 0;
        naquadahUnits = te.getInventory().getStackInSlot(0).getCount() + te.getInventory().getStackInSlot(1).getCount() + te.getInventory().getStackInSlot(2).getCount() + te.getInventory().getStackInSlot(3).getCount();

        // Buffer Available
        drawRightAlignedString("DHD Buffer:", 125, 30);
        drawString(dFormat.format(Math.min(Math.max(te.energyInBuffer, 0), te.maxEnergyBuffer)), 130, 30);

        // Buffer Max
        drawRightAlignedString("Buffer Max:", 125, 40);
        drawString(dFormat.format(te.maxEnergyBuffer), 130, 40);

        // Naquadah Units
        drawRightAlignedString("Naquadah:", 125, 60);
        drawString(dFormat.format(naquadahUnits), 130, 60);

        // Naquadah Power Units
        drawRightAlignedString("Available Power Units:", 125, 70);
        drawString(dFormat.format(naquadahUnits * SGBaseTE.energyPerFuelItem), 130, 70);

        if (this.te.numFuelSlots > 0)
            drawString("Fuel", 150, 96);
    }
    
    void drawFuelGauge() {
        int level = (int)(fuelGaugeHeight * te.energyInBuffer / te.maxEnergyBuffer);
        if (level > fuelGaugeHeight)
            level = fuelGaugeHeight;
        GL11.glEnable(GL11.GL_BLEND);
        drawTexturedRect(fuelGaugeX, fuelGaugeY + fuelGaugeHeight - level,
            fuelGaugeWidth, level, fuelGaugeU, fuelGaugeV);
        GL11.glDisable(GL11.GL_BLEND);
    }

}
