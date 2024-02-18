package dev.lemonclient.addon.mixins;

import dev.lemonclient.addon.utils.RaycastUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(CrystalAura.class)
public abstract class MixinCrystalAura {
    @Shadow(remap = false)
    private int attacks;

    @Shadow(remap = false)
    @Final
    public Setting<CrystalAura.SwingMode> swingMode;

    @Shadow(remap = false)
    @Final
    private Setting<CrystalAura.AutoSwitchMode> autoSwitch;

    @Shadow(remap = false)
    private boolean placing;

    @Shadow(remap = false)
    private int placingTimer;

    @Shadow(remap = false)
    public int kaTimer;

    @Shadow(remap = false)
    @Final
    private BlockPos.Mutable placingCrystalBlockPos;

    @Shadow(remap = false)
    @Final
    private BlockPos.Mutable placeRenderPos;

    @Shadow(remap = false)
    private double renderDamage;

    @Shadow(remap = false)
    @Final
    private Setting<CrystalAura.RenderMode> renderMode;

    @Shadow(remap = false)
    private int placeRenderTimer;

    @Shadow(remap = false)
    @Final
    private Setting<Integer> placeRenderTime;

    @Shadow(remap = false)
    @Final
    private Setting<Integer> renderTime;

    @Shadow(remap = false)
    @Final
    private Setting<SettingColor> sideColor;

    @Shadow(remap = false)
    @Final
    private Setting<SettingColor> lineColor;

    @Shadow(remap = false)
    @Final
    private Setting<ShapeMode> shapeMode;

    @Shadow(remap = false)
    private int placeTimer;

    @Shadow(remap = false)
    @Final
    private Setting<Integer> supportDelay;

    @Shadow(remap = false)
    protected abstract void placeCrystal(BlockHitResult result, double damage, BlockPos supportBlock);

    @Shadow(remap = false)
    @Final
    private Setting<Boolean> renderPlace;

    @Shadow(remap = false)
    @Final
    private Setting<Boolean> renderBreak;

    @Shadow(remap = false)
    private int breakRenderTimer;

    @Shadow(remap = false)
    @Final
    private BlockPos.Mutable breakRenderPos;

    @Shadow(remap = false)
    private Box renderBoxOne;

    @Shadow(remap = false)
    private Box renderBoxTwo;

    @Shadow(remap = false)
    @Final
    private Setting<Double> height;

    @Shadow(remap = false)
    @Final
    private Setting<Integer> smoothness;

