import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opensourcephysics.frames.DisplayFrame;

/**
 * Calculates Swarczchild radius. 
 * 
 * @method run 
 * 	Runs simulation by stepping particles or, if stopped, dealing with controller. 
 * @method stop 
 * 	Toggles start/stop.  
 * @method clear_trails 
 * 	Clears trails on screen (can slow down sim). 
 * @method set_view 
 * 	Sets the view of the simulation camera. 
 * @method set_camera 
 * 	Actually moves the camera onto the selected mass. 
 * @method calcAng 
 * 	Calculates the angle between a particle and a point. 
 * @method toggle_trails 
 * 	Turns trails on or off in simulation. 
 * @method setupFrame 
 * 	Initializes DisplayFrame's size, location, and adds drawables. 
 * @method reset 
 * 	Resets clusters and reschedules the simulation. 
 * @method step 
 * 	Steps simulation. 
 * @method initClusters 
 * 	Initializes the clusters, adds particles, clears old data. 
 * @method sign 
 *	Randomly returns a -1 or +1.  
 * @method randCoords 
 * 	Returns a double array of random coordinates within the DisplayFrame. 
 * 
 * TODO: 
 * 	1. Tighten up simulation. 
 * 	2. Add GUI.[DONE]
 * 
 * @author Andrew M.
 */
