package engine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import game.State;
import gui.ActivityListener.Level;
import gui.Coordinator;

public class ExternalEvaluatorClient {
	/**
	 * The protocol this client implements consists of two stages.
	 * 1. Setup Stage:
	 * 		1.1 The client connects to the server (a kalah engine).
	 * After the setup, the next stage is reached.
	 * 2. Evaluation Stage:
	 * 		2.1. The client sends a message containing either
	 * 			"1" for a full state evaluation using search
	 * 			"2" for a flat state evaluation or termed differently, evaluation function applied to the current state
	 * 			"3" for the transmission of the principal variation in the current state.
	 * 		2.2. The client then sends a (Kalah) state in JSON format to the server, requesting the previously specified evaluation for that state.
	 * 		2.3. The server answers by sending the according evaluation in JSON format back to the client, containing the evaluation of all successor states.
	 * This stage is maintained (and repeated, when the client sends a new request) until the client closes the connection.
	 * On an local error, both the client and server close the connection, without messaging the other one explicitly.
	 * 
	 * Note: whatever uses this class, it is responsible for closing the client connection. 
	 * This should be done either if the connection should be closed, or if an error occured.
	 */

	private int port;
	private String ip = "127.0.0.1"; //localhost
	private Socket sock;
	private DataOutputStream out;
	private BufferedReader in;
	private static ObjectMapper objm = new ObjectMapper();
	
	public ExternalEvaluatorClient(int port) {
		this.port = port;
	}
	
	public ExternalEvaluatorClient(int port, String ip) {
		this(port);
		this.ip = ip;
	}
	
	//convenience so that you don't have to type "\n" all the time.
	//And by all the time I mean once... *facepalm*
	private void write(String s) throws IOException {
		out.writeBytes(s  + "\n");
	}
	
	private String read() throws IOException {
		Coordinator.log("Awaiting answer.", Level.DEBUG);
		String lnin = in.readLine();
		Coordinator.log("Received answer: " + lnin, Level.DEBUG);
		if(lnin == null) throw new IOException("Server closed the connection unexpectedly.");
		return lnin;
	}
	
	public void setup() throws UnknownHostException, IOException {
			sock = new Socket(ip, port);
			Coordinator.log("Connected to " + ip + ":" + port + ".", Level.INFO);
			out = new DataOutputStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	}
	
	/**
	 * This method returns the external evaluator's full evaluation of the passed state.
	 * 
	 * @param s the state to evaluate by the external evaluator.
	 * @return The normalized state evaluation for the passed state.
	 * @throws JsonProcessingException If the retrieved message could not be json-interpreted as an state evaluation object.
	 * @throws IOException If the communication did not go right.
	 */
	public StateEvaluation getFullEval(State s) throws JsonProcessingException, IOException{
		Coordinator.log("Requesting full state evaluation for state " + s.toString() + ".", Level.DEBUG);
		write("1");
		write(s.toJson());
		String lnin = read();
		StateEvaluation eval = StateEvaluation.fromJson(lnin);
		Coordinator.log("Successfully received and reconstructed the full evaluation.", Level.DEBUG);
		return eval;
	}
	
	public double getFlatEval(State s) throws IOException {
		Coordinator.log("Requesting flat state evaluation for state " + s.toString() + ".", Level.DEBUG);
		write("2");
		write(s.toJson());
		String lnin = read();
		double eval = Double.parseDouble(lnin);
		Coordinator.log("Successfully received and reconstructed the flat evaluation.", Level.DEBUG);
		return eval;
	}
	
	public List<Integer> getPV(State s) throws IOException{
		Coordinator.log("Requesting principal variation for state " + s.toString() + ".", Level.DEBUG);
		write("3");
		write(s.toJson());
		String lnin = read();
		//.boxed() transforms primitive type instances into their corresponding wrapper type instances
		List<Integer> pv = Arrays.stream(objm.readValue(lnin, int[].class)).boxed().collect(Collectors.toList());
		return pv;
	}
	
	public boolean isClosed() {
		return sock.isClosed();
	}
	
	public void close() throws IOException {
		if(!sock.isClosed()) {
			sock.close();
			Coordinator.log("Socket " + ip + ":" + port + " closed.", Level.DEBUG);
		}
	}
	
	public static void main(String[] args) {
		ExternalEvaluatorClient clt = new ExternalEvaluatorClient(2601);
		try {
			clt.setup();
			System.out.println(clt.getFullEval(new State(3,3)));
			clt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
