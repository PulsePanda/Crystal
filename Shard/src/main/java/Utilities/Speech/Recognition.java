/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities.Speech;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.IOException;

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
