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

class client_java_udp
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
		
		//Create socket and streams
		DatagramSocket clientSocket = new DatagramSocket(); 
		InetAddress IPAddress = null; 
		int intPort = Integer.parseInt(port);
		
		byte[] receiveData = new byte[1024]; 
		byte[] sendData  = new byte[1024]; 
		byte[] receiveDataLength = new byte[1024]; 
		byte[] sendDataLength  = new byte[1024]; 
		byte[] sendACK = new byte[3];
		byte[] receiveACK = new byte[3];
		
		for(int i = 0; i < 3; i++)
		{
			//1. Construct message and message length
			String message = "Connect.  Key length:  " + length;
			String messageLength = Integer.toString(message.length());
			sendDataLength = messageLength.getBytes();
			sendData = message.getBytes();
			String ACKMessage = null;
			try
			{
				IPAddress = InetAddress.getByName(ip);
				//2. Send the length packet to server
				DatagramPacket lengthPacket = new DatagramPacket(sendDataLength, sendDataLength.length, IPAddress, intPort);
				clientSocket.send(lengthPacket);
				
				//4. Send the message packet to server
				DatagramPacket messagePacket = new DatagramPacket(sendData, sendData.length, IPAddress, intPort); 
				clientSocket.send(messagePacket);
			}
			
			catch(Exception e)
			{
				System.out.println("Could not connect to server.  Terminating.");
				System.exit(0);
			}	
			
			//8. Receives ACK message from server
			DatagramPacket ACKPacket = new DatagramPacket(receiveACK, receiveACK.length); 
			clientSocket.receive(ACKPacket);		
			ACKMessage = new String(ACKPacket.getData());
			ACKMessage = ACKMessage.replaceAll("\0","");
			
			
			//9. Checks to see if packet from server is ACK
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
			//Wait for 500ms to see if client receives an ACK
			Thread.sleep(500);
		}
		
		while(true)
		{
			String sessionKeyLength = "", sessionKey = "";
			DatagramPacket keyLengthPacket = null, keyPacket = null;
			while(true)
			{			
				try
				{			
					//13. Receives first packet, the key's length packet
					keyLengthPacket = new DatagramPacket(receiveDataLength, receiveDataLength.length);
					clientSocket.receive(keyLengthPacket);
					sessionKeyLength = new String(keyLengthPacket.getData());
					sessionKeyLength = sessionKeyLength.replaceAll("\0","");
					clientSocket.setSoTimeout(2000);
				}
				catch(Exception e)
				{
					System.exit(0);
				}
				
				//14. Receives second packet, the actual key packet from server
				keyPacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(keyPacket);		
				sessionKey = new String(keyPacket.getData());
				sessionKey = sessionKey.replaceAll("\0","");
				System.out.println(sessionKey);
				
				//15. Checks if packet was the correct length, send ACK
				if(Integer.parseInt(sessionKeyLength) == sessionKey.length())
				{
					String ACK = "ACK";
					sendACK = ACK.getBytes();		
					DatagramPacket sendACKPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, intPort);
					clientSocket.send(sendACKPacket);
					break;
				}
				else
				{
					sendACK = "".getBytes();
					DatagramPacket ACKPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, intPort);
					clientSocket.send(ACKPacket);
				}
			}
			
			for(int i = 0; i < 3; i++)
			{
				//18. Sends session key length packet back to the server
				sendDataLength = new byte[1024];
				sendDataLength = sessionKeyLength.getBytes();
				keyLengthPacket = new DatagramPacket(sendDataLength, sendDataLength.length, IPAddress, intPort);
				clientSocket.send(keyLengthPacket);
				
				//20. Sends session key packet back to server
				sendData = new byte[1024];
				sendData = sessionKey.getBytes();
				keyPacket = new DatagramPacket(sendData, sendData.length, IPAddress, intPort);
				clientSocket.send(keyPacket);
				
				//24. Receives ACK message from server
				receiveACK = new byte[3];
				DatagramPacket receivePacket = new DatagramPacket(receiveACK, receiveACK.length);
				clientSocket.receive(receivePacket);
				String ACKMessage2 = new String(receivePacket.getData());
				ACKMessage2 = ACKMessage2.replaceAll("\0","");
				
				//25. Checks if message from server is ACK
				if(ACKMessage2.equals("ACK"))
				{
					break;
				}
				if(i == 2)
				{
					System.out.println("Failed to send message.  Terminating.");		
					System.exit(0);
				}
			}			
			Thread.sleep(1000);
		}
	} 
}
