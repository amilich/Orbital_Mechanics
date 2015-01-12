package riemann_sum;

import java.awt.BasicStroke;
import java.awt.Color;

import org.opensourcephysics.display.Trail; 
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/**
 * Date: September, 2013
 * 
 * The TrapezoidRule class evaluates Riemann sums by creating a trapezoid
 * out of a subinterval. The TrapezoidRule calculates area in 'slice' using 
 * the trapezoid area formula - h*(b1+b2)/2 where b1 and b2 are p(sleft) and 
 * p(sright) where p(x) is the polynomial used to calculate the Riemann sum. 
 * The Trapezoid Rule essentially calculates an average of the LHR and RHR - 
 * b1 in the trapezoid rule is essentially the height of a rectangle in the LHR; 
 * b2 is essentially the height of a rect. in the RHR. Thus the trapezoid rule 
 * is an average of the LHR and RHR. 
 * 
 * TrapezoidRule extends Riemann and implements the abstract methods 
 * 'slice' and 'slicePlot'. 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates area of a slice using the trapezoid rule 
 * @method slicePlot 
 * 	Plots a slice using the trapezoid rule
 */

public class TrapezoidRule extends Riemann {
	/**
	 * Calculates the area of a slice using the trapezoid rule
	 * 
	 * @see riemann_sum.Riemann#slice(polyfun.Polynomial, double, double)
	 * 
	 * @param poly
	 * 	The polynomial used to evaluate the slice
	 * @param sleft
	 * 	The left endpoint of the slice 
	 * @param sright 
	 * 	The right endpoint of the slice
	 */ 
	@Override
	//calculates the area of a trapezoid slice
	public double slice(Polynomial poly, double sleft, double sright) { 
		return ((PolyPractice.eval(poly, sleft) + 
				PolyPractice.eval(poly, sright))/2)*(sright-sleft); //runs trapezoid area formula - height*avg of bases
	}
	
	/**
	 * Graphs a slice using the trapezoid rule.  
	 * This method uses trails to outline a 
	 * trapezoid and then fills it in with 
	 * more trails. 
	 *  
	 * @see riemann_sum.Riemann#slicePlot(org.opensourcephysics.frames.PlotFrame, polyfun.Polynomial, double, double)
	 * 
	 * @param pframe
	 * 	The plotframe used to graph the slices
	 * @param poly
	 * 	The polynomial used to graph the slices 
	 * @param sleft 
	 * 	Left endpoint of slice 
	 * @param sright 
	 * 	Right endpoint of slice
	 */
	@Override
	void slicePlot(PlotFrame pframe, Polynomial poly, double sleft,
			double sright) {
		double precision = 0.001; 
		//use trails to display area of trapezoid
		Trail[] trails = new Trail[(int) ((sright-sleft)/precision)];
		for (int jj = 0; jj < trails.length; jj++){
			trails[jj] = new Trail(); 
		}
		double xStart = sleft; 
		double yStart = PolyPractice.eval(poly, sleft); 
		double xEnd = sright; 
		double yEnd = PolyPractice.eval(poly, sright); 

		//slicePlot takes the two endpoints of the trapezoid, evaluates them in the function, and then 
		//uses these values to create a line between the two top points of the function 
		//It then creates a line using a calculated slope and y intercept to fill in the trapezoid
		
		double m = (yStart-yEnd)/(xStart-xEnd); //slope of line on top of trapezoid
		double b = yEnd - m*xEnd; //y intercept of line on top of trapezoid
		Polynomial trapLine = new Polynomial(new double[] {b, m}); //used to fill in trapezoid

		double left = sleft+precision; 
		//goes through the trapezoid and sets the trails to the position to fill in the trapezoid
		for (int ii = 0; ii < trails.length; ii++){ 
			trails[ii].addPoint(left, 0); 
			trails[ii].addPoint(left, PolyPractice.eval(trapLine, left+precision));
			trails[ii].color = Color.red; 
			pframe.addDrawable(trails[ii]); 
			left += precision; 
		}
		
		//outlines in trapezoid with trails
		Trail outline = new Trail(); 
		outline.addPoint(sleft, 0); 
		outline.addPoint(sright, 0); 
		//finds each vertex of trapezoid and adds it to individual trail
		outline.addPoint(sright, PolyPractice.eval(poly, sright));
		outline.addPoint(sleft, PolyPractice.eval(poly, sleft)); 
		outline.addPoint(sleft, 0); 
		outline.color = Color.green;
		outline.setStroke(new BasicStroke(2)); 
		pframe.addDrawable(outline); 
		
		pframe.setVisible(true); 
	}

}
