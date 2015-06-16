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

package zeldaswordskills.client.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntityOctorok extends RenderLiving
{
	private static final ResourceLocation texture1 = new ResourceLocation(ModInfo.ID + ":textures/entity/octorok1.png");
	private static final ResourceLocation texture2 = new ResourceLocation(ModInfo.ID + ":textures/entity/octorok2.png");

	public RenderEntityOctorok(RenderManager renderManager, ModelBase model, float shadowSize) {
		super(renderManager, model, shadowSize);
	}

	public void renderLivingSquid(EntityOctorok entity, double dx, double dy, double dz, float f, float f1) {
		super.doRender(entity, dx, dy, dz, f, f1);
	}

	protected void rotateSquidsCorpse(EntityOctorok octorok, float dx, float dy, float dz) {
		float f3 = octorok.prevSquidPitch + (octorok.squidPitch - octorok.prevSquidPitch) * dz;
		float f4 = octorok.prevSquidYaw + (octorok.squidYaw - octorok.prevSquidYaw) * dz;
		GlStateManager.translate(0.0F, 0.5F, 0.0F);
		GlStateManager.rotate(180.0F - dy, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(f4, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(0.0F, -1.2F, 0.0F);
	}

	protected float handleRotationFloat(EntityOctorok octorok, float f) {
		return octorok.prevTentacleAngle + (octorok.tentacleAngle - octorok.prevTentacleAngle) * f;
	}

	@Override
	public void doRender(EntityLiving entity, double dx, double dy, double dz, float f, float f1) {
		renderLivingSquid((EntityOctorok) entity, dx, dy, dz, f, f1);
	}

	@Override
	protected float handleRotationFloat(EntityLivingBase entity, float f) {
		return handleRotationFloat((EntityOctorok) entity, f);
	}

	@Override
	protected void rotateCorpse(EntityLivingBase entity, float dx, float dy, float dz) {
		rotateSquidsCorpse((EntityOctorok) entity, dx, dy, dz);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return (((EntityOctorok) entity).getType() == 0 ? texture1 : texture2);
	}
}
