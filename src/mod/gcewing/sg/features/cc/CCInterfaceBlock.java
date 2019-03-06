//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.cc;

import gcewing.sg.*;
import gcewing.sg.block.SGInterfaceBlock;

public class CCInterfaceBlock extends SGInterfaceBlock<CCInterfaceTE> {

    public CCInterfaceBlock() {
        super(SGCraft.machineMaterial, CCInterfaceTE.class);
        setModelAndTextures("block/interface.smeg",
            "ccInterface-bottom", "ccInterface-top", "ccInterface-front", "ccInterface-side");
    }

}
