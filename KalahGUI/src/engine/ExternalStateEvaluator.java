package engine;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import game.Match;
import game.Player;
import game.State;
import game.StateLogic;
import gui.ActivityListener.Level;
import gui.Coordinator;

public class ExternalStateEvaluator extends StateEvaluator {

	private Thread workerThread;
	private BlockingDeque<EvaluationJob> jobs;
	
	private ExternalEvaluatorClient clt;
	private String ip;
	private int port;

	public ExternalStateEvaluator(int port) {
		this(port, "127.0.0.1"); // localhost
	}

	public ExternalStateEvaluator(int port, String ip) {
		this.ip = ip;
		this.port = port;
		jobs = new LinkedBlockingDeque<>();
		workerThread = new Thread(new EvaluationThread(ip, port, jobs, this));
		workerThread.start();
	}

	@Override
	public void close() {
		try {
			clt.close();
		} catch (IOException e) {
			Coordinator.log("Error while closing socket: " + e.getMessage(), Level.ERROR);
		}
		workerThread.interrupt();
	}

	@Override
	public String toString() {
		return ExternalStateEvaluator.class.getName() + ": " + " on " + ip + ":" + port;
	}

	@Override
	public String getName() {
		return this.toString();
	}

	@Override
	public void requestEvaluation(State s, Player player) {
		EvaluationJob job = new EvaluationJob(EvaluationJob.JobType.FULL, s, player);
		if(jobs.contains(job)) {
			Coordinator.log("Requested evaluation already issued. Skipping the request.", Level.DEBUG);
		}
		jobs.addFirst(job); // we want new requests to be handled first.
	}
	
	@Override
	public void requestPV(State s, Player player) {
		EvaluationJob job = new EvaluationJob(EvaluationJob.JobType.PV, s, player);
		if(jobs.contains(job)) {
			Coordinator.log("Requested evaluation already issued. Skipping the request.", Level.DEBUG);
		}
		jobs.addFirst(job);
	}

	@Override
	public void evaluateState(State s) {
		EvaluationJob job = new EvaluationJob(EvaluationJob.JobType.FLAT, s);
		if(jobs.contains(job)) {
			Coordinator.log("Requested evaluation already issued. Skipping the request.", Level.DEBUG);
		}
		jobs.addFirst(job);
	}
	
	private class EvaluationThread implements Runnable{

		private ExternalEvaluatorClient clt;
		private String ip;
		private int port;
		private BlockingDeque<EvaluationJob> jobs;
		private StateEvaluator parent;
		
		public EvaluationThread(String ip, int port, BlockingDeque<EvaluationJob> jobs, StateEvaluator parent) {
			this.port = port;
			this.ip = ip;
			this.clt = new ExternalEvaluatorClient(port, ip);
			this.jobs = jobs;
			this.parent = parent;
		}
		
		/** somewhat dirty hack: if we want the evaluation for the inactive player for the next half turn, 
		 * we just trick the engine to think it's his turn. This way we get the evaluation independently of the moving player
		 * while keeping the interface as simple as possible.
		 */
		private State adjustPlayer(State s, Player player) {
			return s.isP1ToMove() != player.isPlayerOne() ? new State(s.getP1Houses(), s.getP2Houses(), s.getP1Score(), s.getP2Score(), !s.isP1ToMove(), s.getGameInfo()) : s;			
		}
		
		private void connect() {
			while(true) {
				try {
//					Coordinator.log("ExternalStateEvaluator: Trying to connect to: " + ip + ":" + port, Level.DEBUG);
					clt.setup();
				}catch(Exception e) {
//					Coordinator.log("ExternalStateEvaluator: Could not connect to: "+ ip + ":" + port + "; " + e.toString() + ".", Level.WARN);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						return;
					}
					continue;
				}
				Coordinator.log("ExternalStateEvaluator: Connected to: " + ip + ":" + port, Level.DEBUG);
				break;
			}
		}
		
		@Override
		public void run() {
			//setup the connection
			connect();
			//serve requests
			while(true) {
				EvaluationJob job = null;
				try {
					job = jobs.take();
				} catch (InterruptedException e) {
					Coordinator.log("Could not retrieve evaluation job: " + e.toString(), Level.ERROR);
					continue;
				}
				//before working off any job, try to connect to the engine if there is no connection
				if(clt.isClosed()) {
					connect();
				}
				switch(job.tp) {
				case FLAT:
					try {
						State s = (State) job.args[0];
						Coordinator.sendFlatEvaluation(s, clt.getFlatEval(s));
					} catch (Exception e) { //gotta catch them all...
						Coordinator.log("Could not retrive state evaluation from engine: " + e.getClass().getName() + " - " + e.getMessage(), Level.ERROR);
						Coordinator.log("Returning 0.", Level.ERROR);
					}
					break;
				case FULL:
					try {
						State s = (State) job.args[0];
						Player player = (Player) job.args[1];
						s = adjustPlayer(s, player);
						StateEvaluation se = clt.getFullEval(s);
						Coordinator.sendFullEvaluation(s, se, player);
					} catch (Exception e) { //gotta catch them all...
						Coordinator.log("Could not retrive full state evaluation from engine: " + e.getClass().getName() + " - " + e.getMessage(), Level.ERROR);
					}
					break;
				case PV:
					try {
						State s = (State) job.args[0];
						Player player = (Player) job.args[1];
						s = adjustPlayer(s, player);
						List<Integer> pv = clt.getPV(s);
						Match pvAsMatch = new Match(s.getGameInfo(), new LinkedList<>(), "Principal Variation of " + s.toString());
						pvAsMatch.addState(s);
						for(Integer move : pv) {
							s = StateLogic.getSuc(move, s);
							pvAsMatch.addState(s);
						}
						Coordinator.sendPV(pvAsMatch);
					}catch (Exception e) {//gotta catch them all...
						Coordinator.log("Could not retrive principal variation from engine: " + e.getClass().getName() + " - " + e.getMessage(), Level.ERROR);
					}
					break;
				default:
					System.err.println("Unknown Evaluation Job received!");
					System.exit(0);
				}
			}
		}
	}
	
	private static class EvaluationJob{
		public static enum JobType{FLAT, FULL, PV};
		public static int jobIDCounter = 0;
		public int jobID;
		public JobType tp;
		public Object[] args;
		
		public EvaluationJob(JobType jtp, Object... args) {
			this.args = args;
			jobID = EvaluationJob.jobIDCounter;
			EvaluationJob.jobIDCounter += 1;
			tp = jtp;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof EvaluationJob) {
				return ((EvaluationJob) other).jobID == jobID;
			}
			return false;
		}
	}
}
