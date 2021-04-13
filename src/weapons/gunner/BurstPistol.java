package weapons.gunner;

import java.util.ArrayList;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.ConditionalArrayList;
import utilities.MathUtils;
import weapons.Weapon;

public class BurstPistol extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int burstSize;
	private double delayBetweenBulletsDuringBurst;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public BurstPistol() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public BurstPistol(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public BurstPistol(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "BRT7 Burst Fire Gun";
		weaponPic = WeaponPictures.burstPistol;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 23;
		burstSize = 3;
		delayBetweenBulletsDuringBurst = 0.05;
		carriedAmmo = 168;
		magazineSize = 24;
		rateOfFire = 3.5;
		reloadTime = 2.2;
		armorBreaking = 0.5;
		
		initializeModsAndOverclocks();
		// Grab initial values before customizing mods and overclocks
		setBaselineStats();
		
		// Selected Mods
		selectedTier1 = mod1;
		selectedTier2 = mod2;
		selectedTier3 = mod3;
		selectedTier4 = mod4;
		selectedTier5 = mod5;
		
		// Overclock slot
		selectedOverclock = overclock;
	}
	
	@Override
	protected void initializeModsAndOverclocks() {
		tier1 = new Mod[3];
		tier1[0] = new Mod("High Velocity Rounds", "+3 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("Floating Barrel", "x0.6 Spread per Shot, -30% Base Spread", modIcons.baseSpread, 1, 1);
		tier1[2] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Recoil Dampener", "x0.5 Recoil", modIcons.recoil, 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "-0.7 Reload Time", modIcons.reloadSpeed, 2, 1);
		tier2[2] = new Mod("Disabled Safety", "+2 Rate of Fire (translates to less time between bursts)", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("High Capacity Magazine", "+12 Magazine Size", modIcons.magSize, 3, 0);
		tier3[1] = new Mod("Increased Caliber Rounds", "+3 Direct Damage", modIcons.directDamage, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hardened Rounds", "+200% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Expanded Ammo Bags", "+48 Max Ammo", modIcons.carriedAmmo, 4, 1);
		tier4[2] = new Mod("Hollow-Point Bullets", "+25% Weakpoint Bonus", modIcons.weakpointBonus, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Burst Stun", "Stun an enemy for 4 seconds if all 3 shots in a burst hit", modIcons.stun, 5, 0);
		tier5[1] = new Mod("Longer Burst", "+3 Bullets per Burst", modIcons.rateOfFire, 5, 1);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Casings", "+24 Max Ammo, +1 Rate of Fire", overclockIcons.rateOfFire, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Full Chamber Seal", "+3 Direct Damage, -0.3 Reload Time", overclockIcons.directDamage, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Compact Mags", "+72 Max Ammo, -1 Rate of Fire, +0.4 Reload Time", overclockIcons.carriedAmmo, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Experimental Rounds", "+9 Direct Damage, -6 Magazine Size, -24 Max Ammo", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electro Minelets", "Any bullets that impact terrain get converted to Electro Minelets. It takes 0.1 seconds to form the minelets, "
				+ "0.8 seconds to arm them, and they only last for 3 seconds after being armed. If an enemy passes within 1.5m of a minelet, it will detonate and inflict an Electrocute DoT to all enemies "
				+ "within range. The Electrocute DoTs deal an average of " + MathUtils.round(DoTInformation.Electro_TicksPerSec * 4, GuiConstants.numDecimalPlaces)
				+ " Electric Damage per Second for 3 seconds. In exchange, -6 Direct Damage and -9 Magazine Size.", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Micro Flechettes", "+30 Magazine Size, x2.25 Max Ammo, x0.5 Spread per Shot, x0.5 Recoil, x0.5 Damage per bullet", overclockIcons.miniShells, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Lead Spray", "x1.5 Direct Damage, x4 Base Spread", overclockIcons.special, 6);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public BurstPistol clone() {
		return new BurstPistol(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "BurstPistol";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.burstPistolGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 3;
		}
		else if (selectedOverclock == 3) {
			toReturn += 9;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.5;
		}
		else if (selectedOverclock == 6) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private int getBurstSize() {
		int toReturn = burstSize;
		
		if (selectedTier5 == 1) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier4 == 1) {
			toReturn += 48;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 24;
		}
		else if (selectedOverclock == 2) {
			toReturn += 72;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 24;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 2.25;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 0) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn -= 9;
		}
		else if (selectedOverclock == 5) {
			toReturn += 30;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 2) {
			toReturn += 2.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 1.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.3;
		}
		else if (selectedOverclock == 2) {
			toReturn += 0.4;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		double toReturn = armorBreaking;
		
		if (selectedTier4 == 0) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier1 == 2) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier1 == 1) {
			toReturn -= 0.3;
		}
		
		if (selectedOverclock == 6) {
			toReturn *= 4.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier1 == 1) {
			toReturn *= 0.6;
		}
		
		if (selectedOverclock == 5) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 0) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 5) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		if (selectedTier4 == 2) {
			return 0.25;
		}
		else {
			return 0;
		}
	}
	private int getBurstStunDuration() {
		if (selectedTier5 == 0) {
			return 4;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[13];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 1 || (selectedOverclock > 0 && selectedOverclock < 7 && selectedOverclock != 2);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		toReturn[1] = new StatsRow("Burst Size:", getBurstSize(), modIcons.rateOfFire, selectedTier5 == 1);
		
		boolean magSizeModified = selectedTier3 == 0 || (selectedOverclock > 2 && selectedOverclock < 6);
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFModified = selectedTier2 == 2 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, RoFModified);
		
		boolean reloadModified = selectedTier2 == 1 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, reloadModified);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 2, selectedTier4 == 2);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 0);
		
		toReturn[8] = new StatsRow("Stun Duration:", getBurstStunDuration(), modIcons.stun, selectedTier5 == 0, selectedTier5 == 0);
		
		toReturn[9] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier1 == 2, selectedTier1 == 2);
		
		boolean baseSpreadModified = selectedTier1 == 1 || selectedOverclock == 6;
		toReturn[10] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		boolean spreadPerShotModified = selectedTier1 == 1 || selectedOverclock == 5;
		toReturn[11] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, spreadPerShotModified, spreadPerShotModified);
		
		boolean recoilModified = selectedTier2 == 0 || selectedOverclock == 5;
		toReturn[12] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// This weapon can't deal splash damage
		return false;
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = timeToFireMagazine();
		}
		else {
			duration = timeToFireMagazine() + getReloadTime();
		}
		
		double dmg = getDirectDamage();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			dmg *= armorWaste;
		}
		
		// Frozen
		if (statusEffects[1]) {
			dmg *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			dmg *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(dmg, getWeakpointBonus(), 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = dmg;
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 4) {
			if (burst) {
				// Because the Electro Minelets don't arm for 0.9 seconds, the Burst DPS needs to be reduced by an uptime coefficient
				// Additionally, they only do 2 dmg per tick for 2 secs
				double electroMinesUptimeCoefficient = (duration - 0.9) / duration;
				electroDPS = electroMinesUptimeCoefficient * DoTInformation.Electro_TicksPerSec * 4;
			}
			else {
				electroDPS = DoTInformation.Electro_TicksPerSec * 4;
			}
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * dmg) / duration + electroDPS;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double electroDPS = 0;
		if (selectedOverclock == 4) {
			// OC "Electro Minelets" only does 2 dmg/tick for 2 secs
			electroDPS = DoTInformation.Electro_TicksPerSec * 4;
		}
		
		double blowthroughDPS = 0;
		if (selectedTier1 == 2) {
			blowthroughDPS = calculateSingleTargetDPS(false, false, false, false);
		}
		
		return blowthroughDPS + electroDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamage = getDirectDamage() * (getMagazineSize() + getCarriedAmmo());
		
		if (selectedTier1 == 2) {
			totalDamage *= (1 + getMaxPenetrations());
		}
		
		if (selectedOverclock == 4) {
			double accuracy = getGeneralAccuracy() / 100.0;
			int numBulletsThatMiss = (int) Math.ceil((1 - accuracy) * (getCarriedAmmo() + getMagazineSize()));
			// OC "Electro Minelets" only does 2 dmg/tick for 2 secs
			totalDamage += numBulletsThatMiss * DoTInformation.Electro_TicksPerSec * 4 * 3;
		}
		
		return totalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedOverclock == 4) {
			return calculateNumGlyphidsInRadius(1.5);
		}
		else {
			return 1 + getMaxPenetrations();
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine() + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerBurst = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) * getBurstSize();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerBurst) * dmgPerBurst;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() * getBurstSize());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double baseSpread = 2.25 * getBaseSpread();
		double spreadPerShot = 1.2 * getSpreadPerShot();
		double spreadRecoverySpeed = 5.0;
		double maxBloom = 4.0;
		double minSpreadWhileMoving = 1.0;
		
		double recoilPitch = 30.0 * getRecoil();
		double recoilYaw = 10.0 * getRecoil();
		double mass = 1.0;
		double springStiffness = 70.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getRateOfFire(), getMagazineSize(), getBurstSize(), 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double[] areaDamage = new double[5];
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		if (selectedOverclock == 4) {
			// OC "Electro Minelets" only does 2 dmg/tick for 2 secs
			dot_dps[0] = DoTInformation.Electro_TicksPerSec * 4;
			dot_duration[0] = 3;
			dot_probability[0] = 1.0;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electro Minelets" = 100% Electrocute Chance, but only on bullets that miss... maybe (1.0 - Accuracy)?
		if (selectedOverclock == 4) {
			// Electro Minelets arm in 0.9 seconds, detonate on any enemies that come within ~1.5m, and then explode after 4 seconds. 100% chance to apply Electrocute for 2 sec.
			double probabilityBulletsMiss = 1.0 - getGeneralAccuracy() / 100.0;
			int numGlyphidsInMineletRadius = calculateNumGlyphidsInRadius(1.5);
			utilityScores[3] = probabilityBulletsMiss * numGlyphidsInMineletRadius * 3 * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Mod Tier 5 "Burst Stun" = 100% chance for 4 sec stun
		if (selectedTier5 == 0) {
			utilityScores[5] = getGeneralAccuracy() / 100.0 * getBurstStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getDirectDamage() * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		double timeToFireBurst = (getBurstSize() - 1) * delayBetweenBulletsDuringBurst;
		double delayBetweenBursts = 1.0 / getRateOfFire();
		int numBurstsPerMagazine = getMagazineSize() / getBurstSize();
		
		return numBurstsPerMagazine * timeToFireBurst + (numBurstsPerMagazine - 1) * delayBetweenBursts;
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, 0.0, getArmorBreaking(), getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 20, 0, 0, tier1[1].getText(true), "{ \"ex10\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true }, "
				+ "\"ex3\": { \"name\": \"Spread Per Shot\", \"value\": 0.6, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1000, 0, 0, 20, 0, 0, 0, tier1[2].getText(true), "{ \"ex11\": { \"name\": \"Max Penetrations\", \"value\": 1 } }", "Icon_Upgrade_BulletPenetration", "Blow Through"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[0].getText(true), "{ \"ex4\": { \"name\": \"Recoil\", \"value\": 0.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Recoil", "Recoil"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 0, 12, 0, 18, tier2[1].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.7, \"subtract\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 12, 0, 0, 0, tier2[2].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 3 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 12 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 30, 0, 0, 0, 20, 0, tier3[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 25, 36, 0, 0, 0, 15, tier4[0].getText(true), "{ \"ex5\": { \"name\": \"Armor Breaking\", \"value\": 200, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 0, 36, 25, 15, 0, tier4[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 72 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 3800, 15, 0, 0, 0, 36, 25, tier4[2].getText(true), "{ \"ex6\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 40, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 110, 40, 0, 60, 0, 0, tier5[0].getText(true), "{ \"ex8\": { \"name\": \"Burst Stun Duration\", \"value\": 4 } }", "Icon_Upgrade_Stun", "Stun"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 0, 60, 0, 0, 110, 40, tier5[1].getText(true), "{ \"ex1\": { \"name\": \"Burst Size\", \"value\": 3 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7950, 100, 0, 0, 140, 75, 0, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 36 }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 1 } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7850, 110, 120, 0, 0, 0, 75, overclocks[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7350, 135, 0, 105, 0, 0, 75, overclocks[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 84 }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 1, \"subtract\": true }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.4 } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8550, 130, 0, 0, 0, 100, 75, overclocks[3].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 9 }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 36, \"subtract\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 6, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7450, 0, 0, 120, 0, 80, 95, overclocks[4].getText(true), "{ \"ex9\": { \"name\": \"Electro Minelets\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 3, \"subtract\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 6, \"subtract\": true } }", "Icon_Upgrade_Electricity"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 7650, 130, 80, 0, 0, 0, 100, overclocks[5].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 2, \"multiply\": true }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 30 }, \"ex4\": { \"name\": \"Recoil\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, \"ex3\": { \"name\": \"Spread Per Shot\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 0.5, \"multiply\": true } }", "Icon_Overclock_SmallBullets"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[6].getShortcutRepresentation(), overclocks[6].getName(), 7650, 75, 125, 105, 0, 0, 0, overclocks[6].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1.5, \"multiply\": true }, "
				+ "\"ex10\": { \"name\": \"Base Spread\", \"value\": 4, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Special"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
