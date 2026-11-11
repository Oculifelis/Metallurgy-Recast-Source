package it.hurts.metallurgy_reforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.metallurgy_reforged.gadget.GadgetLogic;
import it.hurts.metallurgy_reforged.gadget.ShieldLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/** Native player skin and the unchanged gauntlet mesh share one moving arm pose (same as 26.1.2). */
@EventBusSubscriber(modid = "metallurgy", value = Dist.CLIENT)
public final class GauntletHandRenderer {
    @SubscribeEvent
    public static void render(RenderHandEvent event) {
        if (!GadgetLogic.gauntlet(event.getItemStack())) return;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        if (!(minecraft.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;

        HumanoidArm hand = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean left = hand == HumanoidArm.LEFT;
        var model = renderer.getModel();
        var arm = left ? model.leftArm : model.rightArm;
        var pose = event.getPoseStack();
        pose.pushPose();
        try {
            applyArmPose(pose, left, event.getEquipProgress(), event.getSwingProgress());
            if (!player.isInvisible() && !ShieldLogic.invisible(player)) {
                if (left) renderer.renderLeftHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
                else renderer.renderRightHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
            }
            // The same arm pose the vanilla hand draw leaves, also when the skin draw is skipped.
            arm.resetPose();
            arm.xRot = 0.0F;
            arm.zRot = left ? -0.1F : 0.1F;

            // Attach in the same arm-local frame as the third-person hand layer (slim offset included).
            model.translateToHand(hand, pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.mulPose(Axis.YP.rotationDegrees(180));
            pose.translate((left ? -1.0F : 1.0F) / 16.0F, 2.0F / 16.0F, -10.0F / 16.0F);
            minecraft.gameRenderer.itemInHandRenderer.renderItem(player, event.getItemStack(),
                    left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    left, pose, event.getMultiBufferSource(), event.getPackedLight());
            // Replace the ordinary held-item draw; it would omit the skin and
            // apply a second, independent first-person transform to the metal.
            event.setCanceled(true);
        } finally {
            pose.popPose();
        }
    }

    /** ItemInHandRenderer.renderPlayerArm constants (unchanged from 1.20.1 to 26.1.2). */
    private static void applyArmPose(PoseStack pose, boolean left, float equip, float swing) {
        float sign = left ? -1.0F : 1.0F;
        float root = Mth.sqrt(swing);
        float pi = (float) Math.PI;
        pose.translate(sign * (0.64000005F - 0.3F * Mth.sin(root * pi)),
                -0.6F - 0.6F * equip + 0.4F * Mth.sin(root * 2.0F * pi),
                -0.71999997F - 0.4F * Mth.sin(swing * pi));
        pose.mulPose(Axis.YP.rotationDegrees(sign * 45.0F));
        pose.mulPose(Axis.YP.rotationDegrees(sign * Mth.sin(root * pi) * 70.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(sign * Mth.sin(swing * swing * pi) * -20.0F));
        pose.translate(sign * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(sign * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(sign * -135.0F));
        pose.translate(sign * 5.6F, 0, 0);
    }

    private GauntletHandRenderer() {}
}
