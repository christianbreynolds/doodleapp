//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu

import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//handles the interactions of individual users with the server
//works with GroupThread and DoodleServer
public class UserThread extends Thread {
	private Socket s;
	private PrintWriter pw;
	private GroupThread myGroup;
	
	public UserThread(Socket sock){
		s = sock;
		pw = null;
		myGroup = null;
	}
	
	@Override
	public void run(){
		super.run();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream(),true);
			String input;
			
			while((input = br.readLine()) != null){
				processInput(input);
			}
			if(myGroup != null){         //if the connection is broken, the mess is cleaned up
				myGroup.removeUser(this);
			}
			br.close();
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//an easy helper that's also used outside the class
	public void write(String msg){
		pw.println(msg);
	}
	
	//handles the protocol magic
	public void processInput(String input){
		//a list of points was sent to server
		if(input.startsWith("@Point")){
			//stores the points all in a list for the GroupThread
			ArrayList<DrawnPoint> list = new ArrayList<DrawnPoint>();
			input = input.substring(6);
			String[] points = input.split("&");
			
			for(String s:points){
				list.add(new DrawnPoint(s));
			}
			myGroup.addToList(list);
			
			//and sends to the rest of the threads
			myGroup.broadcast("@Point" + input, getId());
		}
		
		//the user is a newcomer to a group
		else if(input.startsWith("@Name")){
			myGroup.broadcast(input, getId()); //announces arrival
			write(myGroup.getPoints()); //gets all previously drawn points
		}
		
		//user requests a list of groups
		else if(input.equals("@Groups")){
			write(DoodleServer.getGroups());
		}
		
		//user attempts to join group
		else if(input.startsWith("@JoinGroup")){
			input = input.substring(10);
			myGroup = DoodleServer.joinGroup(input);
			if(myGroup == null){
				write("@ErrGroup " + input + " doesn't exist");
			}
			else{
				myGroup.addUser(this);
				write("@Success");
			}
		}
		
		//user attempts to create group
		else if(input.startsWith("@CreaGroup")){
			input = input.substring(10);
			GroupThread gt = DoodleServer.createGroup(input);
			if(gt == null){
				write("@ErrGroup " + input + " already exists");
			}
			else{
				write("@Created");
			}
		}
		
		else{
			myGroup.broadcast("Unrecognized Input", getId());
		}
	}
		
}
