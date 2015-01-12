
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author CATE GABRIELLE
 */

import java.io.*;
import javax.sound.sampled.*;

public class Capture {
	boolean stopCapture = false;
	ByteArrayOutputStream byteArrayOutputStream;
	TargetDataLine targetDataLine; // This is the object that acquires data from
	// the microphone and delivers it to the program

	// the declaration of three instance variables used to create a SourceDataLine
	// object that feeds data to the speakers on playback
	AudioFormat audioFormat;    
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;    

	double voiceFreq = 0;    

	FileOutputStream fout;
	AudioFileFormat.Type fileType;
	public static String closestSpeaker;

	public Capture(){
		captureAudio();
	}       

	public void captureAudio(){
		try{
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new   
					DataLine.Info(TargetDataLine.class,audioFormat);
			// object that describes the data line that we need to handle the acquisition 
			// of the audio data from the microphone. The first parameter makes the audio 
			// data readable
			targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
			//  object to handle data acquisition 
			targetDataLine.open(audioFormat);                 
			//from the microphone that matches 
			targetDataLine.start();                            
			// the information encapsulated in the DataLine.Info object  
			Thread captureThread = new Thread(new CaptureThread());
			captureThread.start();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}    

	private AudioFormat getAudioFormat(){
		float sampleRate = 8000.0F; // The number of samples that will be acquired 
		//8000,11025,16000,22050,44100  each second for each channel of audio data.
		int sampleSizeInBits = 16; //The number of bits that will be used to 
		//8,16                        describe the value of each audio sample.
		int channels = 1;           // Two channels for stereo, and one channel for mono.
		//1,2
		boolean signed = true;      // Whether the description of each audio sample 
		//true,false        
		//consists of both positive and negative values, or positive values only.          
		boolean bigEndian = false;
		//true,false
		return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);        
	}

	//Inner class to capture data from microphone
	public class CaptureThread extends Thread {
		byte tempBuffer[] = new byte[8000];  
		// byte buffer variable to contain the raw audio data
		int countzero;                      
		// counter variable to count the number of zero's               
		short convert[] = new short[tempBuffer.length]; 
		// short variable that is appropriate to 

		// collect the audio input for porcessing

		//        public void start(){
		//            Thread voices = new Thread(this);
		//            voices.start();
		//        }

		@Override
		public void run(){               
			// a continuous thread to process the continuous audio input
			byteArrayOutputStream = new ByteArrayOutputStream(); // the object to write the 

			// raw audio input to the byte buffer variable
			stopCapture = false;
			try{
				while(!stopCapture){                    
					int cnt = targetDataLine.read(tempBuffer,0,tempBuffer.length); 
					// reads the raw audio input 

					// and returns the number of bytes actually read
					byteArrayOutputStream.write(tempBuffer, 0, cnt); 
					// writing the number of bytes read to the 
					// container                 
					try{ 
						voiceFreq = getFreq(); 
					}catch(StringIndexOutOfBoundsException e)  
					{System.out.println(e.getMessage());}                                                                                    
					Thread.sleep(0);                                        
				}
				byteArrayOutputStream.close();
			}catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}
		}
		public double getFreq(){
			countzero = 0; 

			for(int i=0; i < tempBuffer.length; i++){  
				// the loop that stores the whole audio data                                        
				convert[i] = tempBuffer[i];    
				// to the convert variable which is a short data type,
				if(convert[i] == 0){countzero++;}     
				// then counts the number of zero's 
			}
			voiceFreq = (countzero/2)+1;               
			// calculates the number of frequency and 
			// stores to the voiceFreq variable
			if(voiceFreq>=80 && voiceFreq<=350)
				System.out.println("Voice"+voiceFreq);
			else
				System.out.println("Unvoice"+voiceFreq);
			return voiceFreq; 
		}

	}      
	
	
	public static void main(String [] args){
		//Capture voiceDetector1 = new Capture();   
		//voiceDetector1.setSize(300,100);
		//voiceDetector1.setDefaultCloseOperation(EXIT_ON_CLOSE);
		//voiceDetector1.setVisible(true);
	}
}