

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 * This class contains the controller for the Cluster simulation. Because it extends JFrame, it is essentially 
 * the application window with an ArrayList for text fields and buttons. It contains the necessary action 
 * listening functions in order to change values for the main Cluster simulation. 
 * 
 * @method init 
 * 	Creates the necessary buttons, action listeners, and adds them to the JFrame. Sets frame visible when all components added. 
 * @method c_num 
 * 	Returns number of clusters input by user. 
 * @method p_num 
 *	Returns number of particles input by user.  
 * @method cr 
 * 	Returns radius of cluster input by user. 
 * @method sr 
 * 	Returns radius of simulation input by user. 
 * @method dist 
 * 	Returns distribution set by user (random or uniform). 
 * @method refresh 
 * 	Refreshes data in JTextFields. 
 * @method time 
 * 	Returns new time step input by user. 
 * 
 * @author Andrew M. 
 */
public class ClusterController extends JFrame implements ActionListener {
	ArrayList<JTextField> fields = new ArrayList<JTextField>();
	ArrayList<JButton> buttons = new ArrayList<JButton>();
	boolean canRead = true; //can set data
	boolean dataChange = false; //user has entered data

	protected boolean DEBUG_MODE = false; //show debug info

	/**
	 * Creates a new ClusterController by initializing the data and GUI. 
	 */
	public ClusterController() {
		//go straight to init - no conditions needed (all set by refresh/init) 
		init();
	}

	/**
	 * Initialize all buttons, text fields, and frame. 
	 */
	public void init() {
		//set size, location, layout 
		setSize(300, 600);
		setLocation(750, 0);
		setLayout(new GridLayout(0, 2));

		//create the buttons in ArrayList
		buttons.add(new JButton("Set # of Clusters"));
		buttons.add(new JButton("Set # of Particles per Cluster"));
		buttons.add(new JButton("Set Radius of Cluster"));
		buttons.add(new JButton("Set Radius of Simulation"));
		buttons.add(new JButton("Random (1) or Even Distribution (2)"));
		buttons.add(new JButton("Cluster Velocity Magnitude"));
		buttons.add(new JButton("Time Step"));
		buttons.add(new JButton("Mass of Center")); 

		//add all action listeners and commands
		buttons.get(0).addActionListener(this);
		buttons.get(0).setActionCommand("cnum");
		buttons.get(1).addActionListener(this);
		buttons.get(1).setActionCommand("pnum");
		buttons.get(2).addActionListener(this);
		buttons.get(2).setActionCommand("cr");
		buttons.get(3).addActionListener(this);
		buttons.get(3).setActionCommand("sr");
		buttons.get(4).addActionListener(this);
		buttons.get(4).setActionCommand("dist");
		buttons.get(5).addActionListener(this);
		buttons.get(5).setActionCommand("vmag");
		buttons.get(6).addActionListener(this);
		buttons.get(6).setActionCommand("time");
		buttons.get(7).addActionListener(this);
		buttons.get(7).setActionCommand("mcent");

		//add text fields 
		fields.add(new JTextField("cluster #"));
		fields.add(new JTextField("particle #"));
		fields.add(new JTextField("cluster r"));
		fields.add(new JTextField("sim r"));
		fields.add(new JTextField("dist"));
		fields.add(new JTextField("vmag"));
		fields.add(new JTextField("time step"));
		fields.add(new JTextField("In Solar Masses"));

		//arrange buttons on frame
		add(buttons.get(0));
		add(fields.get(0));
		add(buttons.get(1));
		add(fields.get(1));
		add(buttons.get(2));
		add(fields.get(2));
		add(buttons.get(3));
		add(fields.get(3));
		add(buttons.get(4));
		add(fields.get(4));
		add(buttons.get(5)); 
		add(fields.get(5)); 
		add(buttons.get(6)); 
		add(fields.get(6)); 
		add(buttons.get(7)); 
		add(fields.get(7)); 

		setVisible(true); //show to user
	}

