package net.jobsaddon.mixin.misc;

import java.util.Iterator;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @Nullable
    @Unique
    private ItemStack oldItemStack = null;

    @Inject(method = "onButtonClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onButtonClickMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info, ItemStack itemStack) {
        oldItemStack = itemStack.copy();
    }

    @Inject(method = "onButtonClick", at = @At(value = "RETURN", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onButtonClickMixin(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> info, ItemStack itemStack, ItemStack itemStack2) {
        if (!player.world.isClient && info.getReturnValue() && ((JobsManagerAccess) player).getJobsManager().isEmployedJob("brewer")) {//

            NbtList list = itemStack.getEnchantments();
            list.removeAll(this.oldItemStack.getEnchantments());

            Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.fromNbt(list);
            Iterator<Enchantment> iterator = enchantmentMap.keySet().iterator();

            int xpCount = 0;
            while (iterator.hasNext()) {
                Enchantment enchantment = iterator.next();
                if (JobLists.brewerEnchantmentMap.get(enchantment) != null)
                    xpCount += JobLists.brewerEnchantmentMap.get(enchantment) * enchantmentMap.get(enchantment);
                else
                    xpCount += ConfigInit.CONFIG.brewerXP * enchantmentMap.get(enchantment);
            }

            JobsServerPacket.writeS2CJobXPPacket((ServerPlayerEntity) player, "brewer", xpCount);
        }
    }
}
