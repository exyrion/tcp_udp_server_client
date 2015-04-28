/*
Justin Liang
Perm #: 5217286
CS 176A
10/14/2012

All code are from the Gauchospace discussion, the textbook,
and http://docs.oracle.com/javase/1.5.0/docs/api/overview-summary.html

Follow the numbers 1. -> 2. -> 3. -> etc. for easier grading
*/

import java.io.*; 
import java.net.*;
 
class server_java_udp
{ 
	public static void main(String argv[]) throws Exception 
	{ 
		String ACK = "ACK", port;
		int keyLength = 0, intPort = 0;
		InetAddress IPAddress = null;
		
		//Checks if there is an argument
		if(argv.length != 1) 
		{
			System.exit(0);
		}
		//Assigns argument to global string port
		else
		{
			port = argv[0];
			intPort = Integer.parseInt(port);
		}
		
		//Create socket and byte arrays
		DatagramSocket serverSocket = new DatagramSocket(intPort); 
		byte[] receiveData = new byte[1024]; 
		byte[] sendData  = new byte[1024]; 
		byte[] receiveDataLength = new byte[1024]; 
		byte[] sendDataLength  = new byte[1024]; 
		byte[] sendACK = new byte[3];
		byte[] receiveACK = new byte[3];	
		
		while(true)
		{			
			//3. Receives first packet, the length packet
			DatagramPacket receivePacketLength = new DatagramPacket(receiveDataLength, receiveDataLength.length);
			serverSocket.receive(receivePacketLength);		
			String messageLength = new String(receivePacketLength.getData());
			messageLength = messageLength.replaceAll("\0","");
			try
			{
				serverSocket.setSoTimeout(2000);
			}
			catch(Exception e)
			{
				System.exit(0);
			}
			
			//5. Receives second packet, the message packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);		
			String message = new String(receivePacket.getData());
			message = message.replaceAll("\0","");			
			
			//6. Determines the length of the rolling session key, stores length in keyLength
			String keyLengthString = message.substring(23,message.length());
			keyLength = Integer.parseInt(keyLengthString);
			
			//Receive IP address from client
			IPAddress = receivePacket.getAddress();
			intPort = receivePacket.getPort();
			
			//7. Checks if packet was the correct length, sends ACK to client
			if(Integer.parseInt(messageLength) == message.length())
			{
				sendACK = ACK.getBytes();		
				DatagramPacket ACKPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, intPort);
				serverSocket.send(ACKPacket);
				break;
			}
			else
			{
				sendACK = "".getBytes();
				DatagramPacket ACKPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, intPort);
				serverSocket.send(ACKPacket);
			}
		}
		
		while(true) 
		{
			//10. Generates a random session key
			String sessionKey = generateKey(keyLength);
			String temp = "Session key:  " + sessionKey;
			String sessionKeyLength = Integer.toString(temp.length());
			sendData = temp.getBytes();
			sendDataLength = sessionKeyLength.getBytes();
			
			DatagramPacket keyLengthPacket = null;
			DatagramPacket keyPacket = null;
			DatagramPacket receiveACKPacket = null;
			String ACKMessage = "";
			
			for(int i = 0; i < 3; i++)
			{
				//11. Sends the length of the session key packet to the client
				keyLengthPacket = new DatagramPacket(sendDataLength, sendDataLength.length, IPAddress, intPort);
				serverSocket.send(keyLengthPacket);
				
				//12. Sends the session key packet to the client
				keyPacket = new DatagramPacket(sendData, sendData.length, IPAddress, intPort);
				serverSocket.send(keyPacket);
				
				try
				{
					//16. Receives ACK message from client
					receiveACKPacket = new DatagramPacket(receiveACK, receiveACK.length); 
					serverSocket.receive(receiveACKPacket);		
					ACKMessage = new String(receiveACKPacket.getData());
					ACKMessage = ACKMessage.replaceAll("\0","");
				}
				catch(Exception e)
				{
					System.exit(0);
				}
				
				//17. Checks to see if packet from client is ACK
				if(ACKMessage.equals("ACK"))
				{
					break;
				}
				//If there is more than 3 attempts, terminate the program
				if(i == 2)
				{
					System.out.println("Failed to send message.  Terminating.");		
					System.exit(0);
				}
				Thread.sleep(500);
			}
			
			while(true)
			{
				//19. Receives session key length packet from client
				keyLengthPacket = new DatagramPacket(receiveDataLength, receiveDataLength.length);
				serverSocket.receive(keyLengthPacket);
				String newSessionKeyLength = new String(keyLengthPacket.getData());
				newSessionKeyLength = newSessionKeyLength.replaceAll("\0","");
				
				try
				{
					serverSocket.setSoTimeout(2000);
				}
				catch(Exception e)
				{
					System.exit(0);
				}
				
				//21. Receives session key packet from client
				keyPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(keyPacket);		
				String newSessionKey = new String(keyPacket.getData());
				newSessionKey = newSessionKey.replaceAll("\0","");
				
				//22. Checks if session key from client is the same as the original session key sent
				String receivedKey = newSessionKey.substring(14,newSessionKey.length());			
				if(receivedKey.equals(sessionKey) == false)
				{
					System.out.println("Invalid session key.  Terminating.");
					System.exit(0);
				}
				
				//23. Checks if session key from client is the correct length, sends ACK to client
				if(Integer.parseInt(newSessionKeyLength) == newSessionKey.length())
				{
					sendACK = ACK.getBytes();		
					DatagramPacket ACKPacket2 = new DatagramPacket(sendACK, sendACK.length, IPAddress, intPort);
					serverSocket.send(ACKPacket2);
					break;
				}
			}			
			Thread.sleep(1000);
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


