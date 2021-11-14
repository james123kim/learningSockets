package chatServer;

import java.io.BufferedReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ServerWorker extends Thread {
	private final Socket client;
	private final Server server;
	private String login = null; // username
	private OutputStream output;
	private HashSet<String> topicSet = new HashSet<String>();

	public ServerWorker(Server server, Socket client) {
		this.server = server;
		this.client = client;
	}

	public void run() // overriding the run method for thread
	{
		try {
			handleClientSocket();
		} catch (SocketException e) {
			try {
				handleLogoff();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void handleClientSocket() throws IOException, InterruptedException {
		InputStream input = client.getInputStream();
		this.output = client.getOutputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line; // we are initializing inside the while loop so we can read multiple times
		while (true) {
			line = reader.readLine();

			if (line != null) {
				while (line.contains("\b")) // handle backspace characters
				{
					line = line.replaceAll("^\b+|[^\b]\b", "");
				}

				String[] tokens = line.split(" ");
				if (tokens != null && tokens.length > 0) // check for null pointer exceptions
				{
					String cmd = tokens[0];
					if("help".equalsIgnoreCase(cmd))
					{
						String msg = "login <user> <pass>" + "\r\n" +
								"logout/quit" + "\r\n" + 
								"msg <recipient/group> <message>" + "\r\n" +
								"join <#groupname>" + "\r\n" +
								"leave <#groupname>";
						send(msg);
					}
					if (login == null) {					//commands that notloggedin can do
						if ("login".equalsIgnoreCase(cmd)) {
							if (tokens.length == 3) {
								handleLogin(tokens);
							} else {
								String msg = "format is login <user> <pass> \r\n";
								send(msg);
							}
						}
						else if("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd))
						{
							handleLogoff();
							break;
						}
					} else {								//commands that loggedin can do
						if ("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd))
						{
							handleLogoff();
							break;
						} else if ("login".equalsIgnoreCase(cmd)) {
							String msg = "already logged in";
							send(msg);
						} else if ("msg".equalsIgnoreCase(cmd)) // private messaging
						{
							if (tokens.length > 2) {

								String[] tokens2 = line.split(" ", 3);
								handleMessage(tokens2);
							} else {
								String msg = "format is msg <recipient/group> <message> \r\n";
								send(msg);
							}
						} else if("join".equalsIgnoreCase(cmd)) {
							if (tokens.length == 2 && tokens[1].charAt(0) == '#')
							{
								handleJoin(tokens[1]);
							}
							else
							{
								send("format is join <#groupname>\r\n");
							}
						} else if("leave".equalsIgnoreCase(cmd)) {
							if (tokens.length == 2 && tokens[1].charAt(0) == '#' && topicSet.contains(login))
							{
								handleLeave(tokens[1]);
							}
						} else {
							String msg = "unknown command: " + cmd + "\r\n";
							send(msg);
						}
					}
				} else {
					String msg = "format is login <user> <pass>\r\n";
					send(msg); // include a help command instead
				}
			} else {
				break;
			}
		}
//		for(int i =0; i <10; i++) // testing multithreading by counting to 10 
//		{
//			output.write(("Time now is " + new Date() + "\r\n").getBytes()); // \r\n is newline for windows 
//			Thread.sleep( 1000);
//		}
		client.close();
	}
	
	private void handleLeave(String string) {
		
		topicSet.remove(string);
		server.removefromTopic(string, login);
	}

	public boolean isMemberOfTopic(String topic) {
		return topicSet.contains(topic);
	}

	private void handleJoin(String topic) throws IOException {
		topicSet.add(topic);
		server.addTopic(topic, login);
		send("joined!\r\n");
	}
	
	//msg login body OR msg #topic body
	private void handleMessage(String[] tokens) throws IOException {
		String sendTo = tokens[1];
		String body = tokens[2];
		
		Map<String, ServerWorker> workers = server.getWorkerList();
		boolean isTopic = sendTo.charAt(0) == '#';
		if(isTopic)
		{
			Map<String, HashSet<String>> topics = server.getTopics();
			for(String member: topics.get(sendTo))
			{
				if(member != login)
				{
					//this message is sent to client. must parse
					workers.get(member).send("msg " + sendTo + ":" + login + ": " + body + "\r\n"); 
				}
			}
		}
		else
		{
			//this one uses a : after the login. so client must take substring
			workers.get(sendTo).send("msg " + login + ": " + body + "\r\n");
		}
	}

	public String getLogin() {
		return login;
	}

	private void handleLogin(String[] tokens) throws IOException {
		String login = tokens[1];
		String password = tokens[2];
		System.out.println(login + password);

		// user/pass is hard coded for now
		if ((login.equals("tae") && password.equals("xyz")) || (login.equals("james") && password.equals("abc")) || (login.equals("guest")&& password.equals("guest") )) {

			String msg = "successfully logged in\r\n";
			send(msg);

			System.out.println("user has logged in: " + login);

			// get list of workers to broadcast whos online to all online members
			Map<String, ServerWorker> workers = server.getWorkerList();

			for (ServerWorker worker : workers.values()) {
				// send current user list of ppl online
				String msg2 = "online: " + worker.getLogin() + "\r\n";
				send(msg2);

				// send other online ppl the current user's status
				String onlineMsg = "online: " + login + "\r\n";
				worker.send(onlineMsg);

			}

			this.login = login;
			server.login(login, this);

		} else {
			String msg = "login failed \r\n";
			output.write(msg.getBytes()); // CANT use send here because login failed
			System.err.println("Login failed for " + login);
		}
	}

	private void handleLogoff() throws IOException {
		server.removeWorker(login, this);
		if(login != null)
		{
			Map<String, ServerWorker> workers = server.getWorkerList();
			for (ServerWorker worker : workers.values()) {
				String offlineMsg = "offline: " + login + "\r\n";
				worker.send(offlineMsg);
			}
		}
		System.out.println("user has logged out: " + login);

		client.close();
	}

	private void send(String msg) throws IOException {

		output.write(msg.getBytes());
	}

}
