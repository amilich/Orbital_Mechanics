package riemann_sum;

import java.util.Random;

import org.opensourcephysics.display.DrawableShape;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;


/**
 * Date: September, 2013
 *
 * This class contains a random rule that essentially makes up a rule. 
 * A random double is chosen - say 5/6, and then slice will return the 
 * area of a rectangle with a height calculated 5/6 of the way between 
 * the left and right endpoints. 
 * 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates a slice's area using the Random Rule
 * @method slicePlot 
 * 	Plots a slice using the Random Rule
 *
 */
public class RandomRule extends Riemann {
	Random random = new Random(); 
	double randNum = random.nextDouble(); //makes random rule 
	
	/** 
	 * finds a slice using the randomized rule set when the class is instantiated
	 * 
	 * @param poly 
	 * 	The polynomial used to slice
	 * @param sleft 
	 * 	Left endpoint of slice 
	 * @param sright 
	 * 	Righrt endpoint of slice
	 * 
	 * @see riemann_sum.Riemann#slice(polyfun.Polynomial, double, double)
	 * 
	 */
	@Override
	public double slice(Polynomial poly, double sleft, double sright) {
		return (sright-sleft)*PolyPractice.eval(poly, sleft + (sright-sleft)*randNum); //evaluate at left + difference between right and left * random number
	}
	
	/**
	 * Plots a slice on a pframe using the random rule
	 * 
	 * @param pframe 
	 * 	The plotframe to plot the slice on 
	 * @param poly 
	 * 	The polynomial used to calculate the slice
	 * @param sleft 
	 * 	The left endpoint of the slice 
	 * @param sright 
	 * 	The right endpoint of the slice
	 * 
	 * @see riemann_sum.Riemann#slicePlot(org.opensourcephysics.frames.PlotFrame, polyfun.Polynomial, double, double)
	 */
	@Override
	public void slicePlot(PlotFrame pframe, Polynomial poly, double sleft,
			double sright) {
		double centerX = (sright+sleft)/2; //average of left and right
		double centerY = PolyPractice.eval(poly, sleft + (sright-sleft)*randNum)/2; //evaluate at left + difference between right and left * random number 
		double width = Math.abs(sright-sleft); 
		double height = Math.abs(centerY*2); 

		DrawableShape sliceRec = DrawableShape.createRectangle(centerX,centerY,width,height); //the rectangle representing the slice
		pframe.addDrawable(sliceRec); 
		pframe.setVisible(true); //need to set it to be visible
	}

}
