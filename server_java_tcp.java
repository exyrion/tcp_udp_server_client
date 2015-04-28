/*
Justin Liang
Perm #: 5217286
CS 176A
10/14/2012

Follow the numbers 1. -> 2. -> 3. -> etc. for easier grading
*/

import java.io.*;
import java.net.*;

class server_java_tcp
{
	public static void main(String argv[]) throws Exception
	{
		String port;
		int intPort = 0;
		
		//Checks if there is an argument
		if(argv.length != 1) 
		{
			System.exit(0);
		}		
		else
		{
			port = argv[0];
			intPort = Integer.parseInt(port);
		}		
		
		//1. Create socket and streams
		ServerSocket welcomeSocket = null;
		try
		{
			welcomeSocket = new ServerSocket(intPort);
		}
		catch(BindException e)
		{
			System.exit(0);
		}
		Socket connectionSocket = welcomeSocket.accept();
		InputStream inFromClient = connectionSocket.getInputStream();
		OutputStream outToClient = connectionSocket.getOutputStream();
		
		//3. Reads key length message from client
		byte[] keyLengthFromMessage = new byte[1000];
		int l = inFromClient.read(keyLengthFromMessage);
		String packetData = new String(keyLengthFromMessage, 0, l);
		
		//4. Takes the number at the end of the message and converts to type int
		String keyLengthString = packetData.substring(23,packetData.length());
		int keyLength = Integer.parseInt(keyLengthString);
		
		//5. Generates the first session key and sends it to client
		String sessionKey = generateKey(keyLength);
		byte[] a = new byte[10000];
		String message = "Session key:  " + sessionKey;
    	a = message.getBytes();
     	outToClient.write(a);
						
		while(true)
		{	
			//8. Receives client's key
			byte[] b = new byte[1000];
			int l2 = inFromClient.read(b);
			String receivedMessage = "", receivedKey = "";
			try
			{
				receivedMessage = new String(b, 0, l2);
				receivedKey = receivedMessage.substring(14,receivedMessage.length());
			}
			catch(StringIndexOutOfBoundsException e)
			{
				System.exit(0);
			}
			
			//9. Checks to see if client's key is equal to original session key
			//If it is, send key back to client
			if(sessionKey.equals(receivedKey))
			{				
				sessionKey = generateKey(keyLength);
				byte[] c = new byte[10000];
				message = "Session key:  " + sessionKey;
    				c = message.getBytes();
     			outToClient.write(c);
			}
			else
			{
				System.out.println("Invalid session key.  Terminating.");
				System.exit(0);
			}
		}
	}
	
	//Helper method that generates a random session key
	public static String generateKey(int length)
	{
		String random = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        	String sessionKey = "";
        	for (int i = 0; i < length; i++) 
		{
            	sessionKey += random.charAt((int) (Math.random() * random.length()));
        	}
        	return sessionKey;
	}
}
