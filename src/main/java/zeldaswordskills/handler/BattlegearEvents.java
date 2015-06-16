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

package zeldaswordskills.handler;


/**
 * 
 * Specifically for handling Battlegear2 events
 *
 */
public class BattlegearEvents {

	/* TODO add if/when BG2 updates
	@SubscribeEvent
	public void onBlocked(ShieldBlockEvent event) {
		if (event.shield.getItem() instanceof ItemZeldaShield) {
			event.ammountRemaining = ((ItemZeldaShield) event.shield.getItem()).onBlock(event.entityPlayer, event.shield, event.source, event.ammount);
			event.performAnimation = false;
			event.damageShield = false;
		}
	}

	/**
	 * Returns the currently selected quiver arrow, or null if empty or no quiver
	 * @param quiver if null, current quiver retrieved using {@link QuiverArrowRegistry#getArrowContainer}
	 *
	public static ItemStack getQuiverArrow(ItemStack bow, EntityPlayer player) {
		return getQuiverArrow(bow, QuiverArrowRegistry.getArrowContainer(bow, player), player);
	}

	/**
	 * Returns the currently selected quiver arrow, or null if quiver is null or does not contain arrows
	 *
	public static ItemStack getQuiverArrow(ItemStack bow, ItemStack quiver, EntityPlayer player) {
		if (quiver != null) {
			int slot = ((IArrowContainer2) quiver.getItem()).getSelectedSlot(quiver);
			return ((IArrowContainer2) quiver.getItem()).getStackInSlot(quiver, slot);
		}
		return null;
	}

	/**
	 * Required to prevent nocking unusable ZSS arrows in vanilla and other bows
	 * (merely cosmetic, as they can not be fired by the default fire handler)
	 *
	@SubscribeEvent(priority=EventPriority.HIGH)
	public void preArrowNock(ArrowNockEvent event) {
		// ISpecialBows should determine nocking result on their own
		if (!(event.result.getItem() instanceof ISpecialBow)) {
			ItemStack arrow = getQuiverArrow(event.result, event.entityPlayer);
			if (arrow != null && arrow.getItem() instanceof ItemZeldaArrow) {
				event.setCanceled(!event.entityPlayer.capabilities.isCreativeMode);
			}
		}
	}
	*/
}
