import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.opensourcephysics.controls.AbstractSimulation;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;

/**
 * The ForceSpringApp controls the creation of a spring, oscillation, and management of the masses. 
 * It creates graphs to plot the x and y positions of masses in the spring and the 2D frame containing 
 * the spring's movement. 
 * 
 * The class uses the mouse position to control the spring, finds the proper oscillation given an amplitude 
 * and a frequency, and can perform the slinky drop. 
 * 
 * Bonuses: The graphs visually demonstrate the spring's position using a variety of colors. The mouse can be 
 * used to drag the spring, wind can be added to the simulation, it can oscillate at various frequencies, and 
 * the simulation can create an inclined plane with a large mass to demonstrate the impact of friction on the 
 * spring. It can also play a tone based on the movement of the spring. 
 * 
 * A boing sound is played when the spring changes direction, and the masses are connected by a trail. 
 * During the oscillation, the user can select which spring will be oscillated. 
 * 
 * During oscillation, the user can select a note (or amplitude/frequency) to oscillate at, or it can go through 
 * a scale by oscillating at the frequencies associated with each note. 
 * 
 * @method sumForces
 * 	sumForces takes an ArrayList of forces and calculates the sum of them (scalar sum). To find the angle, 
 * 	the calcAng method can be used.  
 * 
 * @method dist  
 * 	Calculates the distance between two particles (masses in the spring). 
 * 
 * @method calcAng
 * 	Finds the angle between two masses. The first argument is used as the center, and the angle is found with the second argument. 
 * 
 * @method play 
 * 	Plays a given .wav file. 
 * 
 * @author Andrew M. 
 */

public class ForceSpringApp extends AbstractSimulation {
	static DisplayFrame frame = new DisplayFrame("x", "y", "Frame");  //x-y, 2D pos frame
	static PlotFrame plotX = new PlotFrame("t", "X", "Plot X"); //plot x positions
	static PlotFrame plotY = new PlotFrame("t", "Y", "Plot Y"); //plot y positions
	ArrayList<Mass> masses = new ArrayList<Mass>();  //ArrayList of masses that comprise spring
	Trail string = new Trail(); //connect spring masses 
	int springNum = 0; //number of masses in spring
	double restLength = 0; 
	double initX = 0; 
	double mass; //mass (set by user in control) 
	double timeStep = 0.01; //default time step 
	double k; //spring constant
	double largeMass; //large mass on end for bungee
	double amplitude; //height used to drive mass making wave 
	double lowestPoint = 0; //finds lowest point of mass 
	double initLow; 
	double osc_theta; //theta to oscillate at 
	double count = 0; //count of steps (for boing sound)
	double wind_v; //wind velocity (used as a force)
	double wind_d; //wind direction
	double initLength; //initial length 
	double theta_start; //starting theta 
	double magnitude, period; //for oscillation 
	double drive_mass = 0; //mass to oscillate 
	public static double g = 9.803; //gravity constant
	public static double init_deltaT; //time step
	public double alpha = 0.0000; //air resistance constant 
	public String osc_note;  
	public boolean playNote = false; 
	HashMap<Integer, Color> map = new HashMap<Integer, Color>(); //used to set the color of each mass

	boolean oscillation = false; //horizontal oscillation (on or off)
	boolean timeScale = false; //time scaling
	boolean hasGravity = true; //whether there is gravity
	boolean fix_end; //fixed end
	boolean slinky_drop; //if slinky is dropping 
	boolean mouseDrag = false; //mouse dragging
	boolean checkLow = true; 
	boolean plane = false; 
	boolean double_pend; 
	boolean scale; 
	double stepsPerDisplay; 
	double u_k; //friction coefficient
	double plane_theta; 
	double counter = 0; 
	double human_mass; 
	public boolean voice = true; 
	public boolean osc_two = false; 
	Plane inc_plane; //inclined plane
	Mass human = new Mass(0); 
	Capture cap = new Capture(); 

