package lare.mobgear.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootWorldContext;

import java.util.function.Consumer;

public interface LootTableAdditions {

    boolean mobGear$generateUnprocessedLootWithTest(LootContext context, Consumer<ItemStack> lootConsumer);

    LootContext mobGear$buildLootContext(LootWorldContext parameters);
}
