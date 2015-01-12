import org.opensourcephysics.display.Circle;

/**
 * Contains a Barycenter object. 
 * 
 * @author Andrew M. 
 */
public class Barycenter extends Circle {
	public int i1, i2; 
	
	/**
	 * @param a
	 * 	Particle 1 of system. 
	 * @param b
	 * 	Particle 2 of system. 
	 */
	public Barycenter(Particle a, Particle b){
		Particle p1; 
		if(a.mass > b.mass){
			p1 = a; 
		}
		else {
			p1 = b; 
		}

		//based on http://en.wikipedia.org/wiki/Barycentric_coordinates_(astronomy)
		double b_r = (dist(a, b)*b.mass)/(a.mass + b.mass); 

		//create line between particles 
		MyLine line = new MyLine(a.x_pos, a.y_pos, b.x_pos, b.y_pos); 
		int sign; 
		if(a.x_pos < b.x_pos)
			sign = 1; 
		else 
			sign = -1; 

		double cX = p1.x_pos + sign*b_r/Math.sqrt(1+Math.pow(line.getSlope(), 2)); 
		double cY = line.calculateY(cX); 
		this.setXY(cX, cY);
		this.pixRadius = 5; 
	}
	
	/**
	 * Moves barycenter. 
	 * 
	 * @param a
	 * 	Particle 1 of system. 
	 * @param b
	 * 	Particle 2 of system. 
	 */
	public void update(Particle a, Particle b){
		Particle p1; 
		if(a.mass > b.mass){
			p1 = a; 
		}
		else {
			p1 = b; 
		}
		double b_r = (dist(a, b)*b.mass)/(a.mass + b.mass); 

		MyLine line = new MyLine(a.x_pos, a.y_pos, b.x_pos, b.y_pos); 
		int sign; 
		if(a.x_pos < b.x_pos)
			sign = 1; 
		else 
			sign = -1; 
		double cX = p1.x_pos + sign*b_r/Math.sqrt(1+Math.pow(line.getSlope(), 2)); 
		double cY = line.calculateY(cX); 
		this.setXY(cX, cY); //set xy (extends method in OSP circle class) 
		this.pixRadius = 5; 
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
	private double dist(Particle a, Particle b) {
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2)
				+ Math.pow(a.getY_pos() - b.getY_pos(), 2));
		return dist; // distance formula
	}
}
