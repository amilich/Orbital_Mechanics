

import java.util.ArrayList;

import org.opensourcephysics.frames.DisplayFrame;

/**
 * Proof of Kepler's First Law of Planetary Motion: planets orbit in ellipses. 
 * 
 * @author Andrew M. 
 */
public class Law_One_Done extends Law {	
	private MyEllipse orb; 
	private double myTime = 0; 

	/**
	 * Creates new PlotFrame object to prove law one. 
	 * 
	 * @param a
	 * 	X label of graph. 
	 * @param b
	 * 	Y label of graph. 
	 * @param c
	 * 	Title of graph. 
	 */
	public Law_One_Done(String a, String b, String c, ArrayList<Particle> bodies){
		super(a, b, c); 
		this.myTime = bodies.get(0).time; 
		setLocation(600, 400);
		setVisible(true);
		super.setPreferredMinMaxY(0.9999, 1.0001);
	}

	int counter = 0; 

	/**
	 * Proves law one by calculating a, b and showing that motion follows elipse's equation.  
	 * 
	 * @param bodies
	 * 	ArrayList of Particles. 
	 */
	public void Prove(ArrayList<Particle> bodies, DisplayFrame frame){
		counter ++; 
		for (int ii = 1; ii < bodies.size(); ii++) { //TODO: CHECK CENTER POINT
			double h, k; //center of ellipse 
			h = (bodies.get(ii).maxX + bodies.get(ii).minX)/2; 
			k = (bodies.get(ii).maxY + bodies.get(ii).minY)/2;
			double a, b; //a and b values of ellipse 
			a = dist(h, 0, bodies.get(ii).maxX, 0); 
			b = dist(0, k, 0, bodies.get(ii).maxY); 
			double x_div_a = Math.pow((bodies.get(ii).x_pos - h), 2)/Math.pow(a, 2); //x^2 / a^2  
			double y_div_b = Math.pow((bodies.get(ii).y_pos - k), 2)/Math.pow(b, 2); //y^2 / b^2 
			if((x_div_a + y_div_b < 1.5) && (x_div_a + y_div_b > 0.5)) {
				append(ii, bodies.get(ii).time-myTime, (x_div_a + y_div_b));
				if(counter % 300 == 0 && bodies.get(ii).time-myTime < 
						(2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
					frame.clearDrawables();
					frame.removeDrawable(orb); 
					counter = 0; 
					orb = new MyEllipse(new MyPoint[]{new MyPoint(-a+h, k), new MyPoint(h, bodies.get(ii).maxY+k), new MyPoint(a+h, k), new MyPoint(h, -bodies.get(ii).maxY+k)});
					orb.plot(frame);

				}
				else if (bodies.get(ii).time-myTime < bodies.get(ii).deltaT +  
						(2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
				}
			}
		}
		repaint(); 
	}

	public void rot_plot(ArrayList<Particle> bodies, DisplayFrame frame){
		counter ++; 
		for(int ii = 0; ii < bodies.size(); ii ++){
			double h, k; //center of ellipse 
			h = (bodies.get(ii).maxX + bodies.get(ii).minX)/2; 
			k = (bodies.get(ii).maxY + bodies.get(ii).minY)/2;
			@SuppressWarnings("unused")
			double a, b; //a and b values of ellipse 
			a = dist(h, 0, bodies.get(ii).maxX, 0); 
			b = dist(0, k, 0, bodies.get(ii).maxY); 
			orb = new MyEllipse(new MyPoint[]{new MyPoint(-a+h, k), new MyPoint(h, bodies.get(ii).maxY+k), new MyPoint(a+h, k), new MyPoint(h, -bodies.get(ii).maxY+k)});
			if(counter % 300 == 0){
				orb.rot_show(bodies.get(ii), frame);
				counter = 0; 
			}
		}
	}

	/**
	 * In Law 1, corollary is that the sun is a focus of ellipse, so this proves using distance to sun.   
	 * 
	 * @param bodies
	 * 	ArrayList of Particles. 
	 */
	protected void ProveDist(ArrayList<Particle> bodies){
		for (int ii = 1; ii < bodies.size(); ii++) { //CHECK CENTER POINT
			append(ii, bodies.get(ii).time, dist(bodies.get(ii), bodies.get(0))); 
		}
		repaint(); 
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
	static double dist(Particle a, Particle b) {
		double dist = Math.sqrt(Math.pow(a.getX_pos() - b.getX_pos(), 2) + Math.pow(a.getY_pos() - b.getY_pos(), 2));
		return dist; // distance formula
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
	static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	@Override
	public void Prove(ArrayList<Particle> bodies) {
		// TODO Auto-generated method stub

	}

}