public class Plate_Calc {
	protected static DisplayFrame frame = new DisplayFrame("x", "y", "Frame");
	/**
	 * All the clusters in the simulation.  
	 */
	protected static Cluster main; 
	protected static double g = 9.803; // gravity constant
	protected static double G = 6.67384E-11; //universal gravitational constant
	final private static boolean DEBUG_MODE = false; //display debug messages
	final private static double SUN_MASS = 1.98892E30; // kg
	final private static double EARTH_MASS = 5.9742E24; // kg
	final private static double EARTH_ORBIT = 1.5E11; // m
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	final private static double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 };
	final private static int[] FRAME_DIMENSIONS = { 700, 700 };
	final private static double multiplier = 5;
	final private static double[] FRAME_MINMAX = { -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT, -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT }; // scale at which the frame starts

	protected double radius = 20 * EARTH_ORBIT; //radius of each cluster 
	protected double TIME_STEP = 100;
	protected int particle_num = 1; //number of particles in each cluster 
	long TICK_RATE = 1000000;
	protected static double p_time = 100; 
	protected static boolean elastic = true;
	protected static boolean stop = true; 
	protected double center_mass = 0; 
	protected int view_num = 0; 
	ArrayList<Cluster> clusters = new ArrayList<Cluster>(); 

	static final long TARGET_FPS = 2000;

	public void test(){
		this.initClusters(false, (long)TIME_STEP);
		double rad = 1E10;
		boolean found = false;
		main.add(new Particle()); 
		main.collide = false; 
		double c = 3.0E8; 

		while(!found){			
			rad += 1E8; 
			System.out.println("Searching w " + rad);

			main.get(1).init(rad, 0, c, 0, 0, 0, 1E24, TIME_STEP, 0);
			System.out.println("V: " + main.get(1).v_x);
			main.get(1).setRadius(10);
			main.get(1).color = pastel(); 

			ArrayList<Cluster> clusters = new ArrayList<Cluster>(); 
			clusters.add(main); 
			frame.addDrawable(main.get(1));

			for (int ii = 0; ii < 100; ii++) {
				main.run(frame, clusters);
				frame.repaint(); 
			}
			if(main.get(1).x_pos > rad){
				found = true; 
				System.out.println("Big mass: " + main.get(main.bigIndex()).mass);
				double rs = 2*G*main.get(main.bigIndex()).mass/(Math.pow(c, 2));  
				System.out.println("RADIUS FOUND: " + rad);
				System.out.println("Rs: " + rs);
			}

			frame.addDrawable(main.get(1));
		}
	}

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

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Runs the simulation. 
	 */
	public void run() { //scheduled by Executor
		//frame.setBackground(Color.BLACK);
		if(!stop){
			clusters.clear(); 
			clusters.add(main); 
			main.run(frame, clusters);
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
		for (Particle p : main){
			p.trail.clear();
			p.points.clear(); 
		}
	}

	/**
	 * Sets new view on frame. 
	 */
	public void set_view(){
		if(view_num < main.size()){
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
			/*	clusters.get(view_num-1).centX = clusters.get(view_num-1).get(clusters.get(view_num-1).bigIndex()).x_pos; 
			clusters.get(view_num-1).centY = clusters.get(view_num-1).get(clusters.get(view_num-1).bigIndex()).y_pos; 
			frame.setPreferredMinMax(clusters.get(view_num-1).centX - bigRadius, clusters.get(view_num-1).centX + bigRadius, 
					clusters.get(view_num-1).centY - bigRadius, clusters.get(view_num-1).centY + bigRadius); */
		}
	}

	/**
	 * Creates popup screen for user. 
	 * 
	 * @param text1
	 * 	Popup text. 
	 * @param text2
	 * 	Popup title. 
	 */
	public static void popup(String text1, String text2) {
		JOptionPane popup = new JOptionPane();
		popup.setLocation(300, 300);
		JOptionPane.showMessageDialog(new JFrame(), text1, text2,
				JOptionPane.PLAIN_MESSAGE);
		popup.setSize(300, 150);
		popup.setVisible(true);
	}

	/**
	 * The executor is responsible for running the Simulation object which extends Runnable. It schedules 
	 * the run() method at a fixed rate. 
	 */
	static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	static Random random;
	static Plate_Calc orbit;

	public static void main(String[] args) throws InterruptedException {
		popup("CLUSTER SIMULATOR", "Welcome");
		orbit = new Plate_Calc(); //create simulation 
		random = new Random();
		orbit.setupFrame(); 
		orbit.test(); 

		Thread.sleep(250);
	}

	/**
	 * Turn trails on or off in simulator. 
	 */
	public void toggle_trails(){ 
		for(Particle p : main){
			p.trace = !p.trace; //turn trails on or off
			if(p.trace) 
				frame.addDrawable(p.trail); 
			else 
				frame.removeDrawable(p.trail); 
		}
	}

	/**
	 * Sets up frame with proper location, scale, buttons, and size. 
	 */
	public void setupFrame() {
		frame.setLocation(FRAME_LOC[0], FRAME_LOC[1]); //screen location 
		frame.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]); //min max 
		frame.addButton("step", "Step", "Step Simulation", orbit); //add control buttons
		frame.addButton("stop", "Stop/Start", "Stop or Start Simulation", orbit);
		frame.addButton("clear_trails", "Clear Trails", "Clear Trail Points", orbit);
		frame.addButton("set_view", "Change View", "Change Viewpoint", orbit);
		frame.addButton("reset", "Reset", "Reset Simulation", orbit);
		frame.addButton("toggle_trails", "Toggle Trails", "Turn trails on or off.", orbit);
		frame.setVisible(true); //show to user 
		frame.setSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]); //dimensions  
	}

	// COOL PARAMS:
	// clusterNum = 5;
	// bigRad = 25*Earth Orbit
	// particle num is 30 or so
	// vmag is 150,000

	/**
	 * Initialize clusters. 
	 * 
	 * @param rand
	 * 	Boolean whether location is random or evenly distributed. 
	 * @param t
	 * 	The time step for the particles in the cluster. 
	 */
	private void initClusters(boolean rand, long t) {
		main = new Cluster(1, radius, frame, 0, 0, 0, 0, "Main", t, true);
		double c = 3.0*Math.pow(10, 8); 
		double rs = 2*G*main.get(main.bigIndex()).mass/(c*c);  
		System.out.println("RS: " + rs);
		MyEllipse my = new MyEllipse(new MyPoint[]{new MyPoint(-rs, 0), new MyPoint(0, rs), new MyPoint(rs, 0), new MyPoint(0, -rs)}); 
		my.plot(frame); 
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
		double randX = random.nextFloat() * FRAME_MINMAX[0] * 3 * sign() / 4;
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
		if (random.nextBoolean()) //random boolean 
			return -1;
		return 1;
	}

	/**
	 * Step simulation. 
	 */
	public void step() {
		stop = false;
		this.run(); //go through run once. 
		stop = true;
	}

	public void reset(){
		this.test(); 
	}

	protected double clusterNum = 5; //number of clusters 
	protected double bigRadius = Math.abs(FRAME_MINMAX[0] * 1 / 2); //large radius of cluster simulation 
	protected double vMag = 145000; //magnitude of velocity 
}
