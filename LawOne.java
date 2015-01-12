

import java.util.ArrayList;

import org.opensourcephysics.frames.DisplayFrame;

/**
 * Proof of Kepler's First Law of Planetary Motion: planets orbit in ellipses. 
 * 
 * @author Andrew M. 
 */
public class LawOne extends Law {	
	private double myTime = 0; 
	public ArrayList<MyEllipse> ellipses = new ArrayList<MyEllipse>(); 

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
	public LawOne(String a, String b, String c, ArrayList<Particle> bodies){
		super(a, b, c); 
		this.myTime = bodies.get(0).time; 
		setLocation(600, 400);
		setVisible(true);
		super.setPreferredMinMaxY(0.9999, 1.0001);
	}

	public int counter = 299; 
	public int tick = 1; 
	boolean notSet = true; 

	/**
	 * Proves law one by calculating a, b and showing that motion follows elipse's equation.  
	 * 
	 * @param bodies
	 * 	ArrayList of Particles. 
	 */
	private boolean setup = true; 
	public void Prove(ArrayList<Particle> bodies, DisplayFrame frame){
		counter ++; 
		if(bodies.size()-1 != ellipses.size()){
			ellipses.clear(); 
			//ellipses.add(new MyEllipse()); //so #'s are compatible
			for (int ii = 1; ii < bodies.size(); ii++) {
				ellipses.add(new MyEllipse());
				System.out.println("Added ellipse: " + bodies.size() + " , " + ellipses.size());
			}
			setup = true; 
		}
		for (MyEllipse e : ellipses) {
			frame.removeDrawable(e); 
		}
		for (int ii = 1; ii < bodies.size(); ii++) { //TODO: CHECK CENTER POINT
			double h, k; //center of ellipse 
			h = (bodies.get(ii).maxX + bodies.get(ii).minX)/2; 
			k = (bodies.get(ii).maxY + bodies.get(ii).minY)/2;
			double a, b; //a and b values of ellipse 
			a = dist(h, 0, bodies.get(ii).maxX, 0); 
			b = dist(0, k, 0, bodies.get(ii).maxY); 
			//double x_div_a = Math.pow((bodies.get(ii).x_pos - h), 2)/Math.pow(a, 2); //x^2 / a^2  
			//double y_div_b = Math.pow((bodies.get(ii).y_pos - k), 2)/Math.pow(b, 2); //y^2 / b^2 
			//if((x_div_a + y_div_b < 1.5) && (x_div_a + y_div_b > 0.5)) {
				//append(ii, bodies.get(ii).time-myTime, (x_div_a + y_div_b));
				//if(counter%tick == 0 || setup) {//&& bodies.get(ii).time-myTime < 
					//	(2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
					//System.out.println("Supposed to be setting: " + counter + ", " + tick  + ", " + ellipses.size());
					notSet = false; 
					//frame.clearDrawables();
					//frame.removeDrawable(ellipses.get(ii)); 
					counter = 0; 
					setup = false; 
					//System.out.println("SET: " + a + ", " + b);
					ellipses.set(ii-1, new MyEllipse(new MyPoint[]{new MyPoint(-a+h, k), new MyPoint(h, b+k), new MyPoint(a+h, k), new MyPoint(h, -b+k)}));
					//}
				//else if (bodies.get(ii).time-myTime < bodies.get(ii).deltaT +  
				//(2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
				//}
			//}
		}
		for (MyEllipse e : ellipses){
			//System.out.println(e);
			//if(e.f1 != null && e.f2 != null)
			if(e.semi_major != 0) {
				//System.out.println("Plot: " + e);
				//e.plot(frame);
			}
		}
		//repaint(); 
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
