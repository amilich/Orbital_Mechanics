import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.frames.DisplayFrame;

/**
 * Orbital_AM contains the main user simulation. Functionally, it is extremely similar to Basic_Sim, 
 * however its properties are set by the GUI controller. The controller adds significant functionality 
 * by dynamically adding or removing masses, changing colors, or changing properties of the masses 
 * (velocity, acceleration, coordinates, mass, and the name of each particle). 
 * 
 * The app is focused on an ArrayList of particles. Each particle interacts with all other particles in 
 * the simulation by exerting a gravitational force (Fg = G*m1*m2 / r^2. This governs the movement of 
 * all the particles. Depending on the settings chosen by the user, the speed of the simulation can be 
 * changed (run by the Executor). Particles can also collide (elastically or inelastically). 
 * 
 * Because trails can reduce simulation speed, the user can clear and toggle trails for the bodies in 
 * the simulation. 
 * 
 * Because OSP's control window is extremely limited in functionality, this simulation uses only OSP's 
 * DisplayFrame for graphics (it provides an excellent graphing frame to display the particles). Instead 
 * of running an OSP AbstractSimulation, a Java Executor is used to choose a simulation speed. 
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
 * TODO: 
 * 	1. Log/print data. [DONE] 
 * 	2. Save/load simulation states. [DONE] 
 * 	3. Prove Kepler's laws [DONE - Basic_Sim.java]. 
 * 
 * @author Andrew M.
 */
public class Orbital_AM implements Runnable {
	protected DisplayFrame frame = new DisplayFrame("x", "y", "Frame");
	JFrame info = new JFrame("Info");

	/**
	 * An ArrayList of all the particles in the simulation. Particles can be added and deleted dynamically. 
	 */
	protected ArrayList<Particle> bodies = new ArrayList<Particle>();
	protected static ParticleController control;

	protected double g = 9.803; // gravity constant
	protected double G = 6.67384E-11; //universal gravitational constant

