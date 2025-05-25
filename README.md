# MobGear
> A fabric mod which adds the ability for datapacks to control what equipment mobs spawn with using loot tables.

Loot tables matching the entity's resource location under `loot_table/mobgear/` folder will determine the type of equipment that entity spawns with. As an example `data/minecraft/loot_table/mobgear/zombie.json` will control what equipment the zombie will spawn with. For modded mobs the equivalent would be `data/modname/loot_table/mobgear/modded_mob.json` (this is not tested but it might work!).

## New Combined Group loot entry type
Currently vanilla loot tables have no proper way of selecting multiple specific items at once. Introducing `mobgear:combinedgroup`! CombinedGroup behaves similarly to regular Group but all of the items generated within the children are provided as potential equipment.

<details>
    <summary>Example Loot table using CombinedGroup to equip full iron armor</summary>

```json
{
    "type": "minecraft:equipment",
    "pools": [
        {
            "rolls": 1,
            "entries": [
                {
                    "type": "mobgear:combinedgroup",
                    "children": [
                        {
                            "type": "minecraft:item",
                            "name": "minecraft:iron_helmet"
                        },
                        {
                            "type": "minecraft:item",
                            "name": "minecraft:iron_chestplate"
                        },
                        {
                            "type": "minecraft:item",
                            "name": "minecraft:iron_leggings"
                        },
                        {
                            "type": "minecraft:item",
                            "name": "minecraft:iron_boots"
                        }
                    ]
                }
            ]
        }
    ]
}
```

</details>

## Equip order
There are only a limited number of slots in a mob which can be filled with items. Items are selected to their corresponding slots in first come first serve order and will attempt to equip the item to it's matching slot. This however can be bypassed using custom data component!

### Custom data controls
The equipped items can be controlled using the following custom data components:

#### `mobgear:priority`
Number value.
Controls item's equip priority. Highest number is equipped first.

#### `mobgear:slot`
Possible values:
 - "MAINHAND"
 - "OFFHAND"
 - "FEET"
 - "LEGS"
 - "CHEST"
 - "HEAD"
 - "BODY"
    - Refers to animal body slot. Used with horse armor and Lama carpets.
 - "SADDLE"

Determines which slot this item attempts to occupy. If the slot is already taken, the item will be discarded.
> You can put any item to any slot but they might not show up correctly!

<details>
    <summary>Example of putting a black banner on the head</summary>

```json
{
    "type": "minecraft:item",
    "name": "minecraft:black_banner",
    "functions": [
        {
            "function": "minecraft:set_custom_data",
            "tag": {
                "mobgear:slot": "HEAD"
            }
        }
    ]
}
```
</details>

<details>
    <summary>Example of dual wielding wooden axes</summary>

```json
"entries": [
    {
        "type": "minecraft:item",
        "name": "minecraft:wooden_axe",
        "functions": [
            {
                "function": "minecraft:set_custom_data",
                "tag": {
                    "mobgear:slot": "MAINHAND"
                }
            }
        ]
    },
    {
        "type": "minecraft:item",
        "name": "minecraft:wooden_axe",
        "functions": [
            {
                "function": "minecraft:set_custom_data",
                "tag": {
                    "mobgear:slot": "OFFHAND"
                }
            }
        ]
    }
]
```

</details>

#### `mobgear:dropchance`
Number value between 0-1.
Controls % chance of this item being dropped on death. 1 meaning 100%, 0.5 meaning 50% and 0 meaning 0% chance. This is useful for making equipment undroppable even with looting when the equipment was not made to be used by players.

<details>
    <summary>Example of making OP equipment not drop</summary>

```json
{
    "type": "minecraft:item",
    "name": "minecraft:netherite_chestplate",
    "functions": [
        {
            "function": "minecraft:set_enchantments",
            "enchantments": {
                "minecraft:protection": 10,
                "minecraft:projectile_protection": 10,
                "minecraft:blast_protection": 10
            }
        },
        {
            "function": "minecraft:set_custom_data",
            "tag": {
                "mobgear:dropchance": 0
            }
        }
    ]
}
```

</details>

<details>
    <summary>Example of having 30% chance to drop the equipped zombie head</summary>

```json
{
    "type": "minecraft:item",
    "name": "minecraft:zombie_head",
    "functions": [
        {
            "function": "minecraft:set_custom_data",
            "tag": {
                "mobgear:dropchance": 0.3,
                "mobgear:slot": "HEAD"
            }
        }
    ]
}
```

</details>

#### `mobgear:deathloottable`
String value to loot table resource path.
This changes the mob's DeathLootTable which determines which Loot table is used to generate the mob's dropped loot. This is the only custom data value that isn't specifically targetting the item but the mob itself.

<details>
<summary>Example of setting mob's death loot table to desert pyramid's archaeology loot</summary>


```json
{
  "type": "minecraft:equipment",
  "pools": [
    {
      "rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:egg",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "type": "minecraft:uniform",
                "min": 3,
                "max": 7
              }
            },
            {
              "function": "minecraft:set_custom_data",
              "tag": {
                "mobgear:deathloottable": "minecraft:archaeology/desert_pyramid"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

</details>

## Using vanilla equipment logic
Most hostile mobs have pre-existing logic for their equipped gear which can be a hassle to recreate using loot tables. I've implemented a simple solution for having an option to decide whether to use vanilla equipment or to use custom equipment.
> If loot table has no valid Loot Pools the vanilla behavior is used! This can be controlled using Loot Pool's conditions.

<details>
<summary>Example of only using custom equipment if mob spawns in a Stronghold.</summary>

> If the location_check condition is false then vanilla behavior is used as none of the available loot pools are valid.


```json
{
  "type": "minecraft:equipment",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        ...
      ],
      "conditions": [
        {
          "condition": "minecraft:location_check",
          "predicate": {
            "structures": "minecraft:stronghold"
          }
        }
      ]
    }
  ]
}
```

</details>

<details>
<summary>Example of mobs below y=0 use custom equipment</summary>

> If the location_check condition is false then vanilla behavior is used as none of the available loot pools are valid.


```json
{
  "type": "minecraft:equipment",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        ...
      ],
      "conditions": [
        {
          "condition": "minecraft:location_check",
          "predicate": {
            "position": {
              "y": {
                "max": 0
              }
            }
          }
        }
      ]
    }
  ]
}
```

</details>