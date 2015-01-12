
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;

/**
 * Contains ellipse object. 
 * 
 * @author Andrew M. 
 */
public class MyEllipse extends Trail implements Drawable {
	public double semi_major; 
	public double semi_minor; 
	public MyPoint center; 
	public MyPoint f1 = new MyPoint(); 
	public MyPoint f2 = new MyPoint(); 
	public MyPoint[] my = new MyPoint[4]; 
	double precision = 5E8; //Math.abs((Math.abs(points[0].x) + Math.abs(points[2].x))/(1)); 
	double h, k, a, b; 

	public MyEllipse(){
	}

	public String toString(){
		return "I am an ellipse with left " + my[0] + " and right " + my[1] + ".";
	}

	public MyEllipse(MyPoint[] points){
		//0 is left 
		//1 is top 
		//2 is right
		//3 is bottom
		this.my = points; 
		this.center = new MyPoint((points[0].x+points[2].x)/2, (points[1].y+points[3].y)/2); 
		this.semi_major = points[2].x - center.x; 
		this.semi_minor = points[1].y - center.y; 

		double f = Math.sqrt(this.semi_major*this.semi_major - this.semi_minor*this.semi_minor); 

		this.f1.x = center.x + f; 
		this.f1.y = center.y;
		this.f2.x = center.x - f; 
		this.f2.y = center.y;
		
		h = center.x; 
		k = center.y; 
		b = semi_minor; 
		a = semi_major; 
	}

	/**
	 * Plots ellipse. 
	 * 
	 * @param frame
	 * 	DisplayFrame to plot on. 
	 */
	public Trail plot() {
		//System.out.println("Precision: " + precision);

		Trail cur = new Trail(); 
		MyPoint p = new MyPoint();
		for (double ii = my[0].x; ii < my[2].x; ii += precision) {
			p.x = ii; 
			p.y = Math.sqrt((-Math.pow((ii-h), 2)*(b*b))/(a*a) + (b*b)) + k; 
			cur.addPoint(p.x, p.y);
			//System.out.println("Point: " + p.x + ", " + p.y);
		}
		for (double ii = my[2].x; ii > my[0].x; ii -= precision) {
			p.x = ii; 
			p.y = -Math.sqrt((-Math.pow((ii-h), 2)*(b*b))/(a*a) + (b*b)) + k; 
			cur.addPoint(p.x, p.y);
			//System.out.println("Point: " + p.x + ", " + p.y);
		}
		cur.addPoint(my[0].x, center.y);
		cur.color = pastel(); 
		cur.setStroke(new BasicStroke(3));
		return cur; 
	}

	int counter = 0; 
	public Trail rot_plot(Particle p){
		counter ++; 
		Trail cur = new Trail(); 
		for (MyPoint a : p.points) {
			cur.addPoint(a.x, a.y);
		}
		cur.color = pastel(); 
		cur.setStroke(new BasicStroke(3));
		
		AffineTransform.getRotateInstance(Math.PI / p.theta).createTransformedShape(new Ellipse2D.Double(p.points.get(0).x, p.points.get(0).y,p.points.get(1).x, p.points.get(1).y));
		
		return cur;
	}

	public void rot_show(Particle p, DisplayFrame frame){
		double period = 1.2*2*Math.PI*p.maxX/p.vector(p.v_x, p.v_y);
		if(p.time > period*2/3)
			frame.addDrawable(this.rot_plot(p));
	}

	/**
	 * Nice color for particles. 
	 * 
	 * @return
	 * 	New color. 
	 */
	private static Color pastel(){
		Random random = new Random(); //random gen 
		final float hue = random.nextFloat();
		final float saturation = (random.nextInt(2000) + 2000) / 10000f;
		final float luminance = 0.9f;
		final Color color = Color.getHSBColor(hue, saturation, luminance);
		return color; 
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
	private double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public void remove(DisplayFrame f){
		f.removeDrawable(this);
	}

	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		// TODO Auto-generated method stub
		panel.addDrawable(this.plot());
	}

	public void plot(DisplayFrame frame){
		frame.addDrawable(this.plot());
	}
}
