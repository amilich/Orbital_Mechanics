import java.util.ArrayList;

/**
 * Proof of Kepler's Second Law of Planetary Motion: planets sweep out equal areas over an equal amount of time. 
 * 
 * @author Andrew M. 
 */
public class LawTwo extends Law {	
	/**
	 * Creates new PlotFrame object to prove law. 
	 * 
	 * @param a
	 * 	X label of graph. 
	 * @param b
	 * 	Y label of graph. 
	 * @param c
	 * 	Title of graph. 
	 */
	public LawTwo(String a, String b, String c){
		super(a, b, c); 
		setLocation(900, 400);
		setVisible(true);
	}

	/**
	 * Proves law by calculating equal area over time step. 
	 * 
	 * @param bodies
	 * 	ArrayList of bodies used for proof. 
	 */
	public void Prove(ArrayList<Particle> bodies){
		for (int ii = 1; ii < bodies.size(); ii++) {				//CHECK CENTER POINT
			append(ii, bodies.get(ii).time, tri_area(new MyPoint(0, 0), 
					new MyPoint(bodies.get(ii).x_pos, bodies.get(ii).y_pos), 
					new MyPoint(bodies.get(ii).prev.x, bodies.get(ii).prev.y)));
		}
		repaint(); 
	}

	/**
	 * Calculates the area of a triangle from three points. 
	 * 
	 * @param a
	 * 	Point one. 
	 * @param b
	 * 	Point two. 
	 * @param c
	 * 	Point three. 
	 * @return
	 * 	The area of the triangle. 
	 */
	private double tri_area(MyPoint a, MyPoint b, MyPoint c){
		return Math.abs((a.x-c.x)*(b.y-a.y)-(a.x-b.x)*(c.y-a.y))*0.5;
	}
}