	private boolean DEBUG_MODE = false; //print debug statements. 
	final private static double SUN_MASS = 1.98892E30; //kg
	final private static double EARTH_MASS = 5.9742E24; //kg
	final private static double EARTH_ORBIT = 1.5E11; //m
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	final private double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 }; //location of DisplayFrame on screen 
	final private static int[] FRAME_DIMENSIONS = { 600, 700 }; //size of DisplayFrame 
	final private static double[] FRAME_MINMAX = { -1.2 * EARTH_ORBIT,
		1.2 * EARTH_ORBIT, -1.2 * EARTH_ORBIT, 1.2 * EARTH_ORBIT }; //minmax of frame 

	protected double TIME_STEP = 10000; 
	protected double BODY_NUM = 2; //number of bodies 
	protected static boolean elastic = true; //elastic collisions or inelastic collisions 
	protected boolean collide_real = true; //real collisions (distance) or fake (pixels) 
	protected boolean collide = true; 
	protected boolean stop = true; //simulation running or not 
	protected boolean state_changed = false; 
	protected int view_num = 0; //the view chosen by the user 
	long TICK_RATE = 1000000;

	boolean pone = false; 
	boolean ptwo = false; 
	boolean pthree = false; 
	LawOne one;
	JLabel lab = new JLabel(); 

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		//frame.setBackground(Color.BLACK); //TODO: FIX!!!

		if (stop) { 
			frame.setTitle("STOPPED"); //say stopped at top 
			if (state_changed) {
				state_changed = false;
				control.refresh(this, false);
				control.setEditable(); //allow user to edit Particle properties 
			}
			if (control.DATA_CHANGE) { //if user has entered data 
				if (control.T_CHANGE) { //only if time data has been changed - needs to reset runnable 
					if (control.DEBUG_MODE) 
						System.out.println("Time changed to " + TIME_STEP + " executor should reset.");
					if (control.TIME_STEP > 0) { //cannot set time step to 0
						this.TIME_STEP = control.TIME_STEP;
						this.TICK_RATE = (long) this.TIME_STEP;
					}
					reset_clock(this); //resets executor 
					control.T_CHANGE = false;
				}
				if (control.G_CHANGE) { //changed gravitational constant 
					this.G = control.G;
					if (DEBUG_MODE) 
						System.out.println("Real G changed to " + G);
					control.G_CHANGE = false;
				}
				if(control.load){
					control.load = false; 
					frame.clearDrawables();
					bodies.clear(); 
					this.load(control.loaded); //loads the new particles into the simulation

					for (Particle p : bodies) {
						if(DEBUG_MODE)
							System.out.println(p.toString());
						p.color = pastel(); 
						p.radius = 10; 
						p.pixRadius = 10; 
						System.out.println("setting radius");
						//p.pixRadius = 10; 
					}
				}
				else { 
					//bodies.clear(); //get rid of these bodies, get updated ones from the controller 
					bodies = (ArrayList<Particle>) control.particles.clone();
					control.DATA_CHANGE = false; 
					control.addMass = false; 
				}
				control.DATA_CHANGE = false; //no new data now 
				add_particles(); //add particles to screen 
				control.refresh(this, false); 
			}
			control.canRead = true;
		} else {
			//running simulation 
			control.setUnEditable(); //no edits allowed 
			frame.setTitle("RUNNING: View #" + view_num);
			control.DATA_CHANGE = false;
			control.canRead = false;
			calculate_accelerations();
			move_bodies(frame);
			if(control.prove_one){
				control.prove_one = false; 
				this.pone = !this.pone; 
				one = new LawOne("x", "y", "z", bodies);
				one.setVisible(false);
				if(DEBUG_MODE)
					System.out.println("law one");
			}

			if(this.pone)
				one.Prove(bodies, frame);

			lab.setText("Time: " + bodies.get(0).cTime + " seconds");
			control.set(); 
			//info.setSize(new Dimension(100, 50));

			//one.Prove(bodies, frame);

			if(view_num != 0)
				set_cam(); 
		}
		frame.repaint();
	}

	@SuppressWarnings("unchecked")
	public void load(ArrayList<Particle> loaded){
		System.out.println("loading now");
		bodies = (ArrayList<Particle>) control.loaded.clone(); 
		for (Particle p : bodies) {
			frame.addDrawable(p); 
		}
		//System.out.println("resetting");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 

		control.dispose(); 
		control = new ParticleController(this.bodies);
		control.init();
		control.setSize(FRAME_DIMENSIONS[0] + 100, FRAME_DIMENSIONS[1]);
		control.dimensions = FRAME_DIMENSIONS;
		control.setLocation(650, 0); 
		control.setVisible(true);
		control.refresh(this, true);
		System.out.println("loaded");
	}

	/**
	 * Sets new view on frame. 
	 */
	public void set_view(){
		if(view_num < bodies.size()){
			view_num ++; 
		}
		else {
			view_num = 0; 
		}
	}

	/**
	 * Sets camera to right position. 
	 */
	public void set_cam() {
		if(view_num == 0){
			frame.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]);
		}
		else {
			frame.setPreferredMinMax(bodies.get(view_num-1).x_pos - EARTH_ORBIT*2, 
					bodies.get(view_num-1).x_pos + EARTH_ORBIT*2, 
					bodies.get(view_num-1).y_pos - EARTH_ORBIT*2, 
					bodies.get(view_num-1).y_pos + EARTH_ORBIT*2);
		}
	}


	/**
	 * Method for button on DisplayFrame: stop/start simulation. 
	 */
	public void stop() {
		state_changed = true; //for controller 
		stop = !stop;
	}

	public void reset_sim(){
		if(stop){
			String path = System.getProperty("user.home") + "/reset.orb"; 
			//String path = "/Users/student/Desktop/reset.orb"; 
			File f = new File(path);
			if(f.exists()){
				control.load(path);
			}
			else {
				//just go to earth/sun system 
				bodies.clear(); 
				frame.clearDrawables();
				for (int ii = 0; ii < BODY_NUM; ii++) {
					bodies.add(new Particle());
					bodies.get(ii).useRiemann = false;
					bodies.get(ii).init(ii*EARTH_ORBIT, 0, 0, 0, 0, 0, 0, 2000, 0);
					bodies.get(ii).pixRadius = 10;
					bodies.get(ii).color = pastel();
					bodies.get(ii).cTime = 0; 
					bodies.get(ii).time = 0; 
					frame.addDrawable(bodies.get(ii));
				}

				//set in sun/earth scenario 
				bodies.get(0).mass = SUN_MASS;
				bodies.get(0).actual_r = 695000; 
				bodies.get(0).real_name = "Sun";
				bodies.get(1).actual_r = 6371; 
				bodies.get(1).mass = EARTH_MASS;
				bodies.get(1).real_name = "Earth";
				bodies.get(1).v_y = -EARTH_TANGENTIAL_VELOCITY;
				//control.refresh(this, false); 
				control.particles = this.bodies; 
			}
			control.refresh(this, true);
			control.set(); 
		}
		lab.setText("Time: 0.00 seconds");
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
	public void reset_clock(Orbital_AM orb) {
		if (DEBUG_MODE)
			System.out.println("Resetting Scheduler to " + orb.TICK_RATE);
		//orb.executor.shutdown(); //end current thread 
		//orb.executor = Executors.newScheduledThreadPool(1);
		//orb.executor.scheduleAtFixedRate(this, 0, orb.TICK_RATE, TimeUnit.NANOSECONDS); //schedule new one at new tick rate 
		for (Particle p : this.bodies) { //add old particles to new runnable 
			//System.out.println(p.name);
			p.deltaT = this.TICK_RATE; 
			this.frame.addDrawable(p);
			this.frame.addDrawable(p.trail);
		} 
		orb.add_particles();
		orb.frame.repaint();
	}

	/**
	 * Manages running simulator at a constant rate. 
	 */
	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

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

	public static void main(String[] args) throws InterruptedException {
		System.out.println(System.getProperty("user.home"));
		Orbital_AM orbit = new Orbital_AM(); //create simulation object 

		//initialize DisplayFrame
		orbit.frame.setLocation(FRAME_LOC[0], FRAME_LOC[1]);
		orbit.frame.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]);
		orbit.frame.addButton("stop", "Stop/Start", "Stop or Start Simulation", orbit);
		orbit.frame.addButton("clear_trails", "Clear Trails", "Clear Trail Points", orbit);
		orbit.frame.addButton("set_view", "Change View", "Change Viewpoint", orbit);
		orbit.frame.addButton("toggle_trails", "Toggle Trails", "Turn trails on or off.", orbit);
		orbit.frame.addButton("reset_sim", "Reset", "Reset simulation", orbit);
		orbit.frame.setSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]);

		//initialize the particles 
		for (int ii = 0; ii < orbit.BODY_NUM; ii++) {
			orbit.bodies.add(new Particle());
			orbit.bodies.get(ii).useRiemann = false;
			orbit.bodies.get(ii).init(ii*EARTH_ORBIT, 0, 0, 0, 0, 0, 0, 2000, 0);
			orbit.bodies.get(ii).pixRadius = 10;
			orbit.bodies.get(ii).color = pastel();
			orbit.frame.addDrawable(orbit.bodies.get(ii));
		}

		//set in sun/earth scenario 
		orbit.bodies.get(0).mass = SUN_MASS;
		orbit.bodies.get(0).actual_r = 695000; 
		orbit.bodies.get(0).real_name = "Sun";
		orbit.bodies.get(1).actual_r = 6371; 
		orbit.bodies.get(1).mass = EARTH_MASS;
		orbit.bodies.get(1).real_name = "Earth";
		orbit.bodies.get(1).v_y = -EARTH_TANGENTIAL_VELOCITY;

		//initialize controller 
		control = new ParticleController(orbit.bodies);
		control.init();
		control.setSize(FRAME_DIMENSIONS[0] + 100, FRAME_DIMENSIONS[1]);
		control.dimensions = FRAME_DIMENSIONS;
		control.setLocation(650, 0);
		control.popup("Welcome to Orbital!", "Welcome");
		control.setVisible(true);
		orbit.frame.setVisible(true);

		Thread.sleep(500);
		orbit.executor.scheduleAtFixedRate(orbit, 0, orbit.TICK_RATE, TimeUnit.NANOSECONDS);
		control.refresh(orbit, false);

		orbit.info.getContentPane().add(orbit.lab, BorderLayout.CENTER);
		orbit.info.setVisible(true);
		orbit.info.setLocation(605,0);
		orbit.info.setSize(200, 80); 

		@SuppressWarnings("unused")
		Thread one = new Thread() {
			public void run() {
				play("sail.wav"); 
			}  
		};
		//one.start();
	}

	/**
	 * Moves all the particles by checking for collisions and then stepping them. 
	 * 
	 * @param frame
	 * 	Frame to move particles on. 
	 */
	private void move_bodies(DisplayFrame frame) {
		if(collide) {
			for (int ii = 0; ii < this.bodies.size(); ii++) { 
				for (int jj = 0; jj < this.bodies.size(); jj++) {
					if (ii == jj) continue;
					else if (checkCollision(this.bodies.get(ii), this.bodies.get(jj), frame)){ //check if collision has occurred 
						setCollision(this.bodies.get(ii), this.bodies.get(jj), frame, this); //respond to it
						if(!elastic){
							if(DEBUG_MODE)
								System.out.println("BREAKING FOR INELASTIC");
							break; //extremely important: inelastic collisions change the size of ArrayLists, so must break
						}
					}
					else {
						this.bodies.get(ii).bump = null; //no recent crash
					}
				}
			}
		}
		for (int ii = 0; ii < this.bodies.size(); ii++) {
			this.bodies.get(ii).Step(frame, true, true); // not 2d simulated, step on frame 
			this.frame.addDrawable(this.bodies.get(ii));
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
	 * 	True or false (if collision has/has no)t happened). 
	 */
	private boolean checkCollision(Particle m1, Particle m2, DisplayFrame frame) {
		if(collide_real){
			double nullZone = (Math.abs(m1.vector(m1.v_x, m1.v_y)*m1.deltaT) + Math.abs(m2.vector(m2.v_x, m2.v_y)*m2.deltaT))*1;
			if(dist(m1, m2) > m1.actual_r + m2.actual_r + nullZone){ //HARDCODED TOLERANCE ZONE 
				return false;
			}
			else if(m1.bump == null || m2.bump == null){ //they have collided recently
				if(DEBUG_MODE)
					System.out.println("A real collision has occurred!"); //pretty rare... 
				return true;
			}

			else if(m1.bump.equals(m2) || m2.bump.equals(m1))
				return false; //collision has occurred

			if(DEBUG_MODE)
				System.out.println("A real collision has occurred!"); //pretty rare... 
			return true;
		}
		else {
			DrawingPanel panel = frame.getDrawingPanel(); //gets pixel positions of each particle. 
			double dist = dist(panel.xToPix(m1.getX()), panel.yToPix(m1.getY()), panel.xToPix(m2.getX()), panel.yToPix(m2.getY()));

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
	private void setCollision(Particle m1, Particle m2, DisplayFrame frame, Orbital_AM orbit) {
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

		if (elastic) { // efficiency == 100
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

			/*collide = false; 
			for (int ii = 0; ii < 15; ii++) {
				System.out.println("moving");
				calculate_accelerations(); 
				move_bodies(frame); 
			}
			collide = true;*/ 

			if(DEBUG_MODE){
				System.out.println("M1's vx changed from " + v1ix + " to " + m1.v_x);
				System.out.println("M2's vx changed from " + v2ix + " to " + m2.v_x);
			}
		} 
		else {
			// inelastic collision
			Particle monster = new Particle();
			double v_final = (m1.mass*m1.vector(m1.v_x, m1.v_y) + m2.mass*m2.vector(m1.v_x, m1.v_y))/(m1.mass + m2.mass);
			monster.init(m1.x_pos, m1.y_pos, v_final, calcAng(m1, m2), orbit.TIME_STEP, 0);

			Color newColor = new Color(
					(m1.color.getRed() + m2.color.getRed())/2,
					(m1.color.getGreen() + m2.color.getGreen())/2,
					(m1.color.getBlue() + m2.color.getBlue())/2);
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
			this.bodies.remove(m1); 
			this.bodies.remove(m2); 
			this.bodies.add(monster); 
			frame.addDrawable(monster); 
			monster.Step(frame, true, true);
		}
	}

	/**
	 * Calculate and set accelerations of all particles based on gravitational forces with other particles and clusters. 
	 */
	private void calculate_accelerations() {
		for (int ii = 0; ii < this.bodies.size(); ii++) { // step through arraylist
			// of masses
			this.bodies.get(ii).forces.clear(); // clear old forces
			this.bodies.get(ii).forces.removeAll(this.bodies.get(ii).forces);

			for (int jj = 0; jj < this.bodies.size(); jj++) {
				if (ii == jj)
					continue;
				this.bodies.get(ii).forces.add(grav_force(this.bodies.get(ii), this.bodies.get(jj), this));
				this.bodies.get(ii).forces.add(electric_force(this.bodies.get(ii), this.bodies.get(jj), this));
			}
			double xSum = sumForces(this.bodies.get(ii).forces, 0);
			double ySum = sumForces(this.bodies.get(ii).forces, 1);
			this.bodies.get(ii).acc_x = xSum / this.bodies.get(ii).mass; // a = F/m
			this.bodies.get(ii).acc_y = ySum / this.bodies.get(ii).mass;

			if (DEBUG_MODE) {
				System.out.println("X Acceleration of " + ii + " is "
						+ this.bodies.get(ii).acc_x);
				System.out.println("Y Acceleration of " + ii + " is "
						+ this.bodies.get(ii).acc_y);
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
	private Force grav_force(Particle m1, Particle m2, Orbital_AM orbit) {
		double dist = dist(m1, m2);
		double mag = orbit.G * m1.mass * m2.mass / Math.pow(dist, 2);

		if (DEBUG_MODE)
			System.out.println("Force added to mass " + m1 + " in magnitude " + mag + " dir: " + calcAng(m1, m2) + " bc dist is " + dist);
		return new Force(mag, calcAng(m1, m2), "Gravitational Force");
	}

	private Force electric_force(Particle m1, Particle m2, Orbital_AM orbit) {
		double dist = dist(m1, m2);
		double k = 8.9875517873681764E9; 
		double mag = k * m1.charge * m2.charge / Math.pow(dist, 2);

		if (DEBUG_MODE)
			System.out.println("E force added to mass " + m1 + " in magnitude " + mag + " dir: " + calcAng(m1, m2) + " bc dist is " + dist);
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
	double dist(Particle a, Particle b) {
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
	double dist(double x1, double y1, double x2, double y2) {
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
	double sumForces(ArrayList<Force> f, double dir) {
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
}
