import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The ParticleController is the GUI controller for Orbital_AM. It contains ArrayLists of JTextFields, 
 * JButtons, and a JComboBox to allow the user to both select a mass to edit as well as view and change 
 * the properties of each mass. This includes changing the color, velocity, acceleration, mass, and name. 
 * 
 * Global simulation properties can also be set - the time step, the collision type, and the gravitational 
 * constant can also be altered by the user. 
 * 
 * The Controller includes a private class Entry that is used to create the list of items within the 
 * JComboBox. 
 * 
 * @method refresh 
 * 	Refreshes data in the JTextFields when simulation is paused by user. 
 * @method setEditable 
 * 	Allows user to edit all text fields. 
 * @method setUnEditable 
 * 	Sets all fields to uneditable. 
 * @method popup 
 * 	Creates a popup for the user. 
 * @method init 
 * 	Initializes the JFrame by adding and setting textfields, color chooser, and buttons. 
 * @method redraw 
 * 	Redraws the JFrame when the number of particles is changed (one is added or deleted). 
 * 
 * TODO: 
 * 1. Add dyanmic name changing of masses. [DONE] 
 * 2. Add option to toggle
 * trails (DO ON DISPLAYFRAME).
 * 
 * @author Andrew M.
 */
public class ParticleController extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JComboBox<String> combo = new JComboBox<String>();
	JPanel selector = new JPanel();
	JPanel text = new JPanel();
	//components to select, edit, and view properties of masses 
	ArrayList<Particle> particles = new ArrayList<Particle>();
	ArrayList<JTextField> fields = new ArrayList<JTextField>();
	ArrayList<JButton> buttons = new ArrayList<JButton>();
	JColorChooser colorChooser = new JColorChooser();
	ArrayList<Entry> entries = new ArrayList<Entry>(); //entries in JComboBox

	protected boolean DATA_CHANGE = false; //tells simulator whether properties have been changed 
	protected boolean DEBUG_MODE = false;
	protected boolean G_CHANGE = false;
	protected boolean T_CHANGE = false;
	protected boolean canRead = false;
	protected double G;
	protected int[] dimensions = new int[] { 700, 850 };
	protected int TIME_STEP; 
	protected int current_particle; //to reset data in fields, know which particle to alter 

	//private ParticleWriter writer = new ParticleWriter("/users/student/Desktop/orbital.txt"); 
	public ArrayList<Particle> loaded = new ArrayList<Particle>(); 
	protected boolean load = false; 
	JFileChooser chooser = new JFileChooser();

	/**
	 * Creates new Particle controller. 
	 * 
	 * @param list
	 * 	List of particles to extract data from. 
	 */
	@SuppressWarnings("unchecked")
	public ParticleController(ArrayList<Particle> list) {
		combo.addActionListener(this);
		combo.setActionCommand("selection");
		this.particles = (ArrayList<Particle>) list.clone(); //give the particles to this controller. 
	}

	void set(){
		if (current_particle < particles.size()) {
			fields.get(0).setText(particles.get(current_particle).real_name); //set fields to particle's data 
			fields.get(1).setText("" + particles.get(current_particle).mass);
			fields.get(2).setText("" + particles.get(current_particle).x_pos);
			fields.get(3).setText("" + particles.get(current_particle).y_pos);
			fields.get(4).setText("" + particles.get(current_particle).v_x);
			fields.get(5).setText("" + particles.get(current_particle).v_y);
			fields.get(6).setText("" + particles.get(current_particle).acc_x);
			fields.get(7).setText("" + particles.get(current_particle).acc_y);
			set_colors(); 
		}
	}
	
	/**
	 * Reloads data in text boxes. 
	 * 
	 * @param orbit
	 * 	Simulation with particle data (needed for time step, G). 
	 */
	public void refresh(Orbital_AM orbit, boolean set) {
		//only reload if particle within ArrayList.
		if(set){
			this.particles = orbit.bodies;
			current_particle = 0; 
		}
		if (current_particle < particles.size()) {
			fields.get(0).setText(particles.get(current_particle).real_name); //set fields to particle's data 
			fields.get(1).setText("" + particles.get(current_particle).mass);
			fields.get(2).setText("" + particles.get(current_particle).x_pos);
			fields.get(3).setText("" + particles.get(current_particle).y_pos);
			fields.get(4).setText("" + particles.get(current_particle).v_x);
			fields.get(5).setText("" + particles.get(current_particle).v_y);
			fields.get(6).setText("" + particles.get(current_particle).acc_x);
			fields.get(7).setText("" + particles.get(current_particle).acc_y);
			fields.get(8).setText("" + orbit.TIME_STEP); //set time step and G
			fields.get(9).setText("" + orbit.G);

			set_colors(); 
		}
		//setup_data_tab(true); 
		repaint(); //now reload JFrame
	}

	/**
	 * Set color of fields. 
	 */
	private void set_colors(){
		Color this_col = particles.get(current_particle).color; 
		for(int ii = 0; ii < fields.size(); ii ++){
			fields.get(ii).setBackground(this_col);
		}
		/*fields.get(0).setBackground(this_col); 
		fields.get(1).setBackground(this_col); 
		fields.get(2).setBackground(this_col); 
		fields.get(3).setBackground(this_col); 
		fields.get(4).setBackground(this_col); 
		fields.get(5).setBackground(this_col); 
		fields.get(6).setBackground(this_col); 
		fields.get(7).setBackground(this_col); 
		fields.get(8).setBackground(this_col); 
		fields.get(9).setBackground(this_col); */
		combo.setBackground(this_col);
		//combo.setForeground(this_col);
	}

	/*/**
	 * Sets up data tab of JFrame. 

	public void setup_data_tab(boolean refresh){
		int selected = tabbedPane.getSelectedIndex(); //to put it back where you were before table is redrawn
		if(refresh)
			tabbedPane.removeTabAt(1);
		JPanel dataTab = new JPanel(new FlowLayout()); 
		dataTab.add(data()); 
		dataTab.add(buttons.get(14)); //add write to file button
		tabbedPane.addTab("Data", dataTab);
		tabbedPane.setSelectedIndex(selected);
	}*/

	/**
	 * Creates popup screen for user. 
	 * 
	 * @param text1
	 * 	Popup text. 
	 * @param text2
	 * 	Popup title. 
	 */
	public void popup(String text1, String text2) {
		JOptionPane popup = new JOptionPane();
		popup.setLocation(300, 300);
		JOptionPane.showMessageDialog(new JFrame(), text1, text2,
				JOptionPane.PLAIN_MESSAGE);
		popup.setSize(300, 150);
		popup.setVisible(true);
	}

	/**
	 * Sets all text fields to editable. 
	 */
	public void setEditable() {
		combo.setEnabled(true);
		for (JTextField f : fields) {
			f.setEditable(true);
		}
	}

	/**
	 * Sets all text fields to uneditable. 
	 */
	public void setUnEditable() {
		combo.setEnabled(false);
		for (JTextField f : fields) {
			f.setEditable(false);
		}
	}

	/**
	 * Initializes all the buttons, entry fields, and layout of the controller. 
	 * All action listeners are added to the buttons, which are then arranged on 
	 * various JPanels and on the ParticleController object. 
	 */
	public void init() {
		fields.add(new JTextField("Name"));
		fields.add(new JTextField("Mass"));
		fields.add(new JTextField("X"));
		fields.add(new JTextField("Y"));
		fields.add(new JTextField("Vx"));
		fields.add(new JTextField("Vy"));
		fields.add(new JTextField("Ax"));
		fields.add(new JTextField("Ay"));
		//add all the actionlisteners to the buttons function 
		buttons.add(new JButton("Set Name"));
		buttons.get(0).addActionListener(this);
		buttons.get(0).setActionCommand("name");
		buttons.add(new JButton("Set Mass (kg)"));
		buttons.get(1).addActionListener(this);
		buttons.get(1).setActionCommand("mass");
		buttons.add(new JButton("Set X (m)"));
		buttons.get(2).addActionListener(this);
		buttons.get(2).setActionCommand("x");
		buttons.add(new JButton("Set Y (m)"));
		buttons.get(3).addActionListener(this);
		buttons.get(3).setActionCommand("y");
		buttons.add(new JButton("Set Vx (m/s)"));
		buttons.get(4).addActionListener(this);
		buttons.get(4).setActionCommand("vx");
		buttons.add(new JButton("Set Vy (m/s)"));
		buttons.get(5).addActionListener(this);
		buttons.get(5).setActionCommand("vy");
		buttons.add(new JButton("Set Ax (m/s^2)"));
		buttons.get(6).addActionListener(this);
		buttons.get(6).setActionCommand("ax");
		buttons.add(new JButton("Set Ay (m/s^2)"));
		buttons.get(7).addActionListener(this);
		buttons.get(7).setActionCommand("ay");
		buttons.add(new JButton("Delete Mass"));
		buttons.get(8).addActionListener(this);
		buttons.get(8).setActionCommand("del");
		buttons.add(new JButton("Add Mass"));
		buttons.get(9).addActionListener(this);
		buttons.get(9).setActionCommand("add");



		colorChooser.setPreviewPanel(new JPanel()); //hide the preview panel. 
		ColorSelectionModel model = colorChooser.getSelectionModel();
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				particles.get(current_particle).color = colorChooser.getColor();
				set_colors(); 
			}
		};
		model.addChangeListener(changeListener);

		//labels for the buttons 
		buttons.add(new JButton("Set Time Step"));
		buttons.get(10).addActionListener(this);
		buttons.get(10).setActionCommand("time");
		buttons.add(new JButton("Set Grav Constant"));
		buttons.get(11).addActionListener(this);
		buttons.get(11).setActionCommand("G");
		fields.add(new JTextField("Time Step"));
		fields.add(new JTextField("G                   "));

		//global property buttons 
		buttons.add(new JButton("Load"));
		buttons.get(12).addActionListener(this);
		buttons.get(12).setActionCommand("load");
		buttons.add(new JButton("Save"));
		buttons.get(13).addActionListener(this);
		buttons.get(13).setActionCommand("save");
		fields.add(new JTextField("filename"));

		buttons.add(new JButton("Write to file")); 
		buttons.get(14).addActionListener(this);
		buttons.get(14).setActionCommand("write");

		buttons.add(new JButton("Show Ellipse")); 
		buttons.get(15).addActionListener(this);
		buttons.get(15).setActionCommand("ell");

		buttons.add(new JButton("Set Pixel Radius")); 
		buttons.get(16).addActionListener(this);
		buttons.get(16).setActionCommand("prad");
		buttons.add(new JButton("Charge")); 
		buttons.get(17).addActionListener(this);
		buttons.get(17).setActionCommand("charge");

		fields.add(new JTextField("Pixel Rad"));
		fields.add(new JTextField("Charge"));

		setSize(dimensions[0], dimensions[1]); 
		redraw(); 
	}

	boolean addMass = false; 
	boolean prove_one = false;
	boolean prove_two = false;
	boolean prove_three = false;

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * 
	 * Deals with button presses. 
	 */
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (canRead) {
			if (command.equals("name")) { //sort by action type 
				particles.get(current_particle).real_name = fields.get(0).getText();
				particles.get(current_particle).name = "Particle ["+ (particles.size() - 1) + "]: "+ particles.get(current_particle).real_name;
				entries.get(current_particle).title = particles.get(current_particle).name;
				redraw();
				combo.setSelectedIndex(current_particle); //set to one you just renamed 
				if (DEBUG_MODE)
					System.out.println("NAME CHANGED TO " + particles.get(current_particle).name);
			}
			if (command.equals("mass")) {
				particles.get(current_particle).mass = Double.parseDouble(fields.get(1).getText());
				if (DEBUG_MODE)
					System.out.println("MASS CHANGED");
			}
			if (command.equals("x")) {
				particles.get(current_particle).x_pos = Double.parseDouble(fields.get(2).getText());
				if (DEBUG_MODE)
					System.out.println("X CHANGED");
			}
			if (command.equals("y")) {
				particles.get(current_particle).y_pos = Double.parseDouble(fields.get(3).getText());
				if (DEBUG_MODE)
					System.out.println("Y CHANGED");
			}
			if (command.equals("vx")) {
				particles.get(current_particle).v_x = Double.parseDouble(fields.get(4).getText());
				if (DEBUG_MODE)
					System.out.println("VX CHANGED");
			}
			if (command.equals("vy")) {
				particles.get(current_particle).v_y = Double.parseDouble(fields.get(5).getText());
				if (DEBUG_MODE)
					System.out.println("VY CHANGED");
			}
			if (command.equals("ax")) {
				particles.get(current_particle).acc_x = Double.parseDouble(fields.get(6).getText());
				if (DEBUG_MODE)
					System.out.println("AX CHANGED");
			}
			if (command.equals("ay")) {
				particles.get(current_particle).acc_y = Double.parseDouble(fields.get(7).getText());
				if (DEBUG_MODE)
					System.out.println("AY CHANGED");
			}
			if (command.equals("selection")) {
				String text = combo.getSelectedItem().toString();
				String result = text.substring(text.indexOf("[") + 1, text.indexOf("]"));
				current_particle = Integer.parseInt(result);
			}
			if (command.equals("del")) {
				if (particles.size() != 1) {
					particles.remove(current_particle);
					combo.removeItemAt(current_particle);

					redraw();
					current_particle = 0; //reset to first particle 
					repaint();

					this.DATA_CHANGE = true;
					return;
				} else {
					if(DEBUG_MODE)
						System.out.println("Deletion failed; size of bodies is " + particles.size());
				}
			}
			if (command.equals("add")) {
				this.add(); 
			}
			if (command.equals("time")) {
				//set the new time step 
				int newTime = (int) Double.parseDouble(fields.get(8).getText());
				TIME_STEP = newTime;
				T_CHANGE = true;
				if (DEBUG_MODE)
					System.out.println("TIME STEP CHANGED to " + newTime);
			}
			if (command.equals("G")) {
				//set new g constant 
				G = Double.parseDouble(fields.get(9).getText());
				G_CHANGE = true;
				if (DEBUG_MODE)
					System.out.println("G CHANGED to " + G);
			}
			if(command.equals("ell")){
				this.prove_one = true; 
			}
			if(command.equals("charge")){
				particles.get(current_particle).charge = Double.parseDouble(fields.get(fields.size()-1).getText());
				System.out.println("Charge");
				if (DEBUG_MODE)
					System.out.println("MASS CHANGED");
			}
			if (command.equals("write")) {
				//write stuff to file
				ParticleWriter.write(SimulationState.simState(particles) + "\n"); //write to file 
			}
			if (command.equals("save")) {
				//save sim state to file 
				ParticleWriter save_writer = new ParticleWriter(System.getProperty("user.home")+ "/" + fields.get(10).getText() + ".orb", true);
				//ParticleWriter save_writer = new ParticleWriter("/users/student/Desktop/" + fields.get(10).getText() + ".orb", true); 
				save_writer.writeNew(SimulationState.saveState(particles, 
						Double.parseDouble(fields.get(8).getText()), 
						Double.parseDouble(fields.get(9).getText()) )); //write to file simulation state 
			}
			if (command.equals("load")) {
				//load from file 
				load(); 
			}
			this.DATA_CHANGE = true; //orbit will refresh 
		}
	}

	void load(String f_name){
		loaded.removeAll(loaded); 
		loaded.clear(); 
		System.out.println(f_name);
		String file_name = f_name; 

		try {
			if(DEBUG_MODE)
				System.out.println("Loading...");
			loaded.clear(); 
			loaded = (ArrayList<Particle>) (new ParticleReader(file_name)).particles.clone();
			this.particles = (ArrayList<Particle>) loaded.clone(); 
			G = (new ParticleReader(file_name)).G;
			//TIME_STEP = (int) (new ParticleReader(file_name)).dT; 
			TIME_STEP = (int) 1E7; 
			//T_CHANGE = true; 
			//G_CHANGE = true; 
			System.out.println("loaded set");
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
		this.DATA_CHANGE = true; //orbit will refresh 
		load = true; 
	}

	void load(){
		final JFrame choose = new JFrame();
		choose.setSize(new Dimension(300, 450));
		chooser.addChoosableFileFilter(new MyFilter());
		choose.add(chooser); 
		choose.setVisible(true); 
		int result = chooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			choose.dispose(); 
			loaded.removeAll(loaded); 
			loaded.clear(); 
			System.out.println(chooser.getSelectedFile().getAbsolutePath());
			String file_name = chooser.getSelectedFile().getAbsolutePath();

			try {
				if(DEBUG_MODE)
					System.out.println("Loading...");
				loaded.clear(); 
				loaded = (ArrayList<Particle>) (new ParticleReader(file_name)).particles.clone();
				this.particles = (ArrayList<Particle>) loaded.clone(); 
				G = (new ParticleReader(file_name)).G;
				//TIME_STEP = (int) (new ParticleReader(file_name)).dT; 
				TIME_STEP = (int) 1E7; 
				//T_CHANGE = true; 
				//G_CHANGE = true; 
				System.out.println("loaded set");
			} catch (IOException e1) {
				e1.printStackTrace();
			} 

			load = true; 
		} 
		else if (result == JFileChooser.CANCEL_OPTION) {
			if(DEBUG_MODE)
				System.out.println("Cancel was selected");
			choose.dispose(); //close jframe
		}
	}

	JPanel all = new JPanel(); //contains all the components before they are added to the Controller 

	/**
	 * Re-adds all items to JComboBox, refreshes text field data, and re-adds all to JFrame. 
	 */
	public void redraw() {
		int[] prevsize = new int[] { getWidth(), getHeight() }; //maintain size 
		all.remove(combo);
		combo.removeAll();
		remove(all); //get rid of old components

		combo = new JComboBox<String>(); 
		selector = new JPanel();
		text = new JPanel();
		all = new JPanel();

		setLayout(new FlowLayout());
		selector.setLayout(new FlowLayout());
		text.setLayout(new GridLayout(0, 2));

		entries.removeAll(entries);
		for (int ii = 0; ii < particles.size(); ii++) { //refresh the entries
			entries.add(new Entry("Particle [" + ii + "]: " + particles.get(ii).real_name));
			if (DEBUG_MODE)
				System.out.println("Renaming " + particles.get(ii).name + " to " + entries.get(ii).title);
			particles.get(ii).name = entries.get(ii).toString();
		}
		combo = new JComboBox<String>();
		for (Entry e : entries) {
			combo.addItem(e.toString());
		}

		combo.addActionListener(this);
		combo.setActionCommand("selection");
		selector.add(combo, BorderLayout.NORTH);
		text.add(buttons.get(0));
		text.add(buttons.get(1));
		text.add(fields.get(0));
		text.add(fields.get(1));
		text.add(buttons.get(2));
		text.add(buttons.get(3));
		text.add(fields.get(2));
		text.add(fields.get(3));
		text.add(buttons.get(4));
		text.add(buttons.get(5));
		text.add(fields.get(4));
		text.add(fields.get(5));
		text.add(buttons.get(6));
		text.add(buttons.get(7));
		text.add(fields.get(6));
		text.add(fields.get(7));

		text.add(buttons.get(16));
		text.add(buttons.get(17));
		text.add(fields.get(11));
		text.add(fields.get(12));

		JPanel color = new JPanel(); 
		color.add(colorChooser);

		JPanel textSection = new JPanel(new GridLayout(0, 2));
		JPanel left = new JPanel(new GridLayout(0, 2));
		JPanel right = new JPanel(new FlowLayout());
		left.setPreferredSize(new Dimension(50, 10));
		right.setPreferredSize(new Dimension(50, 10));

		left.add(buttons.get(10));
		left.add(fields.get(8));
		left.add(buttons.get(11));
		left.add(fields.get(9));

		textSection.add(left);
		textSection.add(text);
		// textSection.add(right);

		JPanel fullText = new JPanel(new GridLayout(2, 0));
		fullText.add(textSection);

		JPanel delete = new JPanel(new FlowLayout());
		delete.add(buttons.get(8), BorderLayout.NORTH);
		delete.add(buttons.get(9), BorderLayout.NORTH);
		delete.add(buttons.get(12), BorderLayout.NORTH);
		delete.add(buttons.get(13), BorderLayout.NORTH);
		delete.add(fields.get(10), BorderLayout.NORTH);
		delete.add(buttons.get(15), BorderLayout.NORTH);

		fullText.add(delete);

		all.setLayout(new BorderLayout());
		all.add(selector, BorderLayout.NORTH);
		all.add(color, BorderLayout.CENTER);
		all.add(fullText, BorderLayout.SOUTH);

		//remove(tabbedPane); 
		//tabbedPane = new JTabbedPane();
		//tabbedPane.addTab("Particles", all);
		//setup_data_tab(false); 
		//add(tabbedPane);
		add(all); 
		pack();
		setSize(prevsize[0], prevsize[1]);
	}

	private JPanel data(){
		String columnNames[] = { "#", "Name", "X" , "Y", "Mass", "V_x", "V_y", "Acc_x", "Acc_y", "Force"};
		String dataValues[][] = new String[particles.size()][columnNames.length];
		for (int ii = 0; ii < dataValues.length; ii++) { //add values to the string[][] of data for table
			dataValues[ii][0] = "" + ii; //number particle
			dataValues[ii][1] = particles.get(ii).real_name; 
			dataValues[ii][2] = "" + particles.get(ii).x_pos; 
			dataValues[ii][3] = "" + particles.get(ii).y_pos; 
			dataValues[ii][4] = "" + particles.get(ii).mass; 
			dataValues[ii][5] = "" + particles.get(ii).v_x; 
			dataValues[ii][6] = "" + particles.get(ii).v_y; 
			dataValues[ii][7] = "" + particles.get(ii).acc_x; 
			dataValues[ii][8] = "" + particles.get(ii).acc_y;
			dataValues[ii][9] = "" + (particles.get(ii).mass)*(particles.get(ii).vector(particles.get(ii).acc_x, particles.get(ii).acc_y)); //add force by F = ma
		}
		JTable table = new JTable(dataValues, columnNames){  
			private static final long serialVersionUID = 6778147652488566070L;

			public boolean isCellEditable(int row, int column){  
				return false; //cannot edit the table - user edits via tab 1
			}  
		};
		table.setPreferredSize(new Dimension(750, 500));
		table.getColumnModel().getColumn(0).setPreferredWidth(36); //#
		table.getColumnModel().getColumn(1).setPreferredWidth(90); //name
		table.getColumnModel().getColumn(2).setPreferredWidth(100); //x 
		table.getColumnModel().getColumn(3).setPreferredWidth(100); //y
		table.getColumnModel().getColumn(4).setPreferredWidth(150); //mass
		table.getColumnModel().getColumn(5).setPreferredWidth(120); //v_x
		table.getColumnModel().getColumn(6).setPreferredWidth(120); //v_y
		table.getColumnModel().getColumn(7).setPreferredWidth(120); //acc_x
		table.getColumnModel().getColumn(8).setPreferredWidth(120); //acc_y 
		table.getColumnModel().getColumn(9).setPreferredWidth(100); //F

		JScrollPane scrollPane = new JScrollPane(table); //can scroll the table 
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(scrollPane);  

		return panel; 
	}

	/**
	 * Used for each entry in the JComboBox selector for masses. 
	 * Every entry contains a title. 
	 * 
	 * @author Andrew M. 
	 */
	private class Entry {
		String title = ""; 

		/**
		 * Create new entry. 
		 * 
		 * @param title
		 * 	Entry's name. 
		 */
		public Entry(String title) {
			this.title = title;
		}

		public String toString() {
			return title; //returns name of entry 
		}
	}

	private void add(){
		//add a new particle 
		particles.add(new Particle()); 
		entries.add(new Entry("Particle [" + (particles.size() - 1) + "]: " + particles.get(particles.size() - 1).name));
		// better way to set time step
		particles.get(particles.size() - 1).init(0, 0, 0, 0, particles.get(0).deltaT, 0);
		particles.get(particles.size() - 1).name = "Particle #" + (particles.size() - 1);
		particles.get(particles.size() - 1).charge = 0; 
		combo.addItem("Particle [" + (particles.size() - 1) + "]: " + particles.get(particles.size() - 1).name);
		addMass = true; 
		if (DEBUG_MODE)
			System.out.println("ADDED MASS " + (particles.size() - 1));
	}
}

class MyFilter extends javax.swing.filechooser.FileFilter {
	public boolean accept(File file) {
		String filename = file.getName();
		return filename.endsWith(".orb");
	}

	public String getDescription() {
		return "*.orb";
	}
}