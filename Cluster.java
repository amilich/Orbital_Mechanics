import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.frames.DisplayFrame;

/**
 * A Cluster is an object of type ArrayList that contains a list of particles. The Cluster is initialized with 
 * the number of particles in an ArrayList, the radius of the cluster, the frame it is displayed on, an x and y 
 * velocity for the center (large) mass, the center of the cluster (x, y pos), the cluster's name, and the time 
 * step for each particle. 
 * 
 * Cluster is the main component of the simulation of clusters of particles (solar systems, galaxies, etc.), and 
 * each cluster will interact with itself as well as with other clusters. Note the similarities between this class 
 * and the Orbital_AM simulation: Cluster.java essentially contains most necessary simulation functions and methods 
 * (similar function as Orbital_AM) because it conducts the entire simulation of the particles within its list. 
 * 
 * @method moveAll 
 * 	Translates entire Cluster. 
 * @method setCenterV 
 * 	Sets velocity of center particle. 
 * @method bigIndex
 * 	Index of most massive particle (0, but with combinations useful to have). 
 * @method pastel 
 * 	Returns pleasing pastel color. 
 * @method v_tan 
 * 	Creates tangent velocity to orbit center. 
 * @method sign
 * 	Returns -1 or +1 randomly. 
 * @method coords 
 * 	Finds random coordinates within a circle. 
 * @method run 
 * 	Runs (similar to Orbital_AM.run) particles but with one other cluster. 
 * @method run
 * 	Runs (similar to Orbital_AM.run) particles but with list of other clusters. 
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
 * @method sunForces 
 * 	Sums a list of forces in a given direction. 
 * 
 * @author Andrew M. 
 */
public class Cluster extends ArrayList<Particle> {
	Random random = new Random(); 

	final private static boolean DEBUG_MODE = false; //print debug messages
	public boolean collide = true; //whether to collide 
	final private static boolean collide_real = true; //whether to collide based on actual radii or pixel overlap 
	protected static boolean elastic = false; //elastic or inelastic

	protected static double g = 9.803; //gravity constant
	protected static double G = 6.67384E-11; //universal gravitational constant
	protected double radius; //radius of cluster 
	protected long TIME_STEP = 18000; //time step of particles 
	protected String name; //cluster's name (good for debugging) 

	final private static double SUN_MASS = 1.98892E30; //kg 
	final private static double EARTH_MASS = 5.9742E24; //kg
	final private static double EARTH_ORBIT = 1.5E11; //m 
	final private static double COLLISION_RAD = 1;

	public double centX; //x pos of center of cluster 
	public double centY; //y pos of center of cluster

	/**
	 * Initializes a new cluster. 
	 * 
	 * @param particleNum
	 * 	# of particles. 
	 * @param rad
	 * 	Cluster radius. 
	 * @param frame
	 * 	Displayframe to use. 
	 * @param vx
	 * 	V_x of center. 
	 * @param vy
	 * 	V_y of center. 
	 * @param tx
	 * 	X pos of center of cluster. 
	 * @param ty
	 * 	Y pos of center of cluster. 
	 * @param name
	 * 	Cluster's name. 
	 * @param TIME_STEP
	 * 	Time step for particles in cluster. 
	 */
	public Cluster(int particleNum, double rad, DisplayFrame frame, double vx, double vy, double tx, double ty, String name, long TIME_STEP, boolean condense){
		this.TIME_STEP = TIME_STEP; 
		centX = tx; 
		centY = ty; 

		radius = rad; 
		add(new Particle()); //add the center particle 
		get(0).init(0, 0, vx, vy, TIME_STEP, 0);
		get(0).useRiemann = true; 
		get(0).pixRadius = 5; //make it big! 
		get(0).color = pastel(); //nice color 
		get(0).actual_r = 6371*1000; //HARDCODED ACTUAL RADIUS 
		get(0).name = "Center of " + name + " cluster."; 

		for (int ii = 1; ii < particleNum; ii++) { //initialize all other particles in cluster
			add(new Particle()); 
			double[] tempCoords = coords(); //random x and y pos
			get(ii).init(tempCoords[0], tempCoords[1], 0, 0, TIME_STEP, 0); 
			if(!condense){
				double[] v = v_tan(get(ii), get(0)); //give it a tangential velocity to orbit
				get(ii).init(tempCoords[0], tempCoords[1], v[0], v[1], TIME_STEP, 0);
			}
			else{ 
				double[] v = v_in(get(ii), get(0)); //give it a tangential velocity to orbit
				get(ii).init(tempCoords[0], tempCoords[1], v[0], v[1], TIME_STEP, 0);
			}
			get(ii).mass = 1E16; //HARDCODED
			get(ii).useRiemann = false; 
			get(ii).pixRadius = 3;  
			get(ii).actual_r = 7000; //a little bigger than earth 
			get(ii).color = pastel(); 
		}

		for (Particle p : this) { //adds all particles to frame 
			p.x_pos += tx; 
			p.y_pos += ty; 
			p.trace = false; 
			p.setX(p.x_pos);
			p.setY(p.y_pos);
			frame.addDrawable(p); 
		}
		
		if(!condense)
			get(0).mass = 1E33; //HARDCODED 
		else {
			get(0).mass = 1E38;
			get(0).actual_r = 100; 
		}
	}

