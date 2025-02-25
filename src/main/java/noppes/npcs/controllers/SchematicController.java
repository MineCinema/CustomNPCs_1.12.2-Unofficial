package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.schematics.Blueprint;
import noppes.npcs.schematics.BlueprintUtil;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.AdditionalMethods;

public class SchematicController {
	
	public static SchematicController Instance = new SchematicController();
	public static long time = 50L;
	
	private List<SchematicWrapper> buildingList;
	public List<String> included;
	public Map<String, SchematicWrapper> map;
	private char chr = ((char) 167);
	
	public SchematicController() {
		this.buildingList = Lists.<SchematicWrapper>newArrayList();
		this.included = Arrays.asList("archery_range.schematic", "bakery.schematic", "barn.schematic",
				"building_site.schematic", "chapel.schematic", "church.schematic", "gate.schematic",
				"glassworks.schematic", "guard_Tower.schematic", "guild_house.schematic", "house.schematic",
				"house_small.schematic", "inn.schematic", "library.schematic", "lighthouse.schematic", "mill.schematic",
				"observatory.schematic", "ship.schematic", "shop.schematic", "stall.schematic", "stall2.schematic",
				"stall3.schematic", "tier_house1.schematic", "tier_house2.schematic", "tier_house3.schematic",
				"tower.schematic", "wall.schematic", "wall_corner.schematic");
		this.map = Maps.<String, SchematicWrapper>newHashMap();
	}

	public void build(SchematicWrapper schem, ICommandSender sender) {
		if (schem == null) {
			this.sendMessage(sender, "schematic.info.notbuild");
			return;
		}
		if (this.buildingList.contains(schem)) {
			this.sendMessage(sender, "schematic.info.already", this.chr + "7" + schem.schema.getName(), this.chr + "7" + schem.getPercentage(), this.chr + "7%");
			if (schem.sender != null) {
				this.sendMessage(sender, "schematic.info.start.name", this.chr + "7" + schem.sender.getName());
			}
			return;
		}
		schem.setBuilder(sender);
		this.buildingList.add(schem);
	}

	public static File getDir() {
		File schematicDir = new File(CustomNpcs.getWorldSaveDirectory(), "schematics");
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		while (saveDir.getParentFile()!=null) {
			saveDir = saveDir.getParentFile();
			if ((new File(saveDir, "config")).exists()) {
				schematicDir = new File(saveDir, "schematics");
				break;
			}
		}
		if (!schematicDir.exists()) { schematicDir.mkdir(); }
		return schematicDir;
	}

	public List<String> list() {
		List<String> list = new ArrayList<String>();
		list.addAll(this.included);
		for (File file : SchematicController.getDir().listFiles()) {
			String name = file.getName();
			if (name.toLowerCase().endsWith(".schematic") || name.toLowerCase().endsWith(".blueprint")) {
				list.add(name);
			}
		}
		Collections.sort(list);
		return list;
	}

	public SchematicWrapper load(String name) {
		InputStream stream = null;
		if (this.included.contains(name)) {
			stream = MinecraftServer.class.getResourceAsStream("/assets/" + CustomNpcs.MODID + "/schematics/" + name);
		}
		if (stream == null) {
			File file = new File(SchematicController.getDir(), name);
			if (!file.exists()) {
				for (File f : SchematicController.getDir().listFiles()) {
					if (f.getName().equalsIgnoreCase(name)) {
						file = f;
						break;
					}
				}
			}
			if (!file.exists()) { return null; }
			try { stream = new FileInputStream(file); }
			catch (FileNotFoundException e2) { return null; }
		}
		SchematicWrapper schemaWr = null;
		try {
			NBTTagCompound compound = CompressedStreamTools.readCompressed(stream);
			stream.close();
			if (name.toLowerCase().endsWith(".blueprint")) {
				Blueprint bp = BlueprintUtil.readBlueprintFromNBT(compound);
				bp.setName(name);
				schemaWr = new SchematicWrapper(bp);
			}
			Schematic schema = new Schematic(name);
			schema.load(compound);
			schemaWr = new SchematicWrapper(schema);
		} catch (IOException e) {
			LogWriter.except(e);
		}
		if (schemaWr!=null) { this.map.put(name.toLowerCase(), schemaWr); }
		return schemaWr;
	}

