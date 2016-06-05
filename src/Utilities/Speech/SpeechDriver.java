package Utilities.Speech;

public class SpeechDriver {

	private TTS tts;
	private boolean running = false;

	public SpeechDriver() {
		running = true;
		tts = new TTS("kevin");
	}

	public void Speak(String m) {
		tts.speak(m);
	}
}
