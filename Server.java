package learningSockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server extends Thread{ 
	private final int port;
	private ArrayList<ServerWorker> workers = new ArrayList<>();
	
	public Server(int port) {
		this.port = port;
	}
	
	public List<ServerWorker> getWorkerList()
	{
		return workers;
	}

	public void run() // every thread needs to override run method
	{
		try {
			ServerSocket server = new ServerSocket(port);
			while(true) //inside while looop so that we can continuously accept client connections
			{
				System.out.println("Accepting client connection...");
				Socket client = server.accept();
				System.out.println("Accepted connection from " + client);
				ServerWorker worker = new ServerWorker(this, client);  // worker is a thread. make a new thread for each client.
				workers.add(worker);
				worker.start();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