	/**
	 * Moves whole cluster.
	 * 
	 * @param tx
	 * 	X pos to translate to. 
	 * @param ty
	 * 	Y pos to translate to. 
	 */
	public void moveAll(double tx, double ty){
		for (Particle p : this) 
			p.setXY(tx, ty);
	}

	/**
	 * Sets x and y velocities of center particle. 
	 * 
	 * @param vx
	 * 	X velocity of center. 
	 * @param vy
	 * 	Y velocity of center. 
	 */
	public void setCenterV(double vx, double vy){
		get(0).v_x = vx; 
		get(0).v_y = vy; 
	}

	/**
	 * Finds index of largest particle by mass (to trace). 
	 * 
	 * @return
	 * 	Index of most massive particle. 
	 */
	public int bigIndex(){
		int index = 0; 
		for (int ii = 0; ii < size(); ii++) {
			if(get(ii).mass > get(index).mass)
				index = ii; 
		}
		return index; 
	}

	/**
	 * Nice color for particles. 
	 * 
	 * @return
	 * 	New color. 
	 */
	private Color pastel(){
		final float hue = random.nextFloat();
		final float saturation = (random.nextInt(2000) + 4000) / 10000f;
		final float luminance = 0.9f;
		final Color color = Color.getHSBColor(hue, saturation, luminance);
		return color; 
	}

	/**
	 * Finds a random tangential velocity for each particle. 
	 * 
	 * @param a
	 * 	Particle orbiting. 
	 * @param b
	 * 	Center particle (most massive one here). 
	 * @return
	 * 	Velocity's magnitude and angle. 
	 */
	private double[] v_tan(Particle a, Particle b){
		double ang = calcAng(a, b) + 90;
		if(DEBUG_MODE)
			System.out.println("Angle is between " + a + " and " + b + " so " +  calcAng(a, get(0)) + ", so setting to " + ang);
		double constant = 80000; 
		//NUMBERS
		//System.out.println("V: " + (50000)*dist(a, b)/EARTH_ORBIT);
		return new double[] {constant*dist(a, b)/EARTH_ORBIT, ang};
		//return new double[] {(random.nextInt(17000)+30000)*dist(a, b)/EARTH_ORBIT, ang}; 
	}
	double r = 1; 
	double M = 3; 

	private double[] v_in(Particle a, Particle b){
		double ang = calcAng(a, b) + 90 - 10;
		if(DEBUG_MODE)
			System.out.println("Angle is between " + a + " and " + b + " so " +  calcAng(a, get(0)) + ", so setting to " + ang);
		double constant = 3.0*Math.pow(10, 6); 
		double ret = Math.sqrt(2*G*M/r)*constant; 
		//NUMBERS
		//System.out.println("V: " + (50000)*dist(a, b)/EARTH_ORBIT);
		return new double[] {constant*dist(a, b)/EARTH_ORBIT, ang};
		//return new double[] {(random.nextInt(17000)+30000)*dist(a, b)/EARTH_ORBIT, ang}; 
	}

