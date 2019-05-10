package gcewing.sg.item;

import gcewing.sg.interfaces.ISGBlock;
import gcewing.sg.tileentity.SGBaseTE;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SGPegasusUpgradeItem extends Item {

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Block block = worldIn.getBlockState(pos).getBlock();

        if(!(block instanceof ISGBlock)) //Case: Block is not an SGBlock
            return EnumActionResult.FAIL;

        SGBaseTE sgTileEntity = ((ISGBlock) block).getBaseTE(worldIn,pos);

        if(sgTileEntity == null) //Case: Gate has no base block
            return EnumActionResult.FAIL;

        sgTileEntity.gateType = 2; //Set type to pegasus style
        sgTileEntity.ringRotationSpeed = 6.0D; //Set rotation speed to 6 as per configurator default
        return EnumActionResult.SUCCESS;
    }
}
