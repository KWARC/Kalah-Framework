package game;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


@JsonSerialize(using = StateSerializer.class)
@JsonDeserialize(using = StateDeserializer.class)
public class State {

	private GameInfo gi;

	private final int[] p1Houses;
	private final int[] p2Houses;
	private final int p1Score;
	private final int p2Score;
	private final boolean p1ToMove;
	private final Result res;
	
	private static ObjectMapper objm = new ObjectMapper();

	public State(int[] p1Houses, int[] p2Houses, int p1Score, int p2Score,
			boolean p1ToMove, GameInfo gi) {
		this.p1Houses = p1Houses;
		this.p2Houses = p2Houses;
		this.p1Score = p1Score;
		this.p2Score = p2Score;
		this.p1ToMove = p1ToMove;
		this.res = Result.get(p1Score, p2Score, gi);
		this.gi = gi;
	}
	
	public State(int[] p1Houses, int[] p2Houses, int p1Score, int p2Score,
			boolean p1ToMove, int houses, int seeds) {
		this.p1Houses = p1Houses;
		this.p2Houses = p2Houses;
		this.p1Score = p1Score;
		this.p2Score = p2Score;
		this.p1ToMove = p1ToMove;
		this.res = Result.get(p1Score, p2Score, gi);
		this.gi = new GameInfo(houses, seeds);
	}
	
	public State(int houses, int seeds) {
		p1Houses = new int[houses]; p2Houses = new int[houses];
		Arrays.fill(p1Houses, seeds); Arrays.fill(p2Houses, seeds);
		p1Score = 0; p2Score = 0;
		p1ToMove = true;
		res = Result.TBD;
		gi = new GameInfo(houses, seeds);
	}

	public String toString() {
		return "state: " + Arrays.toString(p1Houses) + " " + Arrays.toString(p2Houses)
				+ " " + p1Score + " " + p2Score + " " + p1ToMove + " "
				+ res.toString();
	}

	public boolean isTerminal() {
		return this.res != Result.TBD;
	}

	public int[] getP1Houses() {
		return p1Houses;
	}

	public int[] getP2Houses() {
		return p2Houses;
	}
	
	public Player getPlayerToMove() {
		return p1ToMove ? Player.ONE : Player.TWO;
	}
	
	public int[] getP2HousesRev() {
		int[] rev = new int[p2Houses.length];
		for(int i = 0; i < p2Houses.length; i++) {
			rev[rev.length - 1 - i] = p2Houses[i];
		}
		return rev;
	}

	public int getP1Score() {
		return p1Score;
	}

	public int getP2Score() {
		return p2Score;
	}

	public boolean isP1ToMove() {
		return p1ToMove;
	}

	public Result getResult() {
		return res;
	}

	public GameInfo getGameInfo() {
		return gi;
	}
	
	public String toJson() throws JsonProcessingException {
		return objm.writeValueAsString(this);
	}
	
	public static State fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objm.readValue(json, State.class);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof State)) {
			return false;
		}
		State other = (State) o;
		if(! Arrays.equals(other.p1Houses, this.p1Houses)) return false;
		if(! Arrays.equals(other.p2Houses, this.p2Houses)) return false;
		if(other.p1Score != this.p1Score) return false;
		if(other.p2Score != this.p2Score) return false;
		if(other.isP1ToMove() != this.isP1ToMove()) return false;
		if(other.res != this.res) return false; //for enums referential and structural equality coincide.
		return other.getGameInfo().equals(this.gi);
	}
	
	@Override
	public int hashCode() {
		//Why am I doing this? In order for java.util.HashMap to fall back to equals.
		//See: https://stackoverflow.com/questions/21600344/java-hashmap-containskey-returns-false-for-existing-object,
		return 42;
	}
	
	public static class GameInfo {

		private final int houses;
		private final int seeds;
		private final int target;

		public GameInfo(int houses, int seeds) {
			this.houses = houses;
			this.seeds = seeds;
			this.target = houses * seeds;
		}

		public int getHouses() {
			return houses;
		}

		public int getSeeds() {
			return seeds;
		}

		public int target() {
			return target;
		}

		@Override
		public String toString() {
			return "Kalah(" + houses + ", " + seeds + ")";
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof GameInfo)) {
				return false;
			}
			GameInfo other = (GameInfo) o;
			if(other.houses != houses) return false;
			return other.seeds == seeds;
		}
	}
	
	public static enum Result {
		TBD, P1WIN, DRAW, P2WIN;

		public static Result get(int player1Score, int player2Score, GameInfo gi) {
			if (player1Score > gi.target()) {
				return Result.P1WIN;
			}
			if (player2Score > gi.target()) {
				return Result.P2WIN;
			}
			if (player1Score + player2Score == 2 * gi.target()) {
				return Result.DRAW;
			}
			return Result.TBD;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		State test = new State(3,3);
		System.out.println(test);
		String json = test.toJson();
		System.out.println(json);
		System.out.println(State.fromJson(json));
	}
}

class StateSerializer extends StdSerializer<State>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StateSerializer(){
		super(State.class);
	}
	
	protected StateSerializer(Class<State> t) {
		super(t);
	}

	@Override
	public void serialize(State arg0, JsonGenerator arg1,
			SerializerProvider arg2) throws IOException {
		arg1.writeStartObject();
		arg1.writeObjectField("p1h", arg0.getP1Houses());
		arg1.writeObjectField("p2h", arg0.getP2Houses());
		arg1.writeNumberField("p1s",arg0.getP1Score());
		arg1.writeNumberField("p2s",arg0.getP2Score());
		arg1.writeBooleanField("p1tm",arg0.isP1ToMove());
		arg1.writeObjectField("gi", arg0.getGameInfo());
		arg1.writeEndObject();
	}
}

class StateDeserializer extends StdDeserializer<State>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StateDeserializer(){
		super(State.class);
	}
	
	protected StateDeserializer(Class<?> vc) {
		super(vc);
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
	public State deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		JsonNode node = arg0.getCodec().readTree(arg0);
		int[] p1h = handle(node.get("p1h"));
		int[] p2h = handle(node.get("p2h"));
		int p1s = node.get("p1s").asInt();
		int p2s = node.get("p2s").asInt();
		boolean p1tm = node.get("p1tm").asBoolean();
		State.GameInfo gi = new State.GameInfo(node.get("gi").get("houses").asInt(), node.get("gi").get("houses").asInt());
		return new State(p1h,p2h,p1s,p2s,p1tm,gi);
	}

	
}