	public void save(ICommandSender sender, String name, int type, BlockPos pos, short height, short width, short length) {
		name = name.replace(" ", "_");
		if (this.included.contains(name)) {
			return;
		}
		World world = sender.getEntityWorld();
		File file = null;
		ISchematic schema = null;
		if (type == 0) {
			file = new File(SchematicController.getDir(), name + ".schematic");
			schema = Schematic.create(world, name, pos, height, width, length);
		} else if (type == 1) {
			file = new File(SchematicController.getDir(), name + ".blueprint");
			schema = BlueprintUtil.createBlueprint(world, pos, width, length, height);
		}
		NoppesUtilServer.NotifyOPs("Schematic " + name + " succesfully created", new Object[0]);
		try {
			CompressedStreamTools.writeCompressed(schema.getNBT(), (OutputStream) new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(ICommandSender sender, String message, Object ... objs) {
		if (sender == null) { return; }
		sender.sendMessage(new TextComponentTranslation(message, objs));
	}

	public void stop(ICommandSender sender) {
		if (this.buildingList == null || this.buildingList.isEmpty()) {
			this.sendMessage(sender, "schematic.info.build.empty");
		} else {
			String smts = "";
			for (SchematicWrapper sm : this.buildingList) {
				if (!smts.isEmpty()) { smts += ";"+((char) 10); }
				smts += this.chr + "7\"" + sm.schema.getName() + "\" in ["+sm.start.getX() + ", " + sm.start.getY() + ", " + sm.start.getZ()+"]";
			}
			this.sendMessage(sender, "schematic.info.build.stop", smts);
			this.buildingList.clear();
		}
	}

	public void updateBuilding() {
		if (this.buildingList == null || this.buildingList.isEmpty()) { return; }
		List<SchematicWrapper> del = Lists.newArrayList();
		for (SchematicWrapper sm : this.buildingList) {
			sm.build();
			if (sm.sender != null && sm.getPercentage() - sm.buildingPercentage >= 10) {
				this.sendMessage(sm.sender, "schematic.info.build.percentage", this.chr + "7" + sm.schema.getName(), this.chr + "7" + sm.getPercentage(), this.chr + "7%");
				sm.buildingPercentage = sm.getPercentage();
			}
			if (!sm.isBuilding) {
				if (sm.sender != null) {
					if (sm.schema.hasEntitys()) {
						this.sendMessage(sm.sender, "schematic.info.spawn.entitys", this.chr + "7" + sm.schema.getName());
					}
					this.sendMessage(sm.sender, "schematic.info.build.finish", this.chr + "7" + sm.schema.getName());
				}
				del.add(sm);
			}
		}
		for (SchematicWrapper sm : del) { this.buildingList.remove(sm); }
	}

	public SchematicWrapper getSchema(String name) {
		if (!this.map.containsKey(name.toLowerCase())) { this.load(name.toLowerCase()); }
		return this.map.get(name.toLowerCase());
	}

	public static void buildBlocks(EntityPlayerMP player, BlockPos pos, int rotaion, Schematic schema) { // Schematica Build
		if (player == null || pos==null || schema==null) { return; }
		long ticks = 3000L + schema.blockIdsArray.length * SchematicController.time + (long) Math.floor(schema.blockIdsArray.length / CustomNpcs.maxBuilderBlocks) * 1000L;
		player.sendMessage(new TextComponentTranslation("schematic.info.started", schema.name, ""+pos.getX(), ""+pos.getY(), ""+pos.getZ(), AdditionalMethods.ticksToElapsedTime(ticks, true, true, false)));
		SchematicWrapper sw = new SchematicWrapper(schema);
		sw.init(pos.east().south(), player.world, rotaion * 90);
		SchematicController.Instance.build(sw, player);
	}

	public void info(ICommandSender sender) {
		if (this.buildingList.isEmpty()) {
			this.sendMessage(sender, "schematic.info.empty");
			return;
		}
		for (SchematicWrapper sm : this.buildingList) {
			this.sendMessage(sender, "schematic.info.0", this.chr + "7" + sm.schema.getName(), this.chr + "7" + sm.getPercentage(), this.chr + "7%", (sm.sender==null ? "" : new TextComponentTranslation("schematic.info.1").getFormattedText()));
		}
	}

}
