package modelPieces;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import guiPieces.AccuracyAnimation;
import guiPieces.GuiConstants;
import guiPieces.LineGraph;
import spreadCurves.SpreadCurve;
import utilities.MathUtils;
import utilities.Point2D;

public class AccuracyEstimator {
	
	private double delayBeforePlayerReaction;
	private double playerRecoilRecoveryPerSecond;
	
	private double targetDistanceMeters;
	private boolean modelRecoil;
	private boolean visualizeGeneralAccuracy;
	private boolean canBeVisualized;
	
	private SpreadCurve spreadTransformingCurve;
	
	private double[] bulletFiredTimestamps;
	
	private double rateOfFire;
	private int magSize, burstSize;
	private double baseSpread, spreadPerShot, spreadRecoverySpeed, spreadVariance;
	private double recoilPitch, recoilYaw, mass, springStiffness;
	private double naturalFrequency, initialVelocity, recoilGoal, recoilPerShotEndTime;
	
	public AccuracyEstimator() {
		// With these two values, recoil should be reduced to 0% in exactly 0.5 seconds.
		delayBeforePlayerReaction = 0.15;  // seconds
		playerRecoilRecoveryPerSecond = 1.00/0.35;  // Percentage of max recoil that the player recovers per second
		
		// Start at 10m distance for all weapons in AccuracyEstimator, but let it be changed by individual weapons as necessary.
		targetDistanceMeters = 10.0;
		visualizeGeneralAccuracy = true;
		modelRecoil = true;
		
		spreadTransformingCurve = null;
		
		// This variable determines the minimum value for Recoil(t) to fall to before that recoil is discarded by successive shots
		recoilGoal = 0.1;
		
		canBeVisualized = false;
	}
	
	// Setters and Getters
	public void setDistance(double newDistance) {
		targetDistanceMeters = newDistance;
	}
	public double getDistance() {
		return targetDistanceMeters;
	}
	
	public void setModelRecoil(boolean newValue) {
		modelRecoil = newValue;
	}
	public boolean isModelingRecoil() {
		return modelRecoil;
	}
	
	public void makeVisualizerShowGeneralAccuracy(boolean value) {
		visualizeGeneralAccuracy = value;
	}
	public boolean visualizerShowsGeneralAccuracy() {
		return visualizeGeneralAccuracy;
	}
	
	public void setSpreadCurve(SpreadCurve sc) {
		spreadTransformingCurve = sc;
	}
	
	private double convertDegreesToMeters(double degrees) {
		// Because both recoil and spread use degrees as their output, I have to first convert from degrees to radians for the Math package
		return targetDistanceMeters * Math.tan(degrees * Math.PI / 180.0);
	}
	
	private void calculateBulletFiredTimestamps() {
		bulletFiredTimestamps = new double[magSize];
		
		double timeBetweenBursts = 1.0 / rateOfFire;
		
		double currentTime = 0.0;
		for (int i = 0; i < magSize; i++) {
			bulletFiredTimestamps[i] = currentTime;
			
			if (burstSize > 1 && (i+1) % burstSize > 0) {
				// During burst; add 1/20th second
				currentTime += 0.05;
			}
			else {
				// Either this gun doesn't have a burst mode, or it just fired the last bullet during a burst
				currentTime += timeBetweenBursts;
			}
		}
	}
	
