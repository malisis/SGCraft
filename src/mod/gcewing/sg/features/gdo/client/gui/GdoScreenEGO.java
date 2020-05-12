package gcewing.sg.features.gdo.client.gui;

import gcewing.sg.features.ego.SGWindow;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.SGState;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;

public class GdoScreenEGO extends MalisisGui {

    private final SGBaseTE localGate;
    private SGBaseTE remoteGate;
    private GateControlComponent localComponent, remoteComponent;
    private UIButton closeButton;
    private final boolean localIrisAccess;
    private final boolean remoteIrisAccess;

    public GdoScreenEGO(BlockPos pos, boolean localIrisAccess, boolean remoteIrisAccess) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        localGate = SGBaseTE.get(player.world, pos);// GateUtil.findGate(player.world, player, 6);
        this.localIrisAccess = localIrisAccess;
        this.remoteIrisAccess = remoteIrisAccess;

    }

    public void construct() {
        if (localGate == null || !localGate.isMerged) {
            addToScreen(SGWindow.errorWindow("No local Stargate within range."));
            return;
        }

        UIContainer window = SGWindow.builder("sgcraft.gui.gdo.label.gdoController")
                .middleCenter()
                //.size(400, 225)
                .build();

        localComponent = GateControlComponent.builder(localGate, true)
                .parent(window)
                .irisAccess(localIrisAccess)
                .build();

        remoteGate = localGate.getConnectedStargateTE();
        remoteComponent = GateControlComponent.builder(remoteGate, false)
                .parent(window)
                .rightOf(localComponent, 5)
                .visible(() -> remoteGate != null)
                .irisAccess(remoteIrisAccess)
                .build();

        closeButton = UIButton.builder()
                .parent(window)
                .x(this::closeButtonPosition)
                .below(localComponent, 5)
                .text("Close")
                .onClick(this::close)
                .build();

        addDebug("Local state : ", () -> localGate.state.name());

        addToScreen(window);
    }

    private int closeButtonPosition() {
        //button position is aligned to local or remote component
        UIComponent component = localGate.isConnected() && localGate.state != SGState.SyncAwait ? remoteComponent : localComponent;

        return component.position()
                .x() + component.size()
                .width() - closeButton.size()
                .width();
    }

    @Override
    public void update() {
        if (localGate == null) {
            return;
        }

        if (remoteGate == null && localGate.isConnected() && localGate.state != SGState.SyncAwait) {
            remoteGate = localGate.getConnectedStargateTE();
            remoteComponent.setGate(remoteGate);
        }
        if (remoteGate != null && !localGate.isConnected()) {
            remoteGate = null;
            remoteComponent.setGate(null);
        }
    }
}