	protected void doStep(){
		counter ++; 
		super.setStepsPerDisplay(3); //increases speed of simulation 
		super.delayTime = 0; //end delay time 
		PointerInfo a = MouseInfo.getPointerInfo(); //get mouse location 
		Point b = a.getLocation();

		if(masses.get(springNum-1).y_pos < lowestPoint && checkLow && !oscillation && !slinky_drop && !mouseDrag && !plane){ //only check lowest point if not in special modes 
			lowestPoint = masses.get(springNum-1).y_pos; //set to new lowest point 
			//System.out.println("New Lowest Point: " + lowestPoint + " Delta Y is " + (lowestPoint-initLow));
		}
		if(count == 0 && mouseDrag){ //wait a second for mouse to get ready 
			try {
				Thread.sleep(1000); //wait for 1 sec if mouse is being used
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			count = 1; 
		}
		/*if(Math.abs(masses.get(springNum-1).v_y) < 1 && !oscillation && !slinky_drop && !mouseDrag && !plane){ //play boing if shifting directions 
			play("boing.wav"); //plays sound (see method below) 
		}*/
		//super.setStepsPerDisplay((int)stepsPerDisplay); //speed up simulation

		if(timeScale){ //check if scaling time 
			if(Math.sqrt(Math.pow((masses.get(springNum-1).v_x), 2) + Math.pow((masses.get(springNum-1).v_y), 2)) < 1.2){ //velocity vector
				timeStep = 0.01-0.005*Math.sqrt(Math.pow((masses.get(springNum-1).v_x), 2) + Math.pow((masses.get(springNum-1).v_y), 2));
				System.out.println("Scaled time step: " + timeStep); //show scaled time step 
			}
			else {
				timeStep = init_deltaT; //set time back to initial time step  
			}
		}
		for (int ii = 0; ii < masses.size(); ii++) { //step through arraylist of masses
			masses.get(ii).deltaT = timeStep; //set the time step (important if scaling time) 
			masses.get(ii).forces.clear(); //clear old forces
			masses.get(ii).forces.removeAll(masses.get(ii).forces); 
			//add force of gravity (weight)
			if(!oscillation && hasGravity) //gravity ignored if oscillating horizontally
				masses.get(ii).forces.add(new Force(masses.get(ii).mass*g, -90, "mg of spring " + ii)); 

			//needed to calculate upward kx and downward kx
			addForces(ii); 
			//sum forces
			double xSum = sumForces(masses.get(ii).forces, 0); 
			if(Math.abs(xSum) < 0.005) //gets rid of imperfections in rounding with trig 
				xSum = 0; 
			double ySum = sumForces(masses.get(ii).forces, 1);
			if(Math.abs(ySum) < 0.005) //gets rid of imperfections in rounding with trig 
				ySum = 0; 
			//divide by mass to get acceleration (Newton's 2nd Law)
			if(oscillation) {
				if(ii != drive_mass && ii != 0) {
					masses.get(ii).acc_x = xSum/masses.get(ii).mass; //a = F/m
					masses.get(ii).acc_y = ySum/masses.get(ii).mass;
					if(fix_end){
						masses.get(springNum-1).acc_x = 0; 
						masses.get(springNum-1).acc_y = 0;
					}
				}
				else {
					if(!osc_two)
						driveMass(drive_mass);
					else {
						driveMass(0); 
						driveMass(springNum-1); 
					}
				}
				if(mouseDrag){
					period = 0.1+(b.getY()/768)*5; //converts mouse position to oscillation speed 
					System.out.println("Mouse Oscillation Control: PERIOD: " + period);
				}
			}
			else {
				//finds if mouse is within important coordinates
				if(b.getX() < 500 && mouseDrag && !oscillation) //sets position of 1st particle based on mouse pos
					masses.get(0).x_pos = ((int) b.getX() - 175)/10;
				if(b.getY() < 500 && mouseDrag && !oscillation)
					masses.get(0).y_pos = -((int) b.getY() - 265)/10; 

				masses.get(ii).acc_x = xSum/masses.get(ii).mass; //set accelerations 
				masses.get(ii).acc_y = ySum/masses.get(ii).mass;
				if(ii == 0 && !mouseDrag && !slinky_drop){
					masses.get(ii).acc_x = 0; //fix first particle 
					masses.get(ii).acc_y = 0; 
				}
				if(mouseDrag){ 
					masses.get(0).acc_x = 0; 
					masses.get(0).acc_y = 0;
					if(fix_end){ //last spring fixed as well 
						masses.get(springNum-1).acc_x = 0; 
						masses.get(springNum-1).acc_y = 0;
					}
				}
			}
		}
		if(human.mass != 0){
			/*human.pixRadius = 5; 
			human.Step(frame, true);
			human.forces.add(new Force(human_mass, -90, "Human's mass")); */
			frame.addDrawable(human); 
		}
		for (int ii = 0; ii < masses.size(); ii++) {
			if(!playNote)
				masses.get(ii).Step(frame, true); //must step each simulated mass (to refresh frame) 
			else 
				masses.get(ii).Step(frame, false); //not 2d simulated 
		}
		if(plane){
			hasGravity = false; //no gravity on spring masses on plane
		}

		string.clear(); 
		for (int ii = 0; ii < masses.size(); ii++) {
			string.addPoint(masses.get(ii).getX_pos(), masses.get(ii).getY_pos()); //connect the masses
		}
		string.setStroke(new BasicStroke(2));
		frame.addDrawable(string); 
		/*for (int ii = 1; ii < masses.size()-1; ii += 2) { //add to X Y graphs 
			plotY.append(ii*2, masses.get(ii).time, masses.get(ii).getY());
			plotX.append(ii*2, masses.get(ii).time, masses.get(ii).getX());
		}*/
		plotY.append(1, masses.get(masses.size()-2).time, masses.get(masses.size()-2).getY());
		plotX.append(1, masses.get(masses.size()-2).time, masses.get(masses.size()-2).getX());
	}

	double scaleCount = 0; //scales appropriately 

	void driveMass(double massNum){
		if(scale){
			scaleCount ++; 
			if(scaleCount%150 == 0){
				scaleCount = 0; 
				if(appThreads.size() > 0)
					appThreads.get(0).interrupt();
				StringBuilder newString = new StringBuilder(); 
				if(osc_note.toUpperCase().contains("G")){
					System.out.println("not g");
					newString.append("A");
					newString.append(osc_note.charAt(1)); 
				}
				else {
					newString.append((char)(osc_note.charAt(0)+1));
					newString.append(osc_note.charAt(1)); 
				}
				osc_note = newString.toString();
				System.out.println("NEW NOTE: " + osc_note);
				Runnable r = new Runnable() { //make the thread 
					public void run() { //only method needed (runs the sound) 
						period = 1/Frequency.getFrequency(Frequency.parseNoteSymbol(osc_note)); 
						Note note = new Note(); 
						while(true){ //continually play sound
							note.play(note.getNumber(osc_note));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
						}
					}
				};
				Thread sound = new Thread(r); 
				sound.start();
				appThreads.add(sound); 
			}
		}
		else if(voice){
			period = 1/cap.voiceFreq; 
			System.out.println("Setting Voice Freq: " + (1/period)); 
		}
		else if(playNote){
			period = 1/Frequency.getFrequency(Frequency.parseNoteSymbol(osc_note));
			amplitude = (initLength/(springNum-1))/(1/period); 
		}
		//find x and y positions for oscillating particle 
		double x_pos = Math.cos(Math.toRadians(osc_theta)) * amplitude * Math.sin(masses.get((int)massNum).time*Math.PI*2/period); //sets X and Y position based on oscillation formula
		double y_pos = Math.sin(Math.toRadians(osc_theta)) * amplitude * Math.sin(masses.get((int)massNum).time*Math.PI*2/period);
		//set the position 
		masses.get((int)massNum).y_pos = y_pos;
		masses.get((int)massNum).x_pos = masses.get((int)drive_mass).orig_x + x_pos; 
	}

	public void initialize(){
		//clear frame 
		frame.clearData();
		frame.clearDrawables();
		masses.removeAll(masses); 
		masses.clear();

		plotY.setLocation(400, 0); //set location of graphs
		plotX.setLocation(800, 0); 
		//all initial values in control 
		springNum = (int) control.getDouble("Number of masses"); //set number of springs
		restLength = control.getDouble("Rest Length")/(springNum-1); //fix rest length
		initX = control.getDouble("Fixed End Location"); //set fixed x coordinate 
		fix_end = control.getBoolean("Fixed End"); //fix the end (boolean) 
		mass = control.getDouble("Mass of Spring"); //set mass 
		timeStep = control.getDouble("Timestep"); //set delta t 
		init_deltaT = timeStep; 
		k = control.getDouble("Spring Constant"); //spring constant  
		largeMass = control.getDouble("Large Mass"); //mass on end 
		oscillation = control.getBoolean("Oscillator"); //oscillation boolean 
		amplitude = control.getDouble("Amplitude"); //amplitude value 
		alpha = control.getDouble("Alpha"); //air resistance constant 
		magnitude = control.getDouble("Magnitude"); //magnitude of wave 
		period = control.getDouble("Period"); //period of oscillation 
		timeScale = control.getBoolean("Time Scaling"); //whether to scale time
		mouseDrag = control.getBoolean("Mouse Drag"); //whether to use mouse to drag spring 
		osc_theta = control.getDouble("Oscillation Theta"); //theta to oscillate mass at 
		slinky_drop = control.getBoolean("Slinky Drop"); //drop slinky 
		g = control.getDouble("G"); //value of gravity 
		initLength = control.getDouble("Initial Length"); //initial length of spring  
		theta_start = control.getDouble("Start Theta"); //theta to hang spring at 
		stepsPerDisplay = control.getDouble("Steps Per Display"); //steps per display (speed of simulation) 
		drive_mass = control.getDouble("Drive Mass"); //mass to oscillate 
		plane = control.getBoolean("Inclined Plane"); 
		u_k = control.getDouble("U K"); //coef of kinetic friction 
		plane_theta = control.getDouble("Plane Theta"); //theta for inclined plane 
		osc_note = control.getString("Oscillation Note");
		playNote = control.getBoolean("Play Note"); 
		inc_plane = new Plane(plane_theta, 20); //make the plane (may not be showed) 
		osc_two = control.getBoolean("Oscillate Two"); 
		//human_mass = control.getDouble("Human Mass"); //mass of human
		double_pend = control.getBoolean("Double"); 
		scale = control.getBoolean("Scale"); 
		voice = control.getBoolean("Voice"); 

		wind_v = control.getDouble("Wind Force"); //wind force and direction 
		wind_d = control.getDouble("Wind Direction"); 
		//set radius of particles, add to the spring array list
		for (int ii = 0; ii < springNum; ii++) {
			masses.add(new Mass(0/*time*/)); 
			masses.get(ii).pixRadius = 3; 
			masses.get(ii).mass = mass; 
		}

		if(oscillation){
			//runs audio in new thread to not interrupt program 
			if(!voice){
				Runnable r = new Runnable() { //make the thread 
					public void run() { //only method needed (runs the sound) 
						period = 1/Frequency.getFrequency(Frequency.parseNoteSymbol(osc_note)); 
						Note note = new Note(); 
						while(true){ //continually play sound
							note.play(note.getNumber(osc_note));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} 
						}
					}
				};
				Thread sound = new Thread(r); 
				sound.start();
				appThreads.add(sound); 
			}
			else {
			}
		}

		if(human_mass != 0){
			System.out.println("Human has initialized");
			//human.init(initLength/2, 5, 0, 0, 0, 0, human_mass, timeStep, alpha);
			//frame.addDrawable(human);
		}

		for (int ii = 0; ii < springNum; ii++) {
			if(ii == 0) { //initialize particles 
				if(plane)
					masses.get(ii).init(initX, 0.5+inc_plane.mag*Math.sin(Math.toRadians(inc_plane.theta)), 0, 0, 0, 0, mass, timeStep, alpha);
				else
					masses.get(ii).init(initX, 0, 0, 0, 0, 0, mass, timeStep, alpha);
			}
			else {
				if(slinky_drop){
					//calculate equilibrium position 
					double equib_pos; 
					equib_pos = -restLength + -g*.5*(largeMass+(springNum-ii-1)*mass)/k; //formula for calculating equilibrium position 
					System.out.println("Val: " + (springNum-ii-1));
					System.out.println("Tot mass below: " + (largeMass+(springNum-ii-1)*mass));
					equib_pos += masses.get(ii-1).equibLength; //add the equilibrium position 
					masses.get(ii).equibLength = equib_pos;
					masses.get(ii).pixRadius = 3; 
					System.out.println("Equib mass " + ii + " : " + equib_pos);
					if(ii == 0){
						masses.get(ii).init(initX, equib_pos, 0, mass, timeStep, alpha);
					}
					else {
						masses.get(ii).init(initX, equib_pos + masses.get(ii-1).equibLength, 0, mass, timeStep, alpha);
					}
				}
				else if (double_pend){
					if(ii < springNum/2){
						double xComp = initX + Math.cos(Math.toRadians(theta_start))*initLength*((ii-1)/((double)springNum-1)); //x component of init position 
						double yComp = Math.sin(Math.toRadians(theta_start))*initLength*((ii-1)/((double)springNum-1)); //y component of init position 
						masses.get(ii).init(xComp, yComp, 0, 0, 0, 0, mass, timeStep, alpha); //initialize them in initial position 
					}
					else {
						double prevY = masses.get(Math.round(springNum/2)-1).y_pos; 
						double xComp = initX + Math.cos(Math.toRadians(theta_start+45))*initLength*((ii-1)/((double)springNum-1)); //x component of init position 
						double yComp = prevY+ Math.sin(Math.toRadians(theta_start+45))*initLength*((ii-1)/((double)springNum-1)); //y component of init position 
						masses.get(ii).init(xComp, yComp, 0, 0, 0, 0, mass, timeStep, alpha); //initialize them in initial position 
					}
				}
				else {
					double xComp = initX + Math.cos(Math.toRadians(theta_start))*initLength*((ii-1)/((double)springNum-1)); //x component of init position 
					double yComp = Math.sin(Math.toRadians(theta_start))*initLength*((ii-1)/((double)springNum-1)); //y component of init position 
					if(plane){ //align the masses on the plane
						masses.get(ii).init(Math.cos(Math.toRadians(inc_plane.theta))*initLength*((ii-1)/((double)springNum-1)), 
								0.5+Math.sin(Math.toRadians(inc_plane.theta))*inc_plane.mag-Math.sin(Math.toRadians(inc_plane.theta))*initLength*((ii-1)/((double)springNum-1)), 0, 0, 0, 0, mass, timeStep, alpha);
					}
					else if (!oscillation){
						masses.get(ii).init(xComp, yComp, 0, 0, 0, 0, mass, timeStep, alpha); //initialize them in initial position 
					}
					else {
						masses.get(ii).init(xComp, 0, 0, 0, 0, 0, mass, timeStep, alpha); //initialize first mass 
					}
				}
			}
			frame.addDrawable(masses.get(ii));
			masses.get(ii).orig_x = masses.get(ii).x_pos; 
		}

		if(largeMass != mass)
			masses.get(masses.size()-1).setRadius(5); //just make it easier to see!
		masses.get(masses.size()-1).color = Color.GRAY.brighter(); 
		frame.setVisible(true); 

		//fill the hash map
		map.put(0, Color.red); 
		map.put(1, Color.green); 
		map.put(2, Color.blue); 
		map.put(3, Color.yellow.darker()); 
		map.put(4, Color.cyan.darker());
		map.put(5, Color.pink.brighter());
		map.put(6, Color.blue);
		map.put(7, Color.orange);
		map.put(8, Color.green.brighter());
		map.put(9, Color.blue.brighter());
		map.put(10, Color.pink.darker());
		for (int ii = 0; ii < masses.size(); ii++) {
			masses.get(ii).color = map.get(ii%map.keySet().toArray().length); //sets color of each mass (coordinate with graph) 
		}
		if(plane)
			inc_plane.draw(frame); //draw plane 
		else {
			frame.clearDrawables(); //or clear, set gravity 
			hasGravity = true; 
		}
		initLow = masses.get(springNum-1).y_pos; 
		masses.get(springNum-1).mass = largeMass;
		if(oscillation)
			frame.setPreferredMinMax(masses.get(0).x_pos-5, masses.get(masses.size()-1).x_pos+5, -10, 10); //center frame around oscillation
	}

