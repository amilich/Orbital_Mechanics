/**
 * Stores x and y coordinates in point object. 
 * 
 * @author Andrew M. 
 */
public class MyPoint {
	public double x, y; //point's Cartesian coordinates 
	
	/**
	 * Initializes new point. 
	 * 
	 * @param x_pos
	 * 	X pos of point. 
	 * @param y_pos
	 * 	Y pos of point. 
	 */
	public MyPoint(double x_pos, double y_pos){
		this.x = x_pos; 
		this.y = y_pos; 
	}
	
	/**
	 * Creates point with no coordinates. 
	 */
	public MyPoint(){
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "[" + this.x + ", y: " + this.y + "]"; 
	}
}
