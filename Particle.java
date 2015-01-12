
import java.awt.BasicStroke;
import java.util.ArrayList;

import org.opensourcephysics.display.Circle;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;

import polyfun.Polynomial;
import riemann_sum.TrapezoidRule;

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

public class Particle extends Circle {
	/**
	 * The forces ArrayList keeps track of all the forces acting on the mass (Particle) 
	 * at a given time. Forces are essentially named vectors that make it easy to keep 
	 * track of two dimensional spring forces. 
	 */
	protected ArrayList<Force> forces = new ArrayList<Force>();
	protected ArrayList<Trail> trails = new ArrayList<Trail>(); 
	public ArrayList<MyPoint> points = new ArrayList<MyPoint>(); 
	protected Trail trail = new Trail(); //note: not used in springs (used in projectile) 

	protected Particle bump = null; 
	protected int radius = 3; //radius of ball

	protected boolean airResistance = false; //whether air resistance is simulated 
	public boolean useRiemann = false; 
	public boolean fixed; //for springs 
	public boolean trace = false; //if you want to see trail
	public double charge; 

	protected double[] xy = new double[] {0, 0}; 
	protected double alpha; //for air resistance
	protected double theta; //angle particle is launched at
	protected double wind_x_v; //x component of wind velocity 
	protected double wind_y_v; //y component of wind velocity
	protected double x_pos, y_pos; //x and y positions
	protected double v_x, v_y;  //x and y velocities 
	protected double init_v_x, init_v_y; //initial velocities
	protected double acc_x, acc_y; //x, y acceleration
	protected double init_acc_x, init_acc_y; 
	protected double time = 0; //different for each particle
	protected double cTime = 0; 
	protected double deltaT; //means ball is moving in real time (smaller values are more accurate - smaller width of subintervals in Riemann sums) 
	protected double lowerNum; //number of masses lower than this mass
	protected double mass; //the mass of this mass
	protected double orig_x; //original x pos
	protected double v_tangent; 
	protected double actual_r; //real life radius 
	protected int killCount = 0; 
	protected String name = ""; 
	protected String real_name = ""; 
	protected MyPoint prev = new MyPoint(); 
	protected double maxX = Double.NEGATIVE_INFINITY; //for proof of Kepler's 1st law 
	protected double minX = Double.POSITIVE_INFINITY; 
	protected double maxY = Double.NEGATIVE_INFINITY; 
	protected double minY = Double.POSITIVE_INFINITY; 
	protected boolean setMaxX = false, setMinX = false, setMaxY = false, setMinY = false;  

	TrapezoidRule RS = new TrapezoidRule(); //Riemann sum used for changing position 

	/**
	 * Constructor for particle - all initial information is dealt with in the init functions. 
	 */
	public Particle() {
	}

	/**
	 * Constructor for particle - creates Particle object at starting time. 
	 * 
	 * @param initTime
	 * 	Time to initiate particle at. 
	 */
	public Particle(double initTime){ //initialize it with a specific time
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
	protected boolean setColor = false;

	public void Step(DisplayFrame frame, boolean simulated, boolean move){
		if(!setColor){
			trail.setStroke(new BasicStroke(1));
			trail.color = this.color;
			setColor = true; 
		}

		if(airResistance) { //changes x and y accelerations based on air resistance  
			//calculates air resistance 
			this.acc_x = this.acc_x - alpha*Math.abs(v_x)*v_x; 
			this.acc_y = this.acc_y - alpha*Math.abs(v_y)*v_y;
		}
		if(move){
			prev.x = x_pos; 
			prev.y = y_pos; 
			if(!useRiemann){
				this.v_x = this.v_x + this.acc_x*deltaT; 
				this.v_y = this.v_y + this.acc_y*deltaT; 
				this.x_pos = this.x_pos + this.v_x*deltaT;
				this.y_pos = this.y_pos + this.v_y*deltaT; 
				super.setXY(this.x_pos, this.y_pos); 
			}
			else{
				//gets new positions in a double array
				xy = newPos(); //get new positions
				super.setXY(xy[0], xy[1]); //set new positions
			}

			if(x_pos > maxX){
				maxX = x_pos; 
				//System.out.println("Max x is " + maxX);
			}
			if(x_pos < minX){
				minX = x_pos; 
				//System.out.println("Min x is " + minX);
			}
			if(y_pos > maxY){
				maxY = y_pos; 
				//System.out.println("Max y is " + maxY);
			}
			if(y_pos < minY){
				minY = y_pos; 
				//System.out.println("Min y is " + minY);
			}
		}
		trail.addPoint(x_pos, y_pos);
		points.add(new MyPoint(x_pos, y_pos)); 
		if(trace) { //trail to track particle's motion
			//trail.clear(); 
			trail.setStroke(new BasicStroke(1));
			trail.color = this.color; 
			frame.addDrawable(trail);
		}
		if(simulated)
			frame.addDrawable(this); //add to frame
		if(move){
			time += deltaT;  //increment time
			cTime += deltaT;
		}
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

	public double KE(){
		return 0.5*this.mass*Math.pow(this.vector(v_x, v_y), 2); 
	}

	public void trace(DisplayFrame frame, double x, double y){
		trail.addPoint(x, y);  
		frame.addDrawable(trail); 
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
	public double vector(double vx, double vy){
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

	/* (non-Javadoc)
	 * @see org.opensourcephysics.display.Circle#toString()
	 * 
	 * Contains the data for each particle. 
	 */
	public String toString(){
		StringBuilder str = new StringBuilder(); //add all necessary data about particle
		str.append(real_name + ", M : ");
		str.append(mass + ", pX: "); 
		str.append(x_pos + ", pY: "); 
		str.append(y_pos + ", vX: "); 
		str.append(v_x + ", vY: "); 
		str.append(v_y + ", aX: "); 
		str.append(acc_x + ", aY: "); 
		str.append(acc_y + ", pR: "); 
		str.append(radius + ", aR: "); 
		str.append(actual_r + ", aF: "); 
		double force = this.mass*vector(acc_x, acc_y); 
		str.append(force + ", aN: ");
		str.append(real_name + ", mq: ");
		str.append(charge); 
		

		return str.toString();
	}
}
