package riemann_sum;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/** 
 * Uses the maximum rule to calculate a Riemann sum. 
 * 
 * @author Andrew
 * @method slice 
 * 	Calculates the area of a slice 
 * @method slicePlot
 * 	Plots a slice using the maximum rule
 *
 */
public class MaximumRule extends Riemann {
	double precision = 0.005; //precision to find min and max of each subinterval - should not drastically affect solutions

	/**
	 * Makes a slice with the maximum value in a particular range
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
		//evaluates a slice (rectangle) using localMax (similar to localMin)
		//see MinimumRule.java 
		return (sright-sleft)*localMax(poly, sleft, sright); 
	}
	
	/**
	 * Plots a slice using the maximum rule; uses localMax below
	 * 
	 * @param pframe 
	 * 	The plotframe used to plot 
	 * @param poly 
	 * 	The polynomial used to plot the slice 
	 * @param sleft 
	 * 	Left endpoint of slice 
	 * @param sright 
	 * 	Right endpoint of the slice
	 * 
	 * @see riemann_sum.Riemann#slicePlot(org.opensourcephysics.frames.PlotFrame, polyfun.Polynomial, double, double)
	 */
	@Override
	public void slicePlot(PlotFrame pframe, Polynomial poly, double sleft,
			double sright) {		
		//similar in essence to all other slicePlots but uses localMax
		double centerX = (sright+sleft)/2; //average of left + right
		double centerY = localMax(poly, sleft, sright)/2; 
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 

		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height);
		pframe.addDrawable(sliceRec); //add rectangle to pframe
		pframe.setVisible(true); 
	}

	/**
	 * Finds maximum value of a subinterval. 
	 * 
	 * @param poly
	 * 	The poly to evaluate the max. 
	 * @param left
	 * 	The left of the subinterval. 
	 * @param right
	 * 	The right of the subinterval. 
	 * @return
	 * 	The maximum value f(x). 
	 */
	public double localMax(Polynomial poly, double left, double right){
		double maxVal = PolyPractice.eval(poly, left); 
		//finds maximum of an interval 
		double currentVal = left; 
		for (double ii = left; ii <= right; ii += precision) {
			currentVal = PolyPractice.eval(poly, ii); 
			if (currentVal > maxVal) //if value at current point is greater, replace it with the old highest one
				maxVal = currentVal; 
		}
		return maxVal;
	}
}
