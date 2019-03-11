package de.dfki.app;

import de.dfki.assemblyrobot.AssemblyRobotView;
import de.dfki.assemblyrobot.UpdateGrammar;
import de.dfki.pdp.grammar.GrammarManager;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Thrift client that updates the state of the Hololens.
 * Requests are queued and same requests are overwritten by their id: e.g. calling updateGrammar requests
 * will replace old requests if they are not sent yet.
 */
public class HololensClient {
    private Thread networkThread;
    private Map<String, Runnable> tasks = new HashMap<>(); //used to store just the newest update method
    private Queue<String> taskList = new ArrayDeque<>(); //used to alternate between all calls
    private final Logger log = LoggerFactory.getLogger(HololensClient.class);
    private final String host;
    private final int port;
    private TTransport transport;
    private TProtocol protocol;
    private AssemblyRobotView.Client client;

    public HololensClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.networkThread = new Thread(() -> {
            this.update();
        });
        this.networkThread.setDaemon(true);
        this.networkThread.start();
    }

    private void update() {
        while (!Thread.currentThread().isInterrupted()) {
            Runnable task = null;
            synchronized (this) {
                if (taskList.isEmpty()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        log.debug("{} interrupted", this.getClass(), e);
                        return;
                    }
                }

                String taskId = taskList.poll();
                if (taskId == null) {
                    continue;
                }

                log.info("Updating hololens on {}", taskId);
                task = this.tasks.get(taskId);
                tasks.remove(taskId);
            }

            task.run();
        }
    }

    public synchronized void addTask(String id, Runnable runnable) {
        tasks.put(id, runnable);
        if (taskList.contains(id)) {
            taskList.remove(id);
        }
        taskList.add(id);
        this.notifyAll();
    }

    public void updateGrammar(GrammarManager gm) {
//        Grammar grammar = gm.createGrammar();
//        String grammarStr = grammar.toString();
        String grammarStr = "TODO";
        var request = new UpdateGrammar();
        request.setGrammar(grammarStr);
        addTask("updateGrammar", () -> updateGrammar(request));
    }

    private void call(NetworkCall networkCall) {
        boolean connected = transport != null;

        if (!connected) {
            transport = new TSocket(host, port);
        }

        try {
            if (!connected) {
                protocol = new TBinaryProtocol(transport);
                client = new AssemblyRobotView.Client(protocol);
            }
            transport.open();
            networkCall.call(client);
            System.out.println("ok ");
        } catch (TException e) {
            log.warn("Could not connect to Hololens: {}", e.getMessage());
        } finally {
            transport.close();
            transport = null;
        }
    }

    private void updateGrammar(UpdateGrammar request) {
        call(client -> client.updateGrammar(request));
    }

    interface NetworkCall {
        void call(AssemblyRobotView.Client client) throws TException;
    }

    public static void main(String[] args) {
//        var client = new HololensClient("10.2.0.32", 11000);
//        var client = new HololensClient("10.2.0.28", 11000);
        var client = new HololensClient("172.16.68.112", 11000);

        client.updateGrammar(new UpdateGrammar());
    }
}
