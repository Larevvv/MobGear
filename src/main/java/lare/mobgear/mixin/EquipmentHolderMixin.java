package lare.mobgear.mixin;


import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(EquipmentHolder.class)
public interface EquipmentHolderMixin {

    @Shadow void equipStack(EquipmentSlot slot, ItemStack stack);
    @Shadow void setEquipmentDropChance(EquipmentSlot slot, float dropChance);
    @Shadow  EquipmentSlot getSlotForStack(ItemStack stack, List<EquipmentSlot> slotBlacklist);

    @Inject(at = @At("HEAD"), method = "setEquipmentFromTable(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/loot/context/LootWorldContext;JLjava/util/Map;)V", cancellable = true)
    default void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootWorldContext parameters, long seed, Map<EquipmentSlot, Float> slotDropChances, CallbackInfo ci) {
        LootTable lootTable2 = parameters.getWorld().getServer().getReloadableRegistries().getLootTable(lootTable);
        if (lootTable2 != LootTable.EMPTY) {

            List<ItemStack> list = lootTable2.generateLoot(parameters, seed);
            List<EquipmentSlot> list2 = new ArrayList<>();

            for(ItemStack itemStack : list) {
                EquipmentSlot equipmentSlot = this.getSlotForStack(itemStack, list2);
                Float dropchance = null;

                var customData = itemStack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
                if (customData != null) {

                    var copy = customData.copyNbt();
                    NbtElement chanceData = copy.get("mobgear:dropchance");
                    if (chanceData != null) {
                        var chance = chanceData.asFloat();
                        if (chance.isPresent()) {
                            dropchance = chance.get();
                        }
                        // Remove gear dropchance NBT from custom data.
                        copy.remove("mobgear:dropchance");
                    }

                    NbtElement slotData = copy.get("mobgear:slot");
                    if (slotData != null) {
                        var slot = slotData.asString();
                        if (slot.isPresent()) {
                            equipmentSlot = switch (slot.get().toUpperCase()) {
                                case "MAINHAND" -> EquipmentSlot.MAINHAND;
                                case "OFFHAND" -> EquipmentSlot.OFFHAND;
                                case "FEET" -> EquipmentSlot.FEET;
                                case "LEGS" -> EquipmentSlot.LEGS;
                                case "CHEST" -> EquipmentSlot.CHEST;
                                case "HEAD" -> EquipmentSlot.HEAD;
                                case "BODY" -> EquipmentSlot.BODY;
                                case "SADDLE" -> EquipmentSlot.SADDLE;
                                default -> equipmentSlot;
                            };
                        }
                        // Remove gear slot NBT from custom data.
                        copy.remove("mobgear:slot");
                    }

                    // Update custom data (or remove if empty)
                    NbtComponent.set(DataComponentTypes.CUSTOM_DATA, itemStack, copy);
                }

                if (equipmentSlot != null) {
                    ItemStack itemStack2 = equipmentSlot.split(itemStack);
                    this.equipStack(equipmentSlot, itemStack2);
                    Float float_ = dropchance != null ? dropchance : (Float)slotDropChances.get(equipmentSlot);
                    if (float_ != null) {
                        this.setEquipmentDropChance(equipmentSlot, float_);
                    }

                    list2.add(equipmentSlot);
                }
            }
        }
        ci.cancel();
    }
}
