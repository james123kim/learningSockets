package learningSockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class ServerWorker extends Thread{     
	private final Socket client;
	private final Server server;
	private String login = null; //username
	private OutputStream output;

	public ServerWorker(Server server, Socket client) 
	{
		this.server = server;
		this.client = client;
	}
	public void run() // overriding the run method for thread
	{
		try {
			handleClientSocket();
		}
		catch (IOException|InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleClientSocket() throws IOException, InterruptedException {
		InputStream input = client.getInputStream();
		this.output = client.getOutputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line; //we are initializing in the while loop so we can read multiple times
		while((line = reader.readLine()) != null) //get line input here ( HAVE to or else endoffile causes crash)
		{
			String[] tokens = line.split(" ");  // this stores all characters INCLUDING BACKSPACE.. MUST CHANGE or else cant edit commands
			if(tokens != null && tokens.length > 0) // check for null pointer exceptions
			{
				String cmd = tokens[0];
				if("quit".equalsIgnoreCase(cmd)) // if input is quit,  quit.
				{
					break;
				}
				else if ("login".equalsIgnoreCase(cmd))
				{
					handleLogin(output, tokens);
				}
				else 
				{
					String msg = "unknown command: " + cmd + "\r\n";
					output.write(msg.getBytes());
				}
				//String msg = "You typed: " + line + "\r\n";         // echo it back
				//output.write(msg.getBytes());      
			}
			else
			{
				String msg = "format is login <user> <pass> \r\n";
				send(msg);
			}
		}
//		for(int i =0; i <10; i++) // testing multithreading by counting to 10 
//		{
//			output.write(("Time now is " + new Date() + "\r\n").getBytes()); // \r\n is newline for windows apparently
//			Thread.sleep( 1000);
//		}
		client.close();
	}
	
	public String getLogin()
	{
		return login;
	}
	
	private void handleLogin(OutputStream output, String[] tokens) throws IOException
	{
		if(tokens.length == 3) //expected tokens should be login, user, password
		{
			String login = tokens[1];
			String password = tokens[2];
			System.out.println(login + password);
			if((login.equals("tae") && password.equals("xyz")) || (login.equals("james") && password.equals("abc")) ) //hard coded user and pass
			{
				String msg = "successfully logged in\r\n";
				send(msg);
				
				this.login = login;
				System.out.println("user has logged in: " + login);
				
				
				//get list of workers to broadcast whos online to all online members
				List<ServerWorker> workers = server.getWorkerList();     
				// send current user list of ppl online
				for(ServerWorker worker: workers)
				{
					if(!login.equals(worker.getLogin())) // dont send urself to urself
					{
						if(worker.getLogin()!=null) // dont send msg to not logged in ppl
						{
							String msg2 = "online: " + worker.getLogin() + "\r\n";
							send(msg2);
						}
					}
				
				}
				
				//send other online ppl the current user's status
				String onlineMsg = "online: " + login + "\r\n";
				for(ServerWorker worker: workers) 
				{
					if(!login.equals(worker.getLogin()))//dont send to urself
					{
						worker.send(onlineMsg);
					}
				}
				
			}
			else
			{
				String msg = "login failed \r\n";
				output.write(msg.getBytes());
			}
			
		}
	}
	
	private void send(String msg) throws IOException {
		if(login != null) //only sends to logged in users
		{
			output.write(msg.getBytes());
		}
	}
	
}
