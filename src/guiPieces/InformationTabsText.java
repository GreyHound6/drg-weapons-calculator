package guiPieces;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import modelPieces.DoTInformation;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class InformationTabsText {

	private static JPanel createScrollableTextPanel(String title, JPanel scrollableText) {
		 JPanel toReturn = new JPanel();
		 toReturn.setLayout(new BorderLayout());
		 toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		 
		 JLabel header = new JLabel(title);
		 header.setForeground(GuiConstants.drgRegularOrange);
		 header.setFont(GuiConstants.customFontHeader);
		 JPanel centerAlignHeader = new JPanel();
		 centerAlignHeader.setLayout(new FlowLayout(FlowLayout.CENTER));
		 centerAlignHeader.setBackground(GuiConstants.drgBackgroundBrown);
		 centerAlignHeader.add(header);
		 toReturn.add(centerAlignHeader, BorderLayout.NORTH);
		 
		 JScrollPane scrollable = new JScrollPane(scrollableText);
		 scrollable.getVerticalScrollBar().setUnitIncrement(12);
		 toReturn.add(scrollable, BorderLayout.CENTER);
		 
		 return toReturn;
	}
	
	// This method can pull double-duty as both a Q&A box as well as a Term/Definition box
	private static JPanel createQandAPanel(String question, String answer) {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.PAGE_AXIS));
		
		JLabel questionOrTerm = new JLabel(question);
		questionOrTerm.setFont(GuiConstants.customFontBold);
		questionOrTerm.setForeground(GuiConstants.drgRegularOrange);
		// Set the Label to be almost flush with the left side
		JPanel leftAlignLabel = new JPanel();
		leftAlignLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftAlignLabel.setBackground(GuiConstants.drgBackgroundBrown);
		leftAlignLabel.add(questionOrTerm);
		toReturn.add(leftAlignLabel);
		
		JTextArea answerOrDefinition = new JTextArea(answer);
		answerOrDefinition.setFont(GuiConstants.customFont);
		answerOrDefinition.setBackground(GuiConstants.drgBackgroundBrown);
		answerOrDefinition.setForeground(GuiConstants.drgHighlightedYellow);
		// Left-pad the answer a bit for visual clarity
		answerOrDefinition.setMargin(new Insets(0, 30, 8, 0));
		answerOrDefinition.setWrapStyleWord(true);
		answerOrDefinition.setLineWrap(true);
		toReturn.add(answerOrDefinition);
		
		return toReturn;
	}
	
	public static JPanel getMetricsExplanation() {
		String[][] metricsExplanationtext = {
			{"Ideal Burst DPS", "For weapons with a magazine size larger than 1, this metric represents what the DPS would be of emptying an entire magazine at max rate of fire into an enemy, "
					+ "modeled as if every projectile hits flesh (not Armor or a Weakpoint). DoTs have their DPS added to this metric multiplied by the coefficient of how long the DoT afflicts the "
					+ "enemy divided by how long it takes to empty the magazine. If the weapon only fires 1 projectile before reloading, then this is the damage of that single shot divided by "
					+ "reload time, and DoTs are multiplied by the estimated percentage of enemies that the single shot would ignite."},
			{"Ideal Sustained DPS", "Very similar to Ideal Burst DPS, this metric models what the DPS would be if you were to start firing the weapon and not let go of the trigger until the "
					+ "weapon ran out of ammo. Again, this is modeled as if every bullet hits flesh, instead of Armor or a Weakpoint, and DoTs have their full DPS added to this value."},
			{"Sustained DPS + Weakpoints", "This metric is virtually identical to Ideal Sustained DPS, with the key difference being that the weapon's Weakpoint Accuracy is used to estimate "
					+ "how many projectiles would hit an enemy's Weakpoint and thus would have their Direct Damage increased. If the weapon is Manually Aimed, then the Weakpoint Accuracy is "
					+ "instead just an estimate how what percentage of enemies' bodies are weakpoints. If a weapon deals Direct Damage and can score a Weakpoint hit, this should always be a "
					+ "higher value than Ideal Sustained DPS. Keep in mind that Freezing an enemy removes Weakpoint multipliers and instead makes the whole enemy take x3 damage."},
			{"Sustained DPS + Weakpoints + Accuracy", "Adding another layer on top of Sustained DPS + Weakpoints, this metric models how projectiles can be missed due to General Accuracy. "
					+ "A low General Accuracy will result in a low value for this metric since so many projectiles will miss. If a weapon is Manually Aimed, then this metric will be identical "
					+ "to Sustained DPS + Weakpoints."},
			{"Ideal Additional Target DPS", "If the currently selected weapon can hit more than one enemy per projectile, then this metric will represent what the Ideal Sustained DPS dealt to "
					+ "non-primary targets would be. Again, modeled as if it doesn't hit Armor or Weakpoints."},
			{"Max Num Targets", "This metric represents the theoretical maximum number of Glyphid Grunts that take damage from a single projectile fired by the current weapon. For weapons "
					+ "that deal splash damage, like Engineer's Grenade Launcher or Gunner's Autocannon, you can click on this metric to see a visualization of how this program estimates "
					+ "enemies hit by a splash radius."},
			{"Max Multi-Target Damage", "As the name implies, this metric is used to show how much damage can be dealt by this weapon without having to resupply. This is modeled as if every "
					+ "single projectile hits a primary target and all possible secondary targets, and DoT damage dealt to individual enemies contributes to this value as well. As a result, "
					+ "getting a higher number of Max Num Targets will scale this number just as strongly as carrying more ammo."},
			{"Ammo Efficiency", "Ammo Efficiency is a bit more abstract of a metric, and technically doesn't have any units associated with it (unlike DPS, num targets, max damage, etc). "
					+ "The current formula used to calculate Ammo Efficiency is (Max Multi-Target Damage / Math.ceil(Number of bullets needed to kill one enemy, including Weakpoint Bonuses)). "
					+ "As a result of that formula, higher damage per bullet and higher Weakpoint bonus will yield a smaller denominator, while higher damage per bullet, more targets per shot, "
					+ "and more carried ammo will result in a higher numerator. Using a combination of those 4 upgrades will result in a very high AE score."},
			{"General Accuracy", "A pretty straight-forward metric to understand, General Accuracy is an estimate of what percentage of projectiles would hit a target from 7m away using sustained "
					+ "fire. For the two shotguns, the distance has been reduced to 5m. Some weapons like both of Driller's primary weapons, Engineer's Grenade Launcher, or Scout's M1000 "
					+ "Classic (Focused Shots) can't have their accuracy modeled and will instead say \"Manually Aimed\"."},
			{"Weakpoint Accuracy", "Just like General Accuracy, this metric represents what percentage of projectiles would hit an enemy's Weakpoint from 7m away (5m for the two shotguns). "
					+ "For weapons that can't have their accuracy modeled, it will instead say \"Manually Aimed\"."},
			{"Firing Duration", "This answers the question of how long it will take to fire every projectile from the weapon if you were to fire continuously, even through reloads or cooldowns. "
					+ "Slower rates of fire and large carried ammo capacities increase the duration, whereas faster rates of fire and faster reloads decrease duration."},
			{"Avg Overkill", "This is an estimate of how much damage gets \"wasted\" by bullets when enemies have lower remaining health than the damage per projectile. Because different creatures have "
					+ "different healthpools that scale with Hazard Level and Player Count, this uses a weighted average of all enemies' healthpools for its Overkill calculations."},
			{"Avg Time to Kill", "A very simple metric; all this does is divide the weighted average healthpool of all enemies by the current Sustained + Weakpoint DPS to get an estimate of "
					+ "how quickly the current weapon and build can kill an enemy."},
			{"Breakpoints", "Although the number displayed is pretty meaningless by itself, clicking on this metric will have a small window pop up that shows you the fewest number of projectiles "
					+ "needed to kill various enemies under different conditions."},
			{"Utility", "Another abstract metric, this tries to numerically represent the value of certain mods that don't affect DPS or total damage, but do things like slow or stun enemies. "
					+ "Additionally, if the weapon can break Light Armor Plates, then the average probability that each shot can break a Light Armor plate will be listed."},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < metricsExplanationtext.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(metricsExplanationtext[i][0], metricsExplanationtext[i][1]));
		}
		
		return createScrollableTextPanel("What do each of the calculated metrics mean?", panelContainedWithinScrollPane);
	}
	
	public static JPanel getFAQText() {
		String[][] FAQtext = {
			{"Where is the Breach Cutter?", "The Breach Cutter is substantially harder to model accurately due to how it works. I would need to know the sizes of each enemy, the m/sec that the Breach Cutter line moves at, "
					+ "how many times per second the line does damage, and how much damage per tick it does."},
			{"Why do some Mods and Overclocks have a Red outline?", "Mods or Overclocks with a Red outline either are not implemented yet, or how they work in-game can't be represented by the Weapon's stats."},
			{"What's the point of this program?", "To help the DRG community compare and contrast their preferred builds for each weapon, and to provide more detail about how the weapons work than described in-game or on the wiki."},
			{"How long should I wait for the program to calculate the best build?", "This should run pretty fast. I would expect it to be done in a second or two, five at most."},
			{"I think something is wrong/missing, how do I communicate that to you?", "In the 'Misc. Actions' Menu, there's an option to suggest changes. That should automatically open up this project's GitHub issue creation page for you."},
			{"Can I help improve to this project?", "Yes! This is an open-source, freeware fan project. Although it's started out as just one developer, I would love to have help."},
			{"How frequently will this be updated?", "There are a couple features that I want to add before calling this 'done', but I'm planning to update each weapon's stats as GSG devs update them in-game on their production build."},
			{"Will this be made available as a live website?", "Probably not. Thousands of lines of Java code do not port well into HTML/CSS/Javascript. The data generated by this program will be available on drg-builds.com at some point in the future."},
			{"How did you model [insert mechanic here]?", "This is an open-source project. Feel free to look around the source code and see how it was done. In general though: I chose to model everything like a continuous function instead of "
					+ "discrete. Slight loss of precision, but significantly easier."},
			{"How are Status Effect Utility scores calculated?", "The formula I chose to use is (% Chance to Proc) * (Number of Targets) * (Effect Duration) * (Utility Factor), where 'Utility Factor' is some scalar value assigned to each effect."},
			// I'm intentionally adding blank lines below here so that the content gets pushed to the top of the page
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < FAQtext.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(FAQtext[i][0], FAQtext[i][1]));
		}
		
		return createScrollableTextPanel("Frequently Asked Questions", panelContainedWithinScrollPane);
	}

	public static JPanel getGlossaryText() {
		// Perhaps these should be sorted alphabetically?
		String[][] glossaryText = {
			{"Armor", "Some of the enemies on Hoxxes IV have exterior armor plates to protect them. Grunts have Light Armor that reduces damage by 20%, but has a chance to break every time it gets damaged. Praetorians and "
					+ "Shellbacks have Heavy Armor plates that negate all incoming Direct Damage, but break after absorbing a set amount of damage. The third type of armor is found on Oppressors and Dreadnoughts: it makes them "
					+ "immune to all Direct Damage from the front and can't be broken, so shoot their abdomen."},
			{"Armor Breaking", "Increasing this stat above 100% means that it takes fewer shots to break Grunt and Praetorian armor plates, so you lose less damage to Armor. Doesn't affect the third type of Armor, though. "
					+ "Likewise, if this is less than 100%, it means that damage is less effective vs Armor."},
			{"Weakpoint", "Most enemies have certain spots on their body that will take extra Direct Damage. Those spots are referred to as Weakpoints. Common areas are the mouths of medium-sized Glyphids, the abdomens of "
					+ "Macteras, Praetorians, and Dreadnoughts, and glowing bulbs on the sides of larger enemies like Bulk Detonators, Wardens, and Menaces. With the exception of mouths, weakpoints are usually brightly colored and will light up when damaged."},
			{"Weakpoint Bonus", "Some weapons that deal Direct Damage have Mods or Overclocks that affect how that damage gets multiplied when impacting a Weakpoint. Most of the time, it's a multiplicative bonus that gets applied to the projectile before the Weakpoint's multiplier gets used."},
			{"Direct Damage", "One of the three main Types of damage dealt by weapons, Direct Damage is the only one of the three that is affected by Armor, Weakpoints, and the Frozen Status Effect. Direct Damage gets reduced when passing through Light Armor, negated entirely by Heavy Armor "
					+ "and Unbreakable Armor, and gets multiplied when impacting a creature's Weakpoint or a Frozen enemy. Direct Damage can be any of these 5 Elements: Kinetic, Explosive, Fire, Frost, or Electric."},
			{"Area Damage", "The second of the three main Types of damage, Area Damage ignores Armor, Weakpoints, and the Frozen Status Effect. Any Area Damage inflicted to a creature simply reduces their healthbar. Not all Area Damage is dealt in an Area-of-Effect, like Embedded Detonators "
					+ "or Explosive Reload. Area Damage can be any of these 4 elements: Explosive, Fire, Frost, or Electric"},
			{"Temperature Damage", "The third Type of damage dealt by weapons, all forms of Temperature Damage only affect a creature's Temperature meter and do not directly affect the enemies' healthbars. Heat Damage increases the Temperature Meter, and Cold Damage decreases it."},
			{"Elemental Damage Types", "All Direct Damage, Area Damage, and Damage Over Time is comprised of one or more of the following types: Kinetic, Explosive, Fire, Frost, Electric, Poison, or Radiation. Depending on the creature being shot and the biome you're in, each of these "
					+ "elements can either be more effective against or resisted by the creature."},
			{"Heat (Temperature Damage)", "Heat Damage doesn't actually affect an enemy's healthbar; rather it affects their Temperature. Once enough Heat has been accumulated, the enemy receives a Burn DoT which continues until the enemy "
					+ "sheds enough Heat to be doused, or the enemy dies. Applying more Heat Damage to an already ignited enemy prolongs the Burn duration. Heat Damage counteracts Cold Damage, and can cause Temperature Shock if applied to a Frozen enemy."},
			{"Cold (Temperature Damage)", "As Frost Damage is to Fire, so Cold is to Heat. Applying Cold Damage to an enemy decreases their Temperature until eventually they become Frozen. Enemies remain Frozen until they gain enough Heat to thaw, "
					+ "at which point they can start accumulating Cold Damage again. Applying more Cold Damage to Frozen enemies does NOT increase the Freeze Duration. Cold Damage counteracts Heat Damage, and can cause Temperature Shock if applied to a Burning enemy."},
			{"DoT", "An acronym that stands for \"Damage Over Time\". This term is used to refer to damage which doesn't get applied per projectile, but rather gets applied over a period of time."},
			{"Status Effect", "A conditional effect that can be applied to enemies. Sometimes it's a DoT, other times it's a crowd control effect."},
			{"Burn (DoT)", "When an enemy has its Temperature meter increased to maximum by taking sustained Heat Damage, it ignites and gains a Burn DoT. While Burning, enemies take an average of " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) 
					+  " Fire Damage per second. If no more Heat Damage is applied, then their Temperature will steadily decrease until they are doused and the Burn DoT will end. On the other hand, sustaining even more Heat Damage will prolong the Burn duration. Applying "
					+ "Cold Damage will significantly shorten the duration of the Burn, but also inflict Temperature Shock."},
			{"Frozen (Status Effect)", "Thematically the opposite of the Burn DoT, enemies become Frozen when their Temperature is lowered enough by sustained Cold Damage. Once Frozen, they receive x" + UtilityInformation.Frozen_Damage_Multiplier + " Direct Damage. Frozen enemies "
					+ "cannot have the freeze duration increased; instead they will thaw over time. Once they have thawed, more Cold Damage can be applied to freeze them again. Applying Heat Damage will significantly shorten the duration of the Freeze, but also inflict Temperature Shock."},
			{"Electrocute (DoT, Status Effect)", "Some of the Weapons and Overclocks have a chance to apply the Electrocute Status Effect. Once applied, enemies take an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for " 
					+ DoTInformation.Electro_SecsDuration + " seconds, while also being slowed by 80%. Enemies can only have one Electrocute applied to them at once; if another shot were to apply a second Electrocute, the first DoT has its duration refreshed instead."},
			{"Radiation (DoT)", "There are two types of Radiation: environmental hazards in the Radioactive Exclusion Zone which deal an average of " + MathUtils.round(DoTInformation.Rad_Env_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to the player, and the Radiation field left behind by the "
					+ "Overclock 'Fat Boy', which does an average of " + MathUtils.round(DoTInformation.Rad_FB_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to enemies."},
			{"Neurotoxin (DoT, Status Effect)", "Similar to Electrocute, a few weapons can have a chance to apply Neurotoxin. Enemies afflicted by Neurotoxin take an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + " Poison Damage per second for up to " 
					+ DoTInformation.Neuro_SecsDuration + " seconds, while also being slowed by 30%. Also like Electrocute, enemies can only have one Neurotoxin DoT applied to them at once; anything that would apply a second effect instead refreshes the duration."},
			{"Persistent Plasma (DoT)", "Similar to Radiation, this is an area-of-effect DoT that gets left behind by certain mods and overclocks. It deals an average of " + MathUtils.round(DoTInformation.Plasma_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second."},
			{"Stun (Status Effect)", "Stunning an enemy stops them from moving or attacking for a set duration. That duration changes from weapon to weapon, but it's typically around 2 seconds. Enemies that channel their attacks (like Praetorians) can have those attacks interrurpted by a Stun."},
			{"Fear (Status Effect)", "Inflicting Fear on an enemy causes them to stop what they're doing and run from the source of the Fear as fast as they can move for about " + UtilityInformation.Fear_Duration + " seconds. After the Fear wears off, they return to normal behavior."},
			{"Base Spread", "This stat affects how accurate the first shot will be. At 0%, that means the first shot is guaranteed to go exactly where your crosshair is pointing. As the percentage goes higher, the probability that the first shot will hit decreases."},
			{"Spread Per Shot", "After every shot gets fired, the maximum area of the crosshair increases by this amount. Thus, successive shots get increasingly less likely to hit your intended target until it reaches Max Spread."},
			{"Spread Recovery", "This stat is constantly reducing the current Spread of the gun, trying to return to Base Spread. Because this is a constant rate, it's more effective the lower the Rate of Fire."},
			{"Recoil", "Recoil is an estimate of how far off-axis the center of the Spread is after each shot. Typically, Recoil is primarily a vertical climb with a little horizontal movement, but some weapons have significantly more hotizontal movement than others. "
					+ "While Spread has 4 different pieces (Base, Per Shot, Max, and Recovery), Recoil is only per-shot. Higher RoF means more total recoil."},
			{"Time to Kill (TTK)", "As the name implies, this metric is used to estimate how quickly a weapon can kill an enemy. Because there are so many enemy types with different healthbars and spawn rates, this metric gets evaluated as the weighted average of all "
					+ "healthbars divided by the Sustained Weakpoint DPS of the weapon."},
			{"Overkill", "Because enemy healthbars don't normally come in multiples of the damage done by each Weapon, there's inevitably some damage going to be wasted. Overkill is an approximation of how much damage gets wasted by a Weapon as it kills the weighted average healthbar."},
			{"Mobility", "Some Mods or Overclocks can affect how efficiently players move around the environment. Often, they are conditional increases or decreases to movement speed, but sometimes they provide the ability to 'Blast Jump'."},
			{"Breakpoints", "In a general sense, Breakpoints are how few shots are necessary to kill certain creatures. As a result, higher damage per shot results in lower breakpoints. Glyphid Swarmers, Grunts, and Praetorians, "
					+ "as well as Mactera Spawns, are the most common breakpoints referenced."},
			{"Utility", "This is a generic term used as an umbrella for a variety of non-damage statistics, like buffs to the player or debuffs to enemies."},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < glossaryText.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(glossaryText[i][0], glossaryText[i][1]));
		}
		
		return createScrollableTextPanel("Glossary of Terms", panelContainedWithinScrollPane);
	}
	
	public static JPanel getAcknowledgementsText() {
		String[][] acknowledgementsText = {
			{"Ghost Ship Games", "Thank you for making the game Deep Rock Galactic and letting me use some images and artwork from the game in this program."},
			{"Mike @ GSG / Dagadegatto", "Thank you for being willing to answer so many of my technical questions about DRG and helping to improve the quality of this program's models."},
			{"Elythnwaen", "Thank you for collecting data about elemental weaknesses, resistances, Burn/Freeze temperatures, and more! Also, thank you for letting me know about Subata's 50% Armor Breaking penalty."},
			{"Ian McDonagh", "Thank you for creating the open-source JAR 'image4j' that allows me to use .ico files natively."},
			{"Gaming for the Recently Deceased", "Thank you for helping to promote this project and making a video about it. YouTube Channel: https://www.youtube.com/channel/UCL_8gMChYJD5ls7GaJtGmUw"},
			{"Usteppin", "Thank you for collecting some data and testing weapon builds for me on Hazard 5. Twitch Channel: https://www.twitch.tv/usteppin"},
			{"Alpha and Beta testers", "Thank you Minomess, Royal, CynicalAtropos, and ARobotWithCancer for giving me feedback while this was still being developed and helping test out the builds."},
			// I'm intentionally adding blank lines below here so that the content gets pushed to the top of the page
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < acknowledgementsText.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(acknowledgementsText[i][0], acknowledgementsText[i][1]));
		}
		
		return createScrollableTextPanel("Acknowledgements", panelContainedWithinScrollPane);
	}
}
