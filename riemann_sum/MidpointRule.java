package riemann_sum;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/**
 * Date: September, 2013
 * 
 * The MidpointRule class evaluates Riemann sums using the midpoint of 
 * a subinterval. It is similar to the RHR and LHR classes, but it uses 
 * the average of sleft and sright to find the height of a rectangle in 
 * slice and sliceplot as opposed to sleft or sright. 
 * 
 * MidpointRule extends Riemann and implements the abstract methods 
 * 'slice' and 'slicePlot'. 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates the area of a slice using the midpoint rule
 * @method slicePlot
 * 	Plots a rectangle on the particular slice
 */

public class MidpointRule extends Riemann { //takes a slice with the height evaluated at the average of sleft and sright 
	/**
	 * Makes a slice with the midpoint in a particular range
	 * 
	 * @param poly 
	 * 	The polynomial used to calculate the slice 
	 * @param sleft 
	 * 	The left endpoint of the slice 
	 * @param sright 
	 * 	The right endpoint of the slice
	 *  
	 * @see riemann_sum.Riemann#slice(polyfun.Polynomial, double, double)
	 */
	@Override
	public double slice(Polynomial poly, double sleft, double sright) {
		return (sright-sleft)*PolyPractice.eval(poly, (sright+sleft)/2); //area of rectangle with height as function's value at midpoint
	}

	/**
	 * Graphs a slice using the midpoint rule
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
	public void slicePlot(PlotFrame pframe, Polynomial poly, double sleft,
			double sright) { 
		double centerX = (sright+sleft)/2; //average of left + right
		double centerY = poly.evaluate(centerX).getTerms()[0].getTermDouble()/2; //evaluate midpoint; divide by 2 for center coordinate for rectangle
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 

		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height); //make rectangle with above coordinates
		pframe.addDrawable(sliceRec); 
		pframe.setVisible(true); 
	}
	
}
