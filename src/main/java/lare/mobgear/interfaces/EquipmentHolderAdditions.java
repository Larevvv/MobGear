package lare.mobgear.interfaces;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;

public interface EquipmentHolderAdditions {

    void mobGear$clearEquipment();

    void mobGear$setEquipmentFromTableWithLootPoolCheck(LootTable lootTable, LootWorldContext parameters);
}
