import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;

/**
 * This class runs a simulation of a fixed number of particles randomly distributed with no initial 
 * velocity. It graphs the number of particles per unit of time, and properties of the particles 
 * can be altered to simulate different circumstances (for example their masses, coordinates, 
 * distribution around the frame, and their initial velocities). 
 * 
 * @method run 
 *  Runs simulation by stepping particles. 
 * @method stop 
 * 	Toggles start/stop.  
 * @method clear_trails 
 * 	Clears trails on screen (can slow down sim). 
 * @method randMass 
 * 	Returns random mass to initialize random particle. 
 * @method reset 
 * 	Resets particles and reschedules the simulation. 
 * @method pastel 
 * 	Returns pleasing pastel color.  
 * @method toggle_trails 
 * 	Turns trails on or off in simulation. 
 * @method setupFrame 
 * 	Initializes DisplayFrame's size, location, and adds drawables. 
 * @method calcAng 
 * 	Calculates the angle between two particles. 
 * @method sign 
 *	Randomly returns a -1 or +1.  
 * @method randCoords 
 * 	Returns a double array of random coordinates within the DisplayFrame. 
 * @method move_bodies 
 * 	Steps particles. 
 * @method checkCollision 
 * 	Checks if collision occurred between two particles. 
 * @method setCollision
 * 	Responds to collision (sets new velocities, directions). 
 * @method calculate_accelerations 
 *  Sums all forces and calculates accelerations of particles. 
 * @method grav_force 
 * 	Adds gravitational force between two particles. 
 * @method dist 
 * 	Calculates the distance between two particles. 
 * @method dist 
 * 	Calculates the distance between two points. 
 * @method calcAng 
 * 	Calculates the angle between two particles. 
 * @method sumForces 
 * 	Sums a list of forces in a given direction.
 *  
 * TODO: 1. Tighten up simulation.
 *  
 * @author Andrew M.
 */
public class RandomSim implements Runnable {
	protected static DisplayFrame frame = new DisplayFrame("x", "y", "Frame");
	/**
	 * All the clusters in the simulation.  
	 */
	protected static ArrayList<Particle> bodies = new ArrayList<Particle>();