	public void reset(){
		//reset all conditions, location of graphs 
		plotY.setLocation(400, 0);
		plotX.setLocation(800, 0);
		control.setValue("Fixed End", true); //fixed end is default 
		control.setValue("Slinky Drop", false); 
		control.setValue("Mouse Drag", false); 
		control.setValue("Time Scaling", false); 
		control.setValue("Oscillator", false); //oscillator is default 
		control.setValue("Inclined Plane", false); 
		//all initial conditions
		control.setValue("Timestep", 0.025); 
		control.setValue("Fixed End Location", 0); 
		control.setValue("Rest Length", 0); 
		control.setValue("Initial Length", 1); 
		control.setValue("Number of masses", 15); 
		control.setValue("Mass of Spring", 1);
		control.setValue("Spring Constant", 350); 
		control.setValue("Large Mass", 1); 
		control.setValue("Amplitude", 3);
		control.setValue("Period", 4) ;
		control.setValue("Magnitude", 1); 
		control.setValue("Oscillation Theta", 0); 
		control.setValue("Alpha", 0.0);
		control.setValue("G", 9.803); 
		control.setValue("Start Theta", -90);
		control.setValue("Steps Per Display", 2); 
		//no wind 
		control.setValue("Wind Force", 0); 
		control.setValue("Wind Direction", 0); 
		control.setValue("Drive Mass", 0);
		//high friction (shows that its present) 
		control.setValue("U K", 0.5);
		control.setValue("Plane Theta", 45);
		control.setValue("Oscillation Note", "C2");
		control.setValue("Play Note", false);
		control.setValue("Oscillate Two", false); 
		control.setValue("Human Mass", 100);
		control.setValue("Double", false); 
		control.setValue("Scale", false); 
		control.setValue("Voice", false);

		frame.clearData();
		frame.clearDrawables();
		masses.removeAll(masses); 
		masses.clear();
	}