	private double getTotalSpreadAtTime(double t) {
		// This method is modeled as if every bullet was fired at maximum possible RoF
		
		// For practicality purposes, I have to model it as if the exact moment the bullet gets fired its Total Spread stays the same, and then gets added a very short time afterwards.
		double spreadPerShotAddTime = 0.01;
		double currentSpread = 0.0;
		double bulletFiredTimestamp, nextTimestamp;
		for (int i = 0; i < bulletFiredTimestamps.length; i++) {
			bulletFiredTimestamp = bulletFiredTimestamps[i];
			
			// Early exit condition: if t is before any bullet timestamp, that means that this for loop should stop evaluating
			if (t < bulletFiredTimestamp) {
				break;
			}
			
			if (t > bulletFiredTimestamp + spreadPerShotAddTime) {
				currentSpread = Math.min(currentSpread + spreadPerShot, spreadVariance);
			}
			
			if (i < bulletFiredTimestamps.length - 1) {
				nextTimestamp = bulletFiredTimestamps[i+1];
				if (t >= nextTimestamp) {
					currentSpread = Math.max(currentSpread - (nextTimestamp - bulletFiredTimestamp) * spreadRecoverySpeed, 0);
				}
				else {
					currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadRecoverySpeed, 0);
				}
			}
			else {
				// The last bullet is allowed to trail off to Base Spread
				currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadRecoverySpeed, 0);
			}
		}
		
		if (spreadTransformingCurve != null) {
			return baseSpread + spreadTransformingCurve.convertSpreadValue(currentSpread);
		}
		else {
			return baseSpread + currentSpread;
		}
	}
	
	private double getRecoilPerShotOverTime(double t) {
		return Math.pow(Math.E, -1.0 * naturalFrequency * t) * (initialVelocity * t);
	}
	
	private double getTotalRecoilAtTime(double t, boolean playerReducingRecoil) {
		double total = 0.0;
		double bulletFiredTimestamp;
		
		// Early exit condition: if the user disables "model recoil" just return 0 for all t
		if (!modelRecoil) {
			return 0;
		}
		
		if (playerReducingRecoil) {
			// I'm choosing to model player-reduced recoil as if it goes to zero after 0.5 seconds. For weapons with RoF <=2, that means each burst of bullets become their own pocket of recoil, independent of each other.
			if (rateOfFire > 2) {
				// Early exit condition: if t > 0.5, then the recoil will always be zero.
				if (t > delayBeforePlayerReaction + 1.0/playerRecoilRecoveryPerSecond) {
					return 0;
				}
				
				for (int i = 0; i < bulletFiredTimestamps.length; i++) {
					bulletFiredTimestamp = bulletFiredTimestamps[i];
					if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
						total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
					}
				}
				
				double playerReductionMultiplier = 1.0;
				if (t > delayBeforePlayerReaction) {
					playerReductionMultiplier = Math.max(1.0 - (t - delayBeforePlayerReaction) * playerRecoilRecoveryPerSecond, 0);
				}
				
				return total * playerReductionMultiplier;
			}
			else {
				// 1. Find the timestamp of the first bullet of the most recent burst
				int burstStartIndex = magSize - burstSize;  // Default to the last burst in the magazine 
				for (int i = 1; i < magSize / burstSize; i++) {
					if (bulletFiredTimestamps[i * burstSize] > t) {
						burstStartIndex = (i - 1) * burstSize;
						break;
					}
				}
				
				// 2. Add up the total recoil of that burst
				for (int i = 0; i < burstSize; i++) {
					bulletFiredTimestamp = bulletFiredTimestamps[burstStartIndex + i];
					if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
						total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
					}
				}
				
				// 3. Apply player reduction to that burst relative to t
				double playerReductionMultiplier = 1.0;
				if ((t - bulletFiredTimestamps[burstStartIndex]) > delayBeforePlayerReaction) {
					playerReductionMultiplier = Math.max(1.0 - ((t - bulletFiredTimestamps[burstStartIndex]) - delayBeforePlayerReaction) * playerRecoilRecoveryPerSecond, 0);
				}
				
				return total * playerReductionMultiplier;
			}
		}
		else {
			for (int i = 0; i < bulletFiredTimestamps.length; i++) {
				bulletFiredTimestamp = bulletFiredTimestamps[i];
				if (bulletFiredTimestamp <= t && t <= bulletFiredTimestamp + recoilPerShotEndTime) {
					total += getRecoilPerShotOverTime(t - bulletFiredTimestamp);
				}
			}
			
			return total;
		}
	}
	
	public double calculateCircularAccuracy(
			boolean weakpointTarget, double RoF, int mSize, int bSize, 
			double horizontalBaseSpread, double verticalBaseSpread, double SpS, double SRS, double SV, 
			double rPitch, double rYaw, double m, double sStiffness
		) {
		/*
			Step 1: Calculate when bullets will be fired for this magazine, and store the timestamps internally
		*/
		rateOfFire = RoF;
		magSize = mSize;
		burstSize = bSize;
		calculateBulletFiredTimestamps();
		
		/*
			Step 2: Calculate what the Current Spread value will be across the whole magazine and store that internally
		*/
		// Engineer/Shotgun uses an ellipse instead of a circle. To estimate its accuracy using Lens intersections, I have to approximate that ellipse as a circle with equal area.
		baseSpread = Math.sqrt(horizontalBaseSpread * verticalBaseSpread);
		spreadPerShot = SpS;
		spreadRecoverySpeed = SRS;
		spreadVariance = SV;
		
		/*
			Step 3: Calculate what the recoil will be at any given time, t, and then store both the raw recoil and player-reduced recoil for use later
		*/
		recoilPitch = rPitch;
		recoilYaw = rYaw;
		mass = m;
		springStiffness = sStiffness;
		naturalFrequency = Math.sqrt(springStiffness / mass);
		initialVelocity = Math.hypot(recoilPitch, recoilYaw);
		if (initialVelocity > 0) {
			recoilPerShotEndTime = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-naturalFrequency * recoilGoal / initialVelocity) / naturalFrequency;
		}
		else {
			recoilPerShotEndTime = 0.0;
		}
		
		/*
			Step 4: Use Spread and Player-Reduced Recoil to calculate the size and offset of the crosshair relative to the static target for each bullet in the magazine
		*/
		canBeVisualized = true;
		
		double targetRadius = 0.0;
		if (weakpointTarget) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		double sumOfAllProbabilities = 0.0;
		double bulletFiredTimestamp, crosshairRadius, crosshairRecoil, P; 
		for (int i = 0; i < magSize; i++) {
			bulletFiredTimestamp = bulletFiredTimestamps[i];
			// Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
			crosshairRadius = convertDegreesToMeters(getTotalSpreadAtTime(bulletFiredTimestamp) / 2.0);
			crosshairRecoil = convertDegreesToMeters(getTotalRecoilAtTime(bulletFiredTimestamp, true));
			
			if (targetRadius >= crosshairRadius) {
				if (crosshairRecoil <= targetRadius - crosshairRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
					P = 1.0;
				}
				else if (crosshairRecoil >= targetRadius + crosshairRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = MathUtils.areaOfLens(targetRadius, crosshairRadius, crosshairRecoil) / (Math.PI * Math.pow(targetRadius, 2));
				}
			}
			else {
				if (crosshairRecoil <= crosshairRadius - targetRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
					P = Math.pow((targetRadius / crosshairRadius), 2);
				}
				else if (crosshairRecoil >= crosshairRadius + targetRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = MathUtils.areaOfLens(crosshairRadius, targetRadius, crosshairRecoil) / (Math.PI * Math.pow(crosshairRadius, 2));
				}
			}
			
			// System.out.println("P for bullet # " + (i + 1) + ": " + P);
			sumOfAllProbabilities += P;
		}
		
		return sumOfAllProbabilities / magSize * 100.0;
	}
	
	// TODO: someday I might like to model Recoil into this, too...
	public double calculateRectangularAccuracy(boolean weakpoint, double horizontalBaseSpread, double verticalBaseSpread) {
		// Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
		double crosshairHeightMeters = convertDegreesToMeters(verticalBaseSpread / 2.0);
		double crosshairWidthMeters = convertDegreesToMeters(horizontalBaseSpread / 2.0);
		double targetRadius;
		if (weakpoint) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		
		/*
			From observation, it looks like the horizontal distribution of bullets followed a bell curve such that the highest probabilities were in the center of the rectangle, 
			and the lower probabilities were near the edges. To model that, I'm choosing to calculate the sum of the probabilities that the horizontal spread will be within 
			the target radius as well as the probability of vertical spread being within the target radius, and then taking the area of the "probability ellipse" formed by those two numbers.
		*/
		// Convert the target radius in meters to the unit-less probability ellipse
		double endOfProbabilityCurve = 2.0 * Math.sqrt(2.0);
		double horizontalProbabilityRatio = endOfProbabilityCurve * targetRadius / crosshairWidthMeters;
		double hProb = MathUtils.areaUnderNormalDistribution(-1.0 * horizontalProbabilityRatio, horizontalProbabilityRatio);
		double verticalProbabilityRatio = endOfProbabilityCurve * targetRadius / crosshairHeightMeters;
		double vProb = MathUtils.areaUnderNormalDistribution(-1.0 * verticalProbabilityRatio, verticalProbabilityRatio);
		
		double areaOfProbabilityEllipse = Math.PI * hProb * vProb / 4.0;
		
		return areaOfProbabilityEllipse * 100.0;
	}
	
	public boolean visualizerIsReady() {
		return canBeVisualized;
	}
	
	public JPanel getVisualizer() {
		JPanel toReturn = new JPanel();
		
		// Part 1: figuring out stuff before rendering
		double lastBulletFiredTimestamp = bulletFiredTimestamps[bulletFiredTimestamps.length - 1];
		// For Engineer/Shotgun, SRS = 0, so I have to use Math.min() to sidestep 0/0 errors.
		double loopDuration = lastBulletFiredTimestamp + Math.max((getTotalSpreadAtTime(lastBulletFiredTimestamp + 0.01) - baseSpread) / Math.max(spreadRecoverySpeed, 0.00001), recoilPerShotEndTime);
		
		// There will be this many data points taken per second (should be at least 10?)
		// This number should match the FPS in AccuracyAnimation so that every frame is just pulling a the next value
		double sampleDensity = 100.0;
		double timeBetweenSamples = 1.0 / sampleDensity;
		int numSamples = (int) Math.ceil(loopDuration * sampleDensity) + 1;
		
		// Part 2: constructing the datasets to plot
		int i;
		double currentTime, currentValue;
		
		ArrayList<Point2D> rawSpreadData = new ArrayList<Point2D>();
		ArrayList<Point2D> convertedSpreadData = new ArrayList<Point2D>();
		ArrayList<Point2D> rawRecoilData = new ArrayList<Point2D>();
		ArrayList<Point2D> convertedRawRecoilData = new ArrayList<Point2D>();
		ArrayList<Point2D> reducedRecoilData = new ArrayList<Point2D>();
		ArrayList<Point2D> convertedReducedRecoilData = new ArrayList<Point2D>();
		
		double maxSpread = 0.0;
		double minSpread = 10000.0;
		double maxRawRecoil = 0.0;
		double maxReducedRecoil = 0.0;
		for (i = 0; i < numSamples; i++) {
			currentTime = i * timeBetweenSamples;
			
			// Spread
			currentValue = getTotalSpreadAtTime(currentTime);
			minSpread = Math.min(minSpread, currentValue);
			maxSpread = Math.max(maxSpread, currentValue);
			rawSpreadData.add(new Point2D(currentTime, currentValue));
			convertedSpreadData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue / 2.0)));
			
			// Raw Recoil
			currentValue = getTotalRecoilAtTime(currentTime, false);
			maxRawRecoil = Math.max(maxRawRecoil, currentValue);
			rawRecoilData.add(new Point2D(currentTime, currentValue));
			convertedRawRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));
			
			// Reduced Recoil
			currentValue = getTotalRecoilAtTime(currentTime, true);
			maxReducedRecoil = Math.max(maxReducedRecoil, currentValue);
			reducedRecoilData.add(new Point2D(currentTime, currentValue));
			convertedReducedRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));
		}
		
		// Part 3: displaying the data
		JPanel granularDataPanel = new JPanel();
		granularDataPanel.setPreferredSize(new Dimension(420, 684));
		granularDataPanel.setLayout(new BoxLayout(granularDataPanel, BoxLayout.PAGE_AXIS));
		JPanel variables = new JPanel();
		variables.setLayout(new GridLayout(4, 4));
		variables.add(new JLabel("Base Spread:"));
		variables.add(new JLabel(MathUtils.round(baseSpread, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Recoil Pitch:"));
		variables.add(new JLabel(MathUtils.round(recoilPitch, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Spread per Shot:"));
		variables.add(new JLabel(MathUtils.round(spreadPerShot, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Recoil Yaw:"));
		variables.add(new JLabel(MathUtils.round(recoilYaw, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Spread Recovery:"));
		variables.add(new JLabel(MathUtils.round(spreadRecoverySpeed, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Mass:"));
		variables.add(new JLabel(MathUtils.round(mass, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Spread Variance:"));
		variables.add(new JLabel(MathUtils.round(spreadVariance, GuiConstants.numDecimalPlaces) + ""));
		variables.add(new JLabel("Spring Stiffness:"));
		variables.add(new JLabel(MathUtils.round(springStiffness, GuiConstants.numDecimalPlaces) + ""));
		granularDataPanel.add(variables);
		
		JPanel recoilPerShotPanel = new JPanel();
		//recoilPerShotPanel.setPreferredSize(new Dimension(228, 162));
		recoilPerShotPanel.setLayout(new BoxLayout(recoilPerShotPanel, BoxLayout.PAGE_AXIS));
		recoilPerShotPanel.add(new JLabel("Recoil per Shot Graph"));
		ArrayList<Point2D> recoilPerShotData = new ArrayList<Point2D>();
		double t;
		for (i = 0; i < (int) Math.ceil(recoilPerShotEndTime * sampleDensity) + 1; i++) {
			t = i*0.01;
			recoilPerShotData.add(new Point2D(t, getRecoilPerShotOverTime(t)));
		}
		double maxRecoilPerShot = getRecoilPerShotOverTime(1.0 / naturalFrequency);
		LineGraph recoilPerShot = new LineGraph(recoilPerShotData, recoilPerShotEndTime, Math.max(3.0, maxRecoilPerShot));
		recoilPerShot.setGraphAnimation(false);
		recoilPerShotPanel.add(recoilPerShot);
		granularDataPanel.add(recoilPerShotPanel);
		
		JPanel lineGraphsPanel = new JPanel();
		lineGraphsPanel.setLayout(new BoxLayout(lineGraphsPanel, BoxLayout.PAGE_AXIS));
		
		LineGraph spreadGraph = new LineGraph(rawSpreadData, loopDuration, Math.max(maxSpread, 8.0));
		new Thread(spreadGraph).start();
		JPanel spreadGraphAndLabel = new JPanel();
		spreadGraphAndLabel.setLayout(new BoxLayout(spreadGraphAndLabel, BoxLayout.PAGE_AXIS));
		spreadGraphAndLabel.add(new JLabel("Crosshair diameter (degrees) vs Time (seconds)"));
		spreadGraphAndLabel.add(spreadGraph);
		spreadGraphAndLabel.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(spreadGraphAndLabel);
		
		LineGraph rawRecoilGraph = new LineGraph(rawRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
		new Thread(rawRecoilGraph).start();
		JPanel rawRecoilGraphAndLabel = new JPanel();
		rawRecoilGraphAndLabel.setLayout(new BoxLayout(rawRecoilGraphAndLabel, BoxLayout.PAGE_AXIS));
		rawRecoilGraphAndLabel.add(new JLabel("Recoil offset (degrees) vs Time (seconds)"));
		rawRecoilGraphAndLabel.add(rawRecoilGraph);
		rawRecoilGraphAndLabel.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(rawRecoilGraphAndLabel);
		
		LineGraph playerReducedRecoilGraph = new LineGraph(reducedRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
		new Thread(playerReducedRecoilGraph).start();
		JPanel reducedRecoilAndGraph = new JPanel();
		reducedRecoilAndGraph.setLayout(new BoxLayout(reducedRecoilAndGraph, BoxLayout.PAGE_AXIS));
		reducedRecoilAndGraph.add(new JLabel("Player-reduced recoil offset (degrees) vs Time (seconds)"));
		reducedRecoilAndGraph.add(playerReducedRecoilGraph);
		reducedRecoilAndGraph.setBorder(GuiConstants.blackLine);
		lineGraphsPanel.add(reducedRecoilAndGraph);
		
		AccuracyAnimation rawRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
				convertedSpreadData, convertDegreesToMeters(maxSpread), 
				convertedRawRecoilData, convertDegreesToMeters(maxRawRecoil));
		rawRecoilGif.setBorder(GuiConstants.blackLine);
		new Thread(rawRecoilGif).start();
		
		AccuracyAnimation reducedRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration, 
				convertedSpreadData, convertDegreesToMeters(maxSpread), 
				convertedReducedRecoilData, convertDegreesToMeters(maxReducedRecoil));
		reducedRecoilGif.setBorder(GuiConstants.blackLine);
		new Thread(reducedRecoilGif).start();
		
		toReturn.add(granularDataPanel);
		toReturn.add(lineGraphsPanel);
		toReturn.add(rawRecoilGif);
		toReturn.add(reducedRecoilGif);
		
		/*
			If I ever want to re-add the Spread Curve transformation graphs, this is where I could easily do it.
			
			if (spreadTransformingCurve != null) {
				toReturn.add(spreadTransformingCurve.getGraph());
			}
		*/
		
		return toReturn;
	}
}
