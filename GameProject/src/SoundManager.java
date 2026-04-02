import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {
	
	HashMap<String, Clip> audioClips;
	
	private static SoundManager instance;
	
	private SoundManager() {
		audioClips = new HashMap<>();
		
		// Load Sound Clips Here
	}
	
	public static SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}
		return instance;
	}
	
	public Clip loadClip (String filePath, String title) {
		
		if (audioClips.containsKey(title)) {
			return null;
		}
		
		Clip clip = null;
		
		try {
			InputStream audioSrc = getClass().getResourceAsStream(filePath);
			
			if (audioSrc == null) {
				throw new IllegalArgumentException("Sound file not found: " + filePath);
			}
			
			BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
			
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			
			audioClips.put(title, clip);
			
		} catch (Exception e) {
			System.out.println("Sound File Not Found: " + filePath);
		}
		
		return clip;
	}
	
	public Clip getClip(String title) {
		return audioClips.get(title);
	}
	
	public void playClip(String title, boolean looping) {
		Clip clip = getClip(title);
		
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
	}
	
	public void stopClip(String title) {
		Clip clip = getClip(title);
		
		if (clip != null) {
			clip.stop();
		}
	}
	
	public void setVolume (String title, float volume) {
		Clip clip = getClip(title);

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();

		gainControl.setValue(gain);
	}
}
