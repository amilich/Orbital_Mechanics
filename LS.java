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
 * This app contains a basic orbital simulation. It has the same scientific functionality of 
 * the other Orbital apps, but its principal functionality is for testing new methods, scenarios, 
 * or in displaying the proofs for Kepler's laws. 
 * 
 * @method run 
 * 	Runs simulation by stepping particles or, if stopped, dealing with controller. 
 * @method stop 
 * 	Toggles start/stop. 
 * @method add_particles 
 * 	Adds all particles to simulation. 
 * @method clear_trails 
 * 	Clears trails on screen (can slow down sim). 
 * @method pastel 
 * 	Returns pleasing pastel color.  
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
 * TODO: See Control.java. 
 * 	1. Log/print data. 
 * 	2. Save/load simulation states. 
 * 	3. Prove Kepler's laws. 
 * 	4. Dynamic View Change [DONE]
 * 
 * @author Andrew M.
 */
public class LS implements Runnable {
	protected static DisplayFrame frame = new DisplayFrame("x", "y", "Frame");

	/**
	 * An ArrayList of all the particles in the simulation. Particles can be added and deleted dynamically. 
	 */
	protected static ArrayList<Particle> bodies = new ArrayList<Particle>();

	protected static double g = 9.803; // gravity constant
	protected static double G = 6.67384E-11; //universal gravitational constant

