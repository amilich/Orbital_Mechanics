package riemann_sum;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/**
 * Date: September, 2013
 * 
 * The RightHandRule class uses the RightHandRule to find a Riemann sum.
 * It uses the value of the right endpoint of a subinterval to determine
 * the height of a rectangle (vs. the left endpoint in the LHR). 
 * It differs from the LeftHandRule class only in that it evaluates at 
 * 'sright' as opposed to 'sleft' in slice and slicePlot. 
 * 
 * RightHandRule extends Riemann and implements the abstract methods 
 * 'slice' and 'slicePlot'. 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates area of a slice using the RHR 
 * @method slicePlot 
 * 	Plots a slice using the RHR
 */

public class RightHandRule extends Riemann {
	/**
	 * Calculates the area of a slice using the RHR
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
	public double slice(Polynomial poly, double sleft, double sright) {
		return (sright-sleft)*poly.evaluate(sright).getTerms()[0].getTermDouble(); //area of rect using RHR
	}

	/**
	 * Graphs a slice using the RHR
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
		double centerX = (sright+sleft)/2; //average of left and right
		double centerY = poly.evaluate(sright).getTerms()[0].getTermDouble()/2; //evaluate RIGHT; center is divided by 2
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 

		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height);
		pframe.addDrawable(sliceRec); 
		pframe.setVisible(true); //need to set it to be visible
	}

}
