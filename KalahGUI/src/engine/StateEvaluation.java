package engine;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = StateEvaluationDeserializer.class)
public class StateEvaluation {
	
	//note: the json deserialization process depends on the name of the variable moves and evaluation.
	private List<Integer> moves; 
	private Map<Integer, Double> evaluation; 
	
	private static ObjectMapper objm = new ObjectMapper();
	
	public static StateEvaluation fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objm.readValue(json, StateEvaluation.class);
	}
	
	public StateEvaluation(List<Integer> moves, Map<Integer, Double> evaluation) {
		super();
		this.moves = moves;
		this.evaluation = evaluation;
	}

	public List<Integer> getMoves() {
		return moves;
	}

	public void setMoves(List<Integer> moves) {
		this.moves = moves;
	}

	public Map<Integer, Double> getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Map<Integer, Double> evaluation) {
		this.evaluation = evaluation;
	}
	
	public String toJson() throws JsonProcessingException {
		return objm.writeValueAsString(this);
	}
	
	public static void main(String[] args) throws IOException {
		Map<Integer, Double> eval = new HashMap<>();
		eval.put(1, -100d);
		eval.put(2, 2d);
		eval.put(3, -.2);
		String json = new StateEvaluation(Arrays.asList(1,2,3), eval).toJson();
		System.out.println(json);
		StateEvaluation se = StateEvaluation.fromJson(json);
		System.out.println(se.getMoves() + " " + se.getEvaluation());
	}
	
	@Override
	public String toString() {
		return "State evaluation: " + evaluation.toString();
	}
}

@SuppressWarnings("serial")
class StateEvaluationDeserializer extends StdDeserializer<StateEvaluation>{

	public StateEvaluationDeserializer() {
		super(StateEvaluationDeserializer.class);
	}
	
	protected StateEvaluationDeserializer(Class<StateEvaluation> vc) {
		super(vc);
	}

	@Override
	public StateEvaluation deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		JsonNode node = arg0.getCodec().readTree(arg0);
		LinkedList<Integer> moves = new LinkedList<>();
		for(JsonNode move : node.get("moves")) {
			moves.add(move.asInt());
		}
		JsonNode eval = node.get("evaluation");
		HashMap<Integer, Double> map = new HashMap<>();
		for(Integer move : moves) {
			map.put(move, eval.get(move.toString()).asDouble());
		}
		return new StateEvaluation(moves, map);
	}
	
}
