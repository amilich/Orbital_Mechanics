package riemann_sum;

import java.awt.BasicStroke;
import java.awt.Color;

import org.opensourcephysics.display.Trail;

/*
 * Name: Andrew Milich
 * Date: September, 2013
 * 
 * This abstract class contains the method for calculating a Reimann sum (rs) no matter the rule 
 * as well as the abstract methods implemented by each rule - slice plot and slice - that define
 * the individual rules. 
 */

public abstract class Riemann {
	public double rs(polyfun.Polynomial polynomial, double left, double right, int subintervals) {
		//Calculates a Riemann sum from a left endpoint, a right endpoint, 
		//a polynomial, and the number of subintervals. 
		double area = 0; //accumulative - makes returning easier 
		double delta = (right-left)/subintervals; //convenient for for loop
		for (double ii = 0; ii < subintervals; ii ++) {
			area += this.slice(polynomial, left, left+delta); //this method is implemented by each rule class 
			//this.slice uses the rule's slice method to calculate the Riemann sum 
			left += delta; 
		}
		return area; 
	}

	public void rsAcc(org.opensourcephysics.frames.PlotFrame pframe, polyfun.Polynomial poly, int index, double precision, double base) {
		//this adds accumulated area to the accumulation function - using separate riemann sums 
		//instead of adding slices allows using negative area far more easily
		double x = 10;  //this allows a better view of the accumulation function 
		Trail accTrail = new Trail(); 
		for (double ii = base-x; ii <= base+x; ii += precision) {
			double curRs = this.rs(poly, base, ii, 200); //current riemann sum - uses 250 
			pframe.append(index, ii, curRs); //adds a point
			accTrail.addPoint(ii, curRs); //adds to the trail showing the accumulation function 
		} 
		accTrail.setStroke(new BasicStroke(2)); 
		pframe.addDrawable(accTrail);
		pframe.setMarkerSize(index, 1);
		accTrail.color = Color.blue; 
		
		//autoscale the graph
		pframe.setAutoscaleX(true); 
		pframe.setAutoscaleY(true); 
		pframe.setVisible(true); //sets the graphs visible 
	}

	public void rsPlot(org.opensourcephysics.frames.PlotFrame pframe, polyfun.Polynomial poly, int index, double precision, double left, double right, int subintervals) {
		//Plot a particular Riemann sum from a data set index, a precision (used to graph polynomial), 
		//a left endpoint, a right endpoint, and a number of subintervals. 
		double delta = (right-left)/subintervals; //convenient for for loop
		double leftStep = left; 
		for (int ii = 0; ii < subintervals; ii++) { //steps through subintervals and adds plots for each subinterval  
			this.slicePlot(pframe, poly, leftStep,leftStep+delta); 
			leftStep += delta; 
		}

		Trail polyTrail = new Trail(); 
		//append points to the plot frame
		double yCoord = 0; 
		for (double ii = left; ii < right; ii += precision){
			yCoord = PolyPractice.eval(poly, ii); //this makes graphing faster
			polyTrail.addPoint(ii, yCoord);
			pframe.append(index, ii, yCoord); //now adds point to graph 
		}
		pframe.addDrawable(polyTrail); 
		
		//graphs the poly with points 
		pframe.setMarkerSize(index, 1); //set point size
		pframe.setMarkerColor(index, Color.black); //set point color
		
		pframe.setVisible(true); 
	}

	abstract double slice(polyfun.Polynomial poly, double sleft, double sright); //abstract slice - implemented by each rule - calculates area of a slice of a Riemann sum 

	abstract void slicePlot(org.opensourcephysics.frames.PlotFrame pframe, polyfun.Polynomial poly, double sleft, double sright); //plots a slice - abstract method implemented in each rule  
}
