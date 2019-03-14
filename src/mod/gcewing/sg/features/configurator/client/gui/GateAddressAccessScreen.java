package gcewing.sg.features.configurator.client.gui;

import com.google.common.eventbus.Subscribe;
import gcewing.sg.features.pdd.client.gui.PddEntryScreen;
import gcewing.sg.features.pdd.network.PddNetworkHandler;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.tileentity.data.GateAccessData;
import gcewing.sg.util.GateUtil;
import gcewing.sg.util.SGAddressing;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GateAddressAccessScreen extends BasicScreen {
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin;
    private BasicForm form;
    private BasicContainer<?> addressContainer;
    private UIButton buttonClose;
    private UILabel addressListLabel, gateDefaultIncomingLabel, gateDefaultOutgoingLabel;
    private UICheckBox defaultAllowIncomingCheckbox, defaultDenyIncomingCheckbox, defaultAllowOutgoingCheckbox, defaultDenyOutgoingCheckbox;
    private BlockPos location;
    private World world;
    private EntityPlayer player;
    private SGBaseTE localGate;

    private BasicList<GateAccessData> gateAccessList;

    public GateAddressAccessScreen(BasicScreen parent, EntityPlayer player, World worldIn, boolean isAdmin) {
        super(parent, true);
        this.player = player;
        this.isAdmin = false;
        this.world = worldIn;
        this.location = new BlockPos(player.posX, player.posY, player.posZ);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);
        final TileEntity localGateTE = GateUtil.locateLocalGate(this.world, this.location, 6, true);
        if (localGateTE instanceof SGBaseTE) {
            localGate = (SGBaseTE) localGateTE;
        }

        // Master Panel
        this.form = new BasicForm(this, 300, 225, "Gate Address Access List");
        this.form.setMovable(true);
        this.form.setBackgroundAlpha(255);

        // ****************************************************************************************************************************
        // Address List Area
        // ****************************************************************************************************************************
        this.addressContainer = new BasicContainer(this, 100, 145);
        this.addressContainer.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        this.addressContainer.setBorder(FontColors.WHITE, 1, 185);
        this.addressContainer.setPadding(0, 3);
        this.addressContainer.setBackgroundAlpha(0);

        this.addressListLabel = new UILabel(this, "Address List");
        this.addressListLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        this.addressListLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        this.gateAccessList = new BasicList<>(this, UIComponent.INHERITED, this.addressContainer.getHeight() - 14);
        this.gateAccessList.setPosition(0, 10);
        this.gateAccessList.setItemComponentFactory(GateItemComponent::new);
        this.gateAccessList.setItemComponentSpacing(1);
        this.gateAccessList.setPadding(2);
        this.gateAccessList.setBorder(FontColors.WHITE, 1, 185);
        this.gateAccessList.setBorders(FontColors.WHITE, 185, 0, 1, 0, 0);

        this.addressContainer.add(this.addressListLabel, this.gateAccessList);

        // ****************************************************************************************************************************

        final UISeparator defaultsSeparator = new UISeparator(this);
        defaultsSeparator.setSize(-60, 1);
        defaultsSeparator.setPosition(0, 10, Anchor.BOTTOM | Anchor.CENTER);

        this.gateDefaultIncomingLabel = new UILabel(this, "Default Incoming:");
        this.gateDefaultIncomingLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        this.gateDefaultIncomingLabel.setPosition(0, -25, Anchor.BOTTOM | Anchor.LEFT);

        this.defaultAllowIncomingCheckbox = new UICheckBox(this);
        this.defaultAllowIncomingCheckbox.setText(TextFormatting.WHITE + "Allow");
        this.defaultAllowIncomingCheckbox.setPosition(this.gateDefaultIncomingLabel.getX() + this.gateDefaultIncomingLabel.getWidth() + 10, this.gateDefaultIncomingLabel.getY(), Anchor.LEFT | Anchor.BOTTOM);
        this.defaultAllowIncomingCheckbox.setName("checkbox.defaultallowincoming");
        this.defaultAllowIncomingCheckbox.setChecked(localGate.defaultAllowIncoming);
        this.defaultAllowIncomingCheckbox.register(this);

        this.defaultDenyIncomingCheckbox = new UICheckBox(this);
        this.defaultDenyIncomingCheckbox.setText(TextFormatting.WHITE + "Deny");
        this.defaultDenyIncomingCheckbox.setPosition(this.gateDefaultIncomingLabel.getX() + this.gateDefaultIncomingLabel.getWidth() + 10 + this.defaultAllowIncomingCheckbox.getWidth() + 10, this.gateDefaultIncomingLabel.getY(), Anchor.LEFT | Anchor.BOTTOM);
        this.defaultDenyIncomingCheckbox.setName("checkbox.defaultdenyincoming");
        this.defaultDenyIncomingCheckbox.setChecked(!localGate.defaultAllowIncoming);
        this.defaultDenyIncomingCheckbox.register(this);

        this.gateDefaultOutgoingLabel = new UILabel(this, "Default Outgoing");
        this.gateDefaultOutgoingLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        this.gateDefaultOutgoingLabel.setPosition(0, -5, Anchor.BOTTOM | Anchor.LEFT);

        this.defaultAllowOutgoingCheckbox = new UICheckBox(this);
        this.defaultAllowOutgoingCheckbox.setText(TextFormatting.WHITE + "Allow");
        this.defaultAllowOutgoingCheckbox.setPosition(this.gateDefaultOutgoingLabel.getX() + this.gateDefaultOutgoingLabel.getWidth() + 10, this.gateDefaultOutgoingLabel.getY(), Anchor.LEFT | Anchor.BOTTOM);
        this.defaultAllowOutgoingCheckbox.setName("checkbox.defaultallowoutgoing");
        this.defaultAllowOutgoingCheckbox.setChecked(localGate.defaultAllowOutgoing);
        this.defaultAllowOutgoingCheckbox.register(this);

        this.defaultDenyOutgoingCheckbox = new UICheckBox(this);
        this.defaultDenyOutgoingCheckbox.setText(TextFormatting.WHITE + "Deny");
        this.defaultDenyOutgoingCheckbox.setPosition(this.gateDefaultOutgoingLabel.getX() + this.gateDefaultOutgoingLabel.getWidth() + 10 + this.defaultAllowOutgoingCheckbox.getWidth() + 10, this.gateDefaultOutgoingLabel.getY(), Anchor.LEFT | Anchor.BOTTOM);
        this.defaultDenyOutgoingCheckbox.setName("checkbox.defaultdenyoutgoing");
        this.defaultDenyOutgoingCheckbox.setChecked(!localGate.defaultAllowOutgoing);
        this.defaultDenyOutgoingCheckbox.register(this);



        buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text("Close")
            .onClick(this::close)
            .build("button.close");

        this.form.add(this.addressContainer, defaultsSeparator, this.gateDefaultIncomingLabel, this.gateDefaultOutgoingLabel, buttonClose);
        this.form.add(this.defaultAllowIncomingCheckbox, this.defaultDenyIncomingCheckbox, this.defaultAllowOutgoingCheckbox, this.defaultDenyOutgoingCheckbox);
        addToScreen(this.form);

        loadData();
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "checkbox.defaultallowincoming":
                this.defaultDenyIncomingCheckbox.setChecked(this.defaultAllowIncomingCheckbox.isChecked());
                break;
            case "checkbox.defaultdenyincoming":
                this.defaultAllowIncomingCheckbox.setChecked(this.defaultDenyIncomingCheckbox.isChecked());
                break;
            case "checkbox.defaultallowoutgoing":
                this.defaultDenyOutgoingCheckbox.setChecked(this.defaultAllowOutgoingCheckbox.isChecked());
                break;
            case "checkbox.defaultdenyoutgoing":
                this.defaultAllowOutgoingCheckbox.setChecked(this.defaultDenyOutgoingCheckbox.isChecked());
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {
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

    private void loadData() {
        if (localGate != null) {

            if (localGate.getGateAccessData() != null) {
                this.gateAccessList.clearItems();
                this.gateAccessList.addItems(localGate.getGateAccessData());
            }
        }
    }


    private class GateItemComponent extends BasicList.ItemComponent<GateAccessData> {

        private UILabel addressLabel;

        public GateItemComponent(final MalisisGui gui, final BasicList<GateAccessData> parent, final GateAccessData item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            this.setHeight(15);
            this.setPadding(1);

            this.addressLabel = new UILabel(this.getGui(), TextFormatting.WHITE + this.item.getAddress());
            this.addressLabel.setAnchor(Anchor.CENTER | Anchor.MIDDLE);


            this.add(this.addressLabel);
        }

        public void update() {

        }
    }
}
