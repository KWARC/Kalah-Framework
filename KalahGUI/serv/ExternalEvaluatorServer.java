import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket ;
import java.net.Socket;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ExternalEvaluatorServer {
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
	private ServerSocket sock;
	private Socket conn;
	private DataOutputStream out;
	private BufferedReader in;
	private Evaluator evaluator;
	private static ObjectMapper objm = new ObjectMapper();
	
	public ExternalEvaluatorServer(int port, Evaluator evaluator) throws IOException {
		this.port = port;
		this.evaluator = evaluator;
		sock = new ServerSocket(this.port);
	}
	
	public void listen() throws IOException {
		conn = sock.accept();
		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        out= new DataOutputStream(conn.getOutputStream());
	}
	
	private void send(String s) throws IOException {
		out.writeBytes(s + "\n");
	}
	
	private Optional<String> read() throws IOException{
		String recv = in.readLine();
		return recv == null ? Optional.empty() : Optional.of(recv);
	}
	
	public void serveEvalRequests() throws IOException {//Note: call close() after this call.
		while(!conn.isClosed()) {
			Optional<String> opt = read();
			if(!opt.isPresent()){//connection closed by client
				close();
				return;
			}
			Optional<String> json = read();
			if(!json.isPresent()) {//connection closed by client
				close();
				return;
			}
			State toEval = State.fromJson(json.get());
			String answ = null;
			switch(opt.get()) {
			case "1":
				answ = evaluator.evaluateFull(toEval).toJson();
				break;
			case "2":
				answ = Double.toString(evaluator.evaluateState(toEval));
				break;
			case "3":
				answ = objm.writeValueAsString(evaluator.getPrincipalVariation(toEval));
				break;
			default:
				System.err.println("Received invalid evaluation command, closing connection.");
				close();
				return;
			}
			send(answ);
		}
	}
	
	public void close() throws IOException {
		if(!conn.isClosed()) conn.close();
		if(!sock.isClosed()) sock.close();
	}
	
	public static void main(String[] args) throws Exception {
		Evaluator eval = ??? //your engine frontend instance implementing Evaluator goes here
		ExternalEvaluatorServer evalServ = new ExternalEvaluatorServer(2601, eval);
		while(true) {
			try {
				evalServ.listen();
				evalServ.serveEvalRequests();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				}catch(Exception ee) {
					
				}
			}
		}
	}
}
