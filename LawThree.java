import java.util.ArrayList;

/**
 * Proof of Kepler's Third Law of Planetary Motion. 
 * 
 * @author Andrew M. 
 */
public class LawThree extends Law {	
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
	public LawThree(String a, String b, String c){
		super(a, b, c); 
		setLocation(1200, 0);
		setVisible(true);
	}

	private double G = 6.67384E-11; //universal gravitational constant
	private double period_time = 1; 

	/**
	 * Proves third law by calculating period, semimajor axis and showing proportionality. 
	 * 
	 * @param bodies
	 * 	ArrayList of Particles. 
	 */
	public void Prove(ArrayList<Particle> bodies){

		for (int ii = 1; ii < bodies.size(); ii++) {
			//double period = 2*Math.PI*bodies.get(ii).maxX/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y); 
			double h = (bodies.get(ii).maxX + bodies.get(ii).minX)/2; 
			double k = (bodies.get(ii).maxY + bodies.get(ii).minY)/2;
			double constant_left = (G*bodies.get(0).mass)/(4*Math.pow(Math.PI, 2));
			double maxX = bodies.get(ii).maxX - bodies.get(0).x_pos; 
			double constant_right = (Math.pow(maxX, 3))/(Math.pow(period_time, 2)); 
			double a, b; //a and b values of ellipse 
			a = dist(h, 0, bodies.get(ii).maxX, 0); 
			b = dist(0, k, 0, bodies.get(ii).maxY); 
			double x_div_a = Math.pow((bodies.get(ii).x_pos - h), 2)/Math.pow(a, 2); //x^2 / a^2  
			double y_div_b = Math.pow((bodies.get(ii).y_pos - k), 2)/Math.pow(b, 2); //y^2 / b^2 
			//constant_left = track(constant_left, constant_right)[0]; 
			//constant_right = track(constant_left, constant_right)[0]; 
			if(bodies.get(ii).time < (2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
				period_time = bodies.get(ii).time;
			}
			else if (bodies.get(ii).time > period_time && bodies.get(ii).time < bodies.get(ii).deltaT +  
					(2*Math.PI*(bodies.get(ii).maxX - h)/bodies.get(ii).vector(bodies.get(ii).v_x, bodies.get(ii).v_y))){
				double inAU = maxX/149597870700.0; 
				System.out.println();
				System.out.println();
				System.out.println("Semimajor axis: " + a); 
				System.out.println("Semiminor axis: " + b);

				/*MyEllipse orb = new MyEllipse(new Point[]{new Point(-a+h, k), new Point(h, bodies.get(ii).maxY+k), new Point(a+h, k), new Point(h, -bodies.get(ii).maxY+k)});
				System.out.println("F1: (" + orb.f1.x + ", " + orb.f1.y + ").");
				System.out.println("F2: (" + orb.f2.x + ", " + orb.f2.y + ").");*/

				System.out.println("Period in sec: " + period_time + ", in days: " + (period_time)/(60*60*24) + ", in years: " + (period_time)/(365*60*60*24));
				System.out.println("GM / 4pi^2 : " + constant_left);
				System.out.println("a^3 / t^2: " + constant_right);
				System.out.println("Ellipse Equation: x^2 / " + (a*a) + " + y^2 / " + (b*b) + " = " + (x_div_a+y_div_b)); 
				System.out.println("p^2 = a^3: " + (period_time*period_time) + " = " + (Math.pow(inAU, 3)));

				MyEllipse my = new MyEllipse(new MyPoint[]{new MyPoint(-a, 0), new MyPoint(0, b), new MyPoint(a, 0), new MyPoint(0, -b)}); 
				double c_dist = dist(my.center.x, my.center.y, my.f1.x, my.f1.y);
				double a_dist = Math.abs(dist(0, b, my.f1.x, my.f1.y));
				System.out.println("Eccentricity is: " + (c_dist/a_dist));
				System.out.println("F1: " + my.f1);
				System.out.println("F2: " + my.f2);
			}
			//append(ii, bodies.get(ii).time, Math.pow(period, 2)/Math.pow((a), 3));
			append(ii, bodies.get(ii).time, constant_left); 
			append(ii, bodies.get(ii).time, constant_right); 
		}
		repaint();
	}

	/*public double[] track(double x, double y){

	}*/

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
}
