package gcewing.sg.features.pdd;

import static gcewing.sg.tileentity.SGBaseTE.sendErrorMsg;

import com.google.common.collect.Lists;
import gcewing.sg.SGCraft;
import gcewing.sg.features.pdd.network.PddMessage;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class PddItem extends Item {

    public PddItem() {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(getAddresses(stack).size() + " addresses stored.");
    }

    private NBTTagCompound getNBT(ItemStack itemStack) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
            saveAddresses(itemStack, Address.getDefaultAddresses()); //load default addresses when creating the NBT
        }
        return itemStack.getTagCompound();
    }


    private boolean canEdit(List<Address> addresses, String oldKey, String newKey) {
        Address oldAddress = getAddress(addresses, oldKey);
        Address newAddress = getAddress(addresses, newKey);

        return (oldAddress == null || !oldAddress.isLocked()) && (newAddress == null || !newAddress.isLocked());
    }


    public Address getAddress(List<Address> addresses, String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        return addresses.stream().filter(a -> a.getAddress().equals(address)).findAny().orElse(null);
    }


    public void saveAddress(ItemStack itemStack, Address address, String old) {
        List<Address> addresses = getAddresses(itemStack);
        if (!canEdit(addresses, old, address.getAddress())) {
            return;
        }

        addresses.remove(getAddress(addresses, old)); //always delete old address
        addresses.add(address);
        saveAddresses(itemStack, addresses);
    }

    public void deleteAddress(ItemStack itemStack, String address) {
        List<Address> addresses = getAddresses(itemStack);
        addresses.removeIf(a -> a.getAddress().equals(address));
        saveAddresses(itemStack, addresses);
    }

    public void saveAddresses(ItemStack itemStack, Collection<Address> addresses) {
        NBTTagCompound nbt = getNBT(itemStack);
        NBTTagList list = new NBTTagList();
        for (Address address : addresses) {
            list.appendTag(address.toNBT(new NBTTagCompound()));
        }
        nbt.setTag(Address.ADDRESSES, list);
    }

    public List<Address> getAddresses(ItemStack itemStack) {
        NBTTagList list = getNBT(itemStack).getTagList(Address.ADDRESSES, Constants.NBT.TAG_COMPOUND);
        List<Address> addresses = Lists.newArrayList();
        list.forEach(nbt -> addresses.add(Address.fromNBT((NBTTagCompound) nbt)));
        addresses.sort(Comparator.comparingInt(Address::getIndex).thenComparing(Address::getName));
        return addresses;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        if (world.isRemote || hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));  //Both Server & Client expect a returned value.
        }

        boolean isAdmin = SGCraft.isAdmin(player);
        if (!isAdmin && !SGCraft.hasPermission(player, "sgcraft.gui.pdd")) {
            sendErrorMsg(player, "pddPermission");
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));  //Both Server & Client expect a returned value.
        }

        ItemStack itemStack = player.getHeldItemMainhand();
        List<Address> addresses = getAddresses(itemStack);
        SGBaseTE localGate = GateUtil.findGate(world, player, 6);


        PddMessage.openPddGui((EntityPlayerMP) player);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));  //Both Server & Client expect a returned value.
    }
}

