import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Reads in particles from file. 
 * 
 * @author Andrew M. 
 */
public class ParticleReader {
	ArrayList<Particle> particles = new ArrayList<Particle>(); 
	double dT, G; 

	@SuppressWarnings("resource")
	public ParticleReader(String path) throws IOException{
		String line;
		InputStream input = new FileInputStream(path);
		BufferedReader read = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
		String genData = read.readLine(); 
		dT = (int) Double.parseDouble(genData.split(", ")[0]); 
		G = Double.parseDouble(genData.split(", ")[1]); 

		try {
			//goes through file and adds particles. 
			while ((line = read.readLine()) != null) {
				String[] data = line.split(", ..: "); 
				String name = line.split(",")[0]; 
				double mass = Double.parseDouble(data[1]);
				double x = Double.parseDouble(data[2]);
				double y = Double.parseDouble(data[3]);
				double vx = Double.parseDouble(data[4]);
				double vy = Double.parseDouble(data[5]);
				double accx = Double.parseDouble(data[6]);
				double accy = Double.parseDouble(data[7]);
				int pixr = Integer.parseInt(data[8]);
				double r = Double.parseDouble(data[9]);
				double charge = Double.parseDouble(data[12]); 
				if(mass != 0){
					particles.add(new Particle()); 
					particles.get(particles.size()-1).init(x, y, vx, vy, accx, accy, mass, dT, 0);
					particles.get(particles.size()-1).pixRadius = pixr; 
					particles.get(particles.size()-1).setRadius(pixr);
					System.out.println("Set to " + pixr);
					particles.get(particles.size()-1).actual_r = r; 
					particles.get(particles.size()-1).name = "Particle [" + (particles.size()-1) + "]: " + name; 
					particles.get(particles.size()-1).real_name = name; 
					particles.get(particles.size()-1).charge = charge; 
					//System.out.println("Name: " + line.split(",")[0]);
					//System.out.println("P: " + particles.get(particles.size()-1).toString());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
