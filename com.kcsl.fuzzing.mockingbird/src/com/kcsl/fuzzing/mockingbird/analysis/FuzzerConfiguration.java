package com.kcsl.fuzzing.mockingbird.analysis;

import org.json.simple.JSONObject;

public class FuzzerConfiguration {

	private static final String CONSOLE_LOG_LEVEL = "console_log_level";
	private static final String TIMEOUT = "timeout";

	public static void main(String[] args) throws Exception {
		System.out.println(new FuzzerConfiguration(1000, "CONFIG").toString());
	}
	
	private JSONObject fuzzerConfiguration;
	
	@SuppressWarnings("unchecked")
	public FuzzerConfiguration(long timeout, String logLevel) {
		this.fuzzerConfiguration = new JSONObject();
		JSONObject configuration = new JSONObject();
		configuration.put(TIMEOUT, timeout);
		configuration.put(CONSOLE_LOG_LEVEL, logLevel);
		fuzzerConfiguration.put("config", configuration);
	}
	
	@SuppressWarnings("unchecked")
	public void setTimeout(long timeout) {
		JSONObject configuration = (JSONObject) fuzzerConfiguration.get("config");
		configuration.put(TIMEOUT, timeout);
	}
	
	@SuppressWarnings("unchecked")
	public void setLogLevel(String logLevel) {
		JSONObject configuration = (JSONObject) fuzzerConfiguration.get(CONSOLE_LOG_LEVEL);
		configuration.put(CONSOLE_LOG_LEVEL, logLevel);
	}
	
	public String toString() {
		return fuzzerConfiguration.toJSONString();
	}
	
}
