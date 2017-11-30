package info.kwarc.teaching.AI.Kalah.util;
import info.kwarc.teaching.AI.Kalah.Agents.Agent;
import info.kwarc.teaching.AI.Kalah.Board;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Created by Marcel on 10.12.2016.
 */


public class AgentAction
{
    public static int move(Agent agent, long timelimitInMs) throws TimeoutException {
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

    public static void init(Agent agent, Board board, boolean playerOne, long timelimitInMs) throws TimeoutException {
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
/*
class SecureClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (/* name is white-listed JDK class// true ) return super.loadClass(name);
        return findClass(name);
    }
    @Override
    public Class findClass(String name) {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }
    private byte[] loadClassData(String name) {
        // load the untrusted class data here
    }
}

class MySecurityManager extends SecurityManager {
    private Object secret;
    public MySecurityManager(Object pass) { secret = pass; }
    private void disable(Object pass) {
        if (pass == secret) secret = null;
    }
    // ... override checkXXX method(s) here.
    // Always allow them to succeed when secret==null
}

class MyIsolatedThread extends Thread {
    private AgentThread t;
    private Object pass = new Object();
    private SecureClassLoader loader = new SecureClassLoader();
    private MySecurityManager sm = new MySecurityManager(pass);
    public void run() {
        SecurityManager old = System.getSecurityManager();
        System.setSecurityManager(sm);
        runUntrustedCode();
        sm.disable(pass);
        System.setSecurityManager(old);
    }
    private void runUntrustedCode() {
        try {
            t.run();
        } catch (Throwable t) {}
    }
}
*/