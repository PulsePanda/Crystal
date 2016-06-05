package Utilities.Speech;

public class SpeechDriver {

	private TTS tts;

	public SpeechDriver() {
		tts = new TTS("kevin");
	}

	public void Speak(String m) {
		tts.speak(m);
	}
}
