package noppes.npcs.roles;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBard;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class JobBard
extends JobInterface
implements IJobBard {
	
	public boolean hasOffRange, isStreamer, isRange;
	public int[] range, minPos, maxPos;
	public String song;

	public JobBard(EntityNPCInterface npc) {
		super(npc);
		this.isRange = true;
		this.isStreamer = true;
		this.hasOffRange = true;
		this.range = new int[] { 2, 64 }; // min, max
		this.minPos = new int[] { 2, 2, 2 }; // x, y, z
		this.maxPos = new int[] { 64, 64, 64 }; // x, y, z
		this.song = "";
		this.type = JobType.BARD;
	}

	@Override
	public void delete() {
		// stopSound moved to ClientTickHandler.cnpcClientTick(event);
	}

	@Override
	public String getSong() {
		return this.song;
	}

	@Override
	public void killed() {
		if (this.npc.world.isRemote && this.isStreamer && this.hasOffRange && MusicController.Instance.isPlaying(this.song)) {
			MusicController.Instance.stopSound(this.song, this.isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
		}
	}

	public void onLivingUpdate() {
		if (!this.npc.isRemote() || this.song.isEmpty()) { return; }
		MusicController mData = MusicController.Instance;
		if (this.isStreamer ? mData.unloadSongBard : mData.unloadMusicBard) {
			EntityNPCInterface oldNPC = this.isStreamer ? mData.songBard : mData.musicBard;
			if (oldNPC==null) {
				if (this.isStreamer) { mData.unloadSongBard = false; } else { mData.unloadMusicBard = false; }
			} else if (oldNPC.getUniqueID().equals(this.npc.getUniqueID())) {
				if (this.isStreamer) {
					mData.unloadSongBard = false;
					mData.songBard = this.npc;
				} else {
					mData.musicBard = this.npc;
					mData.unloadMusicBard = false;
				}
			}
		}
		if (!mData.isBardPlaying(this.song, this.isStreamer)) { // not bard play song
			AxisAlignedBB aabb = this.npc.getEntityBoundingBox();
			if (this.isRange) {
				aabb = aabb.grow(this.range[0], this.range[0], this.range[0]);
			} else {
				aabb = new AxisAlignedBB(aabb.minX - this.minPos[0], aabb.minY - this.minPos[1], aabb.minZ - this.minPos[2],
						aabb.maxX + this.minPos[0], aabb.maxY + this.minPos[1], aabb.maxZ + this.minPos[2]);
			}
			List<EntityPlayer> list = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
			if (!list.contains(CustomNpcs.proxy.getPlayer())) { return; }
			mData.bardPlaySound(this.song, this.isStreamer, this.npc);
		}
		else if (this.isStreamer ? this.npc.equals(mData.songBard) : this.npc.equals(mData.musicBard) && !this.song.equals(this.isStreamer ? mData.song : mData.music)) {
			if (!mData.song.isEmpty() && this.npc.equals(mData.songBard)) { mData.stopSound(mData.song, SoundCategory.AMBIENT); }
			if (!mData.music.isEmpty() && this.npc.equals(mData.musicBard)) { mData.stopSound(mData.music, SoundCategory.MUSIC); }
		}
		else if (this.isStreamer ? !this.song.equals(mData.song) : !this.song.equals(mData.music)) { // cheak main NPC
			EntityPlayer player = CustomNpcs.proxy.getPlayer();
			if (player==null) { return; }
			EntityNPCInterface oldNPC = this.isStreamer ? mData.songBard : mData.musicBard;
			if (oldNPC==null || this.npc.getDistance(player) < oldNPC.getDistance(player)) {
				AxisAlignedBB aabb = this.npc.getEntityBoundingBox();
				if (this.isRange) {
					aabb = aabb.grow(this.range[0], this.range[0], this.range[0]);
				} else {
					aabb = new AxisAlignedBB(aabb.minX - this.minPos[0], aabb.minY - this.minPos[1], aabb.minZ - this.minPos[2],
							aabb.maxX + this.minPos[0], aabb.maxY + this.minPos[1], aabb.maxZ + this.minPos[2]);
				}
				List<EntityPlayer> list = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
				if (list.contains(CustomNpcs.proxy.getPlayer())) {
					mData.stopSound(this.isStreamer ? mData.song : mData.music, this.isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
					mData.bardPlaySound(this.song, this.isStreamer, this.npc);
				}
			}
		}
		else if (this.hasOffRange && (this.isStreamer ? this.npc.equals(mData.songBard) : this.npc.equals(mData.musicBard))) { // cheak Distance
			AxisAlignedBB aabb = this.npc.getEntityBoundingBox();
			if (this.isRange) {
				aabb = aabb.grow(this.range[1], this.range[1], this.range[1]);
			} else {
				aabb = new AxisAlignedBB(aabb.minX - this.maxPos[0], aabb.minY - this.maxPos[1], aabb.minZ - this.maxPos[2],
						aabb.maxX + this.maxPos[0], aabb.maxY + this.maxPos[1], aabb.maxZ + this.maxPos[2]);
			}
			List<EntityPlayer> list = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
			if (!list.contains(CustomNpcs.proxy.getPlayer())) {
				mData.stopSound(this.song, this.isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
			}
		}
		if (!this.isStreamer && mData.isPlaying(this.song)) {
			ObfuscationHelper.setValue(MusicTicker.class, Minecraft.getMinecraft().getMusicTicker(), 12000, 3);
		}
	}

	@Override
	public void setSong(String song) {
		this.song = song;
		this.npc.updateClient = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.BARD;
		this.song = compound.getString("BardSong");
		this.isStreamer = compound.getBoolean("BardStreamer");
		this.hasOffRange = compound.getBoolean("BardHasOff");
		if (compound.hasKey("BardRangeData", 7) && compound.hasKey("BardIsRange", 1)) {
			this.isRange = compound.getBoolean("BardIsRange");
			byte[] data = compound.getByteArray("BardRangeData");
			if (data.length > 1) { this.range = new int[] { data[0], data[1] }; }
			if (data.length > 4) { this.minPos = new int[] { data[2], data[3], data[4] }; }
			else { this.maxPos = new int[] { this.range[0], this.range[0], this.range[0] }; }
			if (data.length > 7) { this.maxPos = new int[] { data[5], data[6], data[7] }; }
			else { this.maxPos = new int[] { this.range[1], this.range[1], this.range[1] }; }
		}
		else if (compound.hasKey("BardMinRange", 3) && compound.hasKey("BardMaxRange", 3) && CustomNpcs.FixUpdateFromPre_1_12) {
			this.range = new int[] { compound.getInteger("BardMinRange"), compound.getInteger("BardMaxRange") };
			this.isRange = true;
			this.minPos = new int[] { this.range[0], this.range[0], this.range[0] };
			this.maxPos = new int[] { this.range[1], this.range[1], this.range[1] };
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.BARD.get());
		compound.setString("BardSong", this.song);
		compound.setBoolean("BardStreamer", this.isStreamer);
		compound.setBoolean("BardHasOff", this.hasOffRange);
		compound.setBoolean("BardIsRange", this.isRange);
		compound.setByteArray("BardRangeData", new byte[] { (byte) this.range[0], (byte) this.range[1],
				(byte) this.minPos[0], (byte) this.minPos[1], (byte) this.minPos[2],
				(byte) this.maxPos[0], (byte) this.maxPos[1], (byte) this.maxPos[2]});
		return compound;
	}

}
