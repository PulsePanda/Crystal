package Utilities.Speech;

import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class Recognition {
	LiveSpeechRecognizer recognizer;
	boolean running = false;

	public Recognition() throws IOException {
		Configuration configuration = new Configuration();

		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		recognizer = new LiveSpeechRecognizer(configuration);
		recognizer.startRecognition(true);
		recognizer.stopRecognition();
	}

	public void Start() {
		running = true;
		recognizer.startRecognition(false);
		SpeechResult result;
		while ((result = recognizer.getResult()) != null && running) {
			System.out.format("Hypothesis: %s\n", result.getHypothesis());
		}
	}

	public void Stop() {
		running = false;
		recognizer.stopRecognition();
	}
}
