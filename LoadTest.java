import java.io.IOException;
import java.util.ArrayList;


/**
 * Tests loading particles. 
 * 
 * @author Andrew M. 
 */
public class LoadTest {
	public static void main(String[] args) throws IOException{
		ArrayList<Particle> loaded = (new ParticleReader("/users/student/Desktop/test.orb")).particles;
		for (int ii = 0; ii < loaded.size(); ii++) {
			System.out.println(loaded.toString());
		}
	}
}
