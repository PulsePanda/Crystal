package Utilities.Speech;

import java.io.IOException;

public class SpeechDriver {

	private TTS tts;
	private Recognition recog;

	public SpeechDriver() {
		tts = new TTS("kevin");
		try {
			recog = new Recognition();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Speak(String m) {
		tts.speak(m);
	}

	public void Listen() {
		recog.Start();
	}

	public void StopListening() {
		recog.Stop();
	}
}