	List<Thread> appThreads = new ArrayList<Thread>();//keeps track of running lists

	public static void main(String[] args) {
		SimulationControl control = SimulationControl.createApp(new ForceSpringApp()); //creates simulation implementing this class
		MouseListen listener = new MouseListen();  // Create MouseListener object.
		control.addMouseListener(listener);  //Register MouseListener with the panel.
		plotX.addMouseListener(listener);
		plotX.addMouseListener(listener);
		frame.addMouseListener(listener);
	}

	/**
	 * Sums an ArrayList of forces. This method is used to calculate 
	 * acceleration on a particular mass: each mass has an ArrayList of 
	 * forces such as its weight and spring forces that must be summed 
	 * to determine acceleration (Newton's 2nd Law). 
	 * 
	 * @param f
	 * 	Arraylist of forces. 
	 * @param dir
	 * 	X or Y direction (changes whether to use x or y components of forces). 
	 * @return
	 * 	The net force in a direction (can be +/-). 
	 */
	static double sumForces(ArrayList<Force> f, double dir){
		double sum = 0; 
		if(dir == 0){
			for (int ii = 0; ii < f.size(); ii++) {
				sum += f.get(ii).forceX(); //get x components
			}
		}
		else {
			for (int ii = 0; ii < f.size(); ii++) {
				sum += f.get(ii).forceY(); //get y components
			}
		}
		return sum; 
	}