    @Inject(method = "onRender", at = @At("HEAD"), remap = false, cancellable = true)
    private void onRender(Render3DEvent event, CallbackInfo ci) {
        ci.cancel();

        if (renderMode.get() == CrystalAura.RenderMode.None) return;

        switch (renderMode.get()) {
            case Normal -> {
                if (renderPlace.get()) {
                    event.renderer.box(placeRenderPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                }
                if (renderBreak.get()) {
                    event.renderer.box(breakRenderPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                }
            }

            case Smooth -> {
                if (placeRenderTimer <= 0) return;

                if (renderBoxOne == null) renderBoxOne = new Box(placeRenderPos);
                if (renderBoxTwo == null) renderBoxTwo = new Box(placeRenderPos);
                else ((IBox) renderBoxTwo).set(placeRenderPos);

                double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.get();
                double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.get();
                double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.get();

                ((IBox) renderBoxOne).set(
                    renderBoxOne.minX + offsetX,
                    renderBoxOne.minY + offsetY,
                    renderBoxOne.minZ + offsetZ,
                    renderBoxOne.maxX + offsetX,
                    renderBoxOne.maxY + offsetY,
                    renderBoxOne.maxZ + offsetZ
                );

                event.renderer.box(renderBoxOne, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }

            case Gradient -> {
                if (placeRenderTimer <= 0) return;

                Color bottom = new Color(0, 0, 0, 0);

                int x = placeRenderPos.getX();
                int y = placeRenderPos.getY() + 1;
                int z = placeRenderPos.getZ();

                if (shapeMode.get().sides()) {
                    event.renderer.quadHorizontal(x, y, z, x + 1, z + 1, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x + 1, y - height.get(), z, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x, y - height.get(), z + 1, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x + 1, y, z, x + 1, y - height.get(), z + 1, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z + 1, x + 1, y - height.get(), z + 1, bottom, sideColor.get());
                }

                if (shapeMode.get().lines()) {
                    event.renderer.line(x, y, z, x + 1, y, z, lineColor.get());
                    event.renderer.line(x, y, z, x, y, z + 1, lineColor.get());
                    event.renderer.line(x + 1, y, z, x + 1, y, z + 1, lineColor.get());
                    event.renderer.line(x, y, z + 1, x + 1, y, z + 1, lineColor.get());

                    event.renderer.line(x, y, z, x, y - height.get(), z, lineColor.get(), bottom);
                    event.renderer.line(x + 1, y, z, x + 1, y - height.get(), z, lineColor.get(), bottom);
                    event.renderer.line(x, y, z + 1, x, y - height.get(), z + 1, lineColor.get(), bottom);
                    event.renderer.line(x + 1, y, z + 1, x + 1, y - height.get(), z + 1, lineColor.get(), bottom);
                }
            }
        }
    }

    @Inject(method = "placeCrystal", at = @At("HEAD"), cancellable = true)
    private void onPlaceCrystal(BlockHitResult result, double damage, BlockPos supportBlock, CallbackInfo ci) {
        ci.cancel();

        // Switch
        Item targetItem = supportBlock == null ? Items.END_CRYSTAL : Items.OBSIDIAN;

        FindItemResult item = InvUtils.findInHotbar(targetItem);
        if (!item.found()) return;

        int prevSlot = mc.player.getInventory().selectedSlot;

        if (autoSwitch.get() != CrystalAura.AutoSwitchMode.None && !item.isOffhand()) InvUtils.swap(item.slot(), false);

        Hand hand = item.getHand();
        if (hand == null) return;

        if (!(RaycastUtils.raycast(mc.player.getYaw(), mc.player.getPitch(), mc.interactionManager.getReachDistance(), true) instanceof BlockHitResult bresult) || !bresult.getBlockPos().equals(result.getBlockPos()))
            return;

        // Place
        if (supportBlock == null) {
            // Place crystal
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));

            if (swingMode.get().client()) mc.player.swingHand(hand);
            if (swingMode.get().packet()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

            placing = true;
            placingTimer = 4;
            kaTimer = 8;
            placingCrystalBlockPos.set(result.getBlockPos()).move(0, 1, 0);

            placeRenderPos.set(result.getBlockPos());
            renderDamage = damage;

            if (renderMode.get() == CrystalAura.RenderMode.Normal) {
                placeRenderTimer = placeRenderTime.get();
            } else {
                placeRenderTimer = renderTime.get();
                if (renderMode.get() == CrystalAura.RenderMode.Fading) {
                    RenderUtils.renderTickingBlock(
                        placeRenderPos, sideColor.get(),
                        lineColor.get(), shapeMode.get(),
                        0, renderTime.get(), true,
                        false
                    );
                }
            }
        } else {
            // Place support block
            BlockUtils.place(supportBlock, item, false, 0, swingMode.get().client(), true, false);
            placeTimer += supportDelay.get();

            if (supportDelay.get() == 0) placeCrystal(result, damage, null);
        }

        // Switch back
        if (autoSwitch.get() == CrystalAura.AutoSwitchMode.Silent) InvUtils.swap(prevSlot, false);
    }

    @Inject(method = "attackCrystal", at = @At("HEAD"), cancellable = true)
    private void onAttackCrystal(Entity entity, CallbackInfo ci) {
        ci.cancel();

        if (RaycastUtils.raycast(mc.player.getYaw(), mc.player.getPitch(), mc.interactionManager.getReachDistance(), false) instanceof EntityHitResult result) {
            if (!result.getEntity().equals(entity)) return;

            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

            Hand hand = InvUtils.findInHotbar(Items.END_CRYSTAL).getHand();
            if (hand == null) hand = Hand.MAIN_HAND;

            if (swingMode.get().client()) mc.player.swingHand(hand);
            if (swingMode.get().packet()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

            attacks++;
        }
    }
}
