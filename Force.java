

/**
 * The force class has the properties of a vector. Each mass has an 
 * ArrayList of forces, which can be summed to find the acceleration 
 * using Newton's 2nd Law. Each force has an angle, magnitude, and a 
 * name (useful for debugging purposes). 
 * 
 * The force class also provides x and y components of individual forces 
 * to make adding them extremely easy. 
 * 
 * @method forceX	 
 * 	Returns x component of force 
 * @method forceY 
 * 	Returns y component of force
 * 
 * @author Andrew M. 
 */

public class Force {
	double magnitude; //magnitude of force
	double angle; //angle with horizontal (negative angles work also)
	String name = ""; //force's name -s e.g. "mg of mass #1"

	/**
	 * Initializes a force given a magnitude, angle, and name. 
	 * 
	 * @param magnitude
	 * 	The magnitude of the force. 
	 * @param angle
	 * 	The force's angle with the horizontal. 
	 * @param name
	 * 	The force's name, a string that is useful for debugging the various forces on a mass. 
	 */
	public Force(double magnitude, double angle, String name){
		this.magnitude = magnitude; 
		this.angle = angle; 
		this.name = name; 
	}

	/**
	 * Overloaded constructor that does not require specifying name/angle (can be individually set). 
	 */
	public Force(){	
	}

	/**
	 * This method calculates the X component of a force f.
	 *   	
	 * @param f
	 * 	The force used to find x component. 
	 * @return
	 * 	The x component of the force. 
	 */
	double forceX(Force f){
		double xComp = magnitude*Math.cos(Math.toRadians(angle)); 
		return xComp; 
	}

	/**
	 * This method calculates the Y component of a force f.
	 *   	
	 * @param f
	 * 	The force used to find y component. 
	 * @return
	 * 	The y component of the force. 
	 */
	double forceY(Force f){
		double yComp = magnitude*Math.sin(Math.toRadians(angle)); 
		return yComp; 
	}

	/**
	 * Returns x component of a force (no parameters necessary). 
	 * 
	 * @return
	 * 	The force's x component. 
	 */
	double forceX(){
		double xComp = magnitude*Math.cos(Math.toRadians(angle));
		return xComp; 
	}

	/**
	 * Returns y component of a force (no parameters necessary). 
	 * 
	 * @return
	 * 	The force's y component. 
	 */
	double forceY(){
		double yComp =  magnitude*Math.sin(Math.toRadians(angle)); 
		return yComp; 
	}
}
