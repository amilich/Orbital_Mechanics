import org.opensourcephysics.frames.DisplayFrame;


/**
 * Tests MyEllipse class. 
 * 
 * @author Andrew M. 
 */
public class EllipseTest {
	private boolean DEBUG_MODE = false; //print debug statements. 
	final private static double SUN_MASS = 1.98892E30; //kg
	final private static double EARTH_MASS = 5.9742E24; //kg
	final private static double EARTH_ORBIT = 1.5E11; //m
	final private static double EARTH_TANGENTIAL_VELOCITY = 30000; // m/s
	final private double COLLISION_RAD = 1;

	final private static int[] FRAME_LOC = { 0, 0 }; //location of DisplayFrame on screen 
	final private static int[] FRAME_DIMENSIONS = { 600, 700 }; //size of DisplayFrame 
	final private static double[] FRAME_MINMAX = { -1.2 * EARTH_ORBIT,
		1.2 * EARTH_ORBIT, -1.2 * EARTH_ORBIT, 1.2 * EARTH_ORBIT }; //minmax of frame 
	
	public static void main(String[] args){
		MyEllipse el = new MyEllipse(new MyPoint[]{new MyPoint(-EARTH_ORBIT, 0), new MyPoint(0, EARTH_ORBIT), new MyPoint(EARTH_ORBIT, 0), new MyPoint(0, -EARTH_ORBIT)}); 
		DisplayFrame f = new DisplayFrame("", "", ""); 
		el.plot(f); //plot new ellipse 
		f.setPreferredMinMax(FRAME_MINMAX[0], FRAME_MINMAX[1], FRAME_MINMAX[2], FRAME_MINMAX[3]);
		f.setVisible(true);
	}
}
