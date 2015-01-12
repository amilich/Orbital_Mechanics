import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;


/**
 * The plane class creates a triangular plane given a theta and magnitude on the screen. It is 
 * used to simulate oscillation along an inclined plane. 
 * 
 * @author Andrew M. 
 */

public class Plane {
	double theta; 
	double mag; 
	
	/**
	 * Sets the parameters of the plane. 
	 * 
	 * @param theta
	 * 	Angle for plane
	 * @param mag
	 * 	Magnitude (length) of hypotenuse of plane
	 */
	public Plane(double theta, double mag){
		this.theta = theta; 
		this.mag = mag; 
	}
	
	/**
	 * Uses trail to show plane. 
	 * 
	 * @param frame
	 * 	The frame to draw on. 
	 */
	public void draw(DisplayFrame frame){
		Trail outline = new Trail(); 
		outline.addPoint(0, 0);
		outline.addPoint(0, Math.sin(Math.toRadians(this.theta))*mag); //add endpoints 
		outline.addPoint(Math.cos(Math.toRadians(this.theta))*mag, 0);
		outline.addPoint(0, 0);
		frame.addDrawable(outline); //add trail to frame
	}
}
