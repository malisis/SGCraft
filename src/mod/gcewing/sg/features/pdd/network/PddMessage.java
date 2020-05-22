package gcewing.sg.features.pdd.network;

import gcewing.sg.features.network.ISGMessageHandler;
import gcewing.sg.features.network.SGNetwork;
import gcewing.sg.features.pdd.client.gui.PddScreen;
import gcewing.sg.features.pdd.client.gui.PddScreenEGO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PddMessage implements ISGMessageHandler<PddMessage.PddPacket> {

    @Override
    public void process(PddPacket message, MessageContext ctx) {
        if (GuiScreen.isShiftKeyDown()) {
            new PddScreen(getPlayer(ctx), getWorld(ctx), true).display();
        } else {
            new PddScreenEGO().display();
        }
    }

    public static void openPddGui(EntityPlayerMP player) {
        SGNetwork.INSTANCE.sendTo(new PddPacket(), player);
    }

    public static class PddPacket implements IMessage {

        @Override
        public void fromBytes(ByteBuf buf) {

        }

        @Override
        public void toBytes(ByteBuf buf) {

        }
    }
}
