/*
Justin Liang
Perm #: 5217286
CS 176A
10/14/2012

Follow the numbers 1. -> 2. -> 3. -> etc. for easier grading
*/

import java.io.*;
import java.net.*;

class client_java_tcp
{
	public static void main(String argv[]) throws Exception
	{
		String ip = "", port = "", length = "";
		
		//Checks if there are 3 arguments
		if(argv.length != 3)
		{			
			System.exit(0);
		}
		
		//Assigns arguments to global strings ip, port, length
		try
		{		
			ip = argv[0];
			port = argv[1];
			length = argv[2];
			
			int intLength = Integer.parseInt(length);
			if(intLength < 1 || intLength > 4096) //Checks if key is between 1-4096
			{	
				System.exit(0);
			}			
		}
		catch(Exception e)
		{
			System.exit(0);
		}
		
		//1. Creates socket and streams		
		Socket clientSocket = null;
		InputStream inFromServer = null;
		OutputStream outToServer = null;
		int intPort = Integer.parseInt(port);		
		
		//2. Create output stream, sends message with key length to server		
		try
		{	
			clientSocket = new Socket(ip, intPort);
			inFromServer = clientSocket.getInputStream();
			outToServer = clientSocket.getOutputStream();
			String message = "Connect.  Key length:  " + length;
			byte[] a = new byte[10000];				
			a = message.getBytes();	
			outToServer.write(a);
		}
		catch(Exception e)
		{
			System.out.println("Could not connect to server.  Terminating.");
			System.exit(0);
		}
		
		while(true)		
		{					
			//6. Receives session key from server and prints it
			byte[] b = new byte[10000];
			String s = "";
			try
			{
				int l = inFromServer.read(b);
				s = new String(b, 0, l);
				System.out.println(s);
			}
			catch(Exception e)
			{
				System.out.println("Could not fetch result.  Terminating.");
				System.exit(0);
			}
			
			//Sleeps for 1 second
			Thread.sleep(1000);
			
			//7. Sends session key back to server
			b = s.getBytes();
			outToServer.write(b);
		}
	}
}


