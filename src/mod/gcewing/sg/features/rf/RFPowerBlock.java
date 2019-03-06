//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.rf;

import gcewing.sg.block.PowerBlock;

public class RFPowerBlock extends PowerBlock<RFPowerTE> {

    public RFPowerBlock() {
        super(RFPowerTE.class);
        setModelAndTextures("block/power.smeg",
            "rfPowerUnit-bottom", "rfPowerUnit-top", "rfPowerUnit-side");
    }
    
}
