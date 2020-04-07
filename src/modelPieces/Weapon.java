package modelPieces;

import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JPanel;

import guiPieces.AoEVisualizer;
import utilities.MathUtils;
import utilities.Point2D;

public abstract class Weapon extends Observable {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	protected String fullName;
	// Since several of the weapons have a Homebrew Powder mod or OC, I'm adding this coefficient in the parent class so that they can all be updated simultaneously.
	// This number was calculated by adding up all numbers in the range [80, 140] and then dividing that sum by 60 to get the average value.
	protected double homebrewPowderCoefficient = 1.11833;
	
	// If any of these shorts is set to -1, that means there should be no mods equipped at that tier.
	protected Mod[] tier1;
	protected int selectedTier1;
	protected Mod[] tier2;
	protected int selectedTier2;
	protected Mod[] tier3;
	protected int selectedTier3;
	protected Mod[] tier4;
	protected int selectedTier4;
	protected Mod[] tier5;
	protected int selectedTier5;
	
	protected Overclock[] overclocks;
	protected int selectedOverclock;
	
	protected double weakpointAccuracy;
	protected double generalAccuracy;
	protected double[] aoeEfficiency;
	// Mobility, Damage Resist, Armor Break, Slow, Fear, Stun, Freeze
	// Set them all to zero to start, then override values in child objects as necessary.
	protected double[] utilityScores = {0,0,0,0,0,0,0};
	
