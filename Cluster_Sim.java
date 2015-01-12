import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opensourcephysics.frames.DisplayFrame;

/**
 * This class simulates the interactions of groups of particles, known as clusters. The clusters 
 * are essentially ArrayLists of particles that are initialized with a location, radius, and 
 * initial tangential velocity. They move around a central, larger mass. The clusters can 
 * simulate the interaction of groups of masses, such as the interactions of multiple solar 
 * systems or perhaps even the interactions of galaxies. 
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
public class Cluster_Sim implements Runnable {
	protected static DisplayFrame frame = new DisplayFrame("x", "y", "Frame");
	/**
	 * All the clusters in the simulation.  
	 */
	protected static ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	protected static double g = 9.803; // gravity constant
	protected static double G = 6.67384E-11; //universal gravitational constant
	final private static double SUN_MASS = 1.98892E30; // kg
	@SuppressWarnings("unused")
	final private static double EARTH_MASS = 5.9742E24; // kg
	final private static double EARTH_ORBIT = 1.5E11; // m
	@SuppressWarnings("unused")
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	@SuppressWarnings("unused")
	final private static double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 };
	final private static int[] FRAME_DIMENSIONS = { 700, 700 };
	final private static double multiplier = 50;
	final private static double[] FRAME_MINMAX = { -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT, -multiplier * EARTH_ORBIT,
		multiplier * EARTH_ORBIT }; // scale at which the frame starts

	protected static double TIME_STEP = 4000;
	protected static double p_time = 18000; 
	protected static boolean elastic = true;
	protected static boolean stop = true; 
	protected double center_mass = 0; 

	protected static double vx = -20000;
	protected static double vy = -80000;
	protected int view_num = 0; 

	static final long TARGET_FPS = 2000;
	static long TICK_RATE = 100000;
	ArrayList<Particle> list = new ArrayList<Particle>(); 
	Multi_Bary mb = new Multi_Bary(list); 

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Runs the simulation. 
	 */
	public void run() { //scheduled by Executor 
		frame.setBackground(Color.BLACK);
		list.clear(); 
		for(Cluster c : clusters){
			for (Particle p : c) {
				list.add(p); 
			}
		}

		if (stop) { 
			control.canRead = true; //can enter data in GUI 
			if (control.dataChange) {
				reset(); //reset simulation (if changed) 
				control.dataChange = false;
			}
			gc = true; 
		} else {
			garb ++; 
			if(gc){
				gc = false; 
				System.gc(); 
				
				Runtime runtime = Runtime.getRuntime();
				runtime.gc();
				long mem = runtime.totalMemory() - runtime.freeMemory();
				System.out.println("Used memory is megabytes: " + bytesToMegabytes(mem));
			}
			if(garb % 1000 == 0){
				garb = 0; 
				System.gc(); 
			}
			control.canRead = false;
			//mb.update(list);
			frame.addDrawable(mb);
			if(view_num != 0)
				set_cam(); //move camera
			for (Cluster c : clusters)
				c.run(frame, clusters);
		}
		frame.repaint();
	}
	boolean gc = false; 
	int garb = 0; 
	
	private static final long MEGABYTE = 1024L * 1024L;

	//http://stackoverflow.com/questions/10754748/monitoring-own-memory-usage-by-java-application
	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
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
		for (Cluster c : clusters)
			for (Particle p : c){
				p.trail.clear();
				p.points.clear(); 
			}
	}

	/**
	 * Sets new view on frame. 
	 */
	public void set_view(){
		if(view_num < clusters.size()){
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
			clusters.get(view_num-1).centX = clusters.get(view_num-1).get(clusters.get(view_num-1).bigIndex()).x_pos; 
			clusters.get(view_num-1).centY = clusters.get(view_num-1).get(clusters.get(view_num-1).bigIndex()).y_pos; 
			frame.setPreferredMinMax(clusters.get(view_num-1).centX - bigRadius, clusters.get(view_num-1).centX + bigRadius, 
					clusters.get(view_num-1).centY - bigRadius, clusters.get(view_num-1).centY + bigRadius); 
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
	static ClusterController control;
	static Cluster_Sim orbit;

	public static void main(String[] args) throws InterruptedException {
		control = new ClusterController(); //initialize GUI controller 
		popup("CLUSTER SIMULATOR", "Welcome");
		orbit = new Cluster_Sim(); //create simulation 
		random = new Random();
		orbit.setupFrame(); 
		control.refresh(orbit);
		orbit.reset(); //reset function sets up all clusters

		Thread.sleep(250);
	}

	/**
	 * Turn trails on or off in simulator. 
	 */
	public void toggle_trails(){ 
		for(Cluster c : clusters){
			for(Particle p : c){
				p.trace = !p.trace; //turn trails on or off
				if(p.trace)
					frame.addDrawable(p.trail); 
				else 
					frame.removeDrawable(p.trail); 
			}
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
		if (!rand) { //even distribution location 
			for (int ii = 0; ii < clusterNum; ii++) {
				double ang = ii * 360 / clusterNum; //distribute evenly around circle 
				double newX = bigRadius * Math.cos(Math.toRadians(ang)); //calculate x, y positions 
				double newY = bigRadius * Math.sin(Math.toRadians(ang));
				clusters.add(new Cluster(particle_num, radius, frame, 0, 0, newX, newY, "Cluster " + ii, t, false)); //create the cluster
			}
		} 
		else { //random position 
			for (int ii = 0; ii < clusterNum; ii++) {
				double[] coords = randCoords(); //get random coordinates 
				clusters.add(new Cluster(particle_num, radius, frame, 0, 0, coords[0], coords[1], "Cluster " + ii, t, false)); //initialize cluster
			}
		}

		for (int ii = 0; ii < clusters.size(); ii++) { //give the clusters velocity around center 
			double vAng = calcAng(clusters.get(ii).get(0), 0, 0) + 90;
			double[] v = new double[] { vMag, vAng }; //velocity is set by user 
			clusters.get(ii).get(0).init(clusters.get(ii).centX, clusters.get(ii).centY, v[0],v[1], clusters.get(ii).get(0).deltaT, 0); //initialize the cluster
		} 

		if(center_mass != 0){
			clusters.add(new Cluster(1, 1E3, frame, 0, 0, 0, 0, "Center", t, false)); 
			clusters.get(clusters.size()-1).get(0).mass = center_mass*SUN_MASS; 
		}
		else {
			System.out.println("Zero Central Mass");
		}

		for (Cluster c : clusters) //set index of stroke of largest particle 
			c.get(c.bigIndex()).trail.setStroke(new BasicStroke(2));

		//Escape velocity test 
		/*for (Cluster c : clusters) {
			if(clusters.size() > 1)
				System.out.println("Escape v should be " + Math.sqrt(2*G*center_mass*SUN_MASS/Cluster.dist(0, 0, c.get(c.bigIndex()).x_pos, c.get(c.bigIndex()).y_pos)));
		}*/
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

	protected double clusterNum = 5; //number of clusters 
	protected double bigRadius = Math.abs(FRAME_MINMAX[0] * 1 / 2); //large radius of cluster simulation 
	protected double radius = 3.7 * EARTH_ORBIT; //radius of each cluster 
	protected double vMag = 145000; //magnitude of velocity 
	protected boolean random_dist = false; //boolean for random or even distribution 
	protected int particle_num = 15; //number of particles in each cluster 

	/**
	 * Reset the simulation. 
	 */
	public void reset() {
		stop = true; //stop sim, clear particles, reset executor 
		frame.clearDrawables();
		executor.shutdown();
		executor = Executors.newScheduledThreadPool(1);
		clusters.removeAll(clusters); //get rid of all clusters in ArrayList 

		if(control.dataChange) { //put in new data from user 
			clusterNum = control.c_num(); //get values from text boxes 
			bigRadius = control.sr(); 
			radius = control.cr(); 
			particle_num = control.p_num();
			vMag = control.vMag(); 
			center_mass = control.cMass(); 

			String rand_txt = control.dist(); 
			if(rand_txt.toLowerCase().contains("t") || rand_txt.toLowerCase().contains("1")) //check if user wants random or even distribution
				random_dist = true; 
			else 
				random_dist = false; 
		}

		initClusters(random_dist, control.time()); //reinitialize clusters 
		executor.scheduleAtFixedRate(this, 0, TICK_RATE, TimeUnit.NANOSECONDS); //restart simulation 
	}
}
