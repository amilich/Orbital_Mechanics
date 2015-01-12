import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes to file. 
 * 
 * @author Andrew M. 
 */
public class ParticleWriter {
	String FILE_PATH; //file path of data file 
	static File file; 

	/**
	 * Initializes writer with file path. 
	 * 
	 * @param file_str
	 * 	File path of desired file. 
	 */
	public ParticleWriter(String file_str) {
		FILE_PATH = file_str; 

		try { 
			ParticleWriter.file = new File(file_str);
			if (!file.exists()) 
				file.createNewFile(); //make the file 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes writer with file path. 
	 * 
	 * @param file_str
	 * 	File path of desired file. 
	 */
	public ParticleWriter(String file_str, boolean delete) {
		FILE_PATH = file_str; 

		try { 
			ParticleWriter.file = new File(file_str);
			if (delete) 
				file.createNewFile(); //make the file 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes content to file. 
	 * 
	 * @param content
	 * 	Content to write. 
	 */
	public static void write(String content){
		try { 
			PrintWriter out = new PrintWriter(new FileWriter(file, true)); //append (don't delete) to file
			out.write(content);	
			out.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes old content and overwrites. 
	 * 
	 * @param content
	 * 	Content to write. 
	 */
	public void writeNew(String content) {
		try { 
			PrintWriter writer = new PrintWriter(file); //delete contents of old file 
			writer.print("");
			writer.close();
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true)); //append (don't delete) to file
			out.write(content);	
			out.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}