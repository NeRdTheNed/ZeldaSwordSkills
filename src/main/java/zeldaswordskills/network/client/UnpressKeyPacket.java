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

package zeldaswordskills.network.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Send from the server to unpress a key (or all keys) on the client
 *
 */
public class UnpressKeyPacket extends CustomClientPacket
{
	@SideOnly(Side.CLIENT)
	private static Map<Integer, KeyBinding> keyMap;

	public static final int
	/** Will unset keyBindAttack using keyBindAttack.getKeyCode */
	LMB = -100,
	/** Will unset keyBindUseItem using keyBindUseItem.getKeyCode */
	RMB = -99,
	/** Will unset keyBindPickBlock using keyBindPickBlock.getKeyCode */
	MMB = -98;

	/**
	 * Call from ClientProxy to initialize key map
	 */
	@SideOnly(Side.CLIENT)
	public static void init() {
		Minecraft mc = Minecraft.getMinecraft();
		keyMap = new HashMap<Integer, KeyBinding>();
		addKeyMapping(LMB, mc.gameSettings.keyBindAttack);
		addKeyMapping(RMB, mc.gameSettings.keyBindUseItem);
		addKeyMapping(MMB, mc.gameSettings.keyBindPickBlock);
	}

	/**
	 * Add a custom key mapping to allow manual unsetting of the key.
	 * Only use this method after FMLPreInitializationEvent is complete, otherwise
	 * the keyMap may not yet be initialized.
	 * @param keyCode	The default key code for the keybinding - should not conflict with any other keys
	 * @param kb		The key binding that will be unset via the keyCode
	 */
	@SideOnly(Side.CLIENT)
	public static void addKeyMapping(int keyCode, KeyBinding kb) {
		if (keyMap.containsKey(keyCode)) {
			LogHelper.warning("UnpressKeyPacket already contains a mapping for key code " + keyCode + "! Key binding " + kb + " will not have a mapping.");
		} else {
			keyMap.put(keyCode, kb);
		}
	}

	private int keyCode;

	public UnpressKeyPacket() {}

	public UnpressKeyPacket(int keyCode) {
		this.keyCode = keyCode;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(keyCode);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		keyCode = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (keyCode == 0) {
			KeyBinding.unPressAllKeys();
		} else if (keyMap.containsKey(keyCode)) {
			KeyBinding.setKeyBindState(keyMap.get(keyCode).keyCode, false);
		}
	}
}
