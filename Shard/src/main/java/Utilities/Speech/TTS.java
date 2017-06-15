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
