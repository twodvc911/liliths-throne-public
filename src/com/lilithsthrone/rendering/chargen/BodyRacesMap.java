package com.lilithsthrone.rendering.chargen;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.utils.Colour;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author twovgSP
 */
public class BodyRacesMap {

	private final Map<String, String> body_races = new HashMap<>();

	public boolean containsKey(String bodypart_type) {
		return body_races.containsKey(bodypart_type);
	}

	public String get(String bodypart_type) {
		return body_races.get(bodypart_type);
	}

	public String getOrDefault(String bodypart_type, String default_val) {
		return body_races.getOrDefault(bodypart_type, default_val);
	}

	public Set<Entry<String,String>> entrySet() {
		return body_races.entrySet();
	}

	private void add_race_for_bodypart(String bodypart_type, Race bodypart_race) {
		add_race_for_bodypart(bodypart_type, bodypart_race, false);
	}

	private void add_race_for_bodypart(String bodypart_type, Race bodypart_race, boolean use_fallback) {
		if (bodypart_race != null && bodypart_race != Race.NONE) {
			body_races.put(bodypart_type, bodypart_race.name().toLowerCase());
		} else if (use_fallback) {
			body_races.put(bodypart_type, "fallback");
		}
	}

	private void add_race_for_clothes(String clothes_bodypart_type, String bodypart_type, InventorySlot clothing_slot, GameCharacter character) {
		if (clothing_slot != null && character.getClothingInSlot(clothing_slot) != null && body_races.containsKey(bodypart_type)) {
			body_races.put(clothes_bodypart_type, body_races.get(bodypart_type));
		}
	}

	public static BodyRacesMap fromCharacter(GameCharacter character) {
		BodyRacesMap real_parts_races = new BodyRacesMap();

		Race body_race = character.getBody().getRace();
		Race head_race = character.getFaceRace();
		Race arm_race = character.getArmRace();
		Race leg_race = character.getLegRace();
		
		Race penis_race = character.getPenisRace();
		Race vagina_race = character.getVaginaRace();
		Race breasts_race = character.getBreastRace();
		Race breast_crotch_race = character.getBreastCrotchRace();
		
		Race hair_race = character.getHairRace();
		Race ear_race = character.getEarRace();
		Race eye_race = character.getEyeRace();
		Race wing_race = character.getWingRace();
		Race horn_race = character.getHornRace();
		Race antenna_race = character.getAntennaRace();
		
		Race ass_race = character.getAssRace();
		Race tongue_race = character.getTongueRace();
		Race tail_race = character.getTailRace();

		Race cloaca_race = character.getGenitalArrangement().toString().equals("CLOACA") ? (vagina_race != null && vagina_race != Race.NONE ? vagina_race : penis_race) : null;

		real_parts_races.add_race_for_bodypart("body", body_race, true);
		real_parts_races.add_race_for_bodypart("belly", body_race, true);
		if (character.isTaur()) real_parts_races.add_race_for_bodypart("body_taur", body_race, true);

		real_parts_races.add_race_for_bodypart("head", head_race, true);
		real_parts_races.add_race_for_bodypart("snout", head_race, true);
		real_parts_races.add_race_for_bodypart("lips", head_race, true);

		real_parts_races.add_race_for_bodypart("arm", arm_race, true);
		real_parts_races.add_race_for_bodypart("hand", arm_race, true);
		real_parts_races.add_race_for_bodypart("leg", leg_race, true);
		real_parts_races.add_race_for_bodypart("foot", leg_race, true);

		if (character.getBlusher() != null && character.getBlusher().getPrimaryColour()!=Colour.COVERING_NONE) {
			real_parts_races.add_race_for_bodypart("blusher", head_race, true);
		}
		if (character.getEyeShadow() != null && character.getEyeShadow().getPrimaryColour()!=Colour.COVERING_NONE) {
			real_parts_races.add_race_for_bodypart("eye_shadow", head_race, true);
		}
		if (character.getEyeLiner() != null && character.getEyeLiner().getPrimaryColour()!=Colour.COVERING_NONE) {
			real_parts_races.add_race_for_bodypart("eye_liner", head_race, true);
		}

		real_parts_races.add_race_for_bodypart("penis", penis_race);
		real_parts_races.add_race_for_bodypart("balls", penis_race);
		real_parts_races.add_race_for_bodypart("vagina", vagina_race);
		real_parts_races.add_race_for_bodypart("breasts", breasts_race);
		real_parts_races.add_race_for_bodypart("nipple", breasts_race);
		real_parts_races.add_race_for_bodypart("breasts-crotch", breast_crotch_race);
		real_parts_races.add_race_for_bodypart("breasts-crotch-nipple", breast_crotch_race);
		real_parts_races.add_race_for_bodypart("hair", hair_race);
		real_parts_races.add_race_for_bodypart("ear", ear_race);
		real_parts_races.add_race_for_bodypart("eye", eye_race);
		real_parts_races.add_race_for_bodypart("eye_sclera", eye_race);
		real_parts_races.add_race_for_bodypart("eye_pupil", eye_race);
		real_parts_races.add_race_for_bodypart("eye_iris", eye_race);
		real_parts_races.add_race_for_bodypart("wing", wing_race);
		real_parts_races.add_race_for_bodypart("horn", horn_race);
		real_parts_races.add_race_for_bodypart("antenna", antenna_race);
		real_parts_races.add_race_for_bodypart("ass", ass_race);
		real_parts_races.add_race_for_bodypart("cloaca", cloaca_race);
		real_parts_races.add_race_for_bodypart("tongue", tongue_race);
		real_parts_races.add_race_for_bodypart("tail", tail_race);

		real_parts_races.add_race_for_clothes("clothes_torso_under", "body", InventorySlot.TORSO_UNDER, character);
		real_parts_races.add_race_for_clothes("clothes_torso_over", "body", InventorySlot.TORSO_OVER, character);
		real_parts_races.add_race_for_clothes("clothes_groin", "body", InventorySlot.GROIN, character);
		real_parts_races.add_race_for_clothes("clothes_penis", "penis", InventorySlot.PENIS, character);
		real_parts_races.add_race_for_clothes("clothes_leg", "leg", InventorySlot.LEG, character);
		real_parts_races.add_race_for_clothes("clothes_foot", "leg", InventorySlot.FOOT, character);
		real_parts_races.add_race_for_clothes("clothes_head", "head", InventorySlot.HEAD, character);
		real_parts_races.add_race_for_clothes("clothes_neck", "head", InventorySlot.NECK, character);
		real_parts_races.add_race_for_clothes("clothes_finger", "hand", InventorySlot.FINGER, character);

		return real_parts_races;
	}

	@Override
	public String toString() {
		return body_races.toString();
	}
}