	/**
	 * Returns - or + sign to create random coordinates. 
	 * 
	 * @return
	 * 	-1 or + 1. 
	 */
	private int sign(){
		if(random.nextBoolean())
			return -1; 
		return 1; 
	}

	/**
	 * Random x and y positions. 
	 * 
	 * @return
	 * 	Double array of new coordinates. 
	 */
	private double[] coords(){
		double randX = random.nextFloat()*radius*sign(); //multiply for - or + randomly 
		double randY = random.nextFloat()*radius*sign();
		while(dist(0, 0, randX, randY) > radius){ //check whether within cluster radius 
			randX = 0.2E11 + random.nextFloat()*radius*sign(); 
			randY = 0.2E11 + random.nextFloat()*radius*sign();
		}
		return new double[] {randX, randY}; 
	}

	/**
	 * Calculate accelerations and move particles. 
	 * 
	 * @param frame
	 * 	DisplayFrame for simulation. 
	 * @param otherCluster
	 * 	Have list of other clusters interact with this one,  
	 */
	public void run(DisplayFrame frame, ArrayList<Cluster> otherClusters){
		calculate_accelerations(otherClusters); 
		move_bodies(frame, otherClusters); 
	}

	/**
	 * Moves all the particles by checking for collisions and then stepping them. 
	 * 
	 * @param frame
	 * 	Frame to move particles on. 
	 */
	private void move_bodies(DisplayFrame frame, ArrayList<Cluster> allClusters){
		if(collide){ //global variable for collisions
			//need to check with all particles in this AND in all other clusters. 
			for (int ii = 0; ii < this.size(); ii ++) {
					for (int jj = 0; jj < allClusters.size(); jj ++) { //through all clusters
						for (int kk = 0; kk < allClusters.get(jj).size(); kk++) { //through other cluster in allclusters 
							if(dist(get(ii), allClusters.get(jj).get(kk)) == 0) continue; //do not collide with self 
							else if (checkCollision(get(ii), allClusters.get(jj).get(kk), frame)) {
								setCollision(get(ii), allClusters.get(jj).get(kk), frame, allClusters, jj); //respond to collision
								continue; 
								//play("boing.wav"); 
							}
							else 
								get(ii).bump = null; //no recent collision 
						}
					}
			}
		}
		for (int ii = 0; ii < this.size(); ii++) {
			if(dist(get(ii), get(bigIndex())) > 1E20){
				frame.removeDrawable(get(ii));
				remove(get(ii)); 
				break; 
			}
			get(ii).Step(frame, true, true); 
			frame.addDrawable(get(ii));
		}
	}

