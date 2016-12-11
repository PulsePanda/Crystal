package Utilities.Speech;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TTS {
	private String voicename = "kevin";
	// private ArrayList<String> ttsQueue;

	public TTS(String voicename) {
		this.voicename = voicename;
		// ttsQueue = new ArrayList<String>();
	}

	public TTS() {
		// ttsQueue = new ArrayList<String>();
	}

	public synchronized void speak(String text) {
		// ttsQueue.add(text);

		// while (!ttsQueue.isEmpty()) {
		Voice voice;
		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice(voicename);
		voice.allocate();
		voice.speak(text);
		// voice.speak(ttsQueue.get(0));
		// ttsQueue.remove(0);
		// }
	}
}
