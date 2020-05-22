package gcewing.sg.features.gdo.client.gui;

import com.google.common.collect.Maps;
import gcewing.sg.features.ego.SGComponent;
import gcewing.sg.features.gdo.network.GdoNetworkHandler;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.render.GuiIcon;
import net.malisis.ego.gui.render.GuiTexture;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class GateControlComponent extends UIContainer {

    private SGBaseTE gate;
    private boolean irisAccess;

    private final Map<String, GuiIcon> gateTextures = Maps.newHashMap();

    public GateControlComponent(SGBaseTE gate, boolean local) {
        this.gate = gate;

        //window without title
        setBackground(SGComponent.defaultBackground(this));

        UILabel title = UILabel.builder()
                .parent(this)
                .text(local ? "sgcraft.gui.gdo.label.localGateControl" : "sgcraft.gui.gdo.label.remoteGateControl")
                .textColor(0xFFFFFF)
                .scale(1.1F)
                .topCenter(0, 5)
                .build();

        UIImage gateStatus = UIImage.builder()
                .parent(this)
                .centered()
                .below(title, 5)
                .size(110, 110)
                .icon(this::gateStatus)
                .build();

        UILabel address = UILabel.builder()
                .parent(this)
                .centered()
                .below(gateStatus, 5)
                .text(this::address)
                .textColor(0x5555FF)
                .scale(1.3F)
                .build();

        UIButton irisControl = UIButton.builder()
                .parent(this)
                .leftAligned()
                .below(address, 5)
                .size(70, 20)
                .text(this::irisControlText)
                .enabled(this::hasIrisAccess)
                .onClick(this::openCloseIris)
                .build();

        if (local) {
            UIButton.builder()
                    .parent(this)
                    .rightOf(irisControl, 5)
                    .below(address, 5)
                    .size(70, 20)
                    .text("sgcraft.gui.button.disconnect")
                    .enabled(this::isGateConnected)
                    .onClick(this::disconnectGate)
                    .build();
        }

    }

    public void setGate(SGBaseTE gate) {
        this.gate = gate;
    }

    public void setIrisAccess(boolean irisAccess) {
        this.irisAccess = irisAccess;
    }

    public GuiIcon gateStatus() {
        if (gate == null) {
            return GuiIcon.NONE;
        }

        return gateTextures.computeIfAbsent(gateStatusLabel(), s -> GuiIcon.full(new GuiTexture(new ResourceLocation("sgcraft:" + s))));
    }

    private String gateStatusLabel() {
        String status = "textures/";
        status += gate.gateType == 2 ? "pegasus_" : "milkyway_";
        status += isGateConnected() ? "connected_" : "disconnected_";
        status += gate.hasIrisUpgrade ? "iris_" + (gate.irisIsClosed() ? "closed" : "open") : "no_iris";
        status += ".png";
        return status;
    }

    private String address() {
        return gate != null ? SGAddressing.formatAddress(gate.homeAddress, "-", "-") : "";
    }

    private String irisControlText() {
        if (gate == null) {
            return "";
        }

        return gate.hasIrisUpgrade && gate.irisIsClosed() ? "sgcraft.gui.button.openIris" : "sgcraft" + ".gui.button.closeIris";
    }

    private boolean hasIrisAccess() {
        return irisAccess && gate != null && gate.hasIrisUpgrade;
    }

    private void openCloseIris() {
        if (gate == null || !gate.hasIrisUpgrade) {
            return;
        }
        GdoNetworkHandler.sendGdoInputToServer(gate, gate.irisIsClosed() ? 1 : 2);
    }

    private boolean isGateConnected() {
        return gate != null && gate.isConnected() && gate.state != SGState.SyncAwait;
    }

    private void disconnectGate() {
        GdoNetworkHandler.sendGdoInputToServer(gate, 3);
    }

    public static GateControlComponentBuilder builder(SGBaseTE gate, boolean local) {
        return new GateControlComponentBuilder(gate, local);
    }

    public static class GateControlComponentBuilder extends UIContainerBuilderG<GateControlComponentBuilder, GateControlComponent> {

        private final SGBaseTE gate;
        private final boolean local;
        private boolean irisAccess;

        private GateControlComponentBuilder(SGBaseTE gate, boolean local) {
            this.gate = gate;
            this.local = local;
            width(155); // by default
            padding(Padding.of(5));
        }

        public GateControlComponentBuilder irisAccess(boolean irisAccess) {
            this.irisAccess = irisAccess;
            return this;
        }

        @Override
        public GateControlComponent build() {
            GateControlComponent gcc = build(new GateControlComponent(gate, local));
            gcc.setIrisAccess(irisAccess);
            return gcc;
        }
    }

}