	final private static boolean DEBUG_MODE = false; // print debug statements. 
	final private static double SUN_MASS = 1.98892E30; // kg
	final private static double EARTH_MASS = 5.9742E24; // kg
	final private static double EARTH_ORBIT = 1.5E11; // m
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	final private static double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 }; //location of DisplayFrame on screen 
	final private static int[] FRAME_DIMENSIONS = { 600, 700 }; //size of DisplayFrame 
	final private static double[] FRAME_MINMAX = { -1.2 * EARTH_ORBIT,
		1.2 * EARTH_ORBIT, -1.2 * EARTH_ORBIT, 1.2 * EARTH_ORBIT }; //min, max of frame 

	protected static double TIME_STEP = 100000; 
	protected static double pTime = 600; 
	protected static double BODY_NUM = 2; //number of bodies 
	protected static boolean elastic = true; //elastic collisions or inelastic collisions 
	protected static boolean stop = true; //simulation running or not 
	protected boolean state_changed = false; 
	protected int view_num = 0; 

	protected static long TICK_RATE = 300000;
	protected static PlotFrame xPos = new PlotFrame("t", "x", "x pos"); 
	protected static PlotFrame yPos = new PlotFrame("y", "y", "y pos"); 

	protected int tick = 0; 

	//TODO: is this necessary? 
	static boolean proving_one = true;
	static boolean proving_two = true;
	static boolean proving_three = true; 

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Runs the simulation. 
	 */
	@Override
	public void run() {
		frame.setBackground(Color.BLACK); //FIX! 

		if (stop) { 
			frame.setTitle("STOPPED"); //say stopped at top 
		} 
		else {
			//running simulation 
			frame.setTitle("RUNNING");

			calculate_accelerations();
			move_bodies();

			tick++; 
			if(tick % 50 == 0){
				for(int ii = 0; ii < bodies.size(); ii ++){
					xPos.append(ii, bodies.get(ii).time, bodies.get(ii).x_pos);
					yPos.append(ii, bodies.get(ii).time, bodies.get(ii).x_pos);
				}
				xPos.repaint(); 
				yPos.repaint(); 
				tick = 0; 
			}
		}
		frame.repaint();
	}

	/**
	 * Method for button on DisplayFrame: stop/start simulation. 
	 */
	public void stop() {
		state_changed = true; //for controller 
		stop = !stop;
	}

	/**
	 * Add particles to screen.  
	 */
	public void add_particles() {
		frame.clearDrawables(); //clear old particles 
		for (Particle p : bodies) {
			p.Step(frame, true, false); //step them once (but do not move - 3rd false means don't move them) 
			p.setXY(p.x_pos, p.y_pos); //set XY pos 
			frame.addDrawable(p); //add to screen 
		}
		frame.repaint();
	}

	/**
	 * Method on bottom of DisplayFrame - clears all particle trails. 
	 */
	public void clear_trails() {
		for (int ii = 0; ii < bodies.size(); ii++)
			bodies.get(ii).trail.clear();
	}

	/**
	 * If user has changed time step, need to reset executor. 
	 */
	public void reset_clock() {
		if (DEBUG_MODE)
			System.out.println("Resetting Scheduler to " + TICK_RATE);
		executor.shutdown(); //end current thread 
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this, 0, TICK_RATE, TimeUnit.NANOSECONDS); //schedule new one at new tick rate 
		for (Particle p : bodies) { //add old particles to new runnable 
			frame.addDrawable(p);
			frame.addDrawable(p.trail);
		} 
		add_particles();
		frame.repaint();
	}

	/**
	 * Manages running simulator at a constant rate. 
	 */
	static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	/**
	 * Nice color for particles. 
	 * 
	 * @return
	 * 	New color. 
	 */
	private static Color pastel(){
		Random random = new Random(); //random gen 
		final float hue = random.nextFloat();
		final float saturation = (random.nextInt(2000) + 2000) / 10000f;
		final float luminance = 0.9f;
		final Color color = Color.getHSBColor(hue, saturation, luminance);
		return color; 
	}

	/**
	 * Turns trails off and on in simulation. 
	 */
	public void toggle_trails(){
		for(Particle p : bodies){
			p.trace = !p.trace; 
			if(!p.trace)
				frame.removeDrawable(p.trail);
			else 
				frame.addDrawable(p.trail);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		LS orbit = new LS(); //create simulation object 

		//initialize DisplayFrame
		frame.setLocation(FRAME_LOC[0], FRAME_LOC[1]);
		frame.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]);
		frame.addButton("stop", "Stop/Start", "Stop or Start Simulation", orbit);
		frame.addButton("clear_trails", "Clear Trails", "Clear Trail Points", orbit);
		frame.addButton("toggle_trails", "Toggle Trails", "Turn trails on or off.", orbit);
		frame.setSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]);

		//initialize the particles 
		for (int ii = 0; ii < BODY_NUM; ii++) {
			bodies.add(new Particle());
			bodies.get(ii).useRiemann = false;
			bodies.get(ii).trace = true; 
			bodies.get(ii).init(ii*EARTH_ORBIT, 0, 0, 0, 0, 0, 0, pTime, 0); 
			bodies.get(ii).pixRadius = 10;
			bodies.get(ii).color = pastel();
			frame.addDrawable(bodies.get(ii));
		}

		//set in sun/earth scenario 
		bodies.get(0).mass = SUN_MASS;
		bodies.get(0).real_name = "Sun";
		bodies.get(1).mass = EARTH_MASS;
		bodies.get(1).real_name = "Earth";
		bodies.get(1).v_y = -EARTH_TANGENTIAL_VELOCITY;
		bodies.get(1).setX_pos(EARTH_ORBIT);
		bodies.get(1).pixRadius = 6; 
		
		bodies.add(new Particle()); 
		bodies.get(2).init(-1.5E11, 0, 
				0, 30000, 0, 0, 1E4, pTime, 0);
		bodies.get(2).pixRadius = 4; 
		bodies.get(2).color = pastel();
		bodies.get(2).trace = true; 
		frame.addDrawable(bodies.get(2));
		
		bodies.add(new Particle()); 
		double r = EARTH_ORBIT*Math.pow(bodies.get(1).mass/(3*bodies.get(0).mass), (double)1/(double)3);
		bodies.get(3).init(EARTH_ORBIT + r, 0, 0, -EARTH_TANGENTIAL_VELOCITY, 0, 0, 1E4, pTime, 0);
		bodies.get(3).pixRadius = 4; 
		bodies.get(3).color = pastel();
		bodies.get(3).trace = true; 
		frame.addDrawable(bodies.get(3));
		
		bodies.add(new Particle()); 
		bodies.get(4).init(EARTH_ORBIT - r, 0, 0, -EARTH_TANGENTIAL_VELOCITY, 0, 0, 1E4, pTime, 0);
		bodies.get(4).pixRadius = 4; 
		bodies.get(4).color = pastel();
		bodies.get(4).trace = true; 
		frame.addDrawable(bodies.get(4));
		
		double x = EARTH_ORBIT/2; 
		double y = x*Math.sqrt(3); 
		System.out.println("X: " + x + " Y: " + y);
		bodies.add(new Particle()); 
		bodies.get(5).init(x, y, EARTH_TANGENTIAL_VELOCITY*Math.cos(Math.toRadians(30)), -EARTH_TANGENTIAL_VELOCITY*Math.sin(Math.toRadians(30)), 0, 0, 1E4, pTime, 0);
		bodies.get(5).pixRadius = 4; 
		bodies.get(5).color = pastel();
		bodies.get(5).trace = true; 
		frame.addDrawable(bodies.get(5));
		
		bodies.add(new Particle()); 
		bodies.get(6).init(x, -y, -EARTH_TANGENTIAL_VELOCITY*Math.cos(Math.toRadians(30)), -EARTH_TANGENTIAL_VELOCITY*Math.sin(Math.toRadians(30)), 0, 0, 1E4, pTime, 0);
		bodies.get(6).pixRadius = 4; 
		bodies.get(6).color = pastel();
		bodies.get(6).trace = true; 
		frame.addDrawable(bodies.get(6));

		frame.setVisible(true);
		xPos.setLocation(600, 0); //set location of graphs
		yPos.setLocation(900, 0); 
		xPos.setVisible(true);
		yPos.setVisible(true);

		Thread.sleep(500);
		executor.scheduleAtFixedRate(orbit, 0, TICK_RATE, TimeUnit.NANOSECONDS);
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
				else if (checkCollision(bodies.get(ii), bodies.get(jj))){ //check if collision has occurred 
					setCollision(bodies.get(ii), bodies.get(jj)); //respond to it
					if(!elastic)
						break; 
				}
				else 
					bodies.get(ii).bump = null; //no recent crash 
			}
		}
		for (int ii = 0; ii < bodies.size(); ii++) {
			if(ii == 0 && (proving_one || proving_two || proving_three)) 
				bodies.get(ii).Step(frame, true, false); // not 2d simulated, step on frame 
			else 
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
		DrawingPanel panel = frame.getDrawingPanel();
		int p1cX = panel.xToPix(m1.getX()); //get pixel positions 
		int p1cY = panel.yToPix(m1.getY());
		int p2cX = panel.xToPix(m2.getX());
		int p2cY = panel.yToPix(m2.getY());
		double dist = dist(p1cX, p1cY, p2cX, p2cY);

		if (m1.radius + m2.radius < dist + COLLISION_RAD)
			return false;
		else if (m1.bump == null || m2.bump == null) // their collision has not occurred
			return true;
		else if (m2.bump.equals(m1) || m1.bump.equals(m2))
			return false; //just happened instant ago

		return true;
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
	private static void setCollision(Particle m1, Particle m2) {
		m1.bump = m2;
		m2.bump = m1;

		double v1ix = m1.v_x;
		double v2ix = m2.v_x;
		double v1iy = m1.v_y;
		double v2iy = m2.v_y;

		double v1fx;
		double v2fx;
		double v1fy;
		double v2fy;

		if (elastic) { // efficiency == 100
			// elastic collision
			// v1i + v2i = v1f + v2f
			// mv1 + mv2 = mv1 + mv2
			v1fx = 2 * (m1.mass * v1ix + m2.mass * v2ix) / (m1.mass + m2.mass);
			v1fx -= v1ix;
			v2fx = 2 * (m1.mass * v1ix + m2.mass * v2ix) / (m1.mass + m2.mass);
			v2fx -= v2ix;
			v1fy = 2 * (m1.mass * v1iy + m2.mass * v2iy) / (m1.mass + m2.mass);
			v1fy -= v1iy;
			v2fy = 2 * (m1.mass * v1iy + m2.mass * v2iy) / (m1.mass + m2.mass);
			v2fy -= v2iy;

			m1.v_x = v1fx;
			m2.v_x = v2fx;
			m1.v_y = v1fy;
			m2.v_y = v2fy;

			if (DEBUG_MODE) {
				System.out.println("M1's vx changed from " + v1ix + " to "
						+ m1.v_x);
				System.out.println("M2's vx changed from " + v2ix + " to "
						+ m2.v_x);
			}
			// control.popup("Collision!", "Boom!");
		} else {
			// inelastic collision
			Particle monster = new Particle();
			double v_final = (m1.mass * m1.vector(m1.v_x, m1.v_y) + m2.mass
					* m2.vector(m1.v_x, m1.v_y))
					/ (m1.mass + m2.mass);
			monster.init(m1.x_pos, m1.y_pos, v_final, calcAng(m1, m2),
					pTime, 0);
			System.out.println("MONSTER: " + v_final + " ANG: "
					+ calcAng(m1, m2));
			Color newColor = new Color(
					(m1.color.getRed() + m2.color.getRed()) / 2,
					(m1.color.getGreen() + m2.color.getGreen()) / 2,
					(m1.color.getBlue() + m2.color.getBlue()) / 2);
			monster.color = newColor;
			monster.useRiemann = false;
			monster.pixRadius = 10;
			monster.mass = m1.mass + m2.mass;

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
			for (Force force : f) {
				sum += force.forceX(); // get x components
			}
		} else {
			for (Force force : f) {
				sum += force.forceY(); // get y components
			}
		}
		return sum;
	}
}
