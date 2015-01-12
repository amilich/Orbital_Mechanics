package riemann_sum;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/**
 * Date: September, 2013
 * 
 * The LeftHandRule class implements the LeftHandRule for finding Riemann sums.
 * As discussed in class, it uses the value of the left endpoint of a subinterval 
 * to determine the height of the rectangle (as opposed to the right endpoint). 
 * It differs from the RightHandRule.java class only in that it evaluates at 
 * 'sleft' as opposed to 'sright' in slice and slicePlot. 
 * 
 * LeftHandRule extends Riemann and implements the abstract methods 'slice' and 
 * 'slicePlot'. 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates the area of a slice using the LHR
 * @method slicePlot
 * 	Plots a rectangle on the slice
 */

public class LeftHandRule extends Riemann {
	/**
	 * Calculates the area of a slice using the LHR
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
		return (sright-sleft)*PolyPractice.eval(poly, sleft); //(b-a)*polynomial evaluated at left endpoint of subinterval
	}

	/**
	 * Graphs a slice using the LHR
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
		//plots a slice using the LHR
		double centerX = (sright+sleft)/2; //average of left + right - rectangle is defined by its center
		double centerY = PolyPractice.eval(poly, sleft)/2; //evaluate LEFT; divide by 2
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 

		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height);
		pframe.addDrawable(sliceRec); //shows the rectangle
		pframe.setVisible(true); 
	}
}
