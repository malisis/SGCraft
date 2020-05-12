package gcewing.sg.features.ego;

import gcewing.sg.util.SGAddressing;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.UIComponentBuilder;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.element.size.Size;

import java.util.function.Supplier;

/* Unused WIP */
public class SGAddressComponent extends UIComponent {

    public SGAddressComponent(Supplier<String> address) {

        UILabel label = UILabel.builder()
                .parent(this)
                .middleCenter()
                .text(() -> SGAddressing.formatAddress(address.get(), "-", "-"))
                .textColor(0x5555FF)
                .scale(1.3F)
                .build();

        setForeground(label);
    }

    public static SGAddressComponentBuilder builder(String address) {
        return builder(() -> address);
    }

    public static SGAddressComponentBuilder builder(Supplier<String> address) {
        return new SGAddressComponentBuilder(address);
    }

    public static class SGAddressComponentBuilder extends UIComponentBuilder<SGAddressComponentBuilder, SGAddressComponent> {

        private final Supplier<String> address;

        private SGAddressComponentBuilder(Supplier<String> address) {
            this.address = address;
            size(Size.of(100, 20));
        }

        @Override
        public SGAddressComponent build() {
            return build(new SGAddressComponent(address));
        }
    }

}
