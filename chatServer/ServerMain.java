package chatServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

//199.247.108.41 public ip address ----> other computers outside wifi must use this. 
// i opened the port 5331 to let outside traffic in  
// if ip address dynamic, have to open ports again once in a while
// telnet localhost 5331 to test
//if u cant see anything telnet
//ctrl + ] ,   set localecho, enter


// how to kill process (server)
// in cmd “netstat -a -o –n” for all processes on ports
// “taskkill /F /PID (PID number here)”       kill it with fire

public class ServerMain {
	public static void main(String[] args)
	{
		int port = 5331; //specifying port here. can change later
		Server server = new Server(port);
		server.start(); // thread start!
	}
	
	
}