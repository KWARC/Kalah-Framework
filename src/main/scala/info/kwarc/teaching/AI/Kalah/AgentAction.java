package info.kwarc.teaching.AI.Kalah;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Created by Marcel on 10.12.2016.
 */


public class AgentAction
{
    static int move(Agent agent, long timelimitInMs) throws TimeoutException {
        AgentThread tAgent=new AgentThread();
        tAgent.agent=agent;
        tAgent.doInit=false;
        Thread t=new Thread(tAgent);
        t.start();

        Instant before = Instant.now();
        while(true)
        {
            Instant after = Instant.now();
            long delta = Duration.between(before, after).toMillis();
            if (delta > timelimitInMs || !t.isAlive())
            {
                if(t.isAlive())
                {
                    t.stop();
                    throw new TimeoutException("Thread timed out after "+delta+"ms (max. "+timelimitInMs+"ms)!");
                }

                return tAgent.timeoutMove;
            }

            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {}
        }
    }

    static void init(Agent agent,Board board, boolean playerOne, long timelimitInMs) throws TimeoutException {
        AgentThread tAgent=new AgentThread();
        tAgent.doInit=true;
        tAgent.board=board;
        tAgent.playerOne=playerOne;
        tAgent.agent=agent;
        Thread t=new Thread(tAgent);
        t.start();

        Instant before = Instant.now();
        while(true)
        {
            Instant after = Instant.now();
            long delta = Duration.between(before, after).toMillis();
            if (delta > timelimitInMs || !t.isAlive())
            {
                if(t.isAlive())
                {
                    t.stop();
                    throw new TimeoutException("Thread timed out after "+delta+"ms (max. "+timelimitInMs+"ms)!");
                }
                return;
            }

            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {}
        }
    }

}


class AgentThread implements Runnable
{
    Agent agent;
    boolean doInit;
    volatile int timeoutMove;
    Board board;
    boolean playerOne;

    @Override
    public void run()
    {
        timeoutMove=-1;
        if(!doInit)
        {
            timeoutMove=agent.move();
        }
        else
        {
            agent.init(board,playerOne);
        }
    }
}
