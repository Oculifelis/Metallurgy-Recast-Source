package it.hurts.metallurgy_reforged.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.metallurgy_reforged.client.CorpseRenderData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class CorpseRenderMixin {
    @Unique private boolean metallurgy$corpse;
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void metallurgy$corpse(LivingEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo callback) {
        metallurgy$corpse=CorpseRenderData.active(entity);
        if(metallurgy$corpse) pose.translate(CorpseRenderData.jitter(),0,CorpseRenderData.jitter());
    }
    // One @ModifyArg per value: @ModifyArgs needs a synthetic Args class that fails to load outside the mod's own run.
    @ModifyArg(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),index=4)
    private float metallurgy$red(float value) {return metallurgy$corpse?value*100/255F:value;}
    @ModifyArg(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),index=5)
    private float metallurgy$green(float value) {return metallurgy$corpse?value*25/255F:value;}
    @ModifyArg(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),index=6)
    private float metallurgy$blue(float value) {return metallurgy$corpse?value*25/255F:value;}
}
