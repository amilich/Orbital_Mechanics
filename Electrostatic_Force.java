
/**
 * Electrostatic force for charge simulation. 
 * 
 * @author Andrew M. 
 */
public class Electrostatic_Force {
	static double k = 9E9; 
	
	public Force ES_Force(Particle q1, Particle q2){
		return new Force(k*q1.charge*q1.charge/Math.pow((dist(q1, q2)), 2), calcAng(q1, q2), "ES Force");
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
	private static double calcAng(Particle a, Particle b) {
		double xDiff = b.x_pos - a.x_pos; // find delta x
		double yDiff = b.y_pos - a.y_pos; // find delta y
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));
		// angle = Math.round(angle*100)/100;
		return angle; // inverse tangent of y/x
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
	private static double dist(Particle a, Particle b) {
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2)
				+ Math.pow(a.getY_pos() - b.getY_pos(), 2));
		return dist; // distance formula
	}

}