	protected static double g = 9.803; // gravity constant
	protected static double G = 6.67384E-11; //universal gravitational constant
	final private static boolean DEBUG_MODE = false; //display debug messages
	final private static double SUN_MASS = 1.98892E30; // kg
	final private static double EARTH_MASS = 5.9742E24; // kg
	final private static double EARTH_ORBIT = 1.5E11; // m
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	final private static double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 };
	final private static int[] FRAME_DIMENSIONS = { 600, 700 };
	final private static double multiplier = 50;
	final private static double[] FRAME_MINMAX = { -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT, -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT }; // scale at which the frame starts

	protected static double TIME_STEP = 4000;
	protected static double ptime = 25000; 
	protected static boolean elastic = false;
	protected static boolean stop = false;
	protected static boolean collide_real = true; 

	protected static int bodyNum = 250; //number of particles 

	protected static double vx = -20000;
	protected static double vy = -80000;
	static final long TARGET_FPS = 2000;
	static final long TICK_RATE = 100000;

	static PlotFrame pT = new PlotFrame("#", "t", "Graph"); //number of planets vs. time
	protected static double prevSize; 
	protected int resetCount = 0; 

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Runs the simulation. 
	 */
	public void run() {
		frame.setBackground(Color.BLACK); 

		if (stop) { //no GUI so do not need to change data 
		} 
		else {
			calculate_accelerations(); //move all the bodies 
			move_bodies();
			if(bodies.size() != prevSize) { //for graphing # of particles vs. time 
				prevSize = bodies.size(); 
				pT.append(resetCount, bodies.get(0).time, bodies.size());
				//pT.append(resetCount, bodies.get(0).time, 1/bodies.get(0).time);
				pT.repaint(); 
			}
		}
		frame.repaint();
	}

	/**
	 * Method for button on DisplayFrame: stop/start simulation. 
	 */
	public void stop() {
		stop = !stop;
	}

	/**
	 * Method on bottom of DisplayFrame - clears all particle trails. 
	 */
	public void clear_trails() {
		for (Particle p : bodies)
			p.trail.clear();
	}

	/**
	 * Runs runnable at fixed rate. 
	 */
	static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	static Random random = new Random();
	static RandomSim sim; 

	/**
	 * Returns a random mass. 
	 * 
	 * @return
	 * 	Random mass between 10^24 and 10^34. 
	 */
	public static double randMass(){
		double mass = 1E24*Math.pow(10, random.nextInt(10)); 
		return mass; 
	}

	/**
	 * Clears the frame, removes all the bodies, and resets the particles with random coordinates, 
	 * random masses, and reschedules the executor. 
	 */
	public void reset(){
		frame.clearDrawables(); //clear all old items  
		bodies.clear(); 
		bodies.removeAll(bodies);

		resetCount ++; 
		for (int ii = 0; ii < bodyNum; ii++) {
			double[] coords = randCoords(); //new random coordinates
			bodies.add(new Particle()); 
			bodies.get(ii).init(coords[0], coords[1], 0, 0, 0, 0, randMass(), ptime, 0); //initialize the particle 
			bodies.get(ii).useRiemann = false;
			bodies.get(ii).pixRadius = 3;
			bodies.get(ii).color = pastel();
			bodies.get(ii).radius = 10000; 
			frame.addDrawable(bodies.get(ii));
		}

		executor.scheduleAtFixedRate(sim, 0, TICK_RATE, TimeUnit.NANOSECONDS); //schedule run method 
	}

	public static void main(String[] args) throws InterruptedException {
		sim = new RandomSim(); //create simulation 
		random = new Random();
		sim.setupFrame();
		sim.reset(); //reset all particles 
		pT.setLocation(700, 0);
		pT.setVisible(true);
	}

	/**
	 * Nice color for particles. 
	 * 
	 * @return
	 * 	New color. 
	 */
	private static Color pastel(){
		Random random = new Random(); //random gen for color 
		final float hue = random.nextFloat();
		final float saturation = (random.nextInt(2000) + 2000) / 10000f;
		final float luminance = 0.9f;
		final Color color = Color.getHSBColor(hue, saturation, luminance); //create new color 
		return color; 
	}

	/**
	 * Toggle trails off and on in simulation. 
	 */
	public void toggle_trails(){
		for (int ii = 0; ii < bodies.size(); ii++) {
			bodies.get(ii).trace = !bodies.get(ii).trace; //turn live tracing on or off
			if(!bodies.get(ii).trace)
				frame.removeDrawable(bodies.get(ii).trail); //remove or add trail 
			else 
				frame.addDrawable(bodies.get(ii).trail);
		}
	}

	/**
	 * Sets up frame with proper location, scale, buttons, and size. 
	 */
	public void setupFrame() {
		frame.setLocation(FRAME_LOC[0], FRAME_LOC[1]);
		frame.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]);
		frame.addButton("step", "Step", "Step Simulation", sim); //add controller buttons to the DisplayFrame 
		frame.addButton("stop", "Stop/Start", "Stop or Start Simulation", sim);
		frame.addButton("clear_trails", "Clear Trails", "Clear Trail Points", sim);
		frame.addButton("reset", "Reset", "Reset sim", sim);
		frame.addButton("toggle_trails", "Toggle Trails", "Turn trails on or off.", sim);
		frame.setSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]);
		frame.setVisible(true);
	}

	/**
	 * Calculate the angle between one mass and a point. 
	 * 
	 * @param a
	 * 	Center mass. 
	 * @param x
	 * 	Target x position.
	 * @param y  
	 * 	Target y position. 
	 * @return
	 * 	Angle (in degrees) between the mass and the point. 
	 */
	public static double calcAng(Particle a, double x, double y) {
		double xDiff = x - a.x_pos; // find delta x
		double yDiff = y - a.y_pos; // find delta y
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));

		return angle; // inverse tangent of y/x
	}

	/**
	 * Find random x and y position within range displayed by frame. 
	 * 
	 * @return
	 * 	Random x and y position on frame. 
	 */
	private static double[] randCoords() {
		double randX = random.nextFloat() * FRAME_MINMAX[0] * 3 * sign() / 4; //randomly positive or negative 
		double randY = random.nextFloat() * FRAME_MINMAX[0] * 3 * sign() / 4;
		return new double[] { randX, randY };
	}

	/**
	 * Returns -1 or +1 randomly (use as multiplier). 
	 * 
	 * @return
	 * 	-1 or +1. 
	 */
	private static int sign() {
		if (random.nextBoolean())
			return -1;
		return 1;
	}

	/**
	 * Step simulation. 
	 */
	public void step() {
		stop = false;
		run();
		stop = true;
	}

	/**
	 * Moves all the particles by checking for collisions and then stepping them. 
	 * 
	 * @param frame
	 * 	Frame to move particles on. 
	 */
	private static void move_bodies() {
		for (int ii = 0; ii < bodies.size(); ii++) { 
			for (int jj = 0; jj < bodies.size(); jj++) {
				if (ii == jj)
					continue;
				else if (checkCollision(bodies.get(ii), bodies.get(jj))) { //check if collision has occurred 
					setCollision(bodies.get(ii), bodies.get(jj)); //respond to it
					break; 
				}
				else 
					bodies.get(ii).bump = null; //no recent crash 
			}
		}
		for (int ii = 0; ii < bodies.size(); ii++) {
			bodies.get(ii).Step(frame, true, true); // not 2d simulated, step on frame 
			frame.addDrawable(bodies.get(ii));
		}
	}

	/**
	 * Checks if a collision has occurred between 2 particles. 
	 * 
	 * @param m1
	 * 	Particle 1 to check. 
	 * @param m2
	 * 	Particle 2 to check.  
	 * @return
	 * 	True or false (if collision has/has not happened). 
	 */
	private static boolean checkCollision(Particle m1, Particle m2) {
		if(collide_real){
			double nullZone = (Math.abs(m1.vector(m1.v_x, m1.v_y)*m1.deltaT) + Math.abs(m2.vector(m2.v_x, m2.v_y)*m2.deltaT))*1;
			if(dist(m1, m2) > m1.actual_r + m2.actual_r + nullZone){ //HARDCODED TOLERANCE ZONE 
				return false;
			}
			else if(m1.bump == null || m2.bump == null){ //they have collided recently
				return true;
			}

			else if(m1.bump.equals(m2) || m2.bump.equals(m1))
				return false; //collision has occurred

			return true;
		}
		else {
			DrawingPanel panel = frame.getDrawingPanel(); //gets pixel positions of each particle. 
			int p1cX = panel.xToPix(m1.getX());
			int p1cY = panel.yToPix(m1.getY());
			int p2cX = panel.xToPix(m2.getX());
			int p2cY = panel.yToPix(m2.getY());
			double dist = dist(p1cX, p1cY, p2cX, p2cY);

			if(m1.radius + m2.radius < dist + COLLISION_RAD) //uses pixel overlap 
				return false;
			else if(m2.bump == null || m1.bump == null) //their collision has not happened
				return true;
			else if(m1.bump.equals(m2) || m2.bump.equals(m1))
				return false; //do not count because they have just collided one time step ago

			return true;
		} 
	}

	/**
	 * Respond to collision by changing directions and velocities. 
	 * 
	 * @param m1
	 * 	Particle 1. 
	 * @param m2
	 * 	Particle 2. 
	 * @param frame
	 * 	DisplayFrame used (to delete/add particles for inelastic). 
	 */
	private static void setCollision(Particle m1, Particle m2){
		m1.bump = m2; //set that they have collided with each other
		m2.bump = m1; 

		//get initial velocities 
		double v1ix = m1.v_x; 
		double v2ix = m2.v_x; 
		double v1iy = m1.v_y; 
		double v2iy = m2.v_y; 

		//final velocities
		double v1fx;  
		double v2fx; 
		double v1fy; 
		double v2fy;  

		if(elastic){ //efficiency == 100
			//elastic collision 
			//v1i + v2i = v1f + v2f 
			//mv1 + mv2 = mv1 + mv2
			v1fx = 2*(m1.mass*v1ix + m2.mass*v2ix)/(m1.mass + m2.mass); 
			v1fx -= v1ix; 
			v2fx = 2*(m1.mass*v1ix + m2.mass*v2ix)/(m1.mass + m2.mass); 
			v2fx -= v2ix; 
			v1fy = 2*(m1.mass*v1iy + m2.mass*v2iy)/(m1.mass + m2.mass); 
			v1fy -= v1iy; 
			v2fy = 2*(m1.mass*v1iy + m2.mass*v2iy)/(m1.mass + m2.mass); 
			v2fy -= v2iy; 

			//set their velocities to post-collision state  
			m1.v_x = v1fx;
			m2.v_x = v2fx;
			m1.v_y = v1fy;
			m2.v_y = v2fy;

			if(DEBUG_MODE){
				System.out.println("M1's vx changed from " + v1ix + " to " + m1.v_x);
				System.out.println("M2's vx changed from " + v2ix + " to " + m2.v_x);
			}
		}
		else {
			//inelastic collision
			Particle monster = new Particle(); //they have combined! 
			double mass_sum = m1.mass + m2.mass; 
			double v_f_x = (m1.mass * m1.v_x + m2.mass * m2.v_x)/(mass_sum); //final x velocity 
			double v_f_y = (m1.mass * m1.v_y + m2.mass * m2.v_y)/(mass_sum); //final y velocity 
			monster.init(m1.x_pos, m1.y_pos, v_f_x, v_f_y, 0, 0, mass_sum, TIME_STEP, 0);

			//merge their colors 
			Color newColor = new Color((m1.color.getRed() + m2.color.getRed())/2, 
					(m1.color.getGreen() + m2.color.getGreen())/2, 
					(m1.color.getBlue() + m2.color.getBlue())/2); 
			monster.color = newColor;
			monster.useRiemann = false; 

			//trace if either one of the first 2 were traced 
			if(m1.trace || m2.trace)
				monster.trace = true;
			else 
				monster.trace = false; 

			monster.killCount = m1.killCount + m2.killCount + 1; 
			if(monster.killCount < 6)
				monster.pixRadius = monster.killCount+1;
			else 
				monster.pixRadius = 7; 

			//remove old ones, add new 
			frame.removeDrawable(m1);
			frame.removeDrawable(m2); 
			for (int ii = 0; ii < m1.points.size(); ii ++) {
				monster.trail.addPoint(m1.points.get(ii).x, m1.points.get(ii).y);
			}
			monster.trail.moveToPoint(m2.points.get(0).x, m2.points.get(0).y);
			for (int ii = 0; ii < m2.points.size(); ii ++) {
				monster.trail.addPoint(m2.points.get(ii).x, m2.points.get(ii).y);
			}
			m1.trail.clear(); 
			m2.trail.clear(); 
			m1.points.clear(); 
			m2.points.clear(); 
			bodies.remove(m1); 
			bodies.remove(m2); 
			bodies.add(monster); 
			frame.addDrawable(monster); 
			monster.Step(frame, true, true);
		}
	}

	/**
	 * Calculate and set accelerations of all particles based on gravitational forces with other particles and clusters. 
	 */
	private static void calculate_accelerations() {
		for (int ii = 0; ii < bodies.size(); ii++) { // step through arraylist
			// of masses
			bodies.get(ii).forces.clear(); // clear old forces
			bodies.get(ii).forces.removeAll(bodies.get(ii).forces);

			for (int jj = 0; jj < bodies.size(); jj++) {
				if (ii == jj)
					continue;
				bodies.get(ii).forces.add(grav_force(bodies.get(ii),
						bodies.get(jj)));
			}
			double xSum = sumForces(bodies.get(ii).forces, 0);
			double ySum = sumForces(bodies.get(ii).forces, 1);
			bodies.get(ii).acc_x = xSum / bodies.get(ii).mass; // a = F/m
			bodies.get(ii).acc_y = ySum / bodies.get(ii).mass;

			if (DEBUG_MODE) {
				System.out.println("X Acceleration of " + ii + " is "
						+ bodies.get(ii).acc_x);
				System.out.println("Y Acceleration of " + ii + " is "
						+ bodies.get(ii).acc_y);
			}
		}
	}

	/**
	 * Calculates gravitational force between two masses. 
	 * 
	 * F = G*(m1*m2)/(r^2). 
	 * 
	 * @param m1
	 * 	Mass 1. 
	 * @param m2
	 * 	Mass 2. 
	 * @return
	 * 	New force with direction pointing toward opposite mass and with magnitude using above formula. 
	 */
	private static Force grav_force(Particle m1, Particle m2) {
		double dist = dist(m1, m2);
		double mag = G * m1.mass * m2.mass / Math.pow(dist, 2);

		if (DEBUG_MODE)
			System.out.println("Force added to mass " + m1 + " in magnitude "
					+ mag + " dir: " + calcAng(m1, m2));
		return new Force(mag, calcAng(m1, m2), "Gravitational Force");
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
	static double dist(Particle a, Particle b) {
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2)
				+ Math.pow(a.getY_pos() - b.getY_pos(), 2));
		return dist; // distance formula
	}

	/**
	 * Calculate the distance between two points using distance formula. 
	 * 
	 * @param x1
	 * 	X of P1. 
	 * @param y1
	 * 	Y of P1. 
	 * @param x2
	 * 	X of P2. 
	 * @param y2
	 * 	Y of P2. 
	 * @return
	 * 	Scalar distance. 
	 */
	static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
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
	public static double calcAng(Particle a, Particle b) {
		double xDiff = b.x_pos - a.x_pos; // find delta x
		double yDiff = b.y_pos - a.y_pos; // find delta y
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));
		// angle = Math.round(angle*100)/100;
		return angle; // inverse tangent of y/x
	}

	/**
	 * Sums forces in a direction. 
	 * 
	 * @param f
	 * 	List of forces. 
	 * @param dir
	 * 	Direction (0 or 1 for x and y). 
	 * @return
	 * 	Magnitude of forces in that direction. 
	 */
	static double sumForces(ArrayList<Force> f, double dir) {
		double sum = 0;
		if (dir == 0) {
			for (int ii = 0; ii < f.size(); ii++) {
				sum += f.get(ii).forceX(); // get x components
			}
		} else {
			for (int ii = 0; ii < f.size(); ii++) {
				sum += f.get(ii).forceY(); // get y components
			}
		}
		return sum;
	}
}
