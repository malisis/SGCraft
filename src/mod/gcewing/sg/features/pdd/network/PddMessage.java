package gcewing.sg.features.pdd.network;

import gcewing.sg.SGCraft;
import gcewing.sg.features.network.ISGMessageHandler;
import gcewing.sg.features.network.SGNetwork;
import gcewing.sg.features.pdd.Address;
import gcewing.sg.features.pdd.client.gui.PddScreen;
import gcewing.sg.features.pdd.client.gui.PddScreenEGO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PddMessage implements ISGMessageHandler<PddMessage.PddPacket> {

    private enum Type {
        SAVE, DELETE, GUI
    }

    public PddMessage() {
        //message GUI is sent to client
        SGNetwork.INSTANCE.registerMessage(this, PddMessage.PddPacket.class, Side.CLIENT);
        //message SAVE and DELETE are sent to server
        SGNetwork.INSTANCE.registerMessage(this, PddMessage.PddPacket.class, Side.SERVER);
    }

    @Override
    public void process(PddPacket message, MessageContext ctx) {
        ItemStack is = getPlayer(ctx).getHeldItemMainhand();
        if (is.getItem() != SGCraft.pdd) { //wrong item in hand, cancel
            return;
        }

        if (message.type == Type.GUI) {
            if (GuiScreen.isShiftKeyDown()) {
                new PddScreen(getPlayer(ctx), getWorld(ctx), true).display();
            } else {
                new PddScreenEGO().display();
            }
        }

        if (message.type == Type.DELETE) {
            SGCraft.pdd.deleteAddress(is, message.key);
        }
        if (message.type == Type.SAVE) {
            SGCraft.pdd.saveAddress(is, new Address(message.address, message.name, message.index, false), message.key);
        }


    }

    public static void openPddGui(EntityPlayerMP player) {
        SGNetwork.INSTANCE.sendTo(new PddPacket(), player);
    }

    public static void save(String key, Address address) {
        SGNetwork.INSTANCE.sendToServer(new PddPacket(Type.SAVE, key, address));
    }

    public static void delete(String key) {
        SGNetwork.INSTANCE.sendToServer(new PddPacket(Type.DELETE, key, null));
    }

    public static class PddPacket implements IMessage {

        private Type type;
        private String key; //original address;
        private String address;
        private String name;
        private int index;

        public PddPacket() {
            type = Type.GUI;
        }

        public PddPacket(Type type, String key, Address address) {
            this.type = type;
            this.key = key != null ? key : "";
            if (type == Type.DELETE) {
                return;
            }
            this.address = address.getAddress();
            this.name = address.getName();
            this.index = address.getIndex();
        }


        @Override
        public void fromBytes(ByteBuf buf) {
            type = Type.values()[buf.readInt()];
            if (type == Type.GUI) {
                return;
            }
            key = ByteBufUtils.readUTF8String(buf);
            if (type == Type.DELETE) {
                return;
            }
            //SAVE (add or edit)
            address = ByteBufUtils.readUTF8String(buf);
            name = ByteBufUtils.readUTF8String(buf);
            index = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type.ordinal());
            if (type == Type.GUI) {
                return;
            }
            ByteBufUtils.writeUTF8String(buf, key);
            if (type == Type.DELETE) {
                return;
            }
            //SAVE (add or edit)
            ByteBufUtils.writeUTF8String(buf, address);
            ByteBufUtils.writeUTF8String(buf, name);
            buf.writeInt(index);
        }
    }
}
