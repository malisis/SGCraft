package gcewing.sg.features.ego;

import net.malisis.ego.gui.EGOGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.container.UIContainer.UIContainerBuilder;
import net.malisis.ego.gui.component.control.UIMoveHandle;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.render.GuiRenderer;
import net.malisis.ego.gui.render.IGuiRenderer;
import net.malisis.ego.gui.render.shape.GuiShape;
import org.apache.commons.lang3.StringUtils;

//import net.malisis.core.util.FontColors;


public class SGComponent {

    public static UIContainerBuilder window() {
        return window(null);
    }

    public static UIContainerBuilder window(String title) {
        return UIContainer.builder()
                .background((UIContainer c) -> titleBackground(c, title))
                .padding(Padding.of(!StringUtils.isEmpty(title) ? 22 : 3, 3, 3, 3))
                .withControl(UIMoveHandle.builder().name("movable").height(20).build());

    }

    public static UIContainer errorWindow(String error) {
        return errorWindow(error, "Error");
    }

    public static UIContainer errorWindow(String error, String errorTitle) {
        UIContainer container = UIContainer.builder()
                .name("Error")
                .middleCenter()
                .background((UIContainer c) -> errorBackground(c, errorTitle))
                .padding(Padding.of(22, 3, 3, 3))
                .build();

        UILabel errorLbl = UILabel.builder()
                .parent(container)
                .text(error)
                .scale(1.3F)
                .italic()
                .build();

        UIButton close = UIButton.builder()
                .parent(container)
                .text("Close")
                //.textColor(FontColors.RED)
                .centered()
                .below(errorLbl, 5)
                .width(50)
                .background((UIButton b) -> GuiShape.builder(b)
                        .border(1, 0xCC0000, 200)
                        .color(0x000000)
                        .build())
                .onClick(EGOGui::closeGui)
                .build();

        return container;
    }


    ///
    ///Helpers to create backgrounds with default colors
    ///
    public static IGuiRenderer defaultBackground(UIComponent component) {
        return GuiShape.builder(component).color(0x000000).border(1, 0xFFFFFF, 200).alpha(200).build();
    }

    public static IGuiRenderer defaultHoveredBackground(UIComponent component) {
        return GuiShape.builder(component).color(() -> component.isHovered() ? 0x282828 : 0x000000).border(1, 0xFFFFFF).alpha(200).build();
    }

    public static TitleBackground titleBackground(UIComponent component, String title) {
        return new TitleBackground(component, title, 0x000000, 200, 0xFFFFFF, 1, 0xFFFFFF, 0x363636);
    }

    public static TitleBackground errorBackground(UIComponent component, String title) {
        return new TitleBackground(component, title, 0x363636, 200, 0xCC0000, 1, 0xFFFFFF, 0x660000);
    }

    //Background that handles displaying of a title at the top
    public static class TitleBackground implements IGuiRenderer {

        protected final UIComponent component;
        protected String title = "";
        protected final int borderSize;
        protected GuiShape border = null;
        protected GuiShape background = null;
        protected GuiShape titleBackground = null;
        protected GuiShape div = null;
        protected UILabel label = null;

        protected TitleBackground(UIComponent component, String title, int backgroundColor, int backgroundAlpha, int borderColor, int borderSize,
                int titleColor, int titleBackgroundColor) {

            this.component = component;
            this.title = title;
            this.borderSize = borderSize;

            if (borderSize > 0) {
                border = GuiShape.builder(component)
                        .border(borderSize, borderColor, backgroundAlpha)
                        .alpha(0)//hide the regular bg
                        .build();
            }

            int titleHeight = 18 + borderSize;

            background = GuiShape.builder(component)
                    .x(1)
                    .y(titleHeight + 1)
                    .width(this::width)
                    .height(() -> component.size()
                            .height() - borderSize * 2 - titleHeight)
                    .color(backgroundColor)
                    .alpha(backgroundAlpha)
                    .build();


            label = UILabel.builder()
                    .centeredTo(component)
                    .topAlignedTo(component, 5)
                    .text(this::title)
                    .textColor(titleColor)
                    .build();

            titleBackground = GuiShape.builder(component)
                    .position(borderSize, borderSize)
                    .width(this::width)
                    .height(titleHeight - borderSize)
                    .color(titleBackgroundColor)
                    .alpha(backgroundAlpha)
                    .build();

            div = GuiShape.builder(component)
                    .x(borderSize)
                    .y(titleHeight)
                    .width(this::width)
                    .height(borderSize)
                    .color(borderColor)
                    .alpha(backgroundAlpha)
                    .build();

        }

        private int width() {
            return component.size()
                    .width() - borderSize * 2;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }

        @Override
        public void render(GuiRenderer renderer) {
            background.and(border)
                    .and(titleBackground)
                    .and(div)
                    .and(label)
                    .render(renderer);
        }
    }
}