package bletch.pixelmoninformation.top;

import bletch.pixelmoninformation.utils.StringUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class WrappedTextElement implements IElement {

    public static int ELEMENT_ID = -1;
    private String text;
    private int maxLines;

    private List<String> textLines;
    private int width;
    private int height;

    public WrappedTextElement(String text) {
        PopulateServerDetails(text, 4);
    }

    public WrappedTextElement(String text, int maxLines) {
        PopulateServerDetails(text, maxLines);
    }

    protected WrappedTextElement(String text, int maxLines, boolean isClient) {
        if (isClient) {
            PopulateClientDetails(text, maxLines);
        } else {
            PopulateServerDetails(text, maxLines);
        }
    }

    @SuppressWarnings("deprecation")
    public static int renderText(Minecraft mc, MatrixStack matrixStack, int x, int y, String txt) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);

        matrixStack.pushPose();
        matrixStack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableLighting();
        net.minecraft.client.renderer.RenderHelper.setupFor3DItems();

        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        int width = mc.font.width(txt);
        mc.font.drawShadow(matrixStack, txt, x, y, 16777215);
        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        // Fixes opaque cooldown overlay a bit lower
        RenderSystem.enableBlend();

        matrixStack.popPose();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableLighting();

        return width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getID() {
        return ELEMENT_ID;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        int lineY = y;

        for (String line : this.textLines) {
            renderText(minecraft, matrixStack, x, lineY, line);
            lineY += minecraft.font.lineHeight;
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUtf(this.text);
        buf.writeInt(this.maxLines);
    }

    protected void PopulateClientDetails(String text, int maxLines) {
        Minecraft minecraft = Minecraft.getInstance();

        this.text = text;
        this.maxLines = maxLines;

        this.textLines = StringUtils.split(text, minecraft, maxLines);
        this.width = 0;
        this.height = Math.max(0, minecraft.font.lineHeight * this.textLines.size());

        for (String line : this.textLines) {
            int lineWidth = minecraft.font.width(line);
            this.width = Math.max(this.width, lineWidth);
        }
    }

    protected void PopulateServerDetails(String text, int maxLines) {
        this.text = text;
        this.maxLines = maxLines;

        this.width = 0;
        this.height = 0;
        this.textLines = null;
    }

    public static class Factory implements IElementFactory {

        @Override
        public IElement createElement(PacketBuffer buf) {
            String text = buf.readUtf();
            int maxLines = buf.readInt();

            return new WrappedTextElement(text, maxLines, true);
        }

    }

}
