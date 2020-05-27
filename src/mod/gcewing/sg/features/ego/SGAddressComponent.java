package gcewing.sg.features.ego;

import static com.google.common.base.Preconditions.*;

import gcewing.sg.SGCraft;
import gcewing.sg.features.pdd.Address;
import net.malisis.ego.font.FontOptions;
import net.malisis.ego.font.MinecraftFont;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.UIComponentBuilder;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.text.GuiText;
import net.malisis.ego.gui.text.IFontOptionsBuilder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class SGAddressComponent extends UIComponent {

    private final String address;
    private final String glyphAddress;
    private final GuiText text;

    public SGAddressComponent(String address) {
        this.address = address;
        this.glyphAddress = Address.convertToGlyphs(address);
        text = GuiText.builder()
                .parent(this)
                .middleCenter()
                .text(this::addressText)
                .build();

        setForeground(text);
    }

    @Nonnull
    @Override
    public Size.ISize size() {
        return text.size();
    }

    private String addressText() {
        return GuiScreen.isShiftKeyDown() ? address : glyphAddress;
    }

    public void setFontOptions(FontOptions fontOptions) {
        text.setFontOptions(checkNotNull(fontOptions));
    }


    public static SGAddressComponentBuilder builder(Address address) {
        return builder(address.getAddress());
    }

    public static SGAddressComponentBuilder builder(String address) {
        return new SGAddressComponentBuilder(address);
    }


    public static class SGAddressComponentBuilder extends UIComponentBuilder<SGAddressComponentBuilder, SGAddressComponent> implements
            IFontOptionsBuilder<SGAddressComponentBuilder, SGAddressComponent> {

        private final String address;
        private FontOptions.FontOptionsBuilder fontOptionsBuilder = FontOptions.builder();

        private SGAddressComponentBuilder(String address) {
            this.address = address;
            size(Size.of(150, 25));
            textColor(TextFormatting.BLUE);
            font(SGCraft.GLYPHS_FONT);
            scale(1.5F);
            fob().obfuscatedCharList(Address.glyphsChars);
            when(GuiScreen::isShiftKeyDown).font(MinecraftFont.INSTANCE).obfuscatedCharList(Address.baseChars);
            fontOptionsBuilder = fob().base();
        }


        @Override
        public FontOptions.FontOptionsBuilder fob() {
            return fontOptionsBuilder;
        }

        @Override
        public SGAddressComponentBuilder when(Predicate<SGAddressComponent> predicate) {
            fontOptionsBuilder = fob().when(predicate);
            return this;
        }

        @Override
        public SGAddressComponent build() {
            SGAddressComponent ac = build(new SGAddressComponent(address));
            ac.setFontOptions(fontOptionsBuilder.build(ac));
            return ac;
        }
    }

}
