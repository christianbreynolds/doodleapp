package testclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread{
	private String[] testCases;
	public ClientThread(String[] tests){
		testCases = tests;
	}
	@Override
	public void run(){
		try {
			sleep(500);
		} catch (InterruptedException e1) {
		}
		try{
			Socket s = new Socket(DoodleClient.SERVER_SITE, DoodleClient.PORT_NUMBER);
			//BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter pw = new PrintWriter(s.getOutputStream(),true); 
			for(int i = 0; i<testCases.length; i++){
				pw.println(testCases[i]);
			}
			s.close();
		}catch(IOException e){
			System.out.println("In the thread");
			e.printStackTrace();
		}
		System.out.println("Finished in the thread");
	}
}
