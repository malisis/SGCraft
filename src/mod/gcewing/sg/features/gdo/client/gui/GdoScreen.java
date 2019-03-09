package gcewing.sg.features.gdo.client.gui;

import com.google.common.eventbus.Subscribe;
import gcewing.sg.util.IrisState;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.network.SGChannel;
import gcewing.sg.SGCraft;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import gcewing.sg.util.GateUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GdoScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin;
    private boolean debugScreen = false;
    private BasicForm form, localGateControlArea, remoteGateControlArea;
    private UIButton localIrisOpenButton, localGateCloseButton, localIrisCloseButton, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton;
    private UIImage localGateImage, remoteGateImage;
    private UILabel localGateAddressLabel, remoteGateAddressLabel;
    private BlockPos location;
    private World world;
    private EntityPlayer player;
    private boolean isRemoteConnected, r_hasIrisUpgrade, r_hasChevronUpgrade, r_isIrisClosed;
    private String r_address;
    private int r_gateType;

    public GdoScreen(EntityPlayer player, World worldIn,  boolean isAdmin, boolean isRemoteConnected, boolean r_hasIrisUpgrade, boolean r_hasChevronUpgrade, boolean r_isIrisClosed, int r_gateType, String r_address) {
        this.player = player;
        this.isAdmin = isAdmin;
        this.world = worldIn;
        this.location = new BlockPos(player.posX, player.posY, player.posZ);
        this.isRemoteConnected = isRemoteConnected;
        this.r_hasIrisUpgrade = r_hasIrisUpgrade;
        this.r_hasChevronUpgrade = r_hasChevronUpgrade;
        this.r_isIrisClosed = r_isIrisClosed;
        this.r_gateType = r_gateType;
        this.r_address = r_address;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new BasicForm(this, 400, 225, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "GDO Controller");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);
        // ****************************************************************************************************************************
        // Local Gate Control
        // ****************************************************************************************************************************
        localGateControlArea = new BasicForm(this, 195, 185, "");
        localGateControlArea.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        localGateControlArea.setMovable(false);
        localGateControlArea.setClosable(false);
        localGateControlArea.setBorder(FontColors.WHITE, 1, 185);
        localGateControlArea.setBackgroundAlpha(215);
        localGateControlArea.setBottomPadding(3);
        localGateControlArea.setRightPadding(3);
        localGateControlArea.setTopPadding(3);
        localGateControlArea.setLeftPadding(3);

        final UILabel localGateControlLabel = new UILabel(this, "Local Gate Control");
        localGateControlLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        localGateControlLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        localGateImage = new UIImage(this, new GuiTexture(SGCraft.mod.resourceLocation("textures/gui_image1.png")), null);
        localGateImage.setSize(110, 110);
        localGateImage.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        localGateAddressLabel = new UILabel(this, "gateAddress");
        localGateAddressLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.3F).build());
        localGateAddressLabel.setPosition(0, -30, Anchor.CENTER | Anchor.BOTTOM);

        localIrisOpenButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("Open Iris")
            .listener(this)
            .build("button.local.iris.open");

        localGateCloseButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .text("Disconnect")
            .listener(this)
            .build("button.local.gate.disconnect");

        localIrisCloseButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close Iris")
            .listener(this)
            .build("button.local.iris.close");

        localGateControlArea.add(localGateControlLabel, localGateImage, localGateAddressLabel, localIrisOpenButton, localGateCloseButton, localIrisCloseButton);

        // ****************************************************************************************************************************
        // Remote Gate Control
        // ****************************************************************************************************************************
        remoteGateControlArea = new BasicForm(this, 195, 185, "");
        remoteGateControlArea.setPosition(0, 0, Anchor.RIGHT | Anchor.MIDDLE);
        remoteGateControlArea.setMovable(false);
        remoteGateControlArea.setClosable(false);
        remoteGateControlArea.setBorder(FontColors.WHITE, 1, 185);
        remoteGateControlArea.setBackgroundAlpha(215);
        remoteGateControlArea.setBottomPadding(3);
        remoteGateControlArea.setRightPadding(3);
        remoteGateControlArea.setTopPadding(3);
        remoteGateControlArea.setLeftPadding(3);

        final UILabel remoteGateControlLabel = new UILabel(this, "Remote Gate Control");
        remoteGateControlLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        remoteGateControlLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        remoteGateImage = new UIImage(this, new GuiTexture(SGCraft.mod.resourceLocation("textures/gui_image1.png")), null);  // Fake Image
        remoteGateImage.setSize(110, 110);
        remoteGateImage.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        remoteGateAddressLabel = new UILabel(this, "gateAddress"); // Incorrect Value
        remoteGateAddressLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.3F).build());
        remoteGateAddressLabel.setPosition(0, -30, Anchor.CENTER | Anchor.BOTTOM);

        remoteIrisOpenButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("Open Iris")
            .listener(this)
            .build("button.remote.iris.open");

        remoteGateCloseButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .text("Disconnect")
            .listener(this)
            .visible(false)
            .build("button.remote.gate.disconnect");

        remoteIrisCloseButton = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close Iris")
            .listener(this)
            .build("button.remote.iris.close");

        remoteGateControlArea.add(remoteGateControlLabel, remoteGateImage, remoteGateAddressLabel, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton);

        // ****************************************************************************************************************************

        // Test Feature button
        final UIButton buttonTest = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("TEST")
            .listener(this)
            .build("button.test");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, localGateControlArea, remoteGateControlArea, buttonTest, buttonClose);
        addToScreen(this.form);
        this.refresh();
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);

        if (localGate == null) {
            return;
        }

        if (!(localGate instanceof SGBaseTE)) {
            return;
        }

        switch (event.getComponent().getName().toLowerCase()) {

            case "button.local.iris.open":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 1);
                break;

            case "button.local.gate.disconnect":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 3);
                break;

            case "button.local.iris.close":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 2);
                break;

            case "button.remote.iris.open":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 4);
                break;

            case "button.remote.gate.disconnect":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 6);
                break;

            case "button.remote.iris.close":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 5);
                break;

            case "button.test":
                SGChannel.sendGdoInputToServer((SGBaseTE)localGate, 7);
                break;

            case "button.close":
                this.close();
                break;
        }
    }

    private void refresh() {
        TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, false);
        if (localGate != null) {
            if (localGate instanceof SGBaseTE) {
                if (((SGBaseTE) localGate).isConnected() && ((SGBaseTE) localGate).state == SGState.Connected) {
                    this.form.setWidth(400);
                    this.remoteGateControlArea.setVisible(true);
                } else {
                    this.form.setWidth(200);
                    this.remoteGateControlArea.setVisible(false);
                }
                this.localGateAddressLabel.setText(SGAddressing.formatAddress(((SGBaseTE) localGate).homeAddress, "-", "-"));

                // Disconnected No Iris
                if (!((SGBaseTE) localGate).isConnected() && (!((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1)
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_no_iris.png")), null);
                if (!((SGBaseTE) localGate).isConnected() && (!((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2)
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_no_iris.png")), null);
                // Connected No Iris
                if (((SGBaseTE) localGate).isConnected() && (!((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1)
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_no_iris.png")), null);
                if (((SGBaseTE) localGate).isConnected() && (!((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2)
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_no_iris.png")), null);

                // Disconnected Iris Closed
                if (!((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1 && ((SGBaseTE) localGate).irisIsClosed())
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_closed.png")), null);
                if (!((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2 && ((SGBaseTE) localGate).irisIsClosed())
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_closed.png")), null);
                // Connected Iris Closed
                if (((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1 && ((SGBaseTE) localGate).irisIsClosed())
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_closed.png")), null);
                if (((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2 && ((SGBaseTE) localGate).irisIsClosed())
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_closed.png")), null);

                // Disconnected Iris Open
                if (!((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1 && (!((SGBaseTE) localGate).irisIsClosed()))
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_open.png")), null);
                if (!((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2 && (!((SGBaseTE) localGate).irisIsClosed()))
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_open.png")), null);
                // Connected Iris Open
                if (((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 1 && (!((SGBaseTE) localGate).irisIsClosed()))
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_open.png")), null);
                if (((SGBaseTE) localGate).isConnected() && (((SGBaseTE) localGate).hasIrisUpgrade) && ((SGBaseTE) localGate).gateType == 2 && (!((SGBaseTE) localGate).irisIsClosed()))
                    this.localGateImage.setIcon(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_open.png")), null);

                if (!((SGBaseTE) localGate).hasIrisUpgrade) {
                    this.localIrisOpenButton.setEnabled(false);
                    this.localIrisCloseButton.setEnabled(false);
                } else {
                    if (((SGBaseTE) localGate).irisState == IrisState.Closing || ((SGBaseTE) localGate).irisState == IrisState.Opening) {
                        this.localIrisOpenButton.setEnabled(false);
                        this.localIrisCloseButton.setEnabled(false);
                    } else {
                        if (((SGBaseTE) localGate).irisIsClosed()) {
                            this.localIrisOpenButton.setEnabled(true);
                            this.localIrisCloseButton.setEnabled(false);
                        } else {
                            this.localIrisOpenButton.setEnabled(false);
                            this.localIrisCloseButton.setEnabled(true);
                        }
                    }
                }
                if (!((SGBaseTE) localGate).isConnected() || ((SGBaseTE) localGate).state == SGState.Dialling || ((SGBaseTE) localGate).state == SGState.Disconnecting) {
                    this.remoteGateImage.setVisible(false);
                    this.localGateCloseButton.setEnabled(false);
                    this.remoteGateCloseButton.setEnabled(false);
                    this.remoteIrisOpenButton.setEnabled(false);
                    this.remoteIrisCloseButton.setEnabled(false);

                } else if (((SGBaseTE) localGate).isConnected()) {
                    this.remoteGateImage.setVisible(true);
                    this.localGateCloseButton.setEnabled(true);
                    this.remoteGateCloseButton.setEnabled(true);

                    //SGBaseTE remoteGate = ((SGBaseTE) localGate).getConnectedStargateTE();
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
                        //if (remoteGate.irisState == IrisState.Closing || remoteGate.irisState == IrisState.Opening) {
                        //    this.remoteIrisOpenButton.setEnabled(false);
                        //    this.remoteIrisCloseButton.setEnabled(false);
                        //} else {
                            if (r_isIrisClosed) {
                                this.remoteIrisOpenButton.setEnabled(true);
                                this.remoteIrisCloseButton.setEnabled(false);
                            } else {
                                this.remoteIrisOpenButton.setEnabled(false);
                                this.remoteIrisCloseButton.setEnabled(true);
                            }
                        //}
                    }
                }
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

        if (this.lastUpdate == 25) {
            this.refresh();
        }

        if (++this.lastUpdate > 30) {
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
