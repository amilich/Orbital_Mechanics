import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;

import org.opensourcephysics.display.Circle;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;

import polyfun.Polynomial;
import riemann_sum.*;

/**
 * This class deals with the physics simulation of a particle. It contains all 
 * relevant characteristics concerning the motion of the particle's motion - 
 * velocity, acceleration, and position. It also constrains the constraints 
 * of the particle's motion set by the user in the control panel. 
 * 
 * Motion is calculated using Riemann sums; the particle is plotted on a DisplayFrame 
 * and traced by trails that change color depending on the speed of the particle. 
 * 
 * @method Step 
 * 	Moves the particle one time step - calculates new position, plots on displayframe. 
 * @method checkTerminalVelocity
 * 	Checks if particle is @ terminal velocity. 
 * @method newPos
 * 	Using bounce constraints, calculates the new position of the particle. Returns as a double[]. 
 * @method init
 * 	Initializes the particle with starting conditions. 
 * @method initWind
 * 	Initializes the wind from direction and velocity. 
 * @method trace
 * 	Traces the particle's position using trails. 
 * @method calcV
 * 	Calculates the new velocity given current velocity, initial velocity, acceleration, and time. 
 * @method newPos
 * 	Gets new position after a time step using current position, acceleration, and velocity.
 * @method constraints 
 * 	Sets up particle's constraints.  
 * @method velocityVector 
 * 	Returns overall velocity given x and y velocities. 
 * 
 * @author Andrew M. 
 */

public class Mass extends Circle {
	//each time you make a new ball, the trail stays so you can see the previous one
	ArrayList<Trail> trails = new ArrayList<Trail>(); //note: not used in springs (used in projectile) 

	int radius = 3; //radius of ball
	double x_pos, y_pos; //x and y positions
	double v_x, v_y;  //x and y velocities 
	double init_v_x, init_v_y; //initial velocities
	double acc_x, acc_y; //x, y acceleration
	double init_acc_x, init_acc_y; 
	double time = 0; //different for each particle
	double cTime = 0; 
	double deltaT = 0.1; //means ball is moving in real time (smaller values are more accurate - smaller width of subintervals in Riemann sums) 
	boolean trace = true; //if you want to see trail
	double[] xy = new double[] {0, 0}; 
	public final double g = -9.803; //gravity - m/s^2

	boolean airResistance = false; //whether air resistance is simulated 
	boolean show_ground; //shows where ball hit
	double alpha; //for air resistance
	double theta; //angle particle is launched at
	double y_pos_x_100; //y position of particle at x = 100
	double wind_x_v; //x component of wind velocity 
	double wind_y_v; //y component of wind velocity

	double lowerNum; //number of masses lower than this mass
	double mass; //the mass of this mass
	double equibLength; //the equilibrium length of the spring attached to this mass
	boolean useRiemann = false; 
	boolean xMoving = false; //used to drive oscillation  
	boolean yMoving = false; //used to drive oscillation  
	boolean fixed; //for springs 
	double orig_x; //original x pos

	/**
	 * Constructor for particle - all initial information is dealt with in the init functions. 
	 */
	public Mass() {
	}

	/**
	 * Constructor for particle - creates Particle object at starting time. 
	 * 
	 * @param initTime
	 * 	Time to initiate particle at. 
	 */
	public Mass(double initTime){ //initialize it with a specific time
		time = initTime; 
		cTime = initTime; 
	}

	/**
	 * Calculates the new position of a particle 1 time step later - 
	 * checks whether at terminal velocity, whether particle is 
	 * bouncing, and if a home run has been hit. 
	 * 
	 * Air resistance is also dealt with in this function and the 
	 * acceleration is changed accordingly. 
	 * 
	 * @param frame
	 * 	The displayframe used to draw the particle
	 */
	public void Step(DisplayFrame frame, boolean simulated){
		trails.add(new Trail()); //adds a new trail - allows changing color depending on speed of Particle 

		if(airResistance) { //changes x and y accelerations based on air resistance  
			//calculates air resistance 
			this.acc_x = this.acc_x - alpha*Math.abs(v_x)*v_x; 
			this.acc_y = this.acc_y - alpha*Math.abs(v_y)*v_y;
		}
		this.v_x = this.v_x + this.acc_x*deltaT; 
		this.v_y = this.v_y + this.acc_y*deltaT; 
		this.x_pos = this.x_pos + this.v_x*deltaT;
		this.y_pos = this.y_pos + this.v_y*deltaT; 
		super.setXY(this.x_pos, this.y_pos); 

		if(simulated)
			frame.addDrawable(this); //add to frame
		time += deltaT;  //increment time
		cTime += deltaT; 
	}

