package engine.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class CmapConfiguration {

	private static ObjectMapper objm = new ObjectMapper(new YAMLFactory());
	
	@JsonProperty
	private List<CmapDefinition> cmaps;
	
	@JsonProperty
	private String north;
	
	@JsonProperty
	private String south;

	@JsonProperty
	private String fallback;
	
	
	
	public String getFallback() {
		return fallback;
	}

	public void setFallback(String fallback) {
		this.fallback = fallback;
	}

	public List<CmapDefinition> getCmaps() {
		return cmaps;
	}

	public void setCmaps(List<CmapDefinition> cmaps) {
		this.cmaps = cmaps;
	}

	public String getNorth() {
		return north;
	}

	public void setNorth(String north) {
		this.north = north;
	}

	public String getSouth() {
		return south;
	}

	public void setSouth(String south) {
		this.south = south;
	}
	
	public static CmapConfiguration fromFile(File confFile) throws JsonParseException, JsonMappingException, IOException {
		return objm.readValue(confFile, CmapConfiguration.class);
	}
}
