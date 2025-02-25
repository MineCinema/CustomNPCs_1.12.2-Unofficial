package noppes.npcs.client;

import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.IChatMessages;

public class RenderChatMessages
implements IChatMessages {
	
	private int boxLength;
	private String lastMessage;
	private long lastMessageTime;
	private Map<Long, TextBlockClient> messages;
	private float scale;

	public RenderChatMessages() {
		this.messages = new TreeMap<Long, TextBlockClient>();
		this.boxLength = 46;
		this.scale = 0.5f;
		this.lastMessage = "";
		this.lastMessageTime = 0L;
	}

	@Override
	public void addMessage(String message, Entity entity) {
		if (!CustomNpcs.EnableChatBubbles) {
			return;
		}
		long time = System.currentTimeMillis();
		if (message.equals(this.lastMessage) && this.lastMessageTime + 5000L > time) {
			return;
		}
		Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>(this.messages);
		messages.put(time, new TextBlockClient(message, this.boxLength * 4, true, new Object[] { Minecraft.getMinecraft().player, entity }));
		if (messages.size() > 3) { messages.remove(messages.keySet().iterator().next()); }
		this.messages = messages;
		this.lastMessage = message;
		this.lastMessageTime = time;
	}

	private void drawRect(int x0, int y0, int z0, int x1, int y1, double z1) {
		if (x0 < z0) {
			int j1 = x0;
			x0 = z0;
			z0 = j1;
		}
		if (y0 < x1) {
			int j1 = y0;
			y0 = x1;
			x1 = j1;
		}
		float f = (y1 >> 24 & 0xFF) / 255.0f;
		float f2 = (y1 >> 16 & 0xFF) / 255.0f;
		float f3 = (y1 >> 8 & 0xFF) / 255.0f;
		float f4 = (y1 & 0xFF) / 255.0f;
		BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
		GlStateManager.color(f2, f3, f4, f);
		tessellator.begin(7, DefaultVertexFormats.POSITION);
		tessellator.pos(x0, x1, z1).endVertex();
		tessellator.pos(z0, x1, z1).endVertex();
		tessellator.pos(z0, y0, z1).endVertex();
		tessellator.pos(x0, y0, z1).endVertex();
		Tessellator.getInstance().draw();
	}

	private Map<Long, TextBlockClient> getMessages() {
		Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>();
		long time = System.currentTimeMillis();
		for (Map.Entry<Long, TextBlockClient> entry : this.messages.entrySet()) {
			if (time > entry.getKey() + 10000L) {
				continue;
			}
			messages.put(entry.getKey(), entry.getValue());
		}
		return this.messages = messages;
	}

	private void render(double x, double y, double z, float textscale, boolean depth, boolean isPlayer) {
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		float var13 = 1.6f;
		float var14 = 0.016666668f * var13;
		GlStateManager.pushMatrix();
		int size = 0;
		for (TextBlockClient block : this.messages.values()) {
			size += block.lines.size();
		}
		Minecraft mc = Minecraft.getMinecraft();
		int textYSize = (int) (size * font.FONT_HEIGHT * this.scale);
		GlStateManager.translate(x + 0.0f, y + textYSize * textscale * var14, z);
		GlStateManager.scale(textscale, textscale, textscale);
		GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(-var14, -var14, var14);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.depthMask(true);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		if (depth) {
			GlStateManager.enableDepth();
		} else {
			GlStateManager.disableDepth();
		}
		int[] cs = isPlayer ? CustomNpcs.chatPlayerColors : CustomNpcs.chatNpcColors;
		int color = cs[0] + (depth ? 0xFF000000 : 0x55000000);
		int border = cs[1] + (depth ? 0xFF000000 : 0x55000000);
		int place = cs[2] + (depth ? 0xBB000000 : 0x44000000);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.enableCull();
		int w = 0;
		for (TextBlockClient block2 : this.messages.values()) {
			for (ITextComponent chat : block2.lines) {
				int g = font.getStringWidth(chat.getFormattedText()) / 3;
				if (g > w) { w = g; }
			}
		}
		if (w > this.boxLength) { w = this.boxLength; }
		
		this.drawRect(-w - 2, -2, w + 2, textYSize + 1, place, 0.11);
		this.drawRect(-w - 1, -3, w + 1, -2, border, 0.1);
		this.drawRect(-w - 1, textYSize + 2, -1, textYSize + 1, border, 0.1);
		this.drawRect(3, textYSize + 2, w + 1, textYSize + 1, border, 0.1);
		this.drawRect(-w - 3, -1, -w - 2, textYSize, border, 0.1);
		this.drawRect(w + 3, -1, w + 2, textYSize, border, 0.1);
		this.drawRect(-w - 2, -2, -w - 1, -1, border, 0.1);
		this.drawRect(w + 2, -2, w + 1, -1, border, 0.1);
		this.drawRect(-w - 2, textYSize + 1, -w - 1, textYSize, border, 0.1);
		this.drawRect(w + 2, textYSize + 1, w + 1, textYSize, border, 0.1);
		this.drawRect(0, textYSize + 1, 3, textYSize + 4, place, 0.11);
		this.drawRect(-1, textYSize + 4, 1, textYSize + 5, place, 0.11);
		this.drawRect(-1, textYSize + 1, 0, textYSize + 4, border, 0.1);
		this.drawRect(3, textYSize + 1, 4, textYSize + 3, border, 0.1);
		this.drawRect(2, textYSize + 3, 3, textYSize + 4, border, 0.1);
		this.drawRect(1, textYSize + 4, 2, textYSize + 5, border, 0.1);
		this.drawRect(-2, textYSize + 4, -1, textYSize + 5, border, 0.1);
		this.drawRect(-2, textYSize + 5, 1, textYSize + 6, border, 0.1);
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.scale(this.scale, this.scale, this.scale);
		int index = 0;
		for (TextBlockClient block2 : this.messages.values()) {
			for (ITextComponent chat : block2.lines) {
				String message = chat.getFormattedText();
				font.drawString(message, -font.getStringWidth(message) / 2, index * font.FONT_HEIGHT, color);
				++index;
			}
		}
		GlStateManager.disableCull();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderMessages(double x, double y, double z, float textscale, boolean inRange) {
		Map<Long, TextBlockClient> messages = this.getMessages();
		if (messages.isEmpty()) { return; }
		if (inRange) { this.render(x, y, z, textscale, false, false); }
		this.render(x, y, z, textscale, true, false);
	}
	
	public void renderPlayerMessages(double x, double y, double z, float textscale, boolean inRange) {
		Map<Long, TextBlockClient> messages = this.getMessages();
		if (messages.isEmpty()) { return; }
		if (inRange) { this.render(x, y, z, textscale, false, true); }
		this.render(x, y, z, textscale, true, true);
	}
}