	/**
	 * This function is responsible for determining the new position of the particle. 
	 * It accounts for the particle's constraints and sets the velocities accordingly.
	 * 
	 * If a particle approaches a constraint, this function will ensure it bounces properly. 
	 * It also deals with the Fenway Park constraints. The coordinates returned by this 
	 * function are used in the Step method to move the particle.  
	 * 
	 * @return
	 * 	Returns a double[] of the new x and y positions
	 */
	public double[] newPos(){		
		//executes motion step without constraints 
		this.v_x = calcV(this.v_x, this.acc_x); //new velocity in x direction
		this.v_y = calcV(this.v_y, this.acc_y); //new y velocity 
		this.x_pos = newPos(this.x_pos, this.init_v_x, this.acc_x, time, v_x); //uses new values for new x position 
		this.y_pos = newPos(this.y_pos, this.init_v_y, this.acc_y, time, v_y); //new y position

		//returns the new coordinates in double array (used in Step method) 
		double[] newCoords = new double[] {x_pos, y_pos};
		return newCoords; 
	}

	/**
	 * This function is sued to set all the initial relevant 
	 * information for a particle - acceleration, velocity, 
	 * and positions for time = 0. An alpha and time step 
	 * are also set by this function. 
	 * 
	 * @param x
	 * 	The x coordinate to initialize the particle at 
	 * @param y
	 * 	The y coordinate to initialize the particle at 
	 * @param v_x
	 * 	The starting x velocity (can be + or -) 
	 * @param v_y
	 * 	Starting y velocity (can be + or -)
	 * @param acc_x
	 * 	Starting x acceleration (can be + or -) 
	 * @param acc_y
	 * 	Starting y acceleration (can be + or -) 
	 * @param mass
	 * 	The particle's mass. 
	 * @param timeStep
	 * 	The delta time interval to use to calculate changes in motion. 
	 * @param alpha
	 * 	The air resistance coefficient. 
	 */
	public void init(double x, double y, double v_x, double v_y, double acc_x, double acc_y, double mass, double timeStep, double alpha){		
		super.setXY(x, y); //use super class (Circle) to set position 
		this.x_pos = x; //now set class variables to appropriate information 
		this.y_pos = y; 
		this.v_x = v_x; 
		this.v_y = v_y; 
		this.init_v_x = v_x; //it's important to remember the initial values for making functions for a Riemann sum  
		this.init_v_y = v_y; 
		this.init_acc_x = acc_x; 
		this.init_acc_y = acc_y; 
		this.acc_x = acc_x; 
		this.acc_y = acc_y; 
		this.mass = mass;
		this.deltaT = timeStep; 
		super.pixRadius = radius;
		trails.add(new Trail()); 
		trails.get(trails.size()-1).setStroke(new BasicStroke(2));

		this.alpha = alpha; 
		this.airResistance = true; 
	}

	/**
	 * Initializes a particle with an initial velocity and a theta. 
	 * It also sets the alpha and timeStep values (and x,y pos). 
	 * 
	 * @param x
	 * 	X position to set. 
	 * @param y
	 * 	Y position to set. 
	 * @param v_init
	 * 	Initial velocity vector (split into x and y directions) 
	 * @param theta
	 * 	Angle to start projectile at.  
	 * @param timeStep
	 * 	Time step used for simulations. 
	 * @param alpha
	 * 	Alpha coefficient for air resistance. 
	 */
	public void init(double x, double y, double v_init, double theta, double timeStep, double alpha){
		super.setXY(x, y); //set x and y pos 
		this.setX_pos(x); //sets x variable 
		this.setY_pos(y); //sets y variable 
		this.v_x = v_init * Math.cos(Math.toRadians(theta)); //sets initial x velocity from vector
		this.v_y = v_init * Math.sin(Math.toRadians(theta)); //sets initial y velocity

		this.init_v_x = v_x; //sets initial x velocity 
		this.init_v_y = v_y; //sets initial y velocity 
		this.init_acc_x = 0; //sets initial accelerations - this does not initialize with an x acceleration - it is set to zero  
		this.init_acc_y = g; //a y acceleration is also not set - it is set to gravity 
		this.acc_y = g; //set y acceleration to g
		this.acc_x = 0; //set x acceleration to 0
		this.theta = theta; 

		this.deltaT = timeStep; //set time step 
		this.airResistance = true; //adds air resistance 
		this.alpha = alpha; //sets alpha coefficient 
	}

	/**
	 * Sets wind quantities given theta and wind vector velocity. 
	 */
	void initWind(double v, double theta){
		this.wind_x_v = v * Math.cos(Math.toRadians(theta)); //set x v  
		this.wind_y_v = v * Math.sin(Math.toRadians(theta)); //set y v
		this.v_x += this.wind_x_v; 
		this.v_y += this.wind_y_v; 
		this.init_v_x += this.wind_x_v; 
		this.init_v_y += this.wind_y_v;
	}

	/**
	 * Traces the particle. 
	 * 
	 * @param frame
	 * 	The displayframe to trace on 
	 * @param x
	 * 	The x point to trace 
	 * @param y
	 * 	The y point to trace 
	 */
	public void trace(DisplayFrame frame, double x, double y){
		trails.get(trails.size()-1).addPoint(x, y);  
		frame.addDrawable(trails.get(trails.size()-1));
	}