	/**
	 * Calculate the distance between two masses using distance formula. 
	 * 
	 * @param a
	 * 	Center mass. 
	 * @param b
	 * 	Target mass. 
	 * @return
	 * 	Distance between the two. 
	 */
	double dist(Mass a, Mass b){
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2) + Math.pow(a.getY_pos() - b.getY_pos(), 2)); 
		return dist; //distance formula
	}

	/**
	 * Calculate the angle between two masses in degrees. 
	 * 
	 * @param a
	 * 	Center mass. 
	 * @param b
	 * 	Target mass. 
	 * @return
	 * 	Angle (in degrees) between the two masses. 
	 */
	public static double calcAng(Mass a, Mass b) {
		double xDiff = b.x_pos - a.x_pos; //find delta x
		double yDiff = b.y_pos - a.y_pos; //find delta y
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));
		//angle = Math.round(angle*100)/100; 
		return angle; //inverse tangent of y/x
	}

	/**
	 * Play a sound! 
	 * 
	 * @param filename
	 * 	The name of the sound file. 
	 * 
	 * Credit: http://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java 
	 */
	public static void play(String filename){
		try {
			Clip clip = AudioSystem.getClip(); //make a new clip
			clip.open(AudioSystem.getAudioInputStream(new File(filename))); 
			clip.start(); //play clip 
		}
		catch (Exception exc){
			exc.printStackTrace(System.out);
		}
	}

	/**
	 * Adds all forces on a particular mass - wind, upward kx, downward kx, and the mass. 
	 * It calculates the correct angle and distance for the force. 
	 * 
	 * @param ii
	 * 	The number of the mass used to sum forces. 
	 */
	public void addForces(int ii){
		double upAng, downAng; //angle with mass above, mass below
		double upMag = 0, downMag = 0; //magnitude of force above, below

		//add downward kx
		if(ii != springNum-1){ //the last spring does not have a downward kx
			double dist = dist(masses.get(ii), masses.get(ii+1)); //distance for spring force

			downMag = k*(dist-restLength); //calculate kx
			downAng = calcAng(masses.get(ii), masses.get(ii+1)); //calculate angle between two masses 
			masses.get(ii).forces.add(new Force(downMag, downAng, "DOWN kx between spring " + ii + " and " + (ii+1))); //add as force
		}
		//add upward kx
		if(ii != 0){ //the first spring does not have an upward kx 
			double dist = dist(masses.get(ii), masses.get(ii-1)); //distance for spring force

			upMag = k*(dist-restLength); //calculate kx
			upAng = calcAng(masses.get(ii), masses.get(ii-1)); //calculate angle between two masses
			masses.get(ii).forces.add(new Force(upMag, upAng, "UP kx between spring " + ii + " and " + (ii-1))); //add as force
		}
		masses.get(ii).forces.add(new Force(wind_v, wind_d, "Wind Force")); //add wind force - does not matter if wind is 0 (just 0 force)
		if(plane && ii == springNum-1){
			masses.get(springNum-1).forces.add(new Force(masses.get(springNum-1).mass*g*Math.sin(Math.toRadians(inc_plane.theta)), 
					inc_plane.theta*-1, "Mg sin theta")); 
			double fric_mag = u_k*largeMass*g*Math.cos(Math.toRadians(inc_plane.theta)); 
			if(masses.get(springNum-1).v_x > 0) //friction changes direction based on direction 
				masses.get(springNum-1).forces.add(new Force(fric_mag, (-inc_plane.theta+180), "Friction Force"));
			else 
				masses.get(springNum-1).forces.add(new Force(fric_mag, (-inc_plane.theta), "Friction Force"));
		}
	}
}
