package riemann_sum;

import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.*;

public class PolyPractice {

	/**
	 * Evaluates a polynomial at a given x-coordinate. 
	 * 
	 * @param p
	 * 	The polynomial used to evaluate. 
	 * @param x
	 * 	The x point to evaluate at 
	 * @return
	 * 	The y-coordinate of the point on the polynomial 
	 */
	public static double eval(Polynomial p, double x) {
		return p.evaluate(x).getTerms()[0].getTermDouble(); 
		//p.evaluate returns a Coeff; getTerms converts the coeff into a term; getTermDouble converts the term into a double
	}

	/**
	 * Grpahs a polynomial. 
	 * 
	 * @param pFrame
	 * 	The pframe to graph on. 
	 * @param p
	 * 	The poly to graph. 
	 * @param min
	 * 	The minimum of the graph. 
	 * @param max
	 * 	The maximum of the graph. 
	 * @param increment
	 * 	The increment between each data point on the grpah. 
	 * @param dataSet
	 * 	The dataset to add points to. 
	 */
	public static void graphPoly(PlotFrame pFrame, Polynomial p, double min, double max, double increment, int dataSet){ //graphs a polynomial 
		pFrame.setMarkerSize(dataSet, 1);
		Trail polyTrail = new Trail(); 
		for (double ii = min; ii <= max; ii += increment) {
			double y = eval(p, ii); 
			pFrame.append(dataSet, ii, y); // add points to graph
			polyTrail.addPoint(ii, y); 
		}

		pFrame.addDrawable(polyTrail); //also adds a trail to smooth the graph out (good for low precision)
		pFrame.setVisible(true);
	}

	/**
	 * Adds 1x^2 to a polynomial and graphs it. 
	 * 
	 * @param p
	 * 	The polynomial to which an x^2 is added.  
	 */
	public void addXsquared(Polynomial p) { 
		//easiest way is to make a new polynomial and add it to the old one
		Polynomial quad_poly = new Polynomial(new double[] {0, 0, 1}); 
		p = p.plus(quad_poly); 
		p.print(); // print out polynomial

		PlotFrame graph_frame = new PlotFrame("x", "y", "Poly graph");
		//graph the polynomial
		int max = 15; 
		int dataSet = 0; //for points
		for (double ii = -8*max/9; ii < 8*max/9; ii += 0.05) {
			graph_frame.append(dataSet, ii, eval(p, ii)); // add points to graph
			graph_frame.setMarkerSize(dataSet, 1);
		}
		graph_frame.setVisible(true);
	}
}