	protected double[] baselineCalculatedStats;
	private AoEVisualizer illustration = null;
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	public int getSelectedModAtTier(int tierNumber) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					return selectedTier1;
				}
				case 2: {
					return selectedTier2;
				}
				case 3: {
					return selectedTier3;
				}
				case 4: {
					return selectedTier4;
				}
				case 5: {
					return selectedTier5;
				}
				default: {
					return -2;
				}
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
			return -2;
		}
	}
	public void setSelectedModAtTier(int tierNumber, int newSelection) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					if (newSelection > -2 && newSelection < tier1.length) {
						if (newSelection == selectedTier1) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier1 = -1;
						}
						else {
							selectedTier1 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 2: {
					if (newSelection > -2 && newSelection < tier2.length) {
						if (newSelection == selectedTier2) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier2 = -1;
						}
						else {
							selectedTier2 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 3: {
					if (newSelection > -2 && newSelection < tier3.length) {
						if (newSelection == selectedTier3) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier3 = -1;
						}
						else {
							selectedTier3 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 4: {
					if (newSelection > -2 && newSelection < tier4.length) {
						if (newSelection == selectedTier4) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier4 = -1;
						}
						else {
							selectedTier4 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 5: {
					if (newSelection > -2 && newSelection < tier5.length) {
						if (newSelection == selectedTier5) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier5 = -1;
						}
						else {
							selectedTier5 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
			}
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
		}
	}
	
	public int getSelectedOverclock() {
		return selectedOverclock;
	}
	public void setSelectedOverclock(int newSelection) {
		if (newSelection > -2 && newSelection < overclocks.length) {
			if (newSelection == selectedOverclock) {
				// If the same overclock is selected, that indicates that it's being unequipped. Set overclock = -1 to affect the math properly.
				selectedOverclock = -1;
			}
			else {
				selectedOverclock = newSelection;
			}
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Overclock choice is outside array bounds");
		}
	}
	
	public abstract void buildFromCombination(String combination); 
	
	public Mod[] getModsAtTier(int tierNumber) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					return tier1;
				}
				case 2: {
					return tier2;
				}
				case 3: {
					return tier3;
				}
				case 4: {
					return tier4;
				}
				case 5: {
					return tier5;
				}
				default: {
					return null;
				}
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
			return null;
		}
	}
	public Overclock[] getOverclocks() {
		return overclocks;
	}
	
	protected abstract void initializeModsAndOverclocks();
	
	protected void setBaselineStats() {
		int oldT1 = selectedTier1, oldT2 = selectedTier2, oldT3 = selectedTier3, oldT4 = selectedTier4, oldT5 = selectedTier5, oldOC = selectedOverclock;
		selectedTier1 = selectedTier2 = selectedTier3 = selectedTier4 = selectedTier5 = selectedOverclock = -1;
		
		if (currentlyDealsSplashDamage()) {
			setAoEEfficiency();
		}
		
		baselineCalculatedStats = new double[] {
			calculateIdealBurstDPS(),
			calculateIdealSustainedDPS(),
			sustainedWeakpointDPS(),
			sustainedWeakpointAccuracyDPS(),
			calculateAdditionalTargetDPS(),
			calculateMaxMultiTargetDamage(),
			(double) calculateMaxNumTargets(),
			calculateFiringDuration(),
			averageTimeToKill(),
			averageOverkill(),
			estimatedAccuracy(false),
			utilityScore()
		};
		selectedTier1 = oldT1;
		selectedTier2 = oldT2;
		selectedTier3 = oldT3;
		selectedTier4 = oldT4;
		selectedTier5 = oldT5;
		selectedOverclock = oldOC;
	}
	public double[] getBaselineStats() {
		return baselineCalculatedStats;
	}
	
	protected void setAoEEfficiency() {
		// This is a placeholder method that only gets overwritten by weapons that deal splash damage (EPC_ChargedShot, GrenadeLauncher, and Autocannon)
		// It just exists here so that Weapon can reference the method when it changes mods or OCs
		aoeEfficiency = new double[3];
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// Used by GUI and Auto-Calculator
	public abstract String getDwarfClass();
	public abstract String getSimpleName();
	
	// Stats page
	public String getFullName() {
		return fullName;
	}
	public String getCombination() {
		String toReturn = "";
		if (selectedTier1 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier1[selectedTier1].getLetterRepresentation();
		}
		if (selectedTier2 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier2[selectedTier2].getLetterRepresentation();
		}
		if (selectedTier3 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier3[selectedTier3].getLetterRepresentation();
		}
		if (selectedTier4 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier4[selectedTier4].getLetterRepresentation();
		}
		if (selectedTier5 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier5[selectedTier5].getLetterRepresentation();
		}
		if (selectedOverclock < 0) {
			toReturn += "-";
		}
		else {
			toReturn += overclocks[selectedOverclock].getShortcutRepresentation();
		}
		return toReturn;
	}
	protected String convertDoubleToPercentage(double input) {
		int percent = (int) Math.round(input * 100.0);
		return percent + "%";
	}
	
	/*
		getStats() is the method used to interface between the Weapon and the left column of stats in WeaponTab. In general, the stats should be listed in this order:
		
		1. Direct Damage per projectile
		2. Number of projectiles per shot / Burst size
		3. Area Damage per shot
		4. Mechanics about how each shot gets fired (AoE radius, velocity, charge-time, etc)
		5. Magazine size / number of shots fired per burst / ammo consumed per shot
		6. Carried Ammo
		7. Rate of Fire (and any relevant mechanics)
		8. Reload Time / cooldown time and related mechanics or stats
		9. Weakpoint Bonus
		10. Armor Breaking
		11. Crowd Control effects (percentage to proc first, duration second)
		12. Additional Targets per projectile (blowthrough rounds, ricochets)
		13. Accuracy Modifiers
			a. Base Spread
			b. Spread Per Shot
			c. Max Spread (Base Spread + Spread Variance)
			d. Spread Recovery Speed
			e. Recoil Per Shot
		14. Effects on the Dwarf
	*/
	public abstract StatsRow[] getStats();
	public abstract Weapon clone();
	
	protected double calculateRNGDoTDPSPerMagazine(double DoTProcChance, double DoTDPS, int magazineSize) {
		/*
		 	This method should be used whenever applying Electrocute or Neurotoxin DoTs, since they're RNG-based.
		 	It estimates what percentage of the magazine has to be fired before a DoT gets applied, and then uses
		 	that number to reduce the standard DPS of the DoT to effectively model what the DoT's average DPS is 
		 	across the duration of firing the magazine.
		 	
			When DoTs stack, like in BL2, the formula is PelletsPerSec * DoTDuration * DoTChance * DoTDmgPerSec.
			However, in DRG, once a DoT is applied it can only have its duration refreshed.
		*/
		double numBulletsFiredBeforeProc = Math.round(MathUtils.meanRolls(DoTProcChance));
		double numBulletsFiredAfterProc = magazineSize - numBulletsFiredBeforeProc;
		double DoTUptime = numBulletsFiredAfterProc / (numBulletsFiredBeforeProc + numBulletsFiredAfterProc);
		
		return DoTDPS * DoTUptime;
	}
	
	protected double calculateAverageDoTDamagePerEnemy(double timeBeforeProc, double averageDoTDuration, double DoTDPS) {
		/*
			I'm choosing to model the DoT total damage as "How much damage does the DoT do to the average enemy while it's still alive?"
		*/
		

		double timeWhileAfflictedByDoT = averageTimeToKill() - timeBeforeProc;
		
		// Don't let this math create a DoT that lasts longer than the default DoT duration.
		if (timeWhileAfflictedByDoT > averageDoTDuration) {
			timeWhileAfflictedByDoT = averageDoTDuration;
		}
		
		return timeWhileAfflictedByDoT * DoTDPS;
	}
	
	protected double[] calculateAverageAreaDamage(double radius, double fullDamageRadius, double falloffStart, double falloffEnd) {
		/* 
			This method is based off of the hypothesis that the innnermost 50% of the radius gets full damage,
			and then the damage drops to 50%, and then linearly decreases to 33% at the furthest edge of the radius
			
			Theoretically it that should average around 71% of the initial damage, but due to how it's been modeled I highly doubt it will be that precise.
		*/
		
		// Want to test the halfway radius and every radius in +0.1m increments, and finally the outermost radius
		int numRadiiToTest = (int) Math.floor((radius - fullDamageRadius) * 20.0) + 1;
		
		// Add an extra tuple at the start for the return values
		double[][] toReturn = new double[1 + numRadiiToTest][3];
		double currentRadius, currentDamage;
		int totalNumGlyphids = 0;
		int currentGlyphids;
		for (int i = 0; i < numRadiiToTest - 1; i++) {
			currentRadius = fullDamageRadius + i * 0.05;
			if (i > 0) {
				currentDamage = falloffStart - (falloffStart - falloffEnd) * (i - 1) / (numRadiiToTest - 2);
			}
			else {
				currentDamage = 1.0;
			}
			
			toReturn[i+1] = new double[3];
			toReturn[i+1][0] = currentRadius;
			toReturn[i+1][1] = currentDamage;
			currentGlyphids = calculateNumGlyphidsInRadius(currentRadius) - totalNumGlyphids;
			toReturn[i+1][2] = currentGlyphids;
			totalNumGlyphids += currentGlyphids;
		}
		toReturn[numRadiiToTest] = new double[3];
		toReturn[numRadiiToTest][0] = radius;
		toReturn[numRadiiToTest][1] = falloffEnd;
		currentGlyphids = calculateNumGlyphidsInRadius(radius) - totalNumGlyphids;
		toReturn[numRadiiToTest][2] = currentGlyphids;
		totalNumGlyphids += currentGlyphids;
		
		toReturn[0] = new double[3];
		toReturn[0][0] = radius;
		toReturn[0][2] = totalNumGlyphids;
		
		double avgDmg = 0.0;
		for (int i = 1; i < toReturn.length; i++) {
			//System.out.println(toReturn[i][0] + " " + toReturn[i][1] + " " + toReturn[i][2] + " ");
			avgDmg += toReturn[i][1] * toReturn[i][2];
		}
		toReturn[0][1] = avgDmg / totalNumGlyphids;
		
		return toReturn[0];
	}
	
	protected int calculateNumGlyphidsInRadius(double radius) {
		/*
			This method should be used any time a projectile fired from this weapon has area-of-effect (AoE) damage in a radius.
			Assumptions made for this method:
				1. All targets hit by this projectile are Glyphid Grunts (regular, Guard, or Slasher) since they're the most common enemy type in swarms.
				2. All targets are standing on a flat surface so that the calculation can be reduced from three dimensions to two
				3. All targets are standing evenly distributed in an efficiently tessellated pattern (like equilateral hexagons) as close together as possible, allowing their legs to overlap but not their bodies
					(possibly an incorrect assumption, since in large swarms in Haz4+ their bodies can overlap when attacking the player)
				4. Hitbox detection of Glyphids' legs is perfect, so that any splash damage their leg would take is correctly applied to the Glyphid's healthbar
				5. Since Glyphids are all walking towards the player, only the rear half of the radius' circle will have Glyphids in it (because the frontline is flat, and then has more Glyphids behind it)
				6. One Glyphid will be hit directly by the projectile, and as such will be modeled as the center of the radius (guarantees a result of at least 1)
				
			Using those assumptions and some estimated measurements of in-game Glyphid models, this method should provide a reasonable estimate of how many Glyphid Grunts you can expect will 
			take damage from a projectile with the given radius of AoE damage.
			
			Quadratic regression approximation: f(x) = 0.942x^2 + 3.81x + 1.02 
			Approximation of the approximation: f(x) = x^2 + 4x + 1
		*/
		
		double glyphidBodyRadius = EnemyInformation.GlyphidGruntBodyRadius;
		double glyphidBodyAndLegsRadius = EnemyInformation.GlyphidGruntBodyAndLegsRadius;
		
		double effectivePackingRadius = glyphidBodyRadius + 0.5 * (glyphidBodyAndLegsRadius - glyphidBodyRadius);
		
		/*
			Using the packing radius and the triangular method of packing circles into 2D space, the Glyphid count can be estimated by using the center of
			each Glyphid as a point, and the constructing as many equilateral triangles as possible with the area using 2*effectivePackingRadius as the edge length.
		*/
		ArrayList<Point2D> glyphidCenters = new ArrayList<Point2D>();
		int glyphidsAcrossDiameter = 3 + 2 * (int) Math.ceil((radius - effectivePackingRadius)/(2.0*effectivePackingRadius));
		double rowHeight = effectivePackingRadius*Math.sqrt(3);
		int rowsOfGlyphids = 1 + (int) Math.ceil(radius / rowHeight);
		double furthestLeftGlyphidOnDiameter = Math.floor(glyphidsAcrossDiameter/2.0) * -2.0 * effectivePackingRadius;
		
		int numGlyphidsThisRow;
		double xOffset, yOffset;
		for (int row = 0; row <= rowsOfGlyphids; row ++) {
			numGlyphidsThisRow = glyphidsAcrossDiameter - row;
			xOffset = furthestLeftGlyphidOnDiameter + row * effectivePackingRadius;
			yOffset = row * rowHeight;
			for (int glyphidCenter = 0; glyphidCenter < numGlyphidsThisRow; glyphidCenter++) {
				glyphidCenters.add(new Point2D(xOffset + glyphidCenter * 2.0 * effectivePackingRadius, -1.0 * yOffset));
				// If the whole circle needs to be modeled:
				/*
				if (row > 0) {
					glyphidCenters.add(new Point2D(xOffset + 2.0*effectivePackingRadius, yOffset));
				}
				*/
			}
		}
		
		// This is the for loop that checks if a circle centered on each point will intersect the AoE circle
		double distanceFromCenterToOrigin;
		int numGlyphidsHitBySplash = 0;
		for (Point2D center: glyphidCenters) {
			distanceFromCenterToOrigin = center.vectorLength();
			// Special case: the Glyphid impacted by the projectile directly will be centered at (0, 0) which doesn't play nice with the math
			if (distanceFromCenterToOrigin == 0.0) {
				numGlyphidsHitBySplash++;
				continue;
			}
			
			// Due to rounding errors from double subtraction, this gets rounded to 2 decimal points
			if (MathUtils.round((distanceFromCenterToOrigin - glyphidBodyAndLegsRadius), 2) < radius) {
				numGlyphidsHitBySplash++;
			}
		}
		
		illustration = new AoEVisualizer(glyphidBodyRadius, glyphidBodyAndLegsRadius, radius, glyphidCenters);
		
		return numGlyphidsHitBySplash;
	}
	
	public abstract boolean currentlyDealsSplashDamage();
	public JPanel visualizeAoERadius() {
		if (currentlyDealsSplashDamage() && illustration != null) {
			return illustration;
		}
		else {
			return new JPanel();
		}
	}
	
	protected double increaseBulletDamageForWeakpoints2(double preWeakpointBulletDamage) {
		return increaseBulletDamageForWeakpoints2(preWeakpointBulletDamage, 0.0);
	}
	protected double increaseBulletDamageForWeakpoints2(double preWeakpointBulletDamage, double weakpointBonusModifier) {
		double estimatedDamageIncreaseWithoutModifier = EnemyInformation.averageWeakpointDamageIncrease();
		return estimatedDamageIncreaseWithoutModifier * (1.0 + weakpointBonusModifier) * preWeakpointBulletDamage;
	}
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage) {
		return increaseBulletDamageForWeakpoints(preWeakpointBulletDamage, 0.0);
	}
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage, double weakpointBonusModifier) {
		/*
			Before weakpoint bonus modifier, weakpoint damage is roughly a 38% increase per bullet.
			As a rule of thumb, the weakpointBonusModifier is roughly a (2/3 * bonus damage) additional increase per bullet. 
			30% bonus modifier => ~20% increase to DPS
		*/
		double probabilityBulletHitsWeakpoint = EnemyInformation.probabilityBulletWillHitWeakpoint();
		double estimatedDamageIncreaseWithoutModifier = EnemyInformation.averageWeakpointDamageIncrease();
		
		return ((1.0 - probabilityBulletHitsWeakpoint) + probabilityBulletHitsWeakpoint * estimatedDamageIncreaseWithoutModifier * (1.0 + weakpointBonusModifier)) * preWeakpointBulletDamage;
	}
	
	/*
		These methods feed into the output field at the bottom-left of the WeaponTab in the GUI
	*/
	
	// Single-target calculations
	public abstract double calculateIdealBurstDPS();
	public abstract double calculateIdealSustainedDPS();
	public abstract double sustainedWeakpointDPS();
	public abstract double sustainedWeakpointAccuracyDPS();
	
	// Multi-target calculations (based on "ideal" sustained DPS calculations)
	public abstract double calculateAdditionalTargetDPS();
	public abstract double calculateMaxMultiTargetDamage();
	
	// Non-damage calculations
	public abstract int calculateMaxNumTargets();
	
	protected double numMagazines(int carriedAmmo, int magazineSize) {
		// Don't forget to add the magazine that you start out with, in addition to the carried ammo
		return (((double) carriedAmmo) / ((double) magazineSize)) + 1.0;
	}
	protected int numReloads(int carriedAmmo, int magazineSize) {
		if (carriedAmmo % magazineSize == 0) {
			return (carriedAmmo / magazineSize) - 1;
		}
		else {
			return (int) Math.floorDiv(carriedAmmo, magazineSize);
		}
	}
	
	public abstract double calculateFiringDuration();
	public abstract double averageTimeToKill();  // Average health of an enemy divided by weakpoint sustained DPS
	public abstract double averageOverkill();  // (Total Damage done / Avg Health) - 1.0
	public abstract double estimatedAccuracy(boolean weakpointAccuracy); // -1 means manual or N/A; [0.00, 1.00] otherwise
	public abstract double utilityScore();
	
	// This method is used to explain how the Utility Scores are calculated for the UtilityBreakdownButton
	public StatsRow[] utilityExplanation() {
		StatsRow[] toReturn = new StatsRow[utilityScores.length];
		
		toReturn[0] = new StatsRow("Mobility:", utilityScores[0], false);
		toReturn[1] = new StatsRow("Damage Resist:", utilityScores[1], false);
		toReturn[2] = new StatsRow("Armor Break:", utilityScores[2], false);
		toReturn[3] = new StatsRow("Slow:", utilityScores[3], false);
		toReturn[4] = new StatsRow("Fear:", utilityScores[4], false);
		toReturn[5] = new StatsRow("Stun:", utilityScores[5], false);
		toReturn[6] = new StatsRow("Freeze:", utilityScores[6], false);
		
		return toReturn;
	}
	
	// Shortcut method for WeaponStatsGenerator
	public double[] getMetrics() {
		return new double[]{
			calculateIdealBurstDPS(), calculateIdealSustainedDPS(), sustainedWeakpointDPS(), sustainedWeakpointAccuracyDPS(),
			calculateAdditionalTargetDPS(), calculateMaxMultiTargetDamage(), calculateMaxNumTargets(), calculateFiringDuration(),
			averageTimeToKill(), averageOverkill(), estimatedAccuracy(false), calculateIdealSustainedDPS()
		};
	}
}