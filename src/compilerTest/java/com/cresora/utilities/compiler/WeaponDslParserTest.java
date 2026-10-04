package com.cresora.utilities.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Round-trip tests for the ported weapon DSL front-end. */
class WeaponDslParserTest {
	private static List<Ast> parse(String source) {
		return new Parser(source, new Lexer(source).scanTokens()).parse();
	}

	@Test
	void parsesWeaponWithSkillAndActions() {
		String source = """
				weapon "Buff Test Saber" {
				    id: "buff_test_saber"
				    rarity: "4_star"
				    base_item: "minecraft:iron_sword"
				    damage_type: physical
				    role: caster
				    stats {
				        base_damage: 12.0
				        damage_per_level: 0.6
				        speed: 1.6
				        max_base_level: 60
				        max_skill_level: 10
				    }
				    skill "Buff" {
				        effect_id: "buff_test"
				        cooldown: 10s
				        duration: 5s
				        base_value: 10.0
				        value_per_level: 1.0
				        radius: 4.0
				        on_activate {
				            heal()
				            grant_shield(5.0, 10s)
				        }
				    }
				    translations {
				        en_us { name: "Buff Test Saber" }
				    }
				}
				""";

		List<Ast> nodes = parse(source);
		assertEquals(1, nodes.size());

		Ast.WeaponDef weapon = assertInstanceOf(Ast.WeaponDef.class, nodes.get(0));
		assertEquals("buff_test_saber", weapon.id());
		assertEquals("4_star", weapon.rarity());
		assertEquals("caster", weapon.role());
		assertEquals(12.0, weapon.stats().baseAttackDamage());
		assertEquals(60, weapon.stats().maxBaseLevel());

		assertNotNull(weapon.skill());
		assertEquals(10.0, weapon.skill().cooldownSeconds());
		assertEquals(5.0, weapon.skill().durationSeconds());
		assertEquals(1, weapon.skill().handlers().size());

		Ast.SkillHandler handler = weapon.skill().handlers().get(0);
		assertEquals("on_activate", handler.eventName());
		assertEquals(2, handler.actions().size());

		Ast.InstructionCall heal = assertInstanceOf(Ast.InstructionCall.class, handler.actions().get(0));
		assertEquals("heal", heal.functionName());
		assertTrue(heal.arguments().isEmpty());

		Ast.InstructionCall shield = assertInstanceOf(Ast.InstructionCall.class, handler.actions().get(1));
		assertEquals("grant_shield", shield.functionName());
		assertEquals(List.of("5.0", "10s"), shield.arguments());
	}

	@Test
	void rejectsUnknownRole() {
		String source = """
				weapon "Bad" {
				    id: "bad"
				    rarity: "3_star"
				    base_item: "minecraft:iron_sword"
				    role: wizard
				    stats { base_damage: 1.0 }
				}
				""";
		ParseException error = assertThrows(ParseException.class, () -> parse(source));
		assertTrue(error.getMessage().contains("Unknown role 'wizard'"));
	}

	@Test
	void rejectsWeaponWithoutStats() {
		String source = """
				weapon "NoStats" {
				    id: "no_stats"
				    rarity: "3_star"
				    base_item: "minecraft:iron_sword"
				}
				""";
		ParseException error = assertThrows(ParseException.class, () -> parse(source));
		assertTrue(error.getMessage().contains("missing required stats"));
	}

	@Test
	void parsesDictionaryTranslations() {
		String source = """
				dictionary common_tags {
				    translations {
				        en_us {
				            "message.cresora.hotbar_overridden": "Skill loadout open!"
				        }
				        ja_jp {
				            "message.cresora.hotbar_overridden": "スキル解除！"
				        }
				    }
				}
				""";
		List<Ast> nodes = parse(source);
		Ast.DictionaryDef dictionary = assertInstanceOf(Ast.DictionaryDef.class, nodes.get(0));
		assertEquals("common_tags", dictionary.id());
		assertEquals("Skill loadout open!", dictionary.translations().get("en_us").get("message.cresora.hotbar_overridden"));
		assertEquals("スキル解除！", dictionary.translations().get("ja_jp").get("message.cresora.hotbar_overridden"));
	}
}
