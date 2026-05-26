/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.launch.mixins;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.RenderEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Brady
 * @since 2/13/2020
 */
@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {

    // renderLevel signature changed in 1.21.x:
    //   removed PoseStack, float partialTick, long finishNano
    //   added DeltaTracker, split Matrix4f into projectionMatrix + viewMatrix
    @Inject(
            method = "renderLevel",
            at = @At("RETURN")
    )
    private void onRenderLevel(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera,
                                GameRenderer gameRenderer, LightTexture lightTexture,
                                Matrix4f projectionMatrix, Matrix4f viewMatrix, CallbackInfo ci) {
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);

        // Build a PoseStack from the view (model-view) matrix so downstream
        // rendering code that expects a PoseStack continues to work.
        PoseStack modelViewStack = new PoseStack();
        modelViewStack.mulPoseMatrix(viewMatrix);

        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            ibaritone.getGameEventHandler().onRenderPass(new RenderEvent(partialTicks, modelViewStack, projectionMatrix));
        }
    }
}
