

import java.util.ArrayList;

import org.opensourcephysics.frames.PlotFrame;

/**
 * Abstract class for law. Other laws extend this and inherit the PlotFrame object. 
 * 
 * @author Andrew M. 
 */

public abstract class Law extends PlotFrame {
	public Law(String xlabel, String ylabel, String frameTitle) {
		super(xlabel, ylabel, frameTitle);
	}

	public abstract void Prove(ArrayList<Particle> bodies); 
}
