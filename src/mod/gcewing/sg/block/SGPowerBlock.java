//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.block;

import gcewing.sg.block.PowerBlock;
import gcewing.sg.tileentity.SGPowerTE;

public class SGPowerBlock extends PowerBlock<SGPowerTE> {

    public SGPowerBlock() {
        super(SGPowerTE.class);
        setModelAndTextures("block/power.smeg",
            "sgPowerUnit-bottom", "sgPowerUnit-top", "sgPowerUnit-side");
    }
    
}
