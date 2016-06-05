package Utilities.Speech;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TTS {
	private String voicename = "kevin";

	public TTS(String voicename) {
		this.voicename = voicename;
	}

	public TTS() {

	}

	public void speak(String text) {
		Voice voice;
		VoiceManager voiceManager = VoiceManager.getInstance();
		voice = voiceManager.getVoice(voicename);
		voice.allocate();
		voice.speak(text);
	}
}
