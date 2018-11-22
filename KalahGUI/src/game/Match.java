package game;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**A representation of a match. 
 * In this project, the class is used in a broader sense as a collection of states that you can name.
 * 
 * @author LUCA
 *
 */
@JsonSerialize(using = MatchSerializer.class)
@JsonDeserialize(using = MatchDeserializer.class)
public class Match {

	private State.GameInfo gi;
	private List<State> states;
	private String p1, p2, name;
	
	private static ObjectMapper objm = new ObjectMapper();
	
	public static Match fromFile(File f) throws JsonParseException, JsonMappingException, IOException {
		return objm.readValue(f, Match.class);
	}

	public Match(State.GameInfo gi, List<State> states, String p1, String p2) {
		this.gi = gi;
		this.states = states;
		this.p1 = p1;
		this.p2 = p2;
		this.name = getDescriptiveName();
	}

	public Match(State.GameInfo gi, String p1, String p2) {
		this(gi, new LinkedList<>(), p1, p2);
		addState(new State(gi.getHouses(), gi.getSeeds()));		
	}
	
	public Match(State.GameInfo gi, String name) {
		this(gi, "", "");
		this.name = name;
	}
	
	public Match(State.GameInfo gi, LinkedList<State> states, String name) {
		this(gi, states, "", "");
		this.name = name;
	}
	
	public Match(State.GameInfo gi, String p1, String p2, String name) {
		this(gi, p1, p2);
		this.name = name;
	}

	public void addState(State s) {
		this.states.add(s);
	}

	public State.GameInfo getGi() {
		return gi;
	}

	public List<State> getStates() {
		return states;
	}
	
	public String getP1Name() {
		return p1;
	}

	public String getP2Name() {
		return p2;
	}
	
	public String getDescriptiveName() {
		return p1 + " vs. " + p2;
	}

	public String toString() {
		String acc = "";
		for(State s : getStates()) {
			acc += s.toString() + "\n";
		}
		return acc;
	}
	
	public static void main(String[] args) throws Exception{
		State.GameInfo gi = new State.GameInfo(6, 6);
		Match m = new Match(gi, "north", "south");
		for(int i = 0; i < 5; i++) {
			int[] p2h = { 6, 4, 2, 3, 1, 1 };
			int[] p1h = { 1, 1, 1, 1, 1, 1 };
			int p1s = 0, p2s = 0;
			State s = new State(p1h, p2h, p1s, p2s, false, gi);
			m.addState(s);
		}
		ObjectMapper objm = new ObjectMapper();
		objm.enable(SerializationFeature.INDENT_OUTPUT);
		objm.writeValue(new File("test.json"), m);
		Match test = objm.readValue(new File("test.json"), Match.class);
		System.out.println(test);
	}

	public String getName() {
		return name;
	}
}

class MatchSerializer extends StdSerializer<Match>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MatchSerializer() {
		super(Match.class);
	}

	@Override
	public void serialize(Match arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
		arg1.writeStartObject();
		arg1.writeStringField("playerOne", arg0.getP1Name());
		arg1.writeStringField("playerTwo", arg0.getP2Name());
		arg1.writeArrayFieldStart("moves");
		for (State s : arg0.getStates()) {
			arg1.writeStartObject();
			arg1.writeObjectField("p1h", s.getP1Houses());
			arg1.writeObjectField("p2h", s.getP2Houses());
			arg1.writeNumberField("p1s", s.getP1Score());
			arg1.writeNumberField("p2s", s.getP2Score());
			arg1.writeBooleanField("p1tm", s.isP1ToMove());
			arg1.writeEndObject();
		}
		arg1.writeEndArray();
		arg1.writeStringField("version", "long");
		arg1.writeObjectField("matchInfo", arg0.getGi());
		arg1.writeEndObject();
	}
	
}

class MatchDeserializer extends StdDeserializer<Match>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MatchDeserializer() {
		super(Match.class);
	}

	private int[] handle(JsonNode n){
		int[] ret = new int[n.size()];
		int i = 0;
		Iterator<JsonNode> iter = n.iterator();
		while(iter.hasNext()){
			JsonNode nxt = iter.next();
			ret[i] = nxt.asInt();
			i++;
		}
		return ret;
	}
	
	@Override
	public Match deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
		JsonNode node = arg0.getCodec().readTree(arg0);
		JsonNode mi = node.get("matchInfo");
		int houses = mi.get("houses").asInt();
		int seeds = mi.get("seeds").asInt();
		State.GameInfo gi = new State.GameInfo(houses, seeds);
		String p1 = node.get("playerOne").asText();
		String p2 = node.get("playerTwo").asText();
		Match m = new Match(gi, new LinkedList<>(), p1, p2);
		String version = node.get("version").asText();
		JsonNode moves = node.get("moves");
		if(version.equals("short")) {
			State s = new State(gi.getHouses(), gi.getSeeds());
			m.addState(s);
			for(JsonNode move : moves) {
				s = StateLogic.getSuc(move.asInt(), s);
				m.addState(s);
			}
		}else if(version.equals("long")){
			for(JsonNode move : moves) {
				int[] p1h = handle(move.get("p1h"));
				int[] p2h = handle(move.get("p2h"));
				int p1s = move.get("p1s").asInt();
				int p2s = move.get("p2s").asInt();
				boolean p1tm = move.get("p1tm").asBoolean();
				m.addState(new State(p1h, p2h, p1s, p2s, p1tm, gi));
			}
		}else {
			throw new IOException("Version unknown: " + version);
		}
		return m;
	}
}
