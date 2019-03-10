package gcewing.sg.features.pdd;

import com.google.common.collect.Lists;
import gcewing.sg.SGCraft;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
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
        if (!worldIn.isRemote) {  // Execute this ONLY on the client
            final ItemStack stack = player.getHeldItem(handIn);

            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) {
                compound = new NBTTagCompound();
            }

            if (!compound.hasKey(AddressData.ADDRESSES)) {
                List<AddressData> genericAddressList = Lists.newArrayList();
                // Create Generic List
                genericAddressList.add(new AddressData("Orilla - Endor", "T9FH-3VW-VL", true, 1, 0));
                genericAddressList.add(new AddressData("Orilla - Dockside", "X35A-008-YC", true, 1, 0));
                genericAddressList.add(new AddressData("Asgard - Main Island", "V9V2-V9V-ZY", true, 1, 0));
                genericAddressList.add(new AddressData("Asgard - Almura Castle", "9U9S-F4Q-2D", true, 1, 0));
                genericAddressList.add(new AddressData("Dakara - Main Spawn Point", "PFWO-G8F-10", true, 1, 0));
                genericAddressList.add(new AddressData("TEST", "ZFDDUR8", true, 1, 0));

                AddressData.writeAddresses(compound, genericAddressList);
                stack.setTagCompound(compound);
                player.inventoryContainer.detectAndSendChanges();
            }

            TileEntity localGateTE = GateUtil.locateLocalGate(worldIn, new BlockPos(player.posX, player.posY, player.posZ), 6, true);
            if (localGateTE instanceof SGBaseTE) {
                SGBaseTE localGate = (SGBaseTE) localGateTE;

                boolean canEditLocal = localGate.getWorld().isBlockModifiable(player, localGate.getPos());
                boolean canEditRemote = false;
                if (localGate.isConnected() && localGate.state == SGState.Connected) {
                    SGBaseTE remoteGate = localGate.getConnectedStargateTE();
                    canEditRemote = remoteGate.getWorld().isBlockModifiable(player, remoteGate.getPos());
                }

                if (SGCraft.hasPermission(player, "sgcraft.gui.pdd")) {
                    SGChannel.openGuiAtClient(localGate, player, 3, SGCraft.hasPermission(player, "sgcraft.admin"), canEditLocal, canEditRemote);
                }

                return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
            }
        }

        return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
    }
}
