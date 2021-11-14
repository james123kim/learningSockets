package chatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
	
	private String serverName;
	private int serverPort;
	private Socket socket;
	private InputStream serverIn;
	private OutputStream serverOut;
	private BufferedReader bufferedIn;
	
	private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
	private ArrayList<MessageListener> messageListeners = new ArrayList<>();

	public ChatClient(String serverName, int serverPort)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
	}
	
	/*public static void main(String[] args) throws IOException
	{
		ChatClient client = new ChatClient("localhost", 5331);
		
		//listens for EVERYONE, change when implementing friend system
		client.addUserStatusListener(new UserStatusListener() {

			@Override
			public void online(String login) {
				System.out.println("ONLINE: " + login);
			}

			@Override
			public void offline(String login) {
				System.out.println("OFFLINE: " + login);
			}
			
			
		});
		
		client.addMessageListener(new MessageListener() {

			@Override
			public void onMessage(String fromLogin, String msgBody) {
				System.out.println("You got a message from " + fromLogin + " msgBody!");
			}
			
		});
		if (!client.connect())
		{
			System.err.println("Connect failed.");
		}
		else
		{
			System.out.println("Connect successful");
			if(client.login("james", "abc"))           //login password here
			{
				System.out.println("Login successful");
				
				//client.msg("tae", "hi"); causes nullpointer, login isnt initialized yet apparently
			}
			else
			{
				System.err.println("Login failed");
			}
			
			//client.logoff();                        // testing logout function
			//System.out.println("Logout successful");
		}
	}*/

	public void msg(String sendTo, String msg) throws IOException {
		String cmd = "msg " + sendTo + " " + msg + "\r\n";
		serverOut.write(cmd.getBytes());
		
	}

	public boolean login(String login, String password) throws IOException{
		String cmd = "login " + login + " " + password + "\r\n";
		serverOut.write(cmd.getBytes());
		
		String response = bufferedIn.readLine();
		System.out.println("Response line: " + response);
		if("successfully logged in".equalsIgnoreCase(response))
		{
			startMessageReader();  // after logging in, start reading events from the server
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void logoff() throws IOException {
		String cmd = "logoff\r\n";
		serverOut.write(cmd.getBytes());
	}

	private void startMessageReader() { //reads when ppl login/logout
		Thread t = new Thread() {
			public void run()
			{
				readMessageLoop();
			}
		};
		t.start();
		
	}

	private void readMessageLoop() {
		try {
			String line;
			while(true)
			{
				//server-> client, guaranteed format in serverworker
				line = bufferedIn.readLine();
				if(line != null)
				{
					while (line.contains("\b"))        //handle backspace characters
					    line = line.replaceAll("^\b+|[^\b]\b", "");
					
					String[] tokens = line.split(" ");
					if(tokens != null && tokens.length > 0) 
					{
						String cmd = tokens[0];
						if("online:".equalsIgnoreCase(cmd))
						{
							handleOnline(tokens);
						} 
						else if("offline:".equalsIgnoreCase(cmd))
						{
							handleOffline(tokens);
						}
						else if("msg".equalsIgnoreCase(cmd))
						{
							String[] tokens2 = line.split(" ", 3);
							handleMessage(tokens2);
						}
					}
				}
				else
				{
					break;
				}
				
			}
		}
		catch (Exception e){
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
	}

	private void handleMessage(String[] tokens) {
		
		String from = tokens[1];
		String msgBody = tokens[2];
		
		
		for(MessageListener listener: messageListeners)
		{
			listener.onMessage(from, msgBody);
		}
	}

	private void handleOnline(String[] tokens) {
		String login = tokens[1];
		for(UserStatusListener listener: userStatusListeners)
		{
			listener.online(login);
		}
		
	}
	
	private void handleOffline(String[] tokens) {
		String login = tokens[1];
		for(UserStatusListener listener: userStatusListeners)
		{
			listener.offline(login);
		}
		
	}

	public boolean connect() {
		try {
			this.socket = new Socket(serverName, serverPort);
			System.out.println("Client port is " + socket.getLocalPort());
			this.serverOut = socket.getOutputStream();
			this.serverIn = socket.getInputStream();
			this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
			return true;
		} 
		catch(IOException e)
		{
			e.printStackTrace();
			
		}
		return false;
	}
	
	public void addUserStatusListener(UserStatusListener listener)
	{
		userStatusListeners.add(listener);
	}
	public void removeUserStatusListener(UserStatusListener listener)
	{
		userStatusListeners.remove(listener);
	}
	public void addMessageListener(MessageListener listener)
	{
		messageListeners.add(listener);
		}
	public void removeMessageListener(MessageListener listener)
	{
		messageListeners.remove(listener);
	}
}
