package gcewing.sg.features.configurator.client.gui;

import com.google.common.eventbus.Subscribe;
import gcewing.sg.network.SGChannel;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGAddressing;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ConfiguratorScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin;
    private BasicForm form, numericOptionsArea, checkboxOptionsArea;
    private UIButton localIrisOpenButton, localGateCloseButton, localIrisCloseButton, remoteIrisOpenButton, remoteGateCloseButton, remoteIrisCloseButton;
    private UILabel gateAddressLabel;
    private UICheckBox oneWayTravelCheckbox, irisUpgradeCheckbox, chevronUpgradeCheckbox, gateTypeCheckbox, reverseWormholeKillsCheckbox, canBeDialedCheckbox, closeFromEitherEndCheckbox, preserveInventoryCheckbox, noPowerRequiredCheckbox;
    private BlockPos location;
    private World world;
    private EntityPlayer player;

    public ConfiguratorScreen(EntityPlayer player, World worldIn,  boolean isAdmin) {
        this.player = player;
        this.isAdmin = isAdmin;
        this.world = worldIn;
        this.location = new BlockPos(player.posX, player.posY, player.posZ);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new BasicForm(this, 500, 225, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Configurator");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);
        // ****************************************************************************************************************************
        // Numeric Optons Area
        // ****************************************************************************************************************************
        numericOptionsArea = new BasicForm(this, 245, 185, "");
        numericOptionsArea.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        numericOptionsArea.setMovable(false);
        numericOptionsArea.setClosable(false);
        numericOptionsArea.setBorder(FontColors.WHITE, 1, 185);
        numericOptionsArea.setBackgroundAlpha(215);
        numericOptionsArea.setBottomPadding(3);
        numericOptionsArea.setRightPadding(3);
        numericOptionsArea.setTopPadding(3);
        numericOptionsArea.setLeftPadding(3);

        int padding = 15;

        final UILabel numericValuesLabel = new UILabel(this, "Numeric Values");
        numericValuesLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        numericValuesLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        final UISeparator valuesSeparator = new UISeparator(this);
        valuesSeparator.setSize(this.numericOptionsArea.getWidth() - 15, 1);
        valuesSeparator.setPosition(0, 10, Anchor.TOP | Anchor.CENTER);

        final UILabel secondsToStayOpenLabel = new UILabel(this, "Seconds to Stay Open:");
        secondsToStayOpenLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        secondsToStayOpenLabel.setPosition(10, numericValuesLabel.getY() + padding + 5, Anchor.LEFT | Anchor.TOP);

        final UILabel gateRotationSpeedLabel = new UILabel(this, "Gate Rotation Speed:");
        gateRotationSpeedLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        gateRotationSpeedLabel.setPosition(10, secondsToStayOpenLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        final UILabel energyBufferMaxSizeLabel = new UILabel(this, "Energy Buffer Size:");
        energyBufferMaxSizeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        energyBufferMaxSizeLabel.setPosition(10, gateRotationSpeedLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        final UILabel energyPerItemLabel = new UILabel(this, "Energy per Naquadah:");
        energyPerItemLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        energyPerItemLabel.setPosition(10, energyBufferMaxSizeLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        final UILabel energyPerOpeningLabel = new UILabel(this, "Energy Per Opening:");
        energyPerOpeningLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        energyPerOpeningLabel.setPosition(10, energyPerItemLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        final UILabel distanceFactorMultiplierLabel = new UILabel(this, "Distance Factor Multiplier:");
        distanceFactorMultiplierLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        distanceFactorMultiplierLabel.setPosition(10, energyPerOpeningLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        final UILabel interDimensionalMultiplierLabel = new UILabel(this, "Inter-Dimensional Multiplier:");
        interDimensionalMultiplierLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        interDimensionalMultiplierLabel.setPosition(10, distanceFactorMultiplierLabel.getY() + padding, Anchor.LEFT | Anchor.TOP);

        numericOptionsArea.add(numericValuesLabel, valuesSeparator, secondsToStayOpenLabel, gateRotationSpeedLabel, energyBufferMaxSizeLabel, energyPerItemLabel, energyPerOpeningLabel, distanceFactorMultiplierLabel, interDimensionalMultiplierLabel);

        checkboxOptionsArea = new BasicForm(this, 245, 185, "");
        checkboxOptionsArea.setPosition(0, 0, Anchor.RIGHT | Anchor.MIDDLE);
        checkboxOptionsArea.setMovable(false);
        checkboxOptionsArea.setClosable(false);
        checkboxOptionsArea.setBorder(FontColors.WHITE, 1, 185);
        checkboxOptionsArea.setBackgroundAlpha(215);
        checkboxOptionsArea.setBottomPadding(3);
        checkboxOptionsArea.setRightPadding(3);
        checkboxOptionsArea.setTopPadding(3);
        checkboxOptionsArea.setLeftPadding(3);

        int checkboxIndentPadding = 25;

        final UILabel booleanValuesLabel = new UILabel(this, "Boolean Values");
        booleanValuesLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        booleanValuesLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        final UISeparator checkboxSeparator = new UISeparator(this);
        checkboxSeparator.setSize(this.checkboxOptionsArea.getWidth() - 15, 1);
        checkboxSeparator.setPosition(0, 10, Anchor.TOP | Anchor.CENTER);

        oneWayTravelCheckbox = new UICheckBox(this);
        oneWayTravelCheckbox.setText(TextFormatting.WHITE + "One Way Travel Only");
        oneWayTravelCheckbox.setPosition(checkboxIndentPadding, 20, Anchor.LEFT | Anchor.TOP);
        //oneWayTravelCheckbox.setChecked();
        oneWayTravelCheckbox.setName("checkbox.onewaytravel");
        oneWayTravelCheckbox.register(this);

        irisUpgradeCheckbox = new UICheckBox(this);
        irisUpgradeCheckbox.setText(TextFormatting.WHITE + "Iris Upgrade");
        irisUpgradeCheckbox.setPosition(checkboxIndentPadding, oneWayTravelCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //irisUpgradeCheckbox.setChecked();
        irisUpgradeCheckbox.setName("checkbox.irisupgrade");
        irisUpgradeCheckbox.register(this);

        chevronUpgradeCheckbox = new UICheckBox(this);
        chevronUpgradeCheckbox.setText(TextFormatting.WHITE + "Chevron Upgrade");
        chevronUpgradeCheckbox.setPosition(checkboxIndentPadding, irisUpgradeCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //chevronUpgradeCheckbox.setChecked();
        chevronUpgradeCheckbox.setName("checkbox.chevronupgrade");
        chevronUpgradeCheckbox.register(this);

        gateTypeCheckbox = new UICheckBox(this);
        gateTypeCheckbox.setText(TextFormatting.WHITE + "Pegasus Gate Type");
        gateTypeCheckbox.setPosition(checkboxIndentPadding, chevronUpgradeCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //gateTypeCheckbox.setChecked();
        gateTypeCheckbox.setName("checkbox.gateType");
        gateTypeCheckbox.register(this);

        reverseWormholeKillsCheckbox = new UICheckBox(this);
        reverseWormholeKillsCheckbox.setText(TextFormatting.WHITE + "Reverse Wormhole Kills");
        reverseWormholeKillsCheckbox.setPosition(checkboxIndentPadding, gateTypeCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //reverseWormholeKillsCheckbox.setChecked();
        reverseWormholeKillsCheckbox.setName("checkbox.reversekills");
        reverseWormholeKillsCheckbox.register(this);

        canBeDialedCheckbox = new UICheckBox(this);
        canBeDialedCheckbox.setText(TextFormatting.WHITE + "Can be dialed to");
        canBeDialedCheckbox.setPosition(checkboxIndentPadding, reverseWormholeKillsCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //canBeDialedCheckbox.setChecked();
        canBeDialedCheckbox.setName("checkbox.canbedialedto");
        canBeDialedCheckbox.register(this);

        closeFromEitherEndCheckbox = new UICheckBox(this);
        closeFromEitherEndCheckbox.setText(TextFormatting.WHITE + "Close from Either End");
        closeFromEitherEndCheckbox.setPosition(checkboxIndentPadding, canBeDialedCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //closeFromEitherEndCheckbox.setChecked();
        closeFromEitherEndCheckbox.setName("checkbox.canbedialedto");
        closeFromEitherEndCheckbox.register(this);

        preserveInventoryCheckbox = new UICheckBox(this);
        preserveInventoryCheckbox.setText(TextFormatting.WHITE + "Preserve Inventory on Iris Death");
        preserveInventoryCheckbox.setPosition(checkboxIndentPadding, closeFromEitherEndCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //preserveInventoryCheckbox.setChecked();
        preserveInventoryCheckbox.setName("checkbox.canbedialedto");
        preserveInventoryCheckbox.register(this);

        noPowerRequiredCheckbox = new UICheckBox(this);
        noPowerRequiredCheckbox.setText(TextFormatting.WHITE + "No Input Power Required");
        noPowerRequiredCheckbox.setPosition(checkboxIndentPadding, preserveInventoryCheckbox.getY() + padding, Anchor.LEFT | Anchor.TOP);
        //noPowerRequiredCheckbox.setChecked();
        noPowerRequiredCheckbox.setName("checkbox.nopowerrequired");
        noPowerRequiredCheckbox.register(this);

        checkboxOptionsArea.add(booleanValuesLabel, checkboxSeparator, oneWayTravelCheckbox, irisUpgradeCheckbox, chevronUpgradeCheckbox, gateTypeCheckbox, reverseWormholeKillsCheckbox, canBeDialedCheckbox, closeFromEitherEndCheckbox, preserveInventoryCheckbox, noPowerRequiredCheckbox);

        // Test Feature button
        final UIButton buttonTest = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .text("TEST")
            .listener(this)
            .build("button.test");

        gateAddressLabel = new UILabel(this, "");
        gateAddressLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(1.1F).build());
        gateAddressLabel.setPosition(0, -2, Anchor.CENTER | Anchor.BOTTOM);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, numericOptionsArea, checkboxOptionsArea, buttonTest, gateAddressLabel, buttonClose);
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
        TileEntity localGate = GateUtil.locateLocalGate(this.world, this.location, 6, true);
        if (localGate != null) {
            if (localGate instanceof SGBaseTE) {
                this.gateAddressLabel.setText(SGAddressing.formatAddress(((SGBaseTE) localGate).homeAddress, "-", "-"));
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
