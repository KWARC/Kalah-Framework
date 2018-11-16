package engine.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class EvaluationConfiguration {

	private static ObjectMapper objm = new ObjectMapper(new YAMLFactory());

	@JsonProperty
	private List<EvaluatorConfiguration> evaluators;
	
	@JsonProperty
	private String north;
	
	@JsonProperty
	private String south;
	
	public List<EvaluatorConfiguration> getEvaluators() {
		return evaluators;
	}

	public void setEvaluators(List<EvaluatorConfiguration> evaluators) {
		this.evaluators = evaluators;
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

	public static EvaluationConfiguration readConfig(File confFile) throws JsonParseException, JsonMappingException, IOException {
		return objm.readValue(confFile, EvaluationConfiguration.class);
	}
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		EvaluationConfiguration eval = readConfig(new File("eval.yaml"));
		System.out.println(eval.getEvaluators().get(0).getName());
		System.out.println(eval.getSouth());
	}
}
