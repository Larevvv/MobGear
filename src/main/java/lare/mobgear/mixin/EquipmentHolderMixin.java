package lare.mobgear.mixin;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lare.mobgear.interfaces.EquipmentHolderAdditions;
import lare.mobgear.interfaces.LootTableAdditions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lare.mobgear.MobGear.*;
import static net.minecraft.loot.LootTable.processStacks;

@Mixin(EquipmentHolder.class)
public interface EquipmentHolderMixin extends EquipmentHolderAdditions {

    @Shadow void equipStack(EquipmentSlot slot, ItemStack stack);
    @Shadow void setEquipmentDropChance(EquipmentSlot slot, float dropChance);
    @Shadow EquipmentSlot getSlotForStack(ItemStack stack, List<EquipmentSlot> slotBlacklist);

    @Unique
    void mobGear$clearEquipment();

    @Unique
    void mobGear$setDeathLootTable(String DeathLootTableKey);

    @Unique
    default void mobGear$setEquipmentFromTableWithLootPoolCheck(LootTable lootTable, LootWorldContext parameters) {
        if (lootTable != LootTable.EMPTY) {

            LootContext context = ((LootTableAdditions)(Object)lootTable).mobGear$buildLootContext(parameters);
            List<ItemStack> list = new ObjectArrayList<>();
            Objects.requireNonNull(list);
            boolean hasPools = ((LootTableAdditions) lootTable).mobGear$generateUnprocessedLootWithTest(context, processStacks(context.getWorld(), list::add));

            if (hasPools) {

                this.mobGear$clearEquipment();

                // Process custom data first since there may be mobgear values
                // If mobgear:priority value is present, use is to determine sort order.
                list.sort((a, b) -> {
                    NbtComponent customDataA = a.getComponents().get(DataComponentTypes.CUSTOM_DATA);
                    NbtComponent customDataB = b.getComponents().get(DataComponentTypes.CUSTOM_DATA);

                    if (customDataA != null && customDataB != null) {
                        var priorityA = customDataA.copyNbt().getInt(PriorityItemData.toString());
                        var priorityB = customDataB.copyNbt().getInt(PriorityItemData.toString());

                        if (priorityA.isPresent() && priorityB.isPresent()) return priorityB.get() - priorityA.get();
                        else if (priorityA.isPresent()) return priorityA.get();
                        else if (priorityB.isPresent()) return priorityB.get();
                    }
                    else if (customDataA != null) return -1;
                    else if (customDataB != null) return 1;
                    return 0;
                });

                // Keep track of equipped slots
                List<EquipmentSlot> list2 = new ArrayList<>();

                for(ItemStack itemStack : list) {
                    EquipmentSlot equipmentSlot = this.getSlotForStack(itemStack, list2);
                    Float dropchance = null;

                    NbtComponent customData = itemStack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
                    if (customData != null) {

                        NbtCompound copy = customData.copyNbt();

                        copy.remove(PriorityItemData.toString());

                        NbtElement deathLootData = copy.get(DeathLootTableItemData.toString());
                        if (deathLootData != null) {
                            deathLootData.asString().ifPresent(this::mobGear$setDeathLootTable);

                            // Remove death loot NBT from custom data
                            copy.remove(DeathLootTableItemData.toString());
                        }

                        NbtElement chanceData = copy.get(DropchanceItemData.toString());
                        if (chanceData != null) {
                            var chance = chanceData.asFloat();
                            if (chance.isPresent()) {
                                dropchance = chance.get();
                            }
                            // Remove gear drop chance NBT from custom data.
                            copy.remove(DropchanceItemData.toString());
                        }

                        NbtElement slotData = copy.get(SlotItemData.toString());
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
                            copy.remove(SlotItemData.toString());
                        }

                        // Update custom data (or remove if empty)
                        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, itemStack, copy);
                    }

                    if (equipmentSlot != null) {
                        ItemStack itemStack2 = equipmentSlot.split(itemStack);
                        this.equipStack(equipmentSlot, itemStack2);
                        if (dropchance != null) {
                            this.setEquipmentDropChance(equipmentSlot, dropchance);
                        }

                        list2.add(equipmentSlot);
                    }
                }
            }
        }
    }
}
