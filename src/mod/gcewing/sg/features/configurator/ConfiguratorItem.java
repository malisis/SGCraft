package gcewing.sg.features.configurator;

import static gcewing.sg.tileentity.SGBaseTE.sendBasicMsg;
import static gcewing.sg.tileentity.SGBaseTE.sendErrorMsg;

import gcewing.sg.SGCraft;
import gcewing.sg.network.GuiNetworkHandler;
import gcewing.sg.tileentity.DHDTE;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

public class ConfiguratorItem extends Item {

    private SGBaseTE toLinkTE = null;

  public ConfiguratorItem() {}

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, player, tooltip, advanced);
  }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (!world.isRemote && player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof DHDTE) {
                if (toLinkTE != null) {
                    ((DHDTE) te).linkToStargate(toLinkTE);
                    sendBasicMsg(player, "configuratorLink");
                    toLinkTE = null;
                }
            }
            if (te instanceof SGBaseTE) {
                toLinkTE = (SGBaseTE)te;
                sendBasicMsg(player, "gateLocationSaved");
            }
        }

        return EnumActionResult.PASS;
    }


  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
      if (!worldIn.isRemote && !player.isSneaking()) {
          TileEntity localGateTE = GateUtil.locateLocalGate(worldIn, new BlockPos(player.posX, player.posY, player.posZ), 6, false);

          if (localGateTE instanceof SGBaseTE) {
              SGBaseTE localGate = (SGBaseTE) localGateTE;

              boolean canEditLocal = false;
              boolean canEditRemote = false;

              if (localGate.isConnected() && localGate.state == SGState.Connected) {
                  SGBaseTE remoteGate = localGate.getConnectedStargateTE();
                  canEditRemote = remoteGate.getWorld().isBlockModifiable(player, remoteGate.getPos());
              }

              boolean isPermissionsAdmin = SGCraft.hasPermissionSystem() && SGCraft.hasPermission(player, "sgcraft.admin"); // Fallback for a full permissions system override to the Access System

              if (SGCraft.hasPermission(player, "sgcraft.gui.configurator") && localGate.allowAdminAccess(player.getName()) || isPermissionsAdmin) {
                  GuiNetworkHandler.openGuiAtClient(localGate, player, 1, isPermissionsAdmin, canEditLocal, canEditRemote);
              } else {
                  if (!SGCraft.hasPermission(player, "sgcraft.gui.configurator"))
                      sendErrorMsg(player, "configuratorPermission");
                  if (!localGate.allowAdminAccess(player.getName()))
                      sendErrorMsg(player, "insufficientPlayerAdminAccessPermission");

                  return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn));
              }

              return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
          } else {
              sendErrorMsg(player, "cantFindStargate");
              return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn));
          }
      }

      return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
  }
}
