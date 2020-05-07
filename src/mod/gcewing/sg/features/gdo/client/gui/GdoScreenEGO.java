package gcewing.sg.features.gdo.client.gui;

import gcewing.sg.features.ego.SGWindow;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGState;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class GdoScreenEGO extends MalisisGui
{
	public SGBaseTE localGate;
	public SGBaseTE remoteGate;
	public GateControlComponent localComponent, remoteComponent;
	public UIButton closeButton;

	public GdoScreenEGO()
	{
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		localGate = GateUtil.findGate(player.world, player, 6);
	}

	public void construct()
	{
		if (localGate == null || !localGate.isMerged)
		{
			addToScreen(SGWindow.errorWindow("No local Stargate within range."));
			return;
		}

		UIContainer window = SGWindow.builder("sgcraft.gui.gdo.label.gdoController")
									 .middleCenter()
									 //.size(400, 225)
									 .build();

		localComponent = GateControlComponent.builder(localGate, true)
											 .parent(window)
											 .build();

		remoteGate = localGate.getConnectedStargateTE();
		remoteComponent = GateControlComponent.builder(remoteGate, false)
											  .parent(window)
											  .rightOf(localComponent, 5)
											  .visible(() -> remoteGate != null)
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

	private int closeButtonPosition()
	{
		UIComponent rel = localGate.isConnected() && localGate.state != SGState.SyncAwait ? remoteComponent : localComponent;

		return rel.position()
				  .x() + rel.size()
							.width() - closeButton.size()
												  .width();
	}

	@Override
	public void update()
	{
		if (localGate == null)
			return;

		if (remoteGate == null && localGate.isConnected() && localGate.state != SGState.SyncAwait)
		{
			remoteGate = localGate.getConnectedStargateTE();
			remoteComponent.setGate(remoteGate);
		}
		if (remoteGate != null && !localGate.isConnected())
		{
			remoteGate = null;
			remoteComponent.setGate(null);
		}
	}
}
/*
        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.close"))
            .onClick(() -> {
                this.close();
            })
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, localGateControlArea, remoteGateControlArea, buttonClose);
        addToScreen(this.form);
        this.refresh();*/
        /*
    }

    private void refresh() {

        if (localGate != null) {
            if (localGate.isMerged) {
                this.localGateAddressLabel.setText(SGAddressing.formatAddress(localGate.homeAddress, "-", "-"));
            }

            // Disconnected No Iris
            if (!localGate.isConnected() && !localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1))
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_no_iris.png")), null);
            if (!localGate.isConnected() && !localGate.hasIrisUpgrade && localGate.gateType == 2)
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_no_iris.png")), null);
            // Connected No Iris
            if (localGate.isConnected() && !localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1))
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_no_iris.png")), null);
            if (localGate.isConnected() && !localGate.hasIrisUpgrade && localGate.gateType == 2)
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_no_iris.png")), null);

            // Disconnected Iris Closed
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_closed.png")), null);
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_closed.png")), null);
            // Connected Iris Closed
            if (localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_closed.png")), null);
            if (localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_closed.png")), null);

            // Disconnected Iris Open
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && !localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_open.png")), null);
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && !localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_open.png")), null);
            // Connected Iris Open
            if (localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && !localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_open.png")), null);
            if (localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && !localGate.irisIsClosed())
                this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_open.png")), null);

            if (!localGate.hasIrisUpgrade) {
                this.localIrisOpenButton.setEnabled(false);
                this.localIrisCloseButton.setEnabled(false);
            } else {
                if (localGate.irisState == IrisState.Closing || localGate.irisState == IrisState.Opening) {
                    this.localIrisOpenButton.setEnabled(false);
                    this.localIrisCloseButton.setEnabled(false);
                } else {
                    if (this.canAccessLocal) {
                        if (localGate.irisIsClosed()) {
                            this.localIrisOpenButton.setEnabled(true);
                            this.localIrisCloseButton.setEnabled(false);
                        } else {
                            this.localIrisOpenButton.setEnabled(false);
                            this.localIrisCloseButton.setEnabled(true);
                        }
                    }
                }
            }
            if (!localGate.isConnected() || localGate.state == SGState.Dialling || localGate.state == SGState.Disconnecting) {
                this.remoteGateImage.setVisible(false);
                this.localGateCloseButton.setEnabled(false);
                this.remoteGateCloseButton.setEnabled(false);
                this.remoteIrisOpenButton.setEnabled(false);
                this.remoteIrisCloseButton.setEnabled(false);
                this.form.setWidth(200);
                this.remoteGateControlArea.setVisible(false);

            } else if (isRemoteConnected) {
                this.form.setWidth(400);
                this.remoteGateControlArea.setVisible(true);
                this.remoteGateAddressLabel.setText(r_address);

                // Disconnected No Iris
                if (!r_hasIrisUpgrade && (r_gateType == 0 || r_gateType == 1))
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_no_iris.png")), null);
                if (!r_hasIrisUpgrade && r_gateType == 2)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_no_iris.png")), null);
                // Connected No Iris
                if (!r_hasIrisUpgrade && (r_gateType == 0 || r_gateType == 1))
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_no_iris.png")), null);
                if (!r_hasIrisUpgrade && r_gateType == 2)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_no_iris.png")), null);

                // Disconnected Iris Closed
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_closed.png")), null);
                if (r_hasIrisUpgrade && r_gateType == 2 && r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_closed.png")), null);
                // Connected Iris Closed
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_closed.png")), null);
                if (r_hasIrisUpgrade && r_gateType == 2 && r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_closed.png")), null);

                // Disconnected Iris Open
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_open.png")), null);
                if (r_hasIrisUpgrade && r_gateType == 2 && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_open.png")), null);
                // Connected Iris Open
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_open.png")), null);
                if (r_hasIrisUpgrade && r_gateType == 2 && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_open.png")), null);

                if (!r_hasIrisUpgrade) {
                    this.remoteIrisOpenButton.setEnabled(false);
                    this.remoteIrisCloseButton.setEnabled(false);
                } else {
                    if (this.canAccessRemote) {
                        if (r_isIrisClosed) {
                            this.remoteIrisOpenButton.setEnabled(true);
                            this.remoteIrisCloseButton.setEnabled(false);
                        } else {
                            this.remoteIrisOpenButton.setEnabled(false);
                            this.remoteIrisCloseButton.setEnabled(true);
                        }
                    }
                }
                this.remoteGateImage.setVisible(true);
                this.localGateCloseButton.setEnabled(true);
                this.remoteGateCloseButton.setEnabled(true);
            }
        }

    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (!localGate.isMerged)
            this.close();

        if (this.lastUpdate == 100) {
            if (localGate != null) {
                GuiNetworkHandler.sendGuiRequestToServer((SGBaseTE) localGate, player, 2);
            }
            if (localGate.isMerged) {
                this.refresh();
            }
        }

        if (++this.lastUpdate > 120) {
            this.lastUpdate = 0;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
*/