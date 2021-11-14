package chatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Server extends Thread{ 
	private final int port;
	
	//possibility of thread interference. looking into this. review transactions.
	private Map<String, ServerWorker> workers = new HashMap<String, ServerWorker>();
	private List<ServerWorker> notloggedin = new ArrayList<ServerWorker>();
	
	//server stores topics so we dont have to parse through list of workers every time 
	//you send a message
	private Map<String, HashSet<String>> topics = new HashMap<String, HashSet<String>>();
	
	public Server(int port) {
		this.port = port;
	}
	
	public Map<String,ServerWorker> getWorkerList()
	{
		return workers;
	}
	
	public Map<String, HashSet<String>> getTopics()
	{
		return topics;
	}
	
	public void addTopic(String topic, String login)
	{
		if(topics.containsKey(topic))
		{
			topics.get(topic).add(login);
		}
		else
		{
			HashSet<String> x = new HashSet<String>();
			x.add(login);
			topics.put(topic, x);
		}
		
	}

	public void run() // every thread needs to override run method
	{
		try {
			ServerSocket server = new ServerSocket(port);
			while(true) //inside while loop so that we can continuously accept client connections
			{
				System.out.println("Accepting client connection...");
				Socket client = server.accept();
				System.out.println("Accepted connection from " + client);
				ServerWorker worker = new ServerWorker(this, client);  // worker is a thread. make a new thread for each client.
				//workers.add(worker);
				notloggedin.add(worker);
				worker.start();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void login(String login,ServerWorker worker)
	{
		workers.put(login,worker);
		notloggedin.remove(worker);
		
		
	}

	public void removeWorker(String name, ServerWorker worker) {
		workers.remove(name);
		notloggedin.remove(worker);
	}

	public void removefromTopic(String topic, String login) {
		HashSet<String> x = topics.get(topic);
		x.remove(login);
		if(x.isEmpty())
		{
			topics.remove(topic);
		}
	}
	
}
