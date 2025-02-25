package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.IPermission;

public class ItemNpcCloner
extends Item
implements IPermission {
	
	public ItemNpcCloner() {
		this.setRegistryName(CustomNpcs.MODID, "npcmobcloner");
		this.setUnlocalizedName("npcmobcloner");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.CloneList || e == EnumPacketServer.SpawnMob || e == EnumPacketServer.MobSpawner
				|| e == EnumPacketServer.ClonePreSave || e == EnumPacketServer.CloneRemove
				|| e == EnumPacketServer.CloneSave || e == EnumPacketServer.GetClone;
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && player instanceof EntityPlayerMP) {
			boolean summon = false;
			PlayerData data = CustomNpcs.proxy.getPlayerData(player);
			if (data==null) { return EnumActionResult.SUCCESS; }
			ItemStack stackCloner = player.getHeldItemMainhand();
			if (!data.hud.hasOrKeysPressed(42, 54) && stackCloner!=null && stackCloner.getItem()==this) {
				NBTTagCompound nbt = stackCloner.getTagCompound();
				if (nbt!=null && nbt.hasKey("Settings", 10)) {
					NBTTagCompound nbtData = nbt.getCompoundTag("Settings");
					if (nbt.getBoolean("isServerClone")) {
						Client.sendData(EnumPacketServer.SpawnMob, true, pos.getX(), pos.getY(), pos.getZ(), nbtData.getString("Name"), nbtData.getInteger("Tab"));
						summon = true;
					} else {
						Client.sendData(EnumPacketServer.SpawnMob, false, pos.getX(), pos.getY(), pos.getZ(), nbtData.getCompoundTag("EntityNBT"));
						summon = true;
					}
				}
			}
			if (!summon) { NoppesUtilServer.sendOpenGui(player, EnumGuiType.MobSpawner, null, pos.getX(), pos.getY(), pos.getZ()); }
		}
		return EnumActionResult.SUCCESS;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.cloner").getFormattedText());
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt==null || !nbt.hasKey("Settings", 10)) {
			list.add(new TextComponentTranslation("info.item.cloner.empty.0").getFormattedText());
			list.add(new TextComponentTranslation("info.item.cloner.empty.1").getFormattedText());
		} else {
			list.add(new TextComponentTranslation("info.item.cloner.set.0", nbt.getCompoundTag("Settings").getString("Name")).getFormattedText());
			list.add(new TextComponentTranslation("info.item.cloner.set.1").getFormattedText());
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		return super.hasEffect(stack) || (nbt!=null && nbt.hasKey("Settings", 10) && !nbt.getCompoundTag("Settings").getString("Name").isEmpty());
	}
	
}
