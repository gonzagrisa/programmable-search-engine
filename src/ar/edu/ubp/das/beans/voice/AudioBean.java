package ar.edu.ubp.das.beans.voice;

import java.util.HashMap;
import java.util.Map;

public class AudioBean {
	private final Map<String, Object> audio = new HashMap<>();
	private final Map<String, Object> config = new HashMap<>();
	
	public Map<String, Object> getAudio() {
		return audio;
	}

	public Map<String, Object> getConfig() {
		return config;
	}
	
	public void setConfig(String encoding, int rate, String lang) {
		config.put("encoding", encoding);
		config.put("sampleRateHertz", rate);
		config.put("languageCode", lang);
	}
	
    public void setAudio(String content) {
    	audio.put("content", content);
    }
}
