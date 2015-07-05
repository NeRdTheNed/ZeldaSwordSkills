/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFire;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.entity.projectile.EntityMobThrowable;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Magic rods, such as the Fire and Ice Rod
 * 
 * A variety of magical rods are available throughout Link's adventures.
 * Each rod has two abilities: the first is a continuous effect activated while the rod is in
 * use - note that exhaustion will be added each tick; the second is activated by using the
 * item while sneaking, shooting a single projectile per use.
 * 
 * All magic rods can be upgraded by first bathing in the appropriate Sacred Flame, and then
 * tossing it and enough emeralds into an active fairy pool.
 *
 */
public class ItemMagicRod extends Item implements IFairyUpgrade, ISacredFlame, ISpawnParticles
{
	/** The type of magic this rod uses (e.g. FIRE, ICE, etc.) */
	private final MagicType magicType;

	/** The amount of damage inflicted by the rod's projectile spell */
	private final float damage;

	/** Amount of exhaustion to add each tick */
	private final float fatigue;

	/**
	 * @param magicType	The type of magic this rod uses (e.g. FIRE, ICE, etc.)
	 * @param damage	The amount of damage inflicted by the rod's projectile spell
	 * @param fatigue	Amount of exhaustion added when used; fatigue / 20 is added per tick in use
	 */
	public ItemMagicRod(int id, MagicType magicType, float damage, float fatigue) {
		super(id);
		this.magicType = magicType;
		this.damage = damage;
		this.fatigue = fatigue;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/**
	 * Returns the next time this stack may be used
	 */
	private long getNextUseTime(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getLong("next_use") : 0);
	}

