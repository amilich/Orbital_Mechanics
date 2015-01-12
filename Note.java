import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.File;

/**
 * Plays one or more notes through Midi. If you find yourself wanting to use lots of individual
 * sound files for a scenario, such as emulating an instrument for example, you may find using
 * midi a better choice. It has several advantages over using lots of wav files:
 * <ul>
 *   <li>It takes up far less space, so less disk space is used and loading times are reduced</li>
 *   <li>You can switch between instruments really easily if you wish</li>
 *   <li>You can adjust things like the length of time notes are held for really easily</li>
 *   <li>You can generally manipulate the details of midi notes far more easily than you can with actual sound files</li>
 *   <li>If you really want to be clever, you can add pitch bend, adjust how quickly notes are played and more!</li>
 * </ul>
 * However, Midi is not always suited to the task - the sounds aren't always the same on each
 * computer (since they're generated through the sound card and not in advance) and the quality
 * won't be as good as a recorded sound. If you're looking for special effects, a backing track
 * or in game sounds then chances are you'll be better off using normal sound files.
 * 
 * @author Michael Berry (mjrb4)
 * @version 12/02/09
 */
public class Note  
{

	/** The default instrument to use if one isn't specified in the constructor.*/
	public static int DEFAULT_INSTRUMENT = 4;
	/** The channel used to play the Midi notes. */
	private MidiChannel channel;
	/** A sequencer used to play Midi files. */
	private static Sequencer sequencer;

	/**
	 * Try to set the seqencer up.
	 */
	{
		try {
			sequencer = MidiSystem.getSequencer();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create a new MidiPlayer object with a default
	 * instrument specified by DEFAULT_INSTRUMENT - 
	 * usually a piano sound.
	 */
	public Note()
	{
		channel = getChannel(DEFAULT_INSTRUMENT);
	}

	/**
	 * Create a new MidiPlayer object with a specified
	 * instrument.
	 * @param instrument the instrument to use
	 */
	public Note(int instrument)
	{
		channel = getChannel(instrument);
	}

	/**
	 * Change the instrument this MidiPlayer uses.
	 * @param instrument the instrument to change to
	 */
	public void setInstrument(int instrument)
	{
		channel.programChange(instrument);
	}

	/**
	 * Get the instrument the MidiPlayer is using
	 * at present.
	 * @return the instrument in use.
	 */
	public int getInstrument()
	{
		return channel.getProgram();
	}

	/**
	 * Converts a string description of the key
	 * to the number used by the MidiPlayer. 
	 * @throws RuntimeException if the key name is invalid.
	 */
	public int getNumber(String key)
	{
		try {
			String note = new Character(key.charAt(0)).toString();
			boolean accidental = false;
			if(key.charAt(1)=='b') {
				note += "b";
				accidental = true;
			}
			else if(key.charAt(1)=='#') {
				note += "#";
				accidental = true;
			}
			int offset = 1;
			if(accidental) offset = 2;
			int number = Integer.parseInt(key.substring(offset));

			int midiNum = (number+1)*12;
			midiNum += getOffset(note);

			return midiNum;
		}
		catch(Exception ex) {
			throw new RuntimeException(key + " is an invalid key name...");
		}
	}

	/**
	 * Set how far each note is (relatively in semitones) above C.
	 */
	private int getOffset(String note)
	{
		if(note.equalsIgnoreCase("C")) return 0;
		if(note.equalsIgnoreCase("C#")) return 1;
		if(note.equalsIgnoreCase("Db")) return 1;
		if(note.equalsIgnoreCase("D")) return 2;
		if(note.equalsIgnoreCase("D#")) return 3;
		if(note.equalsIgnoreCase("Eb")) return 3;
		if(note.equalsIgnoreCase("E")) return 4;
		if(note.equalsIgnoreCase("E#")) return 5;
		if(note.equalsIgnoreCase("F")) return 5;
		if(note.equalsIgnoreCase("F#")) return 6;
		if(note.equalsIgnoreCase("Gb")) return 6;
		if(note.equalsIgnoreCase("G")) return 7;
		if(note.equalsIgnoreCase("G#")) return 8;
		if(note.equalsIgnoreCase("Ab")) return 8;
		if(note.equalsIgnoreCase("A")) return 9;
		if(note.equalsIgnoreCase("A#")) return 10;
		if(note.equalsIgnoreCase("Bb")) return 10;
		if(note.equalsIgnoreCase("B")) return 11;
		if(note.equalsIgnoreCase("Cb")) return 11;
		else throw new RuntimeException();
	}


	/**
	 * Play a note - this method doesn't turn the note
	 * off after a specified period of time, the release
	 * method must be called to do that.
	 * @param note the note to play
	 */
	public void play(final int note)
	{
		channel.noteOn(note, 50);
	}

	/**
	 * Release a note that was previously played. If this
	 * note isn't on already, this method will do nothing.
	 */
	public void release(final int note)
	{
		channel.noteOff(note, 50);
	}

	/**
	 * Play a note for a certain amount of time.
	 * @param note the integer value for the note to play
	 * @param length the length to play the note (ms).
	 */
	public void play(final int note, final int length)
	{
		new Thread() {
			public void run() {
				channel.noteOn(note, 50);
				try {
					Thread.sleep(length);
				}
				catch(InterruptedException ex) {}
				finally {
					channel.noteOff(note, 50);
				}
				channel.noteOff(note, 50);
			}
		}.start();

	}

	/**
	 * Release all notes smoothly. This can be called in the World.stopped()
	 * method to ensure no notes are playing when the scenario has been 
	 * stopped or reset.
	 */
	public void turnAllOff()
	{
		try {
			channel.allNotesOff();
			//This would turn cut notes of immediately and suddenly:
			//channel.allSoundOff();
			sequencer.stop();
			sequencer.close();
		}
		catch(Exception ex) {}
	}

	/**
	 * Get the MidiChannel object that this MidiPlayer class is using.
	 * If you want to do some more advanced work with the midi channel, you
	 * can use this method to get the MidiChannel object and then work with
	 * it directly. The API for the MidiChannel class is available online
	 * as part of the javax.sound.midi package.<br>
	 * Examples of why you might want to use this - adjusting the speed
	 * notes are played / released with, adding sustain, adding pitch
	 * bend, soloing / muting individual channels - all are fairly advanced
	 * features and as such are not included in this class as standard (to
	 * keep things simple and avoid clutter.)
	 * @return the MidiChannel object behind this MidiPlayer.
	 */
	public MidiChannel getMidiChannel()
	{
		return channel;
	}


	/**
	 * Play a Midi file.
	 */
	public static void playMidiFile(String fileName)
	{
		try {
			Sequence sequence = MidiSystem.getSequence(new File(fileName));
			sequencer.open();
			sequencer.setSequence(sequence);
			sequencer.start();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Stop playing a Midi file.
	 */
	public static void stopMidiFile()
	{
		sequencer.stop();
	}

	/**
	 * Internal method to get the channel from the synthesizer in use.
	 * @param instrument the instrument to load initially.
	 */
	private MidiChannel getChannel(int instrument)
	{
		try {
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			for (int i=0; i<synthesizer.getChannels().length; i++) {
				synthesizer.getChannels()[i].controlChange(7, 127);
			}
			return synthesizer.getChannels()[instrument];
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}