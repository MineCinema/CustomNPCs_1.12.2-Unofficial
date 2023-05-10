package noppes.npcs.client.gui.questtypes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeKill extends SubGuiInterface implements ITextfieldListener, ICustomScrollListener {
	public GuiScreen parent;
	private GuiCustomScroll scroll;
	// private Quest quest; Changed
	// private GuiNpcTextField lastSelected; Changed
	// New
	private QuestObjective task;

	public GuiNpcQuestTypeKill(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		// Changed
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Kill Setup";
		this.setBackground("menubg.png");
		this.xSize = 356;
		this.ySize = 216;
		this.closeOnEsc = true;
		// New
		this.task = task;
	}

	@Override
	public void initGui() {
		super.initGui();
		// int i = 0;
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.player.to").getFormattedText(), this.guiLeft + 6, this.guiTop + 50));
		// New
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70, 180, 20, this.task.getTargetName()));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 186, this.guiTop + 70, 24, 20, this.task.getMaxProgress() + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(1, Integer.MAX_VALUE, 1);

		ArrayList<String> list = new ArrayList<String>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = (Class<? extends Entity>) ent.getEntityClass();
			String name = ent.getName();
			try {
				if (!EntityLivingBase.class.isAssignableFrom(c) || EntityNPCInterface.class.isAssignableFrom(c)
						|| c.getConstructor(World.class) == null || Modifier.isAbstract(c.getModifiers())) {
					continue;
				}
				list.add(name.toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException ex) {
			}
		}
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setList(list);
		this.scroll.setSize(130, 198);
		this.scroll.guiLeft = this.guiLeft + 220;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 140, 98, 20, "gui.back"));
		
		this.addLabel(new GuiNpcLabel(10, "quest.task.pos.set", this.guiLeft + 6, this.guiTop + 94));
		this.addLabel(new GuiNpcLabel(11, "X:", this.guiLeft + 6, this.guiTop + 108));
		this.addTextField(new GuiNpcTextField(10, this, this.fontRenderer, this.guiLeft + 14, this.guiTop + 106, 40, 13, ""+this.task.pos.getX()));
		this.getTextField(10).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(12, "Y:", this.guiLeft + 57, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 65, this.guiTop + 106, 40, 13, ""+this.task.pos.getY()));
		this.getTextField(11).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(13, "Z:", this.guiLeft + 108, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(12, this, this.fontRenderer, this.guiLeft + 118, this.guiTop + 106, 40, 13, ""+this.task.pos.getZ()));
		this.getTextField(12).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(14, "D:", this.guiLeft + 6, this.guiTop + 124));
		this.addTextField(new GuiNpcTextField(13, this, this.fontRenderer, this.guiLeft + 14, this.guiTop + 123, 40, 13, ""+this.task.dimensionID));
		this.getTextField(13).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(15, "R:", this.guiLeft + 160, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(14, this, this.fontRenderer, this.guiLeft + 170, this.guiTop + 106, 45, 13, ""+this.task.rangeCompass));
		this.getTextField(14).numbersOnly = true;
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);
		this.addButton(new GuiNpcButton(10, this.guiLeft+153, this.guiTop + 140, 60, 20, "gui.set"));
		this.addButton(new GuiNpcButton(11, this.guiLeft+131, this.guiTop + 140, 20, 20, "TP"));
		this.addLabel(new GuiNpcLabel(16, "N:", this.guiLeft + 57, this.guiTop + 124));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, this.guiLeft + 65, this.guiTop + 123, 150, 13, this.task.entityName));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		switch(guibutton.id) {
			case 0: {
				this.close();
				break;
			}
			case 10: {
				if (this.task == null) { return; }
				this.task.pos = this.mc.player.getPosition();
				this.task.dimensionID = this.mc.player.world.provider.getDimension();
				this.initGui();
				break;
			}
			case 11: {
				if (this.task == null) { return; }
				Client.sendData(EnumPacketServer.TeleportTo, new Object[] { this.task.dimensionID, this.task.pos.getX(), this.task.pos.getY(), this.task.pos.getZ() });
				break;
			}
		}
	}

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(12)!=null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(13)!=null && this.getTextField(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(14)!=null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(15)!=null && this.getTextField(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport").getFormattedText());
		}
		else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.value", ""+this.getTextField(1).max).getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").getFormattedText());
		}
	}

	@Override
	public void save() {
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());

		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != this.task.getEnumType()) {
				continue;
			}
			if (task.getTargetName().equals(this.task.getTargetName())) {
				this.getTextField(0).setText("");
				this.task.setTargetName("");
				this.task.setMaxProgress(1);
				break;
			}
		}

		if (this.task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
			}
		}
	}

	private void saveTargets() {
		// Changed
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.getTextField(0).setText(guiCustomScroll.getSelected());
		this.saveTargets();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.task == null) { return; }
		switch(textField.getId()) {
			case 0: {
				this.task.setTargetName(textField.getText());
				break;
			}
			case 1: {
				this.task.setMaxProgress(textField.getInteger());
				break;
			}
			case 10: {
				int y = this.task.pos.getY();
				int z = this.task.pos.getZ();
				this.task.pos = new BlockPos(textField.getInteger(), y, z);
				break;
			}
			case 11: {
				int x = this.task.pos.getX();
				int z = this.task.pos.getZ();
				this.task.pos = new BlockPos(x, textField.getInteger(), z);
				break;
			}
			case 12: {
				int x = this.task.pos.getX();
				int y = this.task.pos.getY();
				this.task.pos = new BlockPos(x, y, textField.getInteger());
				break;
			}
			case 13: {
				int dim = textField.getInteger();
				if (!DimensionManager.isDimensionRegistered(dim)) {
					textField.setText(""+this.task.dimensionID);
					return;
				}
				this.task.dimensionID = textField.getInteger();
				break;
			}
			case 14: {
				this.task.rangeCompass = textField.getInteger();
				break;
			}
			case 15: {
				this.task.entityName = textField.getText();
				break;
			}
		}
	}

}
