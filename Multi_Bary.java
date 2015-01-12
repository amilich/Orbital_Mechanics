import java.util.ArrayList;
import org.opensourcephysics.display.Circle;

/**
 * @author Andrew M. 
 */
public class Multi_Bary extends Circle {
	public Multi_Bary(ArrayList<Particle> bodies) {
		this.update(bodies);
	}

	public void update(ArrayList<Particle> bodies){
		MyPoint cent = COM(bodies); 
		this.setXY(cent.x, cent.y);
		this.pixRadius = 5; 
		//System.out.println("Bary: " + this);
	}

	private MyPoint COM(ArrayList<Particle> bodies){
		MyPoint com = new MyPoint(); 
		double c_x = 0;
		double c_y = 0;
		double M = 0;
		for (Particle particle : bodies) {
			M += particle.mass; 
		}
		double mrx = 0; 
		double mry = 0; 
		for (Particle particle : bodies) {
			mrx += particle.mass*particle.x_pos;
			mry += particle.mass*particle.y_pos;
		}
		c_x = mrx/M; 
		c_y = mry/M; 
		com.x = c_x; 
		com.y = c_y; 

		return com;
	}
}
