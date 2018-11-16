package engine.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EvaluatorConfiguration {

	@JsonProperty
	private String ip;
	
	@JsonProperty
	private int port;
	
	@JsonProperty
	private String name;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