	public double getAcceleration(double massNum, double massMass, double k){
		return 0; 
	}

	/**
	 * Calculates a new velocity with a current velocity and acceleration. 
	 * It can be used for x or y velocities. 
	 * 
	 * @param vNow
	 * 	Current velocity 
	 * @param acc
	 * 	Current acceleration 
	 * @return
	 * 	New velocity 
	 */
	public double calcV(double vNow, double acc){
		Polynomial acc_poly = new Polynomial(new double[] {acc});
		//return vNow + acc*deltaT; 
		return vNow + RS.slice(acc_poly, time, time+deltaT);
	}

	/**
	 * Finds the new position after one time step using a Riemann sum. 
	 * The rule used by the Riemann sum is determined above. 
	 * 
	 * @param pos_init
	 * 	Current position - this function returns the next position. 
	 * @param init_v
	 * 	Initial velocity (useful when not having changed accelerations) 
	 * @param acc
	 * 	Current acceleration 
	 * @param time
	 * 	The current time - needed for Riemann sum. 
	 * @return
	 * 	Returns new position as a double. 
	 */
	public double newPos(double pos_init, double init_v, double acc, double time, double v_now){
		double b = v_now - acc*time; 
		Polynomial v_poly = new Polynomial(new double[] {b, acc});
		double newPos = pos_init; 
		newPos += RS.slice(v_poly, time, time+deltaT); 
		return newPos; //add each slice in Riemann sum 
		//return pos_init + v_now*deltaT + 0.5*acc*Math.pow(deltaT, 2);  
	}

	/**
	 * Takes x and y velocities and turns it into a single velocity vector. 
	 * This is useful for knowing overall velocity. The function uses the 
	 * Pythagorean Theorem as the x and y velocities form a right triangle 
	 * with the overall vector. 
	 * 
	 * @param vx
	 * @param vy
	 * @return
	 */
	public double velocityVector(double vx, double vy){
		return Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)); 
	}

	//Auto generated getters and setters 
	/**
	 * Gets x position
	 * 
	 * @return
	 * 	X position
	 */
	public double getX_pos() {
		return x_pos;
	}

	/**
	 * Sets x position
	 * 
	 * @param x_pos
	 * 	New x pos
	 */
	public void setX_pos(double x_pos) {
		this.x_pos = x_pos;
	}

	/**
	 * Gets y pos
	 * 
	 * @return
	 * 	y pos
	 */
	public double getY_pos() {
		return y_pos;
	}

	/**
	 * Sets y pos
	 * 
	 * @param y_pos
	 * 	New y pos
	 */
	public void setY_pos(double y_pos) {
		this.y_pos = y_pos;
	}

	/**
	 * Gets x velocity 
	 * 
	 * @return
	 * 	X velocity
	 */
	public double getV_x() {
		return v_x;
	}

	/**
	 * Sets x velocity
	 * 
	 * @param v_x
	 * 	new x velocity
	 */
	public void setV_x(double v_x) {
		this.v_x = v_x;
	}

	/**
	 * Gets y velocity 
	 * 
	 * @return
	 * 	Y velocity
	 */
	public double getV_y() {
		return v_y;
	}

	/**
	 * Sets y velocity
	 * 
	 * @param v_y
	 * 	New y velocity
	 */
	public void setV_y(double v_y) {
		this.v_y = v_y;
	}

	/**
	 * Gets x acceleration 
	 * 
	 * @return
	 * 	Current x acceleration
	 */
	public double getAcc_x() {
		return acc_x;
	}

	/**
	 * Sets x acceleration 
	 * 
	 * @param acc_x
	 * 	New x acceleration
	 */
	public void setAcc_x(double acc_x) {
		this.acc_x = acc_x;
	}

	/**
	 * Gets y acceleration 
	 * 
	 * @return
	 * 	Current y acceleration 
	 */
	public double getAcc_y() {
		return acc_y;
	}

	/**
	 * Sets y acceleration 
	 * 
	 * @param acc_y
	 * 	New y acceleration 
	 */
	public void setAcc_y(double acc_y) {
		this.acc_y = acc_y;
	}

	/**
	 * Gets mass
	 * 
	 * @return
	 * 	Mass of particle 
	 */
	public double getM() {
		return mass;
	}

	/**
	 * Sets mass of particle 
	 * 
	 * @param m
	 * 	New mass 
	 */
	public void setM(double m) {
		this.mass = m;
	}

	/**
	 * Gets particle's radius 
	 * 
	 * @return
	 * 	Radius
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Sets the particle's radius
	 * 
	 * @param radius
	 * 	New radius 
	 */
	public void setRadius(int radius) {
		this.radius = radius;
		this.pixRadius = radius; 
	}

	/**
	 * Gets current time of particle 
	 * 
	 * @return
	 * 	Time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Sets time
	 * 
	 * @param time
	 * 	New time
	 */
	public void setTime(double time) {
		this.time = time;
	}
}
