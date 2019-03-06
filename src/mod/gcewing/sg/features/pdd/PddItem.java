package gcewing.sg.features.pdd;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

public class PddItem extends Item {

  public PddItem() {}

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, player, tooltip, advanced);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
    //new PddScreen(Minecraft.getMinecraft().player, Minecraft.getMinecraft().world, true).display();
    return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
  }
}
