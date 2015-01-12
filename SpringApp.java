import java.util.ArrayList;

import org.opensourcephysics.controls.AbstractSimulation;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.display.Trail;
import org.opensourcephysics.frames.DisplayFrame;
import org.opensourcephysics.frames.PlotFrame;

/**
 * @author Andrew M. 
 *
 */
public class SpringApp extends AbstractSimulation {
	DisplayFrame frame = new DisplayFrame("x", "y", "Frame");  //x-y, 2D pos frame
	PlotFrame plot = new PlotFrame("x", "y", "Frame"); 
	ArrayList<Mass> masses = new ArrayList<Mass>();  
	int springNum = 0; 
	double restLength = 0; 
	double initX = 0; 
	double mass; 
	double timeStep = 0.1; 
	double k; 
	public static double alpha = 0.0001;  
	public static double g = 10; 
	double largeMass; 
	Trail string = new Trail(); 

	protected void doStep(){
		for (int ii = 0; ii < masses.size(); ii++) {
			double upForce = 0; 
			double downForce = 0;

			for (int jj = 0; jj < masses.size(); jj++) {
				if(masses.get(ii).getY() > masses.get(jj).getY())
					downForce += masses.get(jj).mass*g;  
			}

			downForce += masses.get(ii).mass*g; //add on individual weight

			if(ii != 0){
				double dist = dist(masses.get(ii), masses.get(ii-1)); 
				double rest = restLength/springNum; 
				upForce = (dist-rest); 
				upForce *= k; 
				System.out.println("There is a dist of " + dist + " between spr " + ii + " and " + (ii-1));
				System.out.println("Dist - rest is " + dist); 
				System.out.println("Force of " + upForce + " k is " + k);
			}
			System.out.println("Upward force on spring # " + ii + " is " + upForce + "; y pos is " + masses.get(ii).y_pos);
			System.out.println("Downward force on spring # " + ii + " is " + downForce);
			double forceSum = upForce - downForce; 
			System.out.println("Force sum on spring # " + ii + " is " + forceSum);
			double acceleration = forceSum/masses.get(ii).mass; 
			//if (ii != 0)
			masses.get(ii).acc_y = acceleration;
			System.out.println("Acc of spring " + ii + " is " + acceleration);
			//F = kx 
			//F = ma
		}

		for (int ii = 0; ii < masses.size(); ii++) {
			masses.get(ii).Step(frame, true);
		}
		for (int ii = 0; ii < masses.size(); ii++) {
			//string.addPoint(masses.get(ii).getX_pos(), masses.get(ii).getY_pos());
		}
		plot.append(0, masses.get(masses.size()-1).time, masses.get(masses.size()-1).getY());
		//frame.addDrawable(string); 
	}

	public void initialize(){
		frame.setAutoscaleX(true);
		frame.setAutoscaleX(false);
		springNum = (int) control.getDouble("Number of masses"); 
		restLength = control.getDouble("Rest Length"); 
		initX = control.getDouble("Fixed end"); 
		mass = control.getDouble("Mass of Spring"); 
		timeStep = control.getDouble("Timestep"); 
		k = control.getDouble("Spring Constant"); 
		largeMass = control.getDouble("Large Mass"); 

		for (int ii = 0; ii < springNum; ii++) {
			masses.add(new Mass()); 
			masses.get(ii).mass = mass; 

			double equilibriumLength = mass*g+(ii-1)*mass*g; 
			equilibriumLength /= -k; 
			masses.get(ii).equibLength = equilibriumLength; 
			System.out.println("Equib of spring " + ii + " is " + equilibriumLength);
			if(ii == 0)
				masses.get(ii).init(initX, 0, 0, 0, 0, 0, mass, timeStep, alpha);
			else 
				masses.get(ii).init(initX, equilibriumLength, 0, 0, 0, 0, mass, timeStep, alpha);

			frame.addDrawable(masses.get(ii));
		}
		masses.get(masses.size()-1).mass = largeMass; 

		masses.get(masses.size()-1).setRadius(5);
		frame.setVisible(true); 
	}

	public void reset(){
		control.setValue("Fixed end", 1); 
		control.setValue("Timestep", 0.01); 
		control.setValue("Rest Length", 1); 
		control.setValue("Initial length", 100); 
		control.setValue("Number of masses", 10); 
		control.setValue("Mass of Spring", 1);
		control.setValue("Spring Constant", 50); 
		control.setValue("Large Mass", 1);
		frame.clearData();
		frame.clearDrawables();
		masses.removeAll(masses); 
		masses.clear();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		SimulationControl control = SimulationControl.createApp(new SpringApp()); //creates simulation implementing this class
	}

	static double sumForces(ArrayList<Force> f, double dir){
		double sum = 0; 
		if(dir == 0){
			for (int ii = 0; ii < f.size(); ii++) {
				sum += f.get(ii).forceX(f.get(ii)); 
			}
		}
		else {
			for (int ii = 0; ii < f.size(); ii++) {
				//System.out.println("Summing force with mag " + f.get(ii).magnitude + " and angle " + f.get(ii).angle);
				sum += f.get(ii).forceY(f.get(ii)); 
			}
		}
		return sum; 
	}

	double dist(Mass a, Mass b){
		return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2)); 
	}

	static double calcAng(Mass b, Mass a){
		double ang; 
		double yDiff = a.y_pos - b.y_pos; 
		double xDiff = a.x_pos - b.x_pos; 
		ang = Math.toDegrees(Math.atan(yDiff/xDiff)); 
		return ang; 
	}
}
