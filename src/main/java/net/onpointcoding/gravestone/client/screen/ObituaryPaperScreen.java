package net.onpointcoding.gravestone.client.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.BookScreen.Contents;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.onpointcoding.gravestone.item.ObituaryPaperItem;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public class ObituaryPaperScreen extends Screen {
    public static final Contents EMPTY_PROVIDER = new Contents() {
        public int getPageCount() {
            return 0;
        }

        public StringVisitable getPageUnchecked(int index) {
            return StringVisitable.EMPTY;
        }
    };
    public static final Identifier OBITUARY_TEXTURE = new Identifier("gravestone", "textures/gui/obituary.png");
    private final Contents contents;
    private int pageIndex;
    private List<OrderedText> cachedPage;
    private int cachedPageIndex;
    private Text pageIndexText;
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private final boolean pageTurnSound;

    public ObituaryPaperScreen(Contents pageProvider) {
        this(pageProvider, true);
    }

    private ObituaryPaperScreen(Contents contents, boolean playPageTurnSound) {
        super(NarratorManager.EMPTY);
        this.cachedPage = Collections.emptyList();
        this.cachedPageIndex = -1;
        this.pageIndexText = LiteralText.EMPTY;
        this.contents = contents;
        this.pageTurnSound = playPageTurnSound;
    }

    public boolean setPage(int index) {
        int i = MathHelper.clamp(index, 0, this.contents.getPageCount() - 1);
        if (i != this.pageIndex) {
            this.pageIndex = i;
            this.updatePageButtons();
            this.cachedPageIndex = -1;
            return true;
        } else {
            return false;
        }
    }

    protected boolean jumpToPage(int page) {
        return this.setPage(page);
    }

    protected void init() {
        this.addCloseButton();
        this.addPageButtons();
    }

    protected void addCloseButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 196, 200, 20, ScreenTexts.DONE, (button) -> {
            if (this.client != null) this.client.openScreen(null);
        }));
    }

    protected void addPageButtons() {
        int i = (this.width - 192) / 2;
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, (button) -> {
            this.goToNextPage();
        }, this.pageTurnSound));
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, (button) -> {
            this.goToPreviousPage();
        }, this.pageTurnSound));
        this.updatePageButtons();
    }

    private int getPageCount() {
        return this.contents.getPageCount();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0) {
            --this.pageIndex;
        }

        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < this.getPageCount() - 1) {
            ++this.pageIndex;
        }

        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < this.getPageCount() - 1;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            switch (keyCode) {
                case 266:
                    this.previousPageButton.onPress();
                    return true;
                case 267:
                    this.nextPageButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, OBITUARY_TEXTURE);
        int i = (this.width - 192) / 2;
        this.drawTexture(matrices, i, 2, 0, 0, 192, 192);
        if (this.cachedPageIndex != this.pageIndex) {
            StringVisitable stringVisitable = this.contents.getPage(this.pageIndex);
            this.cachedPage = this.textRenderer.wrapLines(stringVisitable, 114);
            this.pageIndexText = new TranslatableText("book.pageIndicator", this.pageIndex + 1, Math.max(this.getPageCount(), 1));
        }

        this.cachedPageIndex = this.pageIndex;
        int k = this.textRenderer.getWidth(this.pageIndexText);
        this.textRenderer.draw(matrices, this.pageIndexText, (float) (i - k + 192 - 44), 18.0F, 0);
        Objects.requireNonNull(this.textRenderer);
        int l = Math.min(128 / 9, this.cachedPage.size());

        for (int m = 0; m < l; ++m) {
            OrderedText orderedText = this.cachedPage.get(m);
            TextRenderer var10000 = this.textRenderer;
            float var10003 = (float) (i + 36);
            Objects.requireNonNull(this.textRenderer);
            var10000.draw(matrices, orderedText, var10003, (float) (32 + m * 9), 0);
        }

        Style style = this.getTextAt(mouseX, mouseY);
        if (style != null) {
            this.renderTextHoverEffect(matrices, style, mouseX, mouseY);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = this.getTextAt(mouseX, mouseY);
            if (style != null && this.handleTextClick(style)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean handleTextClick(Style style) {
        if (style != null) {
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent == null) {
                return false;
            } else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
                String string = clickEvent.getValue();

                try {
                    int i = Integer.parseInt(string) - 1;
                    return this.jumpToPage(i);
                } catch (Exception var5) {
                    return false;
                }
            } else {
                boolean bl = super.handleTextClick(style);
                if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    this.closeScreen();
                }

                return bl;
            }
        }
        return false;
    }

    protected void closeScreen() {
        if (this.client != null) this.client.openScreen(null);
    }

    @Nullable
    public Style getTextAt(double x, double y) {
        if (this.cachedPage.isEmpty()) {
            return null;
        } else {
            int i = MathHelper.floor(x - (double) ((this.width - 192) / 2) - 36.0D);
            int j = MathHelper.floor(y - 2.0D - 30.0D);
            if (i >= 0 && j >= 0) {
                Objects.requireNonNull(this.textRenderer);
                int k = Math.min(128 / 9, this.cachedPage.size());
                if (i <= 114) {
                    Objects.requireNonNull(Objects.requireNonNull(this.client).textRenderer);
                    if (j < 9 * k + k) {
                        Objects.requireNonNull(this.client.textRenderer);
                        int l = j / 9;
                        if (l < this.cachedPage.size()) {
                            OrderedText orderedText = this.cachedPage.get(l);
                            return this.client.textRenderer.getTextHandler().getStyleAt(orderedText, i);
                        }

                        return null;
                    }
                }

                return null;
            } else {
                return null;
            }
        }
    }

    static List<String> readPages(NbtCompound nbt) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        Objects.requireNonNull(builder);
        method_33888(nbt, builder::add);
        return builder.build();
    }

    public static void method_33888(NbtCompound nbt, Consumer<String> consumer) {
        NbtList nbtList = nbt.getList("pages", NbtType.STRING).copy();
        IntFunction<String> intFunction2;
        if (MinecraftClient.getInstance().shouldFilterText() && nbt.contains("filtered_pages", NbtType.COMPOUND)) {
            NbtCompound nbtCompound = nbt.getCompound("filtered_pages");
            intFunction2 = (ix) -> {
                String string = String.valueOf(ix);
                return nbtCompound.contains(string) ? nbtCompound.getString(string) : nbtList.getString(ix);
            };
        } else {
            Objects.requireNonNull(nbtList);
            intFunction2 = nbtList::getString;
        }

        for (int i = 0; i < nbtList.size(); ++i) {
            consumer.accept(intFunction2.apply(i));
        }

    }

    @Environment(EnvType.CLIENT)
    public static class ObituaryPaperContents implements Contents {
        private final List<String> pages;

        public ObituaryPaperContents(ItemStack stack) {
            this.pages = getPages(stack);
        }

        private static List<String> getPages(ItemStack stack) {
            NbtCompound nbtCompound = stack.getTag();
            return ObituaryPaperItem.isValid(nbtCompound) ? readPages(nbtCompound) : ImmutableList.of(Text.Serializer.toJson((new TranslatableText("text.gravestone.obituary.invalid.tag")).formatted(Formatting.DARK_RED)));
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public StringVisitable getPageUnchecked(int index) {
            String string = this.pages.get(index);

            try {
                StringVisitable stringVisitable = Text.Serializer.fromJson(string);
                if (stringVisitable != null) {
                    return stringVisitable;
                }
            } catch (Exception ignored) {
            }

            return StringVisitable.plain(string);
        }
    }
}
