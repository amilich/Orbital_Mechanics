package riemann_sum;

import java.awt.BasicStroke;
import java.awt.Color;

import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.PlotFrame;

import polyfun.Polynomial;

/**
 * Date: September, 2013
 *
 * Simpson's Rule is another method of approximating the area under the curve 
 * of a polynomial. It differs from the simpler LHR, RHR, midpoint rule, and 
 * trapezoid rule in that it takes a subinterval and calculates a quadratic 
 * that best fits that area of the graph. 
 * 
 * It uses the left endpoint of the subinterval (a,f(a)), the right endpoint 
 * (b,f(b)), and the midpoint of the subinterval (m,f(m)) to calculate a 
 * polynomial's equation. This class essentially calculates polynomial lines 
 * of best fit for each subinterval as opposed to rectangles. 
 * 
 * Numerically, the values obtained by Simpson's Rule is equal to (2M+T)/3 
 * where M is the value obtained from the Midpoint Rule and T is the value 
 * obtained from the Trapezoid Rule. This class uses this formula ((2M+T)/3)
 * to calculate the Riemann sum but uses the quadratic interpolations to 
 * graph the Riemann sum. 
 * 
 * see: 
 * 	http://en.wikipedia.org/wiki/Simpson's_rule
 * 	http://mathworld.wolfram.com/SimpsonsRule.html
 * 
 * SimpsonsRule also implements the abstract methods 'slice' and 'slicePlot' 
 * from Riemann. Due to the complexity of the rule, it also contains other
 * methods used to evaluate the quadratic (Lagrange) polynomial interpolation. 
 * 
 * @author Andrew Milich
 * @method slice 
 * 	Calculates the area of a slice using a quadratic approximation 
 * @method slicePlot
 * 	Plots the quadratic approximating the subinterval 
 * @method evalQuadFit
 * 	Calculates the approximation quadratic for a function
 */

public class SimpsonsRule extends Riemann {
	/** 
	 * This method is essentially the same as the other slice methods - it 
	 * calculates the area of one section of a Riemann sum bounded by sleft 
	 * and sright. As above, it uses the definition of Simpsons Rule as a 
	 * weighted average of the Trapezoid and Midpoint rules. 
	 * 
	 * @param poly
	 * 	The polynomial used to evaluate the slice
	 * @param sleft
	 * 	The left endpoint of the slice 
	 * @param sright 
	 * 	The right endpoint of the slice
	 * 
	 * @see riemann_sum.Riemann#slice(polyfun.Polynomial, double, double)
	 */
	@Override
	public double slice(Polynomial poly, double sleft, double sright) { 
		//does Simpson slice as approximation using midpoint and trapezoid rules
		//Simpsons rule ends up simplifying mathematically to (2*midpoint rule + trapezoid rule)/3
		MidpointRule mr = new MidpointRule(); 
		TrapezoidRule tr = new TrapezoidRule(); 
		return 2*(mr.slice(poly, sleft, sright)/3) + (tr.slice(poly, sleft, sright)/3); 
	}

	/**
	 * This method performs in the same manner to the other slicePlots, but it graphs
	 * a fitted polynomial instead of a rectangle over a subinterval. It then graphs
	 * trails to fill in the polynomials used to approximate the function. 
	 *   
	 * @param pframe
	 * 	The plotframe used to graph the slices
	 * @param poly
	 * 	The polynomial used to graph the slices 
	 * @param sleft 
	 * 	Left endpoint of slice 
	 * @param sright 
	 * 	Right endpoint of slice
	 * 
	 * @see riemann_sum.Riemann#slicePlot(org.opensourcephysics.frames.PlotFrame, polyfun.Polynomial, double, double)
	 */
	@Override
	public void slicePlot(PlotFrame pframe, Polynomial poly, double sleft, double sright){ 
		//# of subintervals for each rs and the data set are not parameters, so must define them here
		int dataSet = 1000; 
		//graph actual function
		graphQuad(pframe, poly, dataSet, sleft, sright); //put it in dataset 1000 to keep it by itself - somewhat arbitrary 

		double precision = 0.02; //precision with which to fill in the polynomials - not that important
		//use trails to display area under polynomial 
		Trail[] trails = new Trail[(int) ((sright-sleft)/precision)];
		for (int jj = 0; jj < trails.length; jj++){
			trails[jj] = new Trail(); 
			trails[jj].setStroke((new BasicStroke(3)));
		}
		double left = sleft; 

		//fill each polynomial to show the area under the curve
		for (int ii = 0; ii < trails.length; ii++){ 
			trails[ii].addPoint(left, 0); 
			trails[ii].addPoint(left, PolyPractice.eval(evalQuadFit(poly, sleft, sright), left+precision));
			trails[ii].color = Color.red; 
			pframe.addDrawable(trails[ii]); 
			left += precision; 
		}
	}