	/**
	 * Sets the next time this stack may be used to the current world time plus a number of ticks
	 */
	private void setNextUseTime(ItemStack stack, World world, int ticks) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		stack.getTagCompound().setLong("next_use", (world.getWorldTime() + ticks));
	}

	/**
	 * Returns whether the rod has absorbed its associated sacred flame
	 */
	private boolean hasAbsorbedFlame(ItemStack stack) {
		return (stack.hasTagCompound() && stack.getTagCompound().getBoolean("absorbedFlame"));
	}

	/**
	 * Returns true if the rod is upgraded to the 'Nice' version
	 */
	private boolean isUpgraded(ItemStack stack) {
		return (stack.hasTagCompound() && stack.getTagCompound().getBoolean("isUpgraded"));
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		String s = (isUpgraded(stack) ? StatCollector.translateToLocal("item.zss.rodmagic.nice") + " " : "");
		return s + StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (player.capabilities.isCreativeMode || (world.getWorldTime() > getNextUseTime(stack) && player.getFoodStats().getFoodLevel() > 0)) {
			player.swingItem();
			if (player.isSneaking()) {
				boolean isUpgraded = isUpgraded(stack);
				EntityMobThrowable magic;
				if (magicType == MagicType.WIND) {
					magic = new EntityCyclone(world, player).setArea(isUpgraded(stack) ? 3.0F : 2.0F);
				} else {
					magic = new EntityMagicSpell(world, player).setType(magicType).setArea(isUpgraded ? 3.0F : 2.0F);
				}
				magic.setDamage(isUpgraded ? damage * 1.5F : damage);
				if (!world.isRemote) {
					WorldUtils.playSoundAtEntity(player, Sounds.WHOOSH, 0.4F, 0.5F);
					world.spawnEntityInWorld(magic);
				}
				player.addExhaustion(fatigue);
				if (!player.capabilities.isCreativeMode) {
					setNextUseTime(stack, world, 30);
				}
			} else {
				player.addExhaustion(fatigue / 8.0F);
				player.setItemInUse(stack, getMaxItemUseDuration(stack));
				if (this == ZSSItems.rodTornado) {
					ZSSPlayerInfo.get(player).reduceFallAmount += (isUpgraded(stack) ? 8.0F : 4.0F);
				}
			}
		} else {
			// TODO need better magic fail sound...
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
		}
		return stack;
	}

	@Override
	public void onUsingItemTick(ItemStack stack, EntityPlayer player, int count) {
		if (this == ZSSItems.rodTornado) {
			player.fallDistance = 0.0F;
		}
		if (!player.worldObj.isRemote) {
			player.addExhaustion(fatigue / 20.0F);
			if (player.getFoodStats().getFoodLevel() < 1) {
				player.clearItemInUse();
			} else {
				int ticksInUse = getMaxItemUseDuration(stack) - count;
				if (this == ZSSItems.rodTornado) {
					if (ticksInUse % 10 == 0) {
						player.worldObj.spawnEntityInWorld(new EntityCyclone(player.worldObj, player.posX, player.posY, player.posZ).disableGriefing());
					}
				} else {
					handleUpdateTick(stack, player.worldObj, player, ticksInUse);
				}
			}
		}
	}

	/**
	 * Handles fire and ice rod update tick
	 */
	private void handleUpdateTick(ItemStack stack, World world, EntityPlayer player, int ticksInUse) {
		float r = 0.5F + Math.min(5.5F, (ticksInUse / 10F));
		if (isUpgraded(stack)) {
			r *= 1.5F;
		}
		WorldUtils.sendPacketToAllAround(new PacketISpawnParticles(player, this, r).makePacket(), world, player, 64.0D);
		if (ticksInUse % 4 == 3) {
			affectBlocks(world, player, r);
			List<EntityLivingBase> targets = TargetUtils.acquireAllLookTargets(player, Math.round(r), 1.0F);
			for (EntityLivingBase target : targets) {
				target.attackEntityFrom(getDamageSource(player), r);
				if (magicType == MagicType.FIRE && !target.isImmuneToFire()) {
					target.setFire(5);
				}
			}
		}
		if (ticksInUse % magicType.getSoundFrequency() == 0) {
			world.playSoundAtEntity(player, magicType.getMovingSound(), magicType.getSoundVolume(world.rand), magicType.getSoundPitch(world.rand));
		}
	}

	/**
	 * Affects blocks within the area of effect provided there is line of sight to the player
	 */
	private void affectBlocks(World world, EntityPlayer player, float radius) {
		Set<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>();
		Vec3 vec3 = player.getLookVec();
		double x = player.posX + vec3.xCoord;
		double y = player.posY + player.getEyeHeight() + vec3.yCoord;
		double z = player.posZ + vec3.zCoord;
		int r = MathHelper.ceiling_float_int(radius);
		for (int n = 0; n < r; ++n) {
			int i = MathHelper.floor_double(x);
			int j = MathHelper.floor_double(y);
			int k = MathHelper.floor_double(z);
			if (canAddBlockPosition(world, player, i, j, k)) {
				affectedBlocks.add(new ChunkPosition(i, j, k));
			}
			x += vec3.xCoord;
			y += vec3.yCoord;
			z += vec3.zCoord;
		}

		affectAllBlocks(world, affectedBlocks, magicType);
	}

	private boolean canAddBlockPosition(World world, EntityPlayer player, int i, int j, int k) {
		Vec3 vec31 = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 vec32 = Vec3.createVectorHelper(i, j, k);
		MovingObjectPosition mop = world.clip(vec31, vec32);
		if (mop == null) {
			return true;
		} else if (mop.typeOfHit == EnumMovingObjectType.TILE) {
			if (mop.blockX == i && mop.blockY == j && mop.blockZ == k) {
				return true;
			}
			int blockId = world.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
			Block block = (blockId > 0 ? Block.blocksList[blockId] : null);
			return (block == null || block.getBlocksMovement(world, mop.blockX, mop.blockY, mop.blockZ));
		} else {
			return true;
		}
	}

	/**
	 * Affects all blocks in the set of chunk positions with the magic type's effect (freeze, thaw, etc.)
	 */
	public static void affectAllBlocks(World world, Set<ChunkPosition> blocks, MagicType type) {
		for (ChunkPosition position : blocks) {
			int blockId = world.getBlockId(position.x, position.y, position.z);
			switch(type) {
			case FIRE:
				if (blockId > 0 && WorldUtils.canMeltBlock(world, blockId, position.x, position.y, position.z) && world.rand.nextInt(4) == 0) {
					world.setBlockToAir(position.x, position.y, position.z);
					world.playSoundEffect(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D,
							Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (blockId == 0 && Block.opaqueCubeLookup[world.getBlockId(position.x, position.y - 1, position.z)] && world.rand.nextInt(8) == 0) {
					world.setBlock(position.x, position.y, position.z, Block.fire.blockID);
					world.playSoundEffect(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D,
							Sounds.FIRE_IGNITE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else if (blockId == ZSSBlocks.bombFlower.blockID) {
					ZSSBlocks.bombFlower.onBlockExploded(world, position.x, position.y, position.z, null);
				}
				break;
			case ICE:
				Block block = (blockId > 0 ? Block.blocksList[blockId] : null);
				if (block != null) {
					if (block.blockMaterial == Material.water && world.rand.nextInt(4) == 0) {
						world.setBlock(position.x, position.y, position.z, Block.ice.blockID);
						world.playSoundEffect(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D,
								Sounds.GLASS_BREAK, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
					} else if (block.blockMaterial == Material.lava && world.rand.nextInt(8) == 0) {
						Block solid = (blockId == Block.lavaStill.blockID ? Block.obsidian : Block.cobblestone);
						world.setBlock(position.x, position.y, position.z, solid.blockID);
						world.playSoundEffect(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D,
								Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
					} else if (block.blockMaterial == Material.fire) {
						world.setBlockToAir(position.x, position.y, position.z);
						world.playSoundEffect(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D,
								Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
					}
				}
				break;
			default:
			}
		}
	}

	/** Only used for Fire and Ice Rods */
	private DamageSource getDamageSource(EntityPlayer player) {
		switch(magicType) {
		case ICE: return new DamageSourceIce("blast.ice", player, 60, 1, true);
		default: return new DamageSourceFire("blast.fire", player, true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, double x, double y, double z, float r, Vec3 lookVector) {
		y += 1.62D;
		for (float f = 0; f < r; f += 0.5F) {
			x += lookVector.xCoord;
			y += lookVector.yCoord;
			z += lookVector.zCoord;
			for (int i = 0; i < 4; ++i) {
				world.spawnParticle(magicType.getTrailingParticle(),
						x + 0.5F - world.rand.nextFloat(),
						y + 0.5F - world.rand.nextFloat(),
						z + 0.5F - world.rand.nextFloat(),
						lookVector.xCoord * (0.5F * world.rand.nextFloat()),
						lookVector.yCoord * 0.15D,
						lookVector.zCoord * (0.5F * world.rand.nextFloat()));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return isUpgraded(stack) ? EnumRarity.rare : EnumRarity.common;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.1"));
		list.add("");
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("tooltip.zss.damage", "", String.format("%.1f", isUpgraded(stack) ? damage * 1.5F : damage)));
		if (this != ZSSItems.rodTornado) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.area", String.format("%.1f", isUpgraded(stack) ? 3.0F : 2.0F)));
		}
	}

	@Override
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		if (!world.isRemote) {
			if (hasAbsorbedFlame(stack)) {
				PlayerUtils.sendFormattedChat(player, "chat.zss.sacred_flame.old.any", getItemStackDisplayName(stack));
			} else if (isActive) {
				boolean canAbsorb = false;
				switch(magicType) {
				case FIRE: canAbsorb = type == BlockSacredFlame.DIN; break;
				case ICE: canAbsorb = type == BlockSacredFlame.NAYRU; break;
				case WIND: canAbsorb = type == BlockSacredFlame.FARORE; break;
				}
				if (canAbsorb) {
					if (!stack.hasTagCompound()) {
						stack.setTagCompound(new NBTTagCompound());
					}
					stack.getTagCompound().setBoolean("absorbedFlame", true);
					world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
					PlayerUtils.sendFormattedChat(player, "chat.zss.sacred_flame.new",
							getItemStackDisplayName(stack), StatCollector.translateToLocal("misc.zss.sacred_flame.name." + type));
					return true;
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
			}
			WorldUtils.playSoundAtEntity(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
		}
		return false;
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		int cost = Math.round(Config.getRodUpgradeCost() * (magicType == MagicType.WIND ? 0.75F : 1.0F));
		if (core.consumeRupees(cost)) {
			if (!item.getEntityItem().hasTagCompound()) {
				item.getEntityItem().setTagCompound(new NBTTagCompound());
			}
			item.getEntityItem().getTagCompound().setBoolean("isUpgraded", true);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return !isUpgraded(stack) && hasAbsorbedFlame(stack);
	}
}
