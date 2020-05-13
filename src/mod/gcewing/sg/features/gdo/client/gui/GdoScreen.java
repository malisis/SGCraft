package gcewing.sg.features.gdo.client.gui;

import gcewing.sg.SGCraft;
import gcewing.sg.features.ego.SGWindow;
import gcewing.sg.features.gdo.network.GdoNetworkHandler;
import gcewing.sg.network.GuiNetworkHandler;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.IrisState;
import gcewing.sg.util.SGAddressing;
import gcewing.sg.util.SGState;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.GuiIcon;
import net.malisis.ego.gui.render.GuiTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class GdoScreen extends MalisisGui {
    private int lastUpdate = 0;
    private boolean isAdmin;
    private boolean debugScreen = false;
    private UIContainer form, localGateControlArea, remoteGateControlArea;
    private UIButton localIrisOpenButton, localGateCloseButton, localIrisCloseButton, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton;
    private UIImage localGateImage, remoteGateImage;
    private UILabel localGateAddressLabel, remoteGateAddressLabel;
    private BlockPos pos;
    private World world;
    private EntityPlayer player;
    public boolean isRemoteConnected, r_hasIrisUpgrade, r_hasChevronUpgrade, r_isIrisClosed, canAccessLocal, canAccessRemote;
    public String r_address;
    public int r_gateType;
    public SGBaseTE localGate;
    private GuiIcon mw_disconnected_no_iris, p_disconnected_no_iris, mw_connected_no_iris, p_connected_no_iris, mw_disconnected_iris_closed, p_disconnected_iris_closed;
    private GuiIcon mw_connected_iris_closed, p_connected_iris_closed, mw_disconnected_iris_open, p_disconnected_iris_open, mw_connected_iris_open, p_connected_iris_open;

    public GdoScreen(BlockPos pos, EntityPlayer player, World worldIn,  boolean isAdmin, boolean isRemoteConnected, boolean r_hasIrisUpgrade, boolean r_hasChevronUpgrade,
        boolean r_isIrisClosed, int r_gateType, String r_address, boolean canAccessLocal, boolean canAccessRemote) {
        this.pos = pos;
        this.player = player;
        this.isAdmin = isAdmin;
        this.world = worldIn;
        this.isRemoteConnected = isRemoteConnected;
        this.r_hasIrisUpgrade = r_hasIrisUpgrade;
        this.r_hasChevronUpgrade = r_hasChevronUpgrade;
        this.r_isIrisClosed = r_isIrisClosed;
        this.r_gateType = r_gateType;
        this.r_address = r_address;
        this.canAccessLocal = canAccessLocal;
        this.canAccessRemote = canAccessRemote;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.background = null;

        // Icon Construction
        mw_disconnected_no_iris = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_no_iris.png")));
        p_disconnected_no_iris = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_no_iris.png")));
        mw_connected_no_iris = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_no_iris.png")));
        p_connected_no_iris = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_no_iris.png")));
        mw_disconnected_iris_closed = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_closed.png")));
        p_disconnected_iris_closed = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_closed.png")));
        mw_connected_iris_closed = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_closed.png")));
        p_connected_iris_closed = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_closed.png")));
        mw_disconnected_iris_open = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_open.png")));
        p_disconnected_iris_open = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_open.png")));
        mw_connected_iris_open = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_open.png")));
        p_connected_iris_open = GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_open.png")));

        Keyboard.enableRepeatEvents(true);
        localGate = SGBaseTE.get(player.world, pos);

        if (localGate != null) {
            if (!localGate.isMerged) { // Block GDO usage when gate is not merged.
                SGBaseTE.sendGenericErrorMsg(player, "No Local Stargate within range.");
                return;
            }
        } else {
            SGBaseTE.sendGenericErrorMsg(player, "No Local Stargate within range.");
            return;
        }

        // Master Panel
        this.form = SGWindow.builder("sgcraft.gui.gdo.label.gdoController")
            .middleCenter()
            .size(400, 225)
            .build();

        UILabel titleLabel = UILabel.builder()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.gdo.label.gdoController"))
            .topCenter(0, -15)
            .textColor(TextFormatting.WHITE)
            .shadow(true)
            .scale(1.1F)
            .build();

        // ****************************************************************************************************************************
        // Local Gate Control
        // ****************************************************************************************************************************
        localGateControlArea = SGWindow.builder()
            .parent(form)
            .topCenter()
            .size(190, 175)
            .build();

        final UILabel localGateControlLabel = UILabel.builder()
            .parent(localGateControlArea)
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.gdo.label.localGateControl"))
            .shadow(true)
            .topCenter()
            .textColor(TextFormatting.WHITE)
            .scale(1.1F)
            .build();

       localGateImage = UIImage.builder()
            .parent(localGateControlArea)
            .topCenter(0,20)
            .size(110, 110)
            .icon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/gui_image1.png"))))
            .build();

        localGateAddressLabel = UILabel.builder()
            .centered()
            .parent(localGateControlArea)
            .text("gateAddress")
            .shadow(true)
            .below(localGateImage, 5)
            .textColor(TextFormatting.BLUE)
            .scale(1.3F)
            .build();

        localIrisOpenButton = UIButton.builder()
            .parent(localGateControlArea)
            .bottomLeft()
            .shadow(true)
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.openIris"))
            .enabled(this.canAccessLocal)
            .onClick(() -> {
                GdoNetworkHandler.sendGdoInputToServer(localGate, 1);
            })
            .build();

        localGateCloseButton = UIButton.builder()
            .parent(localGateControlArea)
            .bottomCenter()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.disconnect"))
            .onClick(() -> {
                GdoNetworkHandler.sendGdoInputToServer((SGBaseTE)localGate, 3);
            })
            .build();

        localIrisCloseButton = UIButton.builder()
            .parent(localGateControlArea)
            .bottomRight()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.closeIris"))
            .enabled(this.canAccessLocal)
            .onClick(() -> {
                GdoNetworkHandler.sendGdoInputToServer(localGate, 2);
            })
            .build();

        localGateControlArea.add(localGateControlLabel, localGateImage, localGateAddressLabel, localIrisOpenButton, localGateCloseButton, localIrisCloseButton);

        // ****************************************************************************************************************************
        // Remote Gate Control
        // ****************************************************************************************************************************
        remoteGateControlArea = SGWindow.builder()
            .parent(form)
            .middleRight()
            .size(190, 175)
            .build();

        final UILabel remoteGateControlLabel = UILabel.builder()
            .parent(remoteGateControlArea)
            .centered()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.gdo.label.remoteGateControl"))
            .topCenter()
            .textColor(TextFormatting.WHITE)
            .scale(1.1F)
            .build();

        remoteGateImage = UIImage.builder()
            .parent(remoteGateControlArea)
            .centered()
            .topCenter(0,20)
            .size(110, 110)
            .icon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/gui_image1.png"))))
            .build();

        remoteGateAddressLabel = UILabel.builder()
            .parent(remoteGateControlArea)
            .centered()
            .below(localGateImage, 5)
            .text("gateAddress")
            .shadow(true)
            .textColor(TextFormatting.BLUE)
            .scale(1.3F)
            .build();

        remoteIrisOpenButton = UIButton.builder()
            .parent(remoteGateControlArea)
            .bottomLeft()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.openIris"))
            .enabled(this.canAccessRemote)
            .onClick(() -> {
                GdoNetworkHandler.sendGdoInputToServer(localGate, 4);
            })
            .build();

        remoteGateCloseButton = UIButton.builder()
            .parent(remoteGateControlArea)
            .bottomCenter()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.disconnect"))
            .visible(false)
            .onClick(() -> {
                // Do Nothing at the moment
            })
            .build();

        remoteIrisCloseButton = UIButton.builder()
            .parent(remoteGateControlArea)
            .bottomRight()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.closeIris"))
            .enabled(this.canAccessRemote)
            .onClick(() -> {
                GdoNetworkHandler.sendGdoInputToServer(localGate, 5);
            })
            .build();

        remoteGateControlArea.add(remoteGateControlLabel, remoteGateImage, remoteGateAddressLabel, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton);

        // ****************************************************************************************************************************

        // Close button
        final UIButton buttonClose = UIButton.builder()
            .width(40)
            .bottomRight()
            .text(TextFormatting.WHITE + I18n.format("sgcraft.gui.button.close"))
            .onClick(() -> {
                this.close();
            })
            .build();

        this.form.add(titleLabel, localGateControlArea, remoteGateControlArea, buttonClose);
        addToScreen(this.form);
        this.refresh();
    }

    private void refresh() {

        if (localGate != null) {
            if (localGate.isMerged) {
                this.localGateAddressLabel.setText(SGAddressing.formatAddress(localGate.homeAddress, "-", "-"));
            }

            // Disconnected No Iris
            if (!localGate.isConnected() && !localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1))
                this.localGateImage.setIcon(mw_disconnected_no_iris);
            if (!localGate.isConnected() && !localGate.hasIrisUpgrade && localGate.gateType == 2)
                this.localGateImage.setIcon(p_disconnected_no_iris);
            // Connected No Iris
            if (localGate.isConnected() && !localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1))
                this.localGateImage.setIcon(mw_connected_no_iris);
            if (localGate.isConnected() && !localGate.hasIrisUpgrade && localGate.gateType == 2)
                this.localGateImage.setIcon(p_connected_no_iris);

            // Disconnected Iris Closed
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && localGate.irisIsClosed())
                this.localGateImage.setIcon(mw_disconnected_iris_closed);
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && localGate.irisIsClosed())
                this.localGateImage.setIcon(p_disconnected_iris_closed);
            // Connected Iris Closed
            if (localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && localGate.irisIsClosed())
                this.localGateImage.setIcon(mw_connected_iris_closed);
            if (localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && localGate.irisIsClosed())
                this.localGateImage.setIcon(p_connected_iris_closed);

            // Disconnected Iris Open
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && !localGate.irisIsClosed())
                this.localGateImage.setIcon(mw_disconnected_iris_open);
            if (!localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && !localGate.irisIsClosed())
                this.localGateImage.setIcon(p_disconnected_iris_open);
            // Connected Iris Open
            if (localGate.isConnected() && localGate.hasIrisUpgrade && (localGate.gateType == 0 || localGate.gateType == 1) && !localGate.irisIsClosed())
                this.localGateImage.setIcon(mw_connected_iris_open);
            if (localGate.isConnected() && localGate.hasIrisUpgrade && localGate.gateType == 2 && !localGate.irisIsClosed())
                this.localGateImage.setIcon(p_connected_iris_open);

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
                this.form.setSize(Size.of(200, 225));
                this.remoteGateControlArea.setVisible(false);

            } else if (isRemoteConnected) {
                this.form.setSize(Size.of(400, 225));
                this.localGateControlArea.setPosition(Position.topLeft(localGateControlArea));
                this.remoteGateControlArea.setPosition(Position.topRight(remoteGateControlArea));
                this.remoteGateControlArea.setVisible(true);
                this.remoteGateAddressLabel.setText(r_address);

                // Disconnected No Iris
                if (!r_hasIrisUpgrade && (r_gateType == 0 || r_gateType == 1))
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_no_iris.png"))));
                if (!r_hasIrisUpgrade && r_gateType == 2)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_no_iris.png"))));
                // Connected No Iris
                if (!r_hasIrisUpgrade && (r_gateType == 0 || r_gateType == 1))
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_no_iris.png"))));
                if (!r_hasIrisUpgrade && r_gateType == 2)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_no_iris.png"))));

                // Disconnected Iris Closed
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_closed.png"))));
                if (r_hasIrisUpgrade && r_gateType == 2 && r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_closed.png"))));
                // Connected Iris Closed
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_closed.png"))));
                if (r_hasIrisUpgrade && r_gateType == 2 && r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_closed.png"))));

                // Disconnected Iris Open
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_disconnected_iris_open.png"))));
                if (r_hasIrisUpgrade && r_gateType == 2 && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_disconnected_iris_open.png"))));
                // Connected Iris Open
                if (r_hasIrisUpgrade && (r_gateType == 0 || r_gateType ==1) && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/milkyway_connected_iris_open.png"))));
                if (r_hasIrisUpgrade && r_gateType == 2 && !r_isIrisClosed)
                    this.remoteGateImage.setIcon(GuiIcon.full(new GuiTexture(SGCraft.mod.resourceLocation("textures/pegasus_connected_iris_open.png"))));

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
    public void update() {
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
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
