/* Copyright (c) 2023 DeflatedPickle under the GPLv3 license */

@file:Suppress(
    "ClassName", "SpellCheckingInspection", "MemberVisibilityCanBePrivate", "HasPlatformType",
    "UNUSED_ANONYMOUS_PARAMETER"
)

package com.deflatedpickle.somftcraft

import com.deflatedpickle.somftcraft.block.dispenser.HorseArmorDispenserBehavior
import com.deflatedpickle.somftcraft.block.dispenser.TorchDispenserBehavior
import com.deflatedpickle.somftcraft.enchantment.DegradationCurseEnchantment
import com.deflatedpickle.somftcraft.enchantment.MalnutritionCurseEnchantment
import com.deflatedpickle.somftcraft.entity.data.IntArrayTrackedDataHandler
import com.deflatedpickle.somftcraft.item.EmptyInkSacItem
import com.deflatedpickle.somftcraft.item.HorseArmorItemExt
import com.deflatedpickle.somftcraft.item.LeashedArrow
import com.deflatedpickle.somftcraft.item.QuiverItem
import com.deflatedpickle.somftcraft.item.SpawnEggExt
import com.deflatedpickle.somftcraft.recipe.LeashedArrowRecipe
import com.deflatedpickle.somftcraft.screen.PetManagerScreenHandlerType
import com.deflatedpickle.somftcraft.server.command.HealthCommand
import com.deflatedpickle.somftcraft.server.command.HungerCommand
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.minecraft.block.dispenser.DispenserBlock
import net.minecraft.command.CommandBuildContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.Items
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTables
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.recipe.CraftingCategory
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.command.api.CommandRegistrationCallback

object SomftCraft : ModInitializer {
    val CHAINMAIL_HORSE_ARMOUR: Item = HorseArmorItemExt(6, "chainmail", Item.Settings().maxCount(1))
    val NETHERITE_HORSE_ARMOUR: Item = HorseArmorItemExt(12, "netherite", Item.Settings().maxCount(1))

    val LEASHED_ARROW_RECIPE_SERIALIZER = RecipeSerializer.register(
        "crafting_special_leashed_arrow",
        SpecialRecipeSerializer {
            identifier: Identifier,
            craftingCategory: CraftingCategory ->
            LeashedArrowRecipe(
                identifier,
                craftingCategory
            )
        }
    )

    val TAMED_WOLF_SPAWN_EGG = SpawnEggExt(EntityType.WOLF, 14144467, 13545366)
    val TAMED_FOX_SPAWN_EGG = SpawnEggExt(EntityType.FOX, 14005919, 13396256)
    val TAMED_PARROT_SPAWN_EGG = SpawnEggExt(EntityType.PARROT, 894731, 16711680)

    val DO_BLOCK_FIRE_GRIEF = GameRuleRegistry.register("doBlockFireGrief", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true))
    val DO_MOB_FIRE_GRIEF = GameRuleRegistry.register("doMobFireGrief", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true))

    val ARMOR_STAND_GUI_PACKET_ID = Identifier("somftcraft", "armor_stand_gui")

    override fun onInitialize(mod: ModContainer) {
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "empty_ink_sac"), EmptyInkSacItem)
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "chainmail_horse_armor"), CHAINMAIL_HORSE_ARMOUR)
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "netherite_horse_armor"), NETHERITE_HORSE_ARMOUR)
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "quiver"), QuiverItem)
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "leashed_arrow"), LeashedArrow)

        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "tamed_wolf_spawn_egg"), TAMED_WOLF_SPAWN_EGG)
        // TODO: add variant eggs?
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "tamed_fox_spawn_egg"), TAMED_FOX_SPAWN_EGG)
        Registry.register(Registries.ITEM, Identifier(mod.metadata().id(), "tamed_parrot_spawn_egg"), TAMED_PARROT_SPAWN_EGG)

        Registry.register(Registries.ENCHANTMENT, Identifier(mod.metadata().id(), "degradation_curse"), DegradationCurseEnchantment)
        Registry.register(Registries.ENCHANTMENT, Identifier(mod.metadata().id(), "malnutrition_curse"), MalnutritionCurseEnchantment)

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { entries ->
            entries.addItem(EmptyInkSacItem)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register { entries ->
            entries.addItem(CHAINMAIL_HORSE_ARMOUR)
            entries.addItem(NETHERITE_HORSE_ARMOUR)
            entries.addItem(QuiverItem)
            entries.addItem(LeashedArrow)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register { entries ->
            entries.addItem(TAMED_WOLF_SPAWN_EGG)
            entries.addItem(TAMED_FOX_SPAWN_EGG)
            entries.addItem(TAMED_PARROT_SPAWN_EGG)
        }

        LootTableEvents.MODIFY.register { resourceManager, lootManager, id, tableBuilder, source ->
            if (source.isBuiltin) {
                val poolBuilder: LootPool.Builder = LootPool.builder()

                when (id) {
                    LootTables.VILLAGE_TANNERY_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(QuiverItem).weight(2))
                    LootTables.PILLAGER_OUTPOST_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(QuiverItem))
                    LootTables.ABANDONED_MINESHAFT_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(QuiverItem).weight(5))
                    LootTables.SIMPLE_DUNGEON_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(QuiverItem).weight(12))
                    LootTables.NETHER_BRIDGE_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR))
                            .with(ItemEntry.builder(NETHERITE_HORSE_ARMOUR))
                            .with(ItemEntry.builder(QuiverItem).weight(7))
                    LootTables.STRONGHOLD_CORRIDOR_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR))
                            .with(ItemEntry.builder(NETHERITE_HORSE_ARMOUR))
                            .with(ItemEntry.builder(QuiverItem))
                    LootTables.ANCIENT_CITY_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR))
                            .with(ItemEntry.builder(NETHERITE_HORSE_ARMOUR))
                    LootTables.BASTION_BRIDGE_CHEST, LootTables.BASTION_OTHER_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(QuiverItem))
                    LootTables.DESERT_PYRAMID_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR).weight(12))
                            .with(ItemEntry.builder(QuiverItem).weight(20))
                    LootTables.END_CITY_TREASURE_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR))
                            .with(ItemEntry.builder(NETHERITE_HORSE_ARMOUR))
                    LootTables.JUNGLE_TEMPLE_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR))
                            .with(ItemEntry.builder(QuiverItem).weight(2))
                    LootTables.RUINED_PORTAL_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR).weight(2))
                    LootTables.WOODLAND_MANSION_CHEST ->
                        poolBuilder
                            .with(ItemEntry.builder(CHAINMAIL_HORSE_ARMOUR).weight(5))
                }

                tableBuilder.pool(poolBuilder)
            }
        }

        DispenserBlock.registerBehavior(CHAINMAIL_HORSE_ARMOUR, HorseArmorDispenserBehavior)
        DispenserBlock.registerBehavior(NETHERITE_HORSE_ARMOUR, HorseArmorDispenserBehavior)
        DispenserBlock.registerBehavior(Items.TORCH, TorchDispenserBehavior)

        Registry.register(Registries.SCREEN_HANDLER_TYPE, "pet_manager", PetManagerScreenHandlerType)

        TrackedDataHandlerRegistry.register(IntArrayTrackedDataHandler)

        CommandRegistrationCallback.EVENT.register {
            dispatcher: CommandDispatcher<ServerCommandSource>,
            buildContext: CommandBuildContext,
            environment: CommandManager.RegistrationEnvironment ->
            HealthCommand.register(dispatcher)
            HungerCommand.register(dispatcher)
        }
    }
}
