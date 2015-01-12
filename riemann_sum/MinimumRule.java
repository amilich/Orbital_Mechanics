package riemann_sum;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/** 
 * Calculates a Riemann sum using slices with 
 * the height of the minimum value of the function 
 * over that range. 
 * 
 * @author Andrew M. 
 * @method slice 
 * 	Calculates the area of a slice 
 * @method slicePlot
 * 	Plots a slice using the minimum rule
 *
 */
public class MinimumRule extends Riemann { 
	double precision = 0.005; //precision to find min and max of each subinterval 

	/**
	 * Makes a slice with the minimum value in a particular range
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
		return (sright-sleft)*localMin(poly, sleft, sright); //evaluates area of rectangle at minimum value
	}

	/**
	 * Plots a slice using the minimum rule; uses localMin below
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
		double centerX = (sright+sleft)/2; //average of left + right
		double centerY = localMin(poly, sleft, sright)/2; 
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 
		//plots the minimum rectangle
		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height);
		pframe.addDrawable(sliceRec); 
		pframe.setVisible(true); 
	}

	/**
	 * Finds the minimum of a polynomial in a given range of left and right
	 * 
	 * @param poly
	 * 	The poly to evaluate the min. 
	 * @param left
	 * 	The left of the subinterval. 
	 * @param right
	 * 	The right of the subinterval. 
	 * @return
	 * 	The minimum value f(x). 
	 */
	public double localMin(Polynomial poly, double left, double right){
		double minVal = PolyPractice.eval(poly, left); 
		//System.out.println(left);
		double currentVal = PolyPractice.eval(poly, left); 
		//samples a bunch of points and finds minimum
		for (double ii = left; ii <= right; ii += precision) { //simple sorting algorithm - if new value is greater, replace with current one
			currentVal = PolyPractice.eval(poly, ii); 
			if (currentVal < minVal)
				minVal = currentVal; 
		}
		return minVal;
	}
}