	/**
	 * Checks if a collision has occurred between 2 particles. 
	 * 
	 * @param m1
	 * 	Particle 1 to check. 
	 * @param m2
	 * 	Particle 2 to check. 
	 * @param frame
	 * 	Displayframe used (if using pixel overlap). 
	 * @return
	 * 	True or false (if collision has/has not happened). 
	 */
	static DrawingPanel panel; 
	private static boolean checkCollision(Particle m1, Particle m2, DisplayFrame frame) {
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
			panel = frame.getDrawingPanel(); //gets pixel positions of each particle. 
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
	private void setCollision(Particle m1, Particle m2, DisplayFrame frame, ArrayList<Cluster> allClusters, int m2C){
		m1.bump = m2; //set that they have collided with each other
		m2.bump = m1; 

		if(elastic){ //efficiency == 100
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
			System.out.println("ELASTIC");
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
			monster.init(m1.x_pos, m1.y_pos, v_f_x, v_f_y, 0, 0, mass_sum, m1.deltaT, 0);

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

			//set to larger pixel radius 
			if(m1.pixRadius > m2.pixRadius)
				monster.pixRadius = m1.pixRadius; 
			else 
				monster.pixRadius = m2.pixRadius;

			//remove old ones, add new 
			frame.removeDrawable(m1);
			frame.removeDrawable(m2); 
			//for (int ii = 0; ii < m1.points.size(); ii ++) 
			//	monster.trail.addPoint(m1.points.get(ii).x, m1.points.get(ii).y);
			monster.trail.moveToPoint(m2.points.get(0).x, m2.points.get(0).y);
			for (int ii = 0; ii < m2.points.size(); ii ++) 
				monster.trail.addPoint(m2.points.get(ii).x, m2.points.get(ii).y);
			m1.trail.clear(); 
			m2.trail.clear(); 
			m1.points.clear(); 
			m2.points.clear(); 
			//this.remove(m1); 
			allClusters.get(m2C).remove(m2);
			this.set(this.indexOf(m1), monster);
			//this.add(monster); 
			frame.addDrawable(monster); 
			monster.Step(frame, true, true);
		}
	}

	/**
	 * Calculate and set accelerations of all particles based on gravitational forces with other particles and clusters. 
	 * 
	 * @param otherCluster
	 * 	Another cluster in the simulation. 
	 */
	private void calculate_accelerations(Cluster otherCluster){
		for (int ii = 0; ii < this.size(); ii++) { //step through arraylist of masses
			this.get(ii).forces.clear(); //clear old forces
			this.get(ii).forces.removeAll(this.get(ii).forces); 

			for (int jj = 0; jj < this.size(); jj++) {
				if(ii == jj) continue; //do not add force with itself 
				this.get(ii).forces.add(grav_force(this.get(ii), this.get(jj))); //add gravitational force with other mass
			}
			for (int jj = 0; jj < otherCluster.size(); jj++) {
				this.get(ii).forces.add(grav_force(this.get(ii), otherCluster.get(jj))); //now add forces with all other clusters 
			}
			double xSum = sumForces(this.get(ii).forces, 0); 
			double ySum = sumForces(this.get(ii).forces, 1);
			this.get(ii).acc_x = xSum/this.get(ii).mass; //a = F/m
			this.get(ii).acc_y = ySum/this.get(ii).mass; //set accelerations 
		}
	}


	/**
	 * Calculate and set accelerations of all particles based on gravitational forces with other particles and clusters. 
	 * 
	 * @param allClusters
	 * 	All clusters in simulation (including this one). 
	 */
	private void calculate_accelerations(ArrayList<Cluster> allClusters){
		for (int ii = 0; ii < this.size(); ii++) { //step through arraylist of masses
			this.get(ii).forces.clear(); //clear old forces
			this.get(ii).forces.removeAll(this.get(ii).forces); 

			for (int jj = 0; jj < allClusters.size(); jj++) {
				for (int kk = 0; kk < allClusters.get(jj).size(); kk++) {
					if(dist(this.get(ii), allClusters.get(jj).get(kk)) != 0) //easier way to make sure you're not adding force on itself 
						this.get(ii).forces.add(grav_force(this.get(ii), allClusters.get(jj).get(kk)));
					else 
						if(DEBUG_MODE)
							System.out.println("Almost went to NaN!"); 
				}
			}
			double xSum = sumForces(this.get(ii).forces, 0); //sum forces, set accelerations
			double ySum = sumForces(this.get(ii).forces, 1);
			this.get(ii).acc_x = xSum/this.get(ii).mass; //a = F/m
			this.get(ii).acc_y = ySum/this.get(ii).mass;
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
	private static Force grav_force(Particle m1, Particle m2){
		double dist = dist(m1, m2); //dist between two masses 
		double mag = G*m1.mass*m2.mass/Math.pow(dist, 2); //magnitude of force (depends on dist) 

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
	static double dist(Particle a, Particle b){
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2) + Math.pow(a.getY_pos() - b.getY_pos(), 2)); 
		return dist; //distance formula
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
	static double dist(double x1, double y1, double x2, double y2){
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
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
		double xDiff = b.x_pos - a.x_pos; //find delta x
		double yDiff = b.y_pos - a.y_pos; //find delta y
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));

		return angle; //inverse tangent of y/x
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
	static double sumForces(ArrayList<Force> f, double dir){
		double sum = 0; 
		if(dir == 0){
			for (int ii = 0; ii < f.size(); ii++) {
				if(f.get(ii).forceX() != Double.NaN)
					sum += f.get(ii).forceX(); //get x components
			}
		}
		else {
			for (int ii = 0; ii < f.size(); ii++) {
				if(f.get(ii).forceY() != Double.NaN)
					sum += f.get(ii).forceY(); //get y components
			}
		}
		return sum; 
	}

	public String toString(){
		return "I'm a Cluster!"; 
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
