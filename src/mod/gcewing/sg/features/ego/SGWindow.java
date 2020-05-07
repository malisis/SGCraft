package gcewing.sg.features.ego;

import static com.google.common.base.Preconditions.*;

import net.malisis.core.util.FontColors;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.container.UIContainer.UIContainerBuilder;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.render.GuiRenderer;
import net.malisis.ego.gui.render.IGuiRenderer;
import net.malisis.ego.gui.render.shape.GuiShape;
import org.apache.commons.lang3.StringUtils;

public class SGWindow
{
	public static UIContainerBuilder builder()
	{
		return builder(null);
	}

	public static IGuiRenderer createBackground(UIContainer container, String title)
	{
		return SGWindowBackground.builder(container)
								 .title(title)
								 .backgroundColor(0x000000)
								 .backgroundAlpha(200)
								 .borderSize(1)
								 .borderColor(0xFFFFFF)
								 .titleColor(0xFFFFFF)
								 .titleBackgroundColor(0x363636)
								 .build();
	}

	public static IGuiRenderer createErrorBackground(UIContainer container)
	{
		return SGWindowBackground.builder(container)
								 .title("Error")
								 .backgroundColor(0x363636)
								 .backgroundAlpha(200)
								 .borderSize(1)
								 .borderColor(0xCC0000)
								 .titleColor(0xFFFFFF)
								 .titleBackgroundColor(0x660000)
								 .build();
	}

	public static UIContainer errorWindow(String error)
	{
		UIContainer container = UIContainer.builder()
										   .name("Error")
										   .middleCenter()
										   .background(SGWindow::createErrorBackground)
										   .padding(Padding.of(24, 5, 5, 5))
										   .build();

		UILabel errorLbl = UILabel.builder()
								  .parent(container)
								  .text(error)
								  .textColor(FontColors.RED)
								  .scale(1.3F)
								  .italic()
								  .build();

		UIButton close = UIButton.builder()
								 .parent(container)
								 .text("Close")
								 .textColor(FontColors.RED)
								 .centered()
								 .below(errorLbl, 5)
								 .width(50)
								 .background((UIButton b) -> GuiShape.builder(b)
																	 .border(1, 0xCC0000, 200)
																	 .color(0x000000)
																	 .build())
								 .onClick(MalisisGui::closeGui)
								 .build();

		return container;
	}

	public static UIContainerBuilder builder(String title)
	{
		return UIContainer.builder()
						  .background((UIContainer c) -> createBackground(c, title))
						  .padding(Padding.of(!StringUtils.isEmpty(title) ? 24 : 5, 5, 5, 5));

	}

	public static class SGWindowBackground implements IGuiRenderer
	{
		protected final UIContainer container;
		protected final int borderSize;
		protected GuiShape border = null;
		protected GuiShape background = null;
		protected GuiShape titleBackground = null;
		protected GuiShape div = null;
		protected UILabel label = null;

		protected SGWindowBackground(UIContainer container, String title, int backgroundColor, int backgroundAlpha, int borderColor, int borderSize, int titleColor, int titleBackgroundColor)
		{
			this.container = container;
			this.borderSize = borderSize;

			if (borderSize > 0)
				border = GuiShape.builder(container)
								 .border(borderSize, borderColor, backgroundAlpha)
								 .alpha(0)//hide the regular bg
								 .build();

			boolean hasTitle = !StringUtils.isEmpty(title);
			int titleHeight = hasTitle ? 18 + borderSize : 0;

			background = GuiShape.builder(container)
								 .x(1)
								 .y(titleHeight + 1)
								 .width(this::width)
								 .height(() -> container.size()
														.height() - borderSize * 2 - titleHeight)
								 .color(backgroundColor)
								 .alpha(backgroundAlpha)
								 .build();

			if (hasTitle)
			{
				label = UILabel.builder()
							   .centeredTo(container)
							   .topAlignedTo(container, 5)
							   .text(title)
							   .textColor(titleColor)
							   .build();

				titleBackground = GuiShape.builder(container)
										  .position(borderSize, borderSize)
										  .width(this::width)
										  .height(titleHeight - borderSize)
										  .color(titleBackgroundColor)
										  .alpha(backgroundAlpha)
										  .build();

				div = GuiShape.builder(container)
							  .x(borderSize)
							  .y(titleHeight)
							  .width(this::width)
							  .height(borderSize)
							  .color(borderColor)
							  .alpha(backgroundAlpha)
							  .build();
			}
		}

		private int width()
		{
			return container.size()
							.width() - borderSize * 2;
		}

		@Override
		public void render(GuiRenderer renderer)
		{
			background.and(border)
					  .and(titleBackground)
					  .and(div)
					  .and(label)
					  .render(renderer);
		}

		public static SGWindowBuilder builder(UIContainer container)
		{
			return new SGWindowBuilder(checkNotNull(container));
		}

		public static class SGWindowBuilder
		{
			private final UIContainer container;
			private String title;
			private int backgroundAlpha = 200;
			private int backgroundColor = 0x000000;
			private int borderSize = 1;
			private int borderColor = 0xFFFFFF;
			private int titleColor = 0xFFFFFF;
			private int titleBackgroundColor = 0x363636;

			private SGWindowBuilder(UIContainer container)
			{
				this.container = container;
			}

			public SGWindowBuilder title(String title)
			{
				this.title = title;
				return this;
			}

			public SGWindowBuilder backgroundAlpha(int alpha)
			{
				backgroundAlpha = alpha;
				return this;
			}

			public SGWindowBuilder backgroundColor(int color)
			{
				backgroundColor = color;
				return this;
			}

			public SGWindowBuilder borderSize(int size)
			{
				borderSize = size;
				return this;
			}

			public SGWindowBuilder borderColor(int color)
			{
				borderColor = color;
				return this;
			}

			public SGWindowBuilder titleColor(int color)
			{
				titleColor = color;
				return this;
			}

			public SGWindowBuilder titleBackgroundColor(int color)
			{
				titleBackgroundColor = color;
				return this;
			}

			public SGWindowBackground build()
			{
				return new SGWindowBackground(container, title, backgroundColor, backgroundAlpha, borderColor, borderSize, titleColor,
											  titleBackgroundColor);
			}
		}

	}
}