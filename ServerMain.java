package learningSockets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

//199.247.108.41 public ip address ----> other computers outside wifi must use this. 
// i opened the port 5331 to let outside traffic in

public class ServerMain {
	public static void main(String[] args)
	{
		int port = 5331; //specifying port here. can change later
		Server server = new Server(port);
		server.start(); // thread start!
	}
	
	
}
