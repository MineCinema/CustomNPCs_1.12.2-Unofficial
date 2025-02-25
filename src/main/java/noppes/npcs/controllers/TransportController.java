package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;

public class TransportController {
	
	private static TransportController instance;

	public static TransportController getInstance() {
		if (TransportController.instance==null) { TransportController.instance = new TransportController(); }
		return TransportController.instance;
	}

	public Map<Integer, TransportCategory> categories;
	public List<Integer> worldIDs;
	private int lastUsedID;

	private Map<Integer, TransportLocation> locations;

	public TransportController() {
		this.locations = Maps.<Integer, TransportLocation>newTreeMap();
		this.categories = Maps.<Integer, TransportCategory>newTreeMap();
		this.lastUsedID = 0;
		this.worldIDs = Lists.<Integer>newArrayList();
		(TransportController.instance = this).loadCategories();
		if (this.categories.isEmpty()) {
			TransportCategory cat = new TransportCategory();
			cat.id = 1;
			cat.title = "Default";
			this.categories.put(cat.id, cat);
		}
	}

	public boolean containsLocationName(String name) {
		name = name.toLowerCase();
		for (TransportLocation loc : this.locations.values()) {
			if (loc.name.toLowerCase().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (TransportCategory category : this.categories.values()) {
			NBTTagCompound compound = new NBTTagCompound();
			category.writeNBT(compound);
			list.appendTag(compound);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", this.lastUsedID);
		nbttagcompound.setTag("NPCTransportCategories", list);
		
		Collection<Integer> set = DimensionHandler.getInstance().getMapDimensionsIDs().values();
		int[] ws = new int[set.size()];
		int i = 0;
		for (int id : set) { ws[i] = id; i++; }
		nbttagcompound.setIntArray("WorldIDs", ws);
		
		return nbttagcompound;
	}

	public TransportLocation getTransport(int transportId) {
		return this.locations.get(transportId);
	}

	public TransportLocation getTransport(String name) {
		for (TransportLocation loc : this.locations.values()) {
			if (loc.name.equals(name)) {
				return loc;
			}
		}
		return null;
	}

	private int getUniqueIdCategory() {
		int id = 0;
		for (int catid : this.categories.keySet()) {
			if (catid > id) {
				id = catid;
			}
		}
		return ++id;
	}

	private int getUniqueIdLocation() {
		if (this.lastUsedID == 0) {
			for (int catid : this.locations.keySet()) {
				if (catid > this.lastUsedID) {
					this.lastUsedID = catid;
				}
			}
		}
		return ++this.lastUsedID;
	}

	private void loadCategories() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		try {
			File file = new File(saveDir, "transport.dat");
			if (!file.exists()) {
				return;
			}
			this.loadCategories(file);
		} catch (IOException e) {
			try {
				File file2 = new File(saveDir, "transport.dat_old");
				if (!file2.exists()) {
					return;
				}
				this.loadCategories(file2);
			} catch (IOException ex) {
			}
		}
	}

	public void loadCategories(File file) throws IOException {
		try {
			this.loadCategories(CompressedStreamTools.readCompressed(new FileInputStream(file)));
		} catch (Exception e) {}
	}
	
	public void loadCategories(NBTTagCompound compound) {
		this.locations.clear();
		this.categories.clear();
		this.lastUsedID = compound.getInteger("lastID");
		NBTTagList list = compound.getTagList("NPCTransportCategories", 10);
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				TransportCategory category = new TransportCategory();
				category.readNBT(list.getCompoundTagAt(i));
				for (TransportLocation location : category.locations.values()) {
					this.locations.put(location.id, location);
				}
				this.categories.put(category.id, category);
			}
		}
		if (compound.hasKey("WorldIDs", 11)) {
			this.worldIDs.clear();
			for (int id : compound.getIntArray("WorldIDs")) {
				this.worldIDs.add(id);
			}
		}
	}

	public void removeCategory(int id) {
		if (this.categories.size() == 1) {
			return;
		}
		TransportCategory cat = this.categories.get(id);
		if (cat == null) {
			return;
		}
		for (int i : cat.locations.keySet()) {
			this.locations.remove(i);
		}
		this.categories.remove(id);
		this.saveCategories();
	}

	public TransportLocation removeLocation(int location) {
		TransportLocation loc = this.locations.get(location);
		if (loc == null) {
			return null;
		}
		loc.category.locations.remove(location);
		this.locations.remove(location);
		this.saveCategories();
		return loc;
	}

	private void saveCategories() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "transport.dat_new");
			File file2 = new File(saveDir, "transport.dat_old");
			File file3 = new File(saveDir, "transport.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(file));
			if (file2.exists()) {
				file2.delete();
			}
			file3.renameTo(file2);
			if (file3.exists()) {
				file3.delete();
			}
			file.renameTo(file3);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	public void saveCategory(NBTTagCompound compound) {
		int id = compound.getInteger("CategoryId");
		if (id < 0) { id = this.getUniqueIdCategory(); }
		if (this.categories.containsKey(id)) {
			this.categories.get(id).readNBT(compound);
		} else {
			TransportCategory category = new TransportCategory();
			category.readNBT(compound);
			category.id = id;
			this.categories.put(id, category);
		}
		this.saveCategories();
	}

	public TransportLocation saveLocation(int categoryId, NBTTagCompound compound, EntityPlayerMP player, EntityNPCInterface npc) {
		TransportCategory category = this.categories.get(categoryId);
		if (category == null || !(npc.advanced.roleInterface instanceof RoleTransporter)) { return null; }
		RoleTransporter role = (RoleTransporter) npc.advanced.roleInterface;
		TransportLocation location = new TransportLocation();
		location.readNBT(compound);
		location.category = category;
		if (role.hasTransport()) {
			location.id = role.transportId;
		}
		if (location.id < 0 || !this.locations.get(location.id).name.equals(location.name)) {
			while (this.containsLocationName(location.name)) {
				StringBuilder sb = new StringBuilder();
				TransportLocation transportLocation = location;
				transportLocation.name = sb.append(transportLocation.name).append("_").toString();
			}
		}
		if (location.id < 0) {
			location.id = this.getUniqueIdLocation();
		}
		category.locations.put(location.id, location);
		this.locations.put(location.id, location);
		this.saveCategories();
		return location;
	}

	public void setLocation(TransportLocation location) {
		if (this.locations.containsKey(location.id)) {
			for (TransportCategory cat : this.categories.values()) {
				cat.locations.remove(location.id);
			}
		}
		this.locations.put(location.id, location);
		location.category.locations.put(location.id, location);
	}
	
	public Map<Integer, TransportLocation> getLocations() { return this.locations; }
}
