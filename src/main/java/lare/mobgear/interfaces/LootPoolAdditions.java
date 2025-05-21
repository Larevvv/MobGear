package lare.mobgear.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

import java.util.function.Consumer;

public interface LootPoolAdditions {

    boolean mobGear$test(LootContext context);

    void mobGear$addGeneratedLootNoTest(Consumer<ItemStack> lootConsumer, LootContext context);
}
