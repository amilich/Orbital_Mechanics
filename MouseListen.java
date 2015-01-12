import java.awt.Component;
import java.awt.event.*;

//CREDIT: http://math.hws.edu/javanotes/c6/s4.html#GUI1.4.2 

public class MouseListen implements MouseListener {
	public void mousePressed(MouseEvent evt) {
		System.out.println("PRESS");
		Component source = (Component)evt.getSource();
		source.repaint();  // Call repaint() on the Component that was clicked.
	}

	public void mouseClicked(MouseEvent evt) { 
		System.out.println("CLICK");
	}
	
	public void mouseReleased(MouseEvent evt) {
		System.out.println("RELEASE");
	}
	
	public void mouseEntered(MouseEvent evt) {
		System.out.println("ENTER");
	}
	
	public void mouseExited(MouseEvent evt) {
		System.out.println("EXIT");
	}
}
