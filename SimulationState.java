import java.util.ArrayList;

/**
 * This class manages the data for saving/logging files. 
 * 
 * @author Andrew M. 
 */
public class SimulationState {
	/**
	 * Creates string data of all particles in simulation. 
	 * 
	 * @param particles
	 * 	ArrayList of bodies. 
	 * @return
	 * 	String with info of all bodies. 
	 */
	public static String simState(ArrayList<Particle> particles){
		StringBuilder str = new StringBuilder(); 
		
		for (Particle p : particles) {
			str.append(p.toString() + "\n"); 
		}
		return str.toString();
	}
	
	/**
	 * Creates string data of all particles in simulation to be saved. 
	 * 
	 * @param particles
	 * 	ArrayList of bodies. 
	 * @return
	 * 	String with info of all bodies. 
	 */
	public static String saveState(ArrayList<Particle> particles, double deltaT, double G){
		StringBuilder str = new StringBuilder(); 
		str.append(deltaT + ", " + G + "\n"); 
		for (Particle p : particles) {
			str.append(p.toString() + "\n"); 
		}
		return str.toString();
	}
	
	public Orbital_AM simulation(){
		return null; 
	}
}
