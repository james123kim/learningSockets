package learningSockets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

// to test connection (using telnet), start this program, run cmd as admin and type, activate telnet:


//"telnet " + address(ipv6 or 4 or localhost) + port (or whatever port you chose) 
// 
// 192.168.0.232 my private ipv4 address
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