	/**
	 * Returns # of clusters. 
	 * 
	 * @return
	 * 	Integer number of clusters. 
	 */
	public int c_num(){
		if(DEBUG_MODE)
			System.out.println("releasing cnum " + fields.get(0).getText());
		return (int) Double.parseDouble(fields.get(0).getText()); 
	}

	/**
	 * Returns radius of cluster from JFrame. 
	 * 
	 * @return
	 * 	Cluster radius input by user. 
	 */
	public double cr(){
		if(DEBUG_MODE)
			System.out.println("releasing cr " + fields.get(2).getText());
		return Double.parseDouble(fields.get(2).getText()); 
	}

	/**
	 * Velocity input by user (v_tan). 
	 * 
	 * @return
	 * 	Double magnitude of velocity. 
	 */
	public double vMag(){
		if(DEBUG_MODE)
			System.out.println("releasing vmag " + fields.get(5).getText());
		return Double.parseDouble(fields.get(5).getText()); 
	}

	/**
	 * Radius of entire simulation (all clusters). 
	 * 
	 * @return
	 * 	Double input by user. 
	 */
	public double sr(){
		if(DEBUG_MODE)
			System.out.println("releasing sr " + fields.get(3).getText());
		return Double.parseDouble(fields.get(3).getText()); 
	}
	
	/**
	 * Returns center mass. 
	 * 
	 * @return
	 * 	Mass of center (input). 
	 */
	public double cMass(){
		if(DEBUG_MODE)
			System.out.println("Releasing central mass");
		return Double.parseDouble(fields.get(7).getText());
	}

	/**
	 * Distribution type. 
	 * 
	 * @return
	 * 	String with user's input (interpreted by Sim). 
	 */
	public String dist(){
		return fields.get(4).getText(); 
	}

	/**
	 * New time step. 
	 * 
	 * @return
	 * 	Time step input by user. 
	 */
	public long time(){
		return (long) Double.parseDouble(fields.get(6).getText()); 
	}

	/**
	 * Number of particles. 
	 * 
	 * @return
	 * 	The number of particles input. 
	 */
	public int p_num(){
		if(DEBUG_MODE)
			System.out.println("releasing pnum " + fields.get(1).getText());
		return (int) Double.parseDouble(fields.get(1).getText()); 
	}

	/**
	 * Reloads textfield data. 
	 * 
	 * @param cluster
	 * 	Simulation used to put data in fields. 
	 */
	@SuppressWarnings("static-access")
	public void refresh(Cluster_Sim cluster) {
		fields.get(0).setText("" + cluster.clusterNum);
		fields.get(1).setText("" + cluster.particle_num);
		fields.get(2).setText("" + cluster.radius);
		fields.get(3).setText("" + cluster.bigRadius);
		fields.get(4).setText("" + cluster.random_dist);
		fields.get(5).setText("" + cluster.vMag);
		fields.get(6).setText("" + cluster.p_time);
		fields.get(7).setText("" + cluster.center_mass);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(canRead){
			dataChange = true; 

			if(command.equals("cnum"))
				if(DEBUG_MODE)
					System.out.println("CNUM");
				else if (command.equals("pnum"))
					if(DEBUG_MODE)
						System.out.println("PNUM");
					else if (command.equals("cr"))
						if(DEBUG_MODE)
							System.out.println("CR");
						else if (command.equals("sr"))
							if(DEBUG_MODE)
								System.out.println("SR");
							else if (command.equals("dist"))
								if(DEBUG_MODE)
									System.out.println("DIST");
								else if (command.equals("vmag"))
									if(DEBUG_MODE)
										System.out.println("VMAG");
									else if (command.equals("time"))
										if(DEBUG_MODE)
											System.out.println("TIME");
										else 
											if(DEBUG_MODE)
												System.out.println("Not Recognized Command");
		}
	}
}
