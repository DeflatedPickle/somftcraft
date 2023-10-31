/* Copyright (c) 2023 DeflatedPickle under the GPLv3 license */

package com.deflatedpickle.somftcraft.mixin.enchantment;

import com.deflatedpickle.somftcraft.api.SlotTypesGetter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnusedMixin")
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements SlotTypesGetter {
  @Shadow @Final public EquipmentSlot[] slotTypes;

  @NotNull
  @Override
  public EquipmentSlot @NotNull [] somft$getSlotTypes() {
    return this.slotTypes;
  }
}