	/**
	 * This function graphs a fitted Lagrange quadratic over one subinterval. 
	 * It uses the method 'evalQuadFit' below to calculate the y-values of 
	 * the fitted polynomial. 
	 * 
	 * @param pframe
	 * 	The plot frame to draw on 
	 * @param poly
	 * 	The polynomial used to calculate the overall Riemann sum
	 * @param dataSet
	 * 	The data set (needed to plot points, change colors, change size, etc.) 
	 * @param a
	 * 	The left endpoint of a subinterval 
	 * @param b
	 * 	The right endpoint of a subinterval 
	 */
	public void graphQuad(PlotFrame pframe, Polynomial poly, int dataSet, double sleft, double sright){
		//graphs a single quadratic approximation for a given interval
		pframe.setMarkerSize(dataSet, 1);	
		pframe.setMarkerColor(dataSet, Color.red); 
		
		//first graph the actual polynomial used to find the Riemann sum
		//this polynomial is fitted to the 3 points of the subinterval
		Polynomial fitted = evalQuadFit(poly, sleft, sright); 

		for(double ii = sleft; ii < sright; ii += 0.01){ 
			pframe.append(dataSet, ii, PolyPractice.eval(fitted, ii)); //add points to graph using Lagrange fit
		}

		//now find the fitted poly 
		pframe.setMarkerSize(dataSet, 1); 
		pframe.setMarkerColor(dataSet, Color.red); 
		pframe.setMarkerColor(1001, Color.black); 
		//these are the three points used to fit the quadratic - they appear as green and black 
		pframe.append(1001, sleft, PolyPractice.eval(fitted, sleft));
		pframe.append(1001, sright, PolyPractice.eval(fitted, sright));
		pframe.append(1001, (sleft+sright)/2, PolyPractice.eval(fitted, (sleft+sright)/2));
	}

	/**
	 * This function is the same as above except it returns a polynomial. 
	 * 
	 * @param poly
	 * 	The polynomial function used to calculate the overall Riemann sum. 
	 * @param x
	 * 	The x-coordinate to evaluate the interpolated quadratic at. 
	 * @param a
	 * 	The left endpoint of the subinterval (same as sleft above)
	 * @param b
	 * 	The right endpoint of the subinterval (same as sright above)
	 * @return
	 * 	Returns the fitted quadratic as an object of type polynomial.  
	 */
	public Polynomial evalQuadFit(Polynomial poly, double X1, double X3){
		//this takes two points (a, b) and uses them and their midpoint to fit a quadratic and evaluate it at x
		//http://mathworld.wolfram.com/LagrangeInterpolatingPolynomial.html
		double X2 = (X1+X3)/2; 

		double Y1 = PolyPractice.eval(poly, X1); 
		double Y2 = PolyPractice.eval(poly, X2); 
		double Y3 = PolyPractice.eval(poly, X3); 
		//evaluates coefficients for Lagrange polynomial 
		double A =  ((Y2-Y1)*(X1-X3) + (Y3-Y1)*(X2-X1))/((X1-X3)*(Math.pow(X2, 2)-Math.pow(X1, 2)) + (X2-X1)*(Math.pow(X3, 2)-Math.pow(X1, 2))); 
		double B = ((Y2 - Y1) - A*(Math.pow(X2, 2) - Math.pow(X1, 2))) / (X2-X1); 
		double C = Y1 - A * Math.pow(X1, 2) - B*X1;

		//makes the actual polynomial. 
		Polynomial Lagrange = new Polynomial(new double[] {C, B, A});
		return Lagrange; 
	}


	/**
	 * Similar to above function but uses faster approximation - only does it for one point. 
	 * 
	 * @param poly
	 * 	Polynomial used for Riemann sum. 
	 * @param x
	 * 	Point to evaluate approximation at. 
	 * @param a
	 * 	Left endpoint of subinterval 
	 * @param b
	 * 	Right endpoint of subinterval. 
	 * @return
	 * 	Returns f(x) of input polynomial 
	 */
	public double evalPointFit(Polynomial poly, double x, double a, double b){
		double fx = 0; 
		double m = (a+b)/2; //midpoint as third point 
		double fa = PolyPractice.eval(poly, a); //evaluates Riemann poly at 3 points - used to make new quadratic 
		double fb = PolyPractice.eval(poly, b); 
		double fm = PolyPractice.eval(poly, m); 

		//this computes the polynomial approximation and evaluates it at x 
		fx = (fa*(x-m)*(x-b))/((a-m)*(a-b)); 
		fx += (fm*(x-a)*(x-b))/((m-a)*(m-b)); 
		fx += (fb*(x-a)*(x-m))/((b-a)*(b-m)); 
		return fx; 
	}
}
