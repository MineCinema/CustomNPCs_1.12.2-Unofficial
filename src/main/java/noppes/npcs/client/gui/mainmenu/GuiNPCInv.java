package noppes.npcs.client.gui.mainmenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCInv
extends GuiContainerNPCInterface2
implements ISubGuiListener, ICustomScrollListener, IGuiData
{
	// private HashMap<Integer, Integer> chances; Changed
	private ContainerNPCInv container;
	private Map<String, DropSet> dropsData = new HashMap<String, DropSet>();
	private DataInventory inventory;
	private DropsTemplate temp;
	private int groupId;
	private GuiCustomScroll scrollTemplate, scrollDrops;
	private ResourceLocation slot;

	public GuiNPCInv(EntityNPCInterface npc, ContainerNPCInv container) {
		super(npc, container, 3);
		// this.chances = new HashMap<Integer, Integer>(); Change
		this.inventory = this.npc.inventory;
		this.setBackground("npcinv.png");
		this.container = container;
		this.ySize = 200;
		this.slot = this.getResource("slot.png");
		this.groupId = 0;
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "inv.minExp", this.guiLeft + 118, this.guiTop + 18));
		this.addTextField(new GuiNpcTextField(0, (GuiScreen) this, this.fontRenderer, this.guiLeft + 108, this.guiTop + 29, 60, 20, this.inventory.getExpMin() + ""));
		this.getTextField(0).numbersOnly = true;
		this.getTextField(0).setMinMaxDefault(0, 32767, 0);
		this.addLabel(new GuiNpcLabel(1, "inv.maxExp", this.guiLeft + 118, this.guiTop + 52));
		this.addTextField(new GuiNpcTextField(1, (GuiScreen) this, this.fontRenderer, this.guiLeft + 108, this.guiTop + 63, 60, 20, this.inventory.getExpMax() + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(0, 32767, 0);
		this.addButton(new GuiNpcButton(10, this.guiLeft + 88, this.guiTop + 88, 80, 20, new String[] { "stats.normal", "inv.auto" }, this.inventory.lootMode ? 1 : 0)); // Changed
		this.addLabel(new GuiNpcLabel(2, "inv.npcInventory", this.guiLeft + 191, this.guiTop + 5));
		this.addLabel(new GuiNpcLabel(3, "inv.inventory", this.guiLeft + 8, this.guiTop + 101));

		this.addButton(new GuiNpcButton(0, this.guiLeft + 175, this.guiTop + 4, 120, 20, new String[] { "inv.use.drops.0", "inv.use.drops.1", "inv.use.drops.2" }, this.inventory.dropType));
		GuiNpcTextField textField = new GuiNpcTextField(2, this, this.guiLeft + 300, this.guiTop + 4, 60, 20, ""+this.inventory.limitation);
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 128, this.inventory.limitation);
		this.addTextField(textField);
		
		this.dropsData.clear();
		this.temp = null;
		if (this.inventory.dropType==0) {
			for (ICustomDrop ids : this.inventory.getDrops()) {
				DropSet ds = (DropSet) ids;
				this.dropsData.put(ds.getKey(), ds);
			}
			if (this.scrollDrops == null) { this.scrollDrops = new GuiCustomScroll(this, 1); }
			this.scrollDrops.setSize(238, 157);
			this.scrollDrops.setList(Lists.newArrayList(this.dropsData.keySet()));
			this.scrollDrops.guiLeft = this.guiLeft + 175;
			this.scrollDrops.guiTop = this.guiTop + 38;
			this.addScroll(this.scrollDrops);
			this.addLabel(new GuiNpcLabel(4, "inv.drops", this.guiLeft + 176, this.guiTop + 27));
			this.addButton(new GuiNpcButton(1, this.guiLeft + 175, this.guiTop + 197, 60, 15, "gui.add", this.dropsData.size() < CustomNpcs.maxItemInDropsNPC));
			this.addButton(new GuiNpcButton(2, this.guiLeft + 240, this.guiTop + 197, 60, 15, "selectServer.edit", this.scrollDrops.selected>=0));
			this.addButton(new GuiNpcButton(3, this.guiLeft + 305, this.guiTop + 197, 60, 15, "gui.remove", this.scrollDrops.selected>=0));
		} else if (this.inventory.dropType==1) {
			this.addLabel(new GuiNpcLabel(4, "gui.templates", this.guiLeft + 176, this.guiTop + 27));
			if (this.scrollTemplate == null) { this.scrollTemplate = new GuiCustomScroll(this, 0); }
			this.scrollTemplate.setSize(98, 140);
			this.scrollTemplate.setList(Lists.newArrayList(DropController.getInstance().templates.keySet()));
			this.scrollTemplate.guiLeft = this.guiLeft + 175;
			this.scrollTemplate.guiTop = this.guiTop + 38;
			this.addScroll(this.scrollTemplate);
			
			if (DropController.getInstance().templates.containsKey(this.inventory.saveDropsName)) {
				this.temp = DropController.getInstance().templates.get(this.inventory.saveDropsName);
				this.scrollTemplate.setSelected(this.inventory.saveDropsName);
				if (this.temp.groups.containsKey(this.groupId)) {
					for (DropSet ds : DropController.getInstance().templates.get(this.inventory.saveDropsName).groups.get(this.groupId).values()) {
						this.dropsData.put(ds.getKey(), ds);
					}
				}
			}
			else { this.groupId = 0; }
			
			this.addButton(new GuiNpcButton(4, this.guiLeft + 175, this.guiTop + 180, 48, 15, "gui.add", true));
			this.addButton(new GuiNpcButton(5, this.guiLeft + 175, this.guiTop + 197, 48, 15, "gui.copy", this.scrollTemplate.getSelected()!=null));
			this.addButton(new GuiNpcButton(6, this.guiLeft + 225, this.guiTop + 180, 48, 15, "selectServer.edit", !this.inventory.saveDropsName.isEmpty()));
			this.addButton(new GuiNpcButton(7, this.guiLeft + 225, this.guiTop + 197, 48, 15, "gui.remove", !this.inventory.saveDropsName.isEmpty()));
			this.addLabel(new GuiNpcLabel(5, "gui.groups", this.guiLeft + 277, this.guiTop + 30));
			
			List<String> l = Lists.<String>newArrayList();
			int g = 1;
			if (this.temp!=null && this.temp.groups.size()>0) { g = this.temp.groups.size(); }
			for (int i=0; i<=g; i++) {
				l.add(i+" / "+g);
			}
			this.addButton(new GuiNpcButton(8, this.guiLeft + 346, this.guiTop + 27, 70, 15, l.toArray(new String[l.size()]), this.groupId));
			
			if (this.scrollDrops == null) { this.scrollDrops = new GuiCustomScroll(this, 1); }
			this.scrollDrops.setSize(140, 117);
			this.scrollDrops.setList(Lists.newArrayList(this.dropsData.keySet()));
			this.scrollDrops.guiLeft = this.guiLeft + 276;
			this.scrollDrops.guiTop = this.guiTop + 44;
			this.addScroll(this.scrollDrops);

			this.addButton(new GuiNpcButton(1, this.guiLeft + 330, this.guiTop + 163, 48, 15, "gui.add", !this.inventory.saveDropsName.isEmpty()));
			this.addButton(new GuiNpcButton(2, this.guiLeft + 330, this.guiTop + 180, 48, 15, "selectServer.edit", this.scrollDrops.getSelected()!=null));
			this.addButton(new GuiNpcButton(3, this.guiLeft + 330, this.guiTop + 197, 48, 15, "gui.remove", this.scrollDrops.getSelected()!=null));
			
			this.addButton(new GuiNpcButton(9, this.guiLeft + 277, this.guiTop + 163, 48, 15, "gui.add", true));
			this.addButton(new GuiNpcButton(10, this.guiLeft + 277, this.guiTop + 180, 48, 15, "gui.copy", this.temp!=null && this.temp.groups.containsKey(this.groupId)));
			this.addButton(new GuiNpcButton(11, this.guiLeft + 277, this.guiTop + 197, 48, 15, "gui.remove", this.temp!=null && this.temp.groups.containsKey(this.groupId)));
			
		} else {
			this.addLabel(new GuiNpcLabel(4, "gui.templates", this.guiLeft + 176, this.guiTop + 27));
			this.addLabel(new GuiNpcLabel(5, "inv.drops", this.guiLeft + 277, this.guiTop + 27));
			
			if (this.scrollTemplate == null) { this.scrollTemplate = new GuiCustomScroll(this, 0); }
			this.scrollTemplate.setSize(98, 174);
			this.scrollTemplate.setList(Lists.newArrayList(DropController.getInstance().templates.keySet()));
			this.scrollTemplate.guiLeft = this.guiLeft + 175;
			this.scrollTemplate.guiTop = this.guiTop + 38;
			this.addScroll(this.scrollTemplate);
			
			if (DropController.getInstance().templates.containsKey(this.inventory.saveDropsName)) {
				this.temp = DropController.getInstance().templates.get(this.inventory.saveDropsName);
				this.scrollTemplate.setSelected(this.inventory.saveDropsName);
			}
			else { this.groupId = 0; }
			
			for (ICustomDrop ids : this.inventory.getDrops()) {
				DropSet ds = (DropSet) ids;
				this.dropsData.put(ds.getKey(), ds);
			}
			
			if (this.scrollDrops == null) { this.scrollDrops = new GuiCustomScroll(this, 1); }
			this.scrollDrops.setSize(140, 157);
			this.scrollDrops.setList(Lists.newArrayList(this.dropsData.keySet()));
			this.scrollDrops.guiLeft = this.guiLeft + 276;
			this.scrollDrops.guiTop = this.guiTop + 38;
			this.addScroll(this.scrollDrops);
			
			this.addButton(new GuiNpcButton(1, this.guiLeft + 277, this.guiTop + 197, 45, 15, "gui.add", this.dropsData.size() < CustomNpcs.maxItemInDropsNPC));
			this.addButton(new GuiNpcButton(2, this.guiLeft + 324, this.guiTop + 197, 45, 15, "selectServer.edit", this.scrollDrops.selected>=0));
			this.addButton(new GuiNpcButton(3, this.guiLeft + 371, this.guiTop + 197, 45, 15, "gui.remove", this.scrollDrops.selected>=0));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: { // lootMode
				this.inventory.dropType = button.getValue();
				this.initGui();
				break;
			}
			case 1: { // add Drop in NPC
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.inventory.dropType, this.groupId, -1);
				break;
			}
			case 2: { // edit Drop in NPC
				if (this.scrollDrops.selected == -1) { return; }
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.inventory.dropType, this.groupId, this.scrollDrops.selected);
				break;
			}
			case 3: { // remove Drop in NPC
				if (this.scrollDrops.selected == -1) { return; }
				NBTTagCompound compound = new NBTTagCompound();
				compound.setTag("Item", ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
				Client.sendData(EnumPacketServer.MainmenuInvDropSave, this.inventory.dropType, this.groupId, this.scrollDrops.selected, compound);
				break;
			}
			case 4: {
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 6: {
				this.setSubGui(new SubGuiEditText(2, this.inventory.saveDropsName));
				break;
			}
			case 8: { // group ID
				this.groupId = button.getValue();
				this.initGui();
				break;
			}
			case 9: { // add Drop in Template
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, -1, this.inventory.dropType, this.groupId);
				break;
			}
			case 10: { // lootMode
				this.inventory.lootMode = (button.getValue() == 1);
				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.slot);
		for (int id = 4; id <= 6; ++id) {
			Slot slot = this.container.getSlot(id);
			if (slot.getHasStack()) {
				this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		if (this.inventory.dropType!=0) {
			this.drawVerticalLine(this.guiLeft+274, this.guiTop+26, this.guiTop+this.ySize+12, 0xFF606060);
		}
		if (this.inventory.dropType==1) {
			this.drawVerticalLine(this.guiLeft+327, this.guiTop+162, this.guiTop+this.ySize+12, 0xFF606060);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		int showname = this.npc.display.getShowName();
		this.npc.display.setShowName(1);
		this.drawNpc(50, 84);
		this.npc.display.setShowName(showname);
		super.drawScreen(i, j, f);
		// New
		if (!CustomNpcs.showDescriptions) { return; }
		String dropName = "";
		if (this.scrollDrops!=null && this.scrollDrops.getSelected()!=null && this.dropsData.get(this.scrollDrops.getSelected())!=null) {
			dropName = this.dropsData.get(this.scrollDrops.getSelected()).getItem().getDisplayName();
		}
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.minxp").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.maxxp").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.amount").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.drops.type").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.add.drop", new Object[] { ""+CustomNpcs.maxItemInDropsNPC }).getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.edit.drop", new Object[] { dropName }).getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.del.drop", new Object[] { dropName }).getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("inv.hover.auto.xp").getFormattedText());
		}
	}

	@Override
	public void save() {
		if (this.getTextField(0)!=null && this.getTextField(1)!=null) {
			this.inventory.setExp(this.getTextField(0).getInteger(), this.getTextField(1).getInteger());
		}
		if (this.getTextField(2)!=null) {
			this.inventory.limitation = this.getTextField(2).getInteger();
		}
		Client.sendData(EnumPacketServer.MainmenuInvSave, this.inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) { return; }
		if (scroll.id == 0) {
			this.inventory.saveDropsName = scroll.getSelected();
			this.initGui();
		}
		if (scroll.id == 1) {
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (scroll.id == 1) {
			if (this.dropsData.get(this.scrollDrops.getSelected()) != null) {
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInvDrop, this.inventory.dropType, this.groupId, this.scrollDrops.selected);
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.inventory.readEntityFromNBT(compound);
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) { return; }
		DropController dData = DropController.getInstance();
		String name = ((SubGuiEditText) subgui).text[0];
		if (subgui.id == 1) {
			this.inventory.saveDropsName = name;
			if (!dData.templates.containsKey(this.inventory.saveDropsName)) {
				dData.templates.put(this.inventory.saveDropsName, new DropsTemplate());
			}
		}
		else if (subgui.id == 2) {
			if (dData.templates.containsKey(name) || !dData.templates.containsKey(this.inventory.saveDropsName)) { return; }
			dData.templates.put(name, dData.templates.get(this.inventory.saveDropsName));
			dData.templates.remove(this.inventory.saveDropsName);
		}
		this.initGui();
	}
	
}
