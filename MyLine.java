/**
 * A 2-D line.  Behold the power of data abstraction.  Even though this
 * version of Line2D has different fields than the version
 * written for lab 4, the methods all work the same!  Try it with
 * LineTest.java from that lab.
 *
 * @author Jim Glenn
 * @version 0.1 10/3/2003
 */

public class MyLine {
	/**
	 * The coordinates of one point on this line.
	 */

	private double x1;
	private double y1;

	/**
	 * The coordinates of another point on this line.
	 */

	private double x2;
	private double y2;

	/**
	 * Constructs the line with the given slop and intercept.
	 *
	 * @param m the slope of the new line
	 * @param b the intercept of the new line
	 */

	public MyLine(double m, double b){
		// Convert from y = mx + b to (x1, y1) (x2, y2)

		x1 = 0;
		y1 = b;

		x2 = 1;
		y2 = m + b;
	}

	/**
	 * Constructs the line through the given points.
	 *
	 * @param x the x-coordinate of one point on the line
	 * @param y the y-coordinate of one point on the line
	 * @param x3 the x-coordinate of another point on the line
	 * @param y3 the y-coordinate of another point on the line
	 */

	public MyLine(double x, double y, double x3, double y3) {
		// arguments have same name as ("shadow") the fields, so we need
		// "this." to refer to the fields

		this.x1 = x;
		this.y1 = y;
		this.x2 = x3;
		this.y2 = y3;
	}

	/**
	 * Returns the slope of this line.
	 *
	 * @return the slope of this line
	 */

	public double getSlope() {
		return (y2 - y1) / (x2 - x1);
	}

	/**
	 * Returns the y-intercept of this line.
	 *
	 * @return the y-intercept of this line
	 */

	public double getIntercept() {
		return y1 - getSlope() * x1;
	}

	/**
	 * Returns the value of y so that this line
	 * goes through (x, y).
	 *
	 * @param x an x-coordinate
	 * @return the y-coordinate y so that this line passes through (x, y)
	 */

	public double calculateY(double x) {
		return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
	}
}