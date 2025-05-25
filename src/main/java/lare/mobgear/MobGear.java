package lare.mobgear;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.Entity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MobGear implements ModInitializer {
	public static final String MOD_ID = "mobgear";

	public static final Identifier CombinedGroupLootPool = Identifier.of(MOD_ID, "combinedgroup");

	public static final Identifier SpawnReasonEntityData = Identifier.of(MOD_ID, "spawnreason");
	public static final Identifier DifficultyEntityData = Identifier.of(MOD_ID, "difficulty");

	public static final Identifier DropchanceItemData = Identifier.of(MOD_ID, "dropchance");
	public static final Identifier SlotItemData = Identifier.of(MOD_ID, "slot");
	public static final Identifier PriorityItemData = Identifier.of(MOD_ID, "priority");
	public static final Identifier DeathLootTableItemData = Identifier.of(MOD_ID, "deathloottable");

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final LootPoolEntryType COMBINEDGROUP;

	@Override
	public void onInitialize() {}

	public static LootTable getGearTable(Entity entity) {
		Identifier mobType = Registries.ENTITY_TYPE.getId(entity.getType());
		RegistryKey<LootTable> gearTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(mobType.getNamespace(),"mobgear/"+mobType.getPath()));

		World world = entity.getEntityWorld();
		MinecraftServer server = world.getServer();
		if (server != null) {
			return server.getReloadableRegistries().getLootTable(gearTable);
		}
		return LootTable.EMPTY;
	}

	static {
		COMBINEDGROUP = Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, CombinedGroupLootPool, new LootPoolEntryType(CombinedGroupEntry.CODEC));
	}
}