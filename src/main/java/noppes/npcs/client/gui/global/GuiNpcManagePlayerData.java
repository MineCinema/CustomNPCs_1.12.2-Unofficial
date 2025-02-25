package noppes.npcs.client.gui.global;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcManagePlayerData
extends GuiNPCInterface2
implements ISubGuiListener, IScrollData, ICustomScrollListener, GuiYesNoCallback {
	
	public HashMap<String, Integer> data;
	public HashMap<String, String> scrollData;
	private boolean isOnline;
	private GuiCustomScroll scroll;
	public String search, selected, selectedPlayer;
	public EnumPlayerData selection;

	public GuiNpcManagePlayerData(EntityNPCInterface npc, GuiNPCInterface2 parent) {
		super(npc);
		this.isOnline = false;
		this.selectedPlayer = null;
		this.selected = null;
		this.data = new HashMap<String, Integer>();
		this.scrollData = new HashMap<String, String>();
		this.selection = EnumPlayerData.Players;
		this.search = "";
		Client.sendData(EnumPacketServer.PlayerDataGet, this.selection);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if (id == 0) {
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("global.playerdata").getFormattedText()+": "+this.selectedPlayer, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
		else if (id >= 1 && id <= 6) {
			if (this.selectedPlayer == null && id != 1) {
				return;
			}
			this.selection = EnumPlayerData.values()[id - 1];
			this.initButtons();
			this.scroll.clear();
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataGet, this.selection, this.selectedPlayer);
			this.selected = null;
			this.getTextField(0).setText("");
		}
		else if (id == 7) {
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("gui.wipe").getFormattedText()+"?", new TextComponentTranslation("data.hover.wipe").getFormattedText().replace("<br>", ""+((char) 10)), 7);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
		else if (id == 8) { // Add
			SubGuiEditText subgui = new SubGuiEditText(0, "");
			subgui.lable = "gui.add";
			switch(this.selection) {
				case Quest: {
					subgui.hovers[0] = "";
					for (int i : QuestController.instance.quests.keySet()) {
						if (this.data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText()+" ID:<br>"+subgui.hovers[0];
					break;
				}
				case Dialog: {
					subgui.hovers[0] = "";
					for (int i : DialogController.instance.dialogs.keySet()) {
						if (this.data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText()+" ID:<br>"+subgui.hovers[0];
					break;
				}
				case Transport: {
					break;
				}
				case Bank: {
					subgui.hovers[0] = "";
					for (int i : BankController.getInstance().banks.keySet()) {
						if (this.data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText()+" ID:<br>"+subgui.hovers[0];
					break;
				}
				case Factions: {
					subgui.hovers[0] = "";
					for (int i : FactionController.instance.factions.keySet()) {
						if (this.data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText()+" ID:<br>"+((char) 167)+"6"+subgui.hovers[0];
					break;
				}
				default: { return; }
			}
			this.setSubGui(subgui);
		}
		else if (id == 9) { // del
			Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 1, this.data.get(this.scrollData.get(this.scroll.getSelected())));
		}
		else if (id == 10) { // del all
			Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 3, -1);
		}
		else if (id == 11) { // edit
			this.editData();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.delete").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.list").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.quests").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.dialogs").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.transports").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.banks").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.factions").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.wipe").getFormattedText());
		} else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.add").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete.all").getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.edit").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.found").getFormattedText());
		}
	}

	private void setCurentList() {
		if (this.scroll==null) { return; }
		List<String> list = Lists.<String>newArrayList();
		List<String> hovers = Lists.<String>newArrayList();
		List<String> suffixs = Lists.<String>newArrayList();
		List<Integer> colors = Lists.<Integer>newArrayList();
		if (this.selection == EnumPlayerData.Wipe) { this.selection = EnumPlayerData.Players; }
		switch(this.selection) {
			case Players: {
				List<String> listOn = Lists.<String>newArrayList(), listOff = Lists.<String>newArrayList();
				for (String name : this.data.keySet()) {
					if (this.search.isEmpty() || name.toLowerCase().contains(this.search)) {
						if (this.data.get(name)==1) { listOn.add(name); } else { listOff.add(name); }
					}
				}
				Collections.sort(listOn);
				Collections.sort(listOff);
				list = listOn;
				for (String n : listOff) { list.add(n); }
				for (String name : list) {
					suffixs.add(new TextComponentTranslation(this.data.get(name)==0 ? "gui.offline" : "gui.online").getFormattedText());
				}
				break;
			}
			case Quest: {
				Map<String, Map<Integer, String>> mapA = Maps.<String, Map<Integer, String>>newTreeMap();
				Map<String, Map<Integer, String>> mapF = Maps.<String, Map<Integer, String>>newTreeMap();
				for (String str : this.data.keySet()) {
					String cat = str.substring(0, str.indexOf(": "));
					String name = str.substring(str.indexOf(": ")+2);
					Map<String, Map<Integer, String>> map;
					if (name.endsWith("(Active quest)")) {
						name = name.substring(0, name.lastIndexOf("(Active quest)"));
						map = mapA;
					} else {
						name = name.substring(0, name.lastIndexOf("(Finished quest)"));
						map = mapF;
					}
					if (!map.containsKey(cat)) { map.put(cat, Maps.<Integer, String>newTreeMap()); }
					map.get(cat).put(this.data.get(str), name);
				}
				for (String cat : mapA.keySet()) {
					ITextComponent sfx = new TextComponentTranslation("availability.active");
					sfx.getStyle().setColor(TextFormatting.GREEN);
					for (int id : mapA.get(cat).keySet()) {
						suffixs.add(sfx.getFormattedText());
						String key = ((char) 167) + "aID:" + id + ((char) 167) + "7 " + cat + ": \""+((char) 167) + "r" + mapA.get(cat).get(id)+((char) 167) + "7\"";
						list.add(key);
						for (String str : this.data.keySet()) {
							if (this.data.get(str)==id) {
								this.scrollData.put(key, str);
								break;
							}
						}
					}
				}
				for (String cat : mapF.keySet()) {
					ITextComponent sfx = new TextComponentTranslation("quest.complete");
					sfx.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
					for (int id : mapF.get(cat).keySet()) {
						suffixs.add(sfx.getFormattedText());
						String key = ((char) 167) + "dID:" + id + ((char) 167) + "7 " + cat + ": \""+((char) 167) + "r" + mapF.get(cat).get(id)+((char) 167) + "7\"";
						list.add(key);
						for (String str : this.data.keySet()) {
							if (this.data.get(str)==id) {
								this.scrollData.put(key, str);
								break;
							}
						}
					}
				}
				break;
			}
			case Dialog: {
				Map<Integer, String> map = Maps.<Integer, String>newTreeMap();
				for (String str : this.data.keySet()) {
					map.put(this.data.get(str), ((char) 167)+"7ID:"+this.data.get(str)+" "+str.replace(": ", ": "+((char) 167)+"r"));
				}
				for (int id : map.keySet()) {
					list.add(map.get(id));
					for (String str : this.data.keySet()) {
						if (this.data.get(str)==id) {
							this.scrollData.put(map.get(id), str);
							break;
						}
					}
				}
				break;
			}
			case Transport: {
				Map<Integer, String> map = Maps.<Integer, String>newTreeMap();
				for (String str : this.data.keySet()) {
					map.put(this.data.get(str), ((char) 167)+"7"+str.replace(": ", ": "+((char) 167)+"r"));
				}
				for (int id : map.keySet()) {
					list.add(map.get(id));
					String catData = "cat null", locData = "loc null", pos = "pos null";
					TransportLocation loc = TransportController.getInstance().getTransport(id);
					if (loc!=null) {
						catData = ((char) 167)+"7"+new TextComponentTranslation("drop.category").getFormattedText()+((char) 167)+"7: \""+((char) 167)+"r"+ new TextComponentTranslation(loc.category.title).getFormattedText()+((char) 167)+"7\" ID: "+((char) 167)+"6"+loc.category.id;
						locData = ((char) 167)+"7"+new TextComponentTranslation("gui.location").getFormattedText()+((char) 167)+"7: \""+((char) 167)+"r"+ new TextComponentTranslation(loc.name).getFormattedText()+((char) 167)+"7\" ID: "+((char) 167)+"6"+id;
						pos = ((char) 167)+"7"+new TextComponentTranslation("parameter.world").getFormattedText()+((char) 167)+"7 ID: "+((char) 167)+"a"+loc.dimension+((char) 167)+"7; "+new TextComponentTranslation("parameter.position").getFormattedText()+((char) 167)+"7 X:"+((char) 167)+"b"+loc.getX()+((char) 167)+"7 Y:"+((char) 167)+"b"+loc.getY()+((char) 167)+"7 Z:"+((char) 167)+"b"+loc.getZ();
					}
					hovers.add(catData+"<br>"+locData+"<br>"+pos);
					for (String str : this.data.keySet()) {
						if (this.data.get(str)==id) {
							this.scrollData.put(map.get(id), str);
							break;
						}
					}
				}
				break;
			}
			case Bank: {
				for (String str : this.data.keySet()) {
					list.add(str);
					hovers.add("ID: "+this.data.get(str));
					this.scrollData.put(str, str);
				}
				Collections.sort(list);
				break;
			}
			case Factions: {
				Map<String, String> mapH = Maps.<String, String>newHashMap();
				Map<String, Integer> mapC = Maps.<String, Integer>newHashMap();
				this.scrollData.clear();
				for (String str : this.data.keySet()) {
					if (this.search.isEmpty() || str.toLowerCase().contains(this.search)) {
						String[] l = str.split(";");
						String key = l[0]+((char) 167)+"7 (ID:"+this.data.get(str)+")";
						list.add(key);
						int value = -1;
						try { value = Integer.parseInt(l[1]); } catch (Exception e) {}
						this.scrollData.put(key, str);
						
						int color = 0xFFFFFF;
						String hover = new TextComponentTranslation("type.value").getFormattedText()+": "+((char) 167)+"3"+value;
						Faction f = FactionController.instance.factions.get(this.data.get(str));
						if (f!=null) {
							hover += "<br>"+new TextComponentTranslation("gui.attitude").getFormattedText()+": ";
							ITextComponent add;
							if (value < f.neutralPoints) {
								add = new TextComponentTranslation("faction.unfriendly");
								add.getStyle().setColor(TextFormatting.DARK_RED);
							}
							else if (value < f.friendlyPoints) {
								add = new TextComponentTranslation("faction.neutral");
								add.getStyle().setColor(TextFormatting.GOLD);
							}
							else {
								add = new TextComponentTranslation("faction.friendly");
								add.getStyle().setColor(TextFormatting.DARK_GREEN);
							}
							hover += add.getFormattedText();
							color = f.color;
						}
						mapH.put(key, hover);
						mapC.put(key, color);
					}
				}
				Collections.sort(list);
				for (String key : list) {
					hovers.add(mapH.get(key));
					colors.add(mapC.get(key));
				}
				break;
			}
			default: { return; }
		}
		this.scroll.setListNotSorted(list);
		this.scroll.hoversTexts = null;
		this.scroll.setSuffixs(null);
		if (!hovers.isEmpty()) {
			this.scroll.hoversTexts = new String[hovers.size()][];
			int i = 0;
			for (String str : hovers) { this.scroll.hoversTexts[i] = str.split("<br>"); i++;}
		}
		if (!suffixs.isEmpty()) {
			this.scroll.setSuffixs(suffixs);
		}
		this.scroll.setColors(null);
		if (!colors.isEmpty()) {
			this.scroll.setColors(colors);
		}
	}

	public void initButtons() {
		boolean hasPlayer = this.selectedPlayer!=null && !this.selectedPlayer.isEmpty();
		this.getButton(0).setEnabled(hasPlayer);
		this.getButton(0).setVisible(this.selection == EnumPlayerData.Players);
		this.getButton(1).setEnabled(this.selection != EnumPlayerData.Players && hasPlayer);
		this.getButton(2).setEnabled(this.selection != EnumPlayerData.Quest && hasPlayer);
		this.getButton(3).setEnabled(this.selection != EnumPlayerData.Dialog && hasPlayer);
		this.getButton(4).setEnabled(this.selection != EnumPlayerData.Transport && hasPlayer);
		this.getButton(5).setEnabled(this.selection != EnumPlayerData.Bank && hasPlayer);
		this.getButton(6).setEnabled(this.selection != EnumPlayerData.Factions && hasPlayer);
		boolean canEdit = this.selection != EnumPlayerData.Players && this.selection != EnumPlayerData.Wipe;
		this.getButton(8).setVisible(true);
		this.getButton(9).setVisible(true);
		this.getButton(10).setVisible(true);
		this.getButton(11).setVisible(true);
		switch(this.selection) {
			case Quest: {
				this.getButton(8).setEnabled(canEdit && hasPlayer);
				this.getButton(9).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(false);
				break;
			}
			case Dialog: {
				this.getButton(8).setEnabled(canEdit && hasPlayer);
				this.getButton(9).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(false);
				break;
			}
			case Transport: {
				this.getButton(8).setEnabled(canEdit && hasPlayer);
				this.getButton(9).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(false);
				break;
			}
			case Bank: {
				this.getButton(8).setEnabled(canEdit && hasPlayer && this.data.size() < BankController.getInstance().banks.size());
				this.getButton(9).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				break;
			}
			case Factions: {
				this.getButton(8).setEnabled(canEdit && hasPlayer && this.data.size() < FactionController.instance.factions.size());
				this.getButton(9).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(canEdit && hasPlayer && this.scroll!=null && this.scroll.hasSelected());
				break;
			}
			default: {
				this.getButton(8).setVisible(false);
				this.getButton(9).setVisible(false);
				this.getButton(10).setVisible(false);
				this.getButton(11).setVisible(false);
			}
		}
		if (!hasPlayer) { this.getLabel(0).setLabel("data.all.players"); }
		else { this.getLabel(0).setLabel(new TextComponentTranslation("data.sel.player", ((char) 167)+(this.isOnline ? "2" : "4")+((char) 167)+"l"+this.selectedPlayer).getFormattedText()); }
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll==null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(300, 152); }
		this.scroll.guiLeft = this.guiLeft + 7;
		this.scroll.guiTop = this.guiTop + 16;
		this.addScroll(this.scroll);
		this.selected = null;
		this.addLabel(new GuiNpcLabel(0, "data.all.players", this.guiLeft + 10, this.guiTop + 6));
		int x = this.guiLeft + 313, y = this.guiTop + 16, w = 99;
		this.addButton(new GuiNpcButton(0, x, y, w, 20, "selectWorld.deleteButton"));
		this.addButton(new GuiNpcButton(1, x, (y += 22), w, 20, "playerdata.players"));
		this.addButton(new GuiNpcButton(2, x, (y += 22), w, 20, "quest.quest"));
		this.addButton(new GuiNpcButton(3, x, (y += 22), w, 20, "dialog.dialog"));
		this.addButton(new GuiNpcButton(4, x, (y += 22), w, 20, "global.transport"));
		this.addButton(new GuiNpcButton(5, x, (y += 22), w, 20, "global.banks"));
		this.addButton(new GuiNpcButton(6, x, (y += 22), w, 20, "menu.factions"));
		this.addButton(new GuiNpcButton(7, x, (y += 22), w, 20, "gui.wipe"));
		y = this.guiTop + 170;
		this.addLabel(new GuiNpcLabel(1, "gui.found", this.guiLeft + 10, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 66, y, 240, 20, this.search));
		x = this.guiLeft + 7;
		w = 73;
		this.addButton(new GuiNpcButton(8, x, (y += 22), w, 20, "gui.add"));
		this.addButton(new GuiNpcButton(9, (x += w + 3), y, w, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(10, (x += w + 3), y, w, 20, "gui.remove.all"));
		this.addButton(new GuiNpcButton(11, (x += w + 3), y, w, 20, "selectServer.edit"));
		this.initButtons();
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
		if (this.selection == EnumPlayerData.Wipe) {
			return;
		}
		if (this.search.equals(this.getTextField(0).getText())) {
			return;
		}
		this.search = this.getTextField(0).getText().toLowerCase();
		this.setCurentList();
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && this.scroll != null) {
			this.scroll.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		ContainerNPCBankInterface.editBank = null;
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.selected = guiCustomScroll.getSelected();
		if (this.selection == EnumPlayerData.Players) {
			this.selectedPlayer = this.selected;
			this.isOnline = this.data.get(this.selected)==1;
		}
		this.initButtons();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		this.editData();
	}

	private void editData() {
		if (!this.scroll.hasSelected()) { return; }
		switch(this.selection) {
			case Bank: {
				Client.sendData(EnumPacketServer.BankShow, this.selection, this.selectedPlayer, this.data.get(this.scrollData.get(this.scroll.getSelected())));
				break;
			}
			case Factions: {
				int factionId = this.data.get(this.scrollData.get(this.scroll.getSelected()));
				SubGuiEditText subgui = new SubGuiEditText(1, "");
				Faction f = FactionController.instance.factions.get(factionId);
				String v = this.scroll.hoversTexts[this.scroll.selected][0];
				int value = -1;
				try { value = Integer.parseInt(v.substring(v.indexOf(((char) 167)+"3") + 2)); } catch (Exception e) {}
				if (f!=null) {
					subgui.numbersOnly = new int[] { 0, f.friendlyPoints * 2, value };
				} else {
					subgui.numbersOnly = new int[] { 0, Integer.MAX_VALUE, value };
				}
				subgui.text[0] = ""+value;
				subgui.lable = "gui.set.new.value";
				this.setSubGui(subgui);
				break;
			}
			default: { return; }
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		this.data.putAll(data);
		this.setCurentList();
		if (this.selection == EnumPlayerData.Players && this.selectedPlayer != null) {
			this.scroll.setSelected(this.selectedPlayer);
			this.selected = this.selectedPlayer;
		}
		if (this.selection == EnumPlayerData.Wipe) {
			this.selection = EnumPlayerData.Players;
		}
		this.initButtons();
		if (ContainerNPCBankInterface.editBank!=null) {
			this.actionPerformed(new GuiNpcButton(5, 0, 0, ""));
			ContainerNPCBankInterface.editBank = null;
		}
	}

	@Override
	public void setSelected(String selected) { }
	
	public void confirmClicked(boolean result, int id) {
		String sel = ""+this.selected;
		String player = ""+this.selectedPlayer;
		EnumPlayerData epd = this.selection;
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		this.selected = sel;
		this.selectedPlayer = player;
		this.selection = epd;
		if (!result) { return; }
		if (id == 0) {
			if (this.selection != EnumPlayerData.Players) { return ; }
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, this.selection, this.selectedPlayer, this.selected);
			this.selected = null;
			this.selectedPlayer = null;
			this.scroll.selected = -1;
			this.initButtons();
		}
		else if (id == 7) {
			this.selection = EnumPlayerData.Wipe;
			this.initButtons();
			this.scroll.clear();
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, this.selection, this.selectedPlayer, this.selected);
			this.selected = null;
			this.selectedPlayer = null;
			this.scroll.selected = -1;
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiEditText)) { return; }
		if (((SubGuiEditText) subgui).id==1) { // set
			try { Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 2, this.data.get(this.scrollData.get(this.scroll.getSelected())), Integer.parseInt(((SubGuiEditText) subgui).text[0])); }
			catch (Exception e) { }
		} else if (((SubGuiEditText) subgui).id==0) { // add
			try { Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 0, Integer.parseInt(((SubGuiEditText) subgui).text[0])); }
			catch (Exception e) { }
		}
	}
		
}
