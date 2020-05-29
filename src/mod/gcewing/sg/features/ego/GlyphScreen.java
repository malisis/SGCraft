package gcewing.sg.features.ego;

import gcewing.sg.SGCraft;
import gcewing.sg.features.pdd.Address;
import net.malisis.ego.font.MinecraftFont;
import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.layout.FloatingLayout;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

public class GlyphScreen extends EGOGui {

    @Override
    public void construct() {
        UIContainer w = SGComponent.window("sgcraft.gui.pdd.label.personalDialerDevice")
                .middleCenter()
                .layout(c -> new FloatingLayout(c, 1))
                .size(600, 300)
                .build();


        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn".chars().forEach(i -> makeLetter(w, (char) i));
        addToScreen(w);
    }

    private void makeLetter(UIContainer parent, char c) {
        UIContainer cont = UIContainer.builder()
                .background(SGComponent::defaultBackground)
                .parent(parent)
                .width(20)
                .height(40)
                .build();

        UILabel lbl = UILabel.builder().parent(cont)
                .topCenter()
                .text("" + c)
                .textColor(TextFormatting.WHITE)
                .font(SGCraft.GLYPHS_FONT)
                .scale(2)
                .when(GuiScreen::isShiftKeyDown)
                .font(MinecraftFont.INSTANCE)
                .build();


        UILabel base = UILabel.builder().parent(cont)
                .centered()
                .below(lbl)
                .text("" + Address.toGlyph(c))
                .textColor(TextFormatting.BLUE)
                .scale(2)
                .build();
    }
}
