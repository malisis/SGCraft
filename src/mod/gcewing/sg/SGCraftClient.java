//------------------------------------------------------------------------------------------------
//
//   SG Craft - Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

// import gcewing.sg.ic2.*; //[IC2]

import gcewing.sg.client.gui.DHDFuelScreen;
import gcewing.sg.client.gui.DHDScreen;
import gcewing.sg.client.gui.PowerScreen;
import gcewing.sg.client.gui.SGBaseScreen;
import gcewing.sg.client.gui.SGGui;
import gcewing.sg.client.renderer.DHDTERenderer;
import gcewing.sg.client.renderer.IrisRenderer;
import gcewing.sg.client.renderer.SGBaseTERenderer;
import gcewing.sg.entity.EntityStargateIris;
import gcewing.sg.features.ic2.zpm.ZPMInterfaceCartScreen;
import gcewing.sg.features.zpm.ZPMConsoleScreen;
import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;

public class SGCraftClient extends BaseModClient<SGCraft> {

    public SGCraftClient(SGCraft mod) {
        super(mod);
        //debugSound = true;
        //debugModelRegistration = true;
    }
    
    @Override
    protected void registerScreens() {
        //System.out.printf("SGCraft: ProxyClient.registerScreens\n");
        addScreen(SGGui.SGBase, SGBaseScreen.class);
        addScreen(SGGui.SGController, DHDScreen.class);
        addScreen(SGGui.DHDFuel, DHDFuelScreen.class);
        addScreen(SGGui.PowerUnit, PowerScreen.class);
        addScreen(SGGui.ZPMInterfaceCart, ZPMInterfaceCartScreen.class);
        addScreen(SGGui.ZPMConsole, ZPMConsoleScreen.class);
    }

    @Override
    protected void registerTileEntityRenderers() {
        addTileEntityRenderer(SGBaseTE.class, new SGBaseTERenderer());
        addTileEntityRenderer(DHDTE.class, new DHDTERenderer());
    }
    
    @Override
    protected void registerEntityRenderers() {
        addEntityRenderer(EntityStargateIris.class, IrisRenderer.class);
    }

}
