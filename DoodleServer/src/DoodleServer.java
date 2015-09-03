//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.HashMap;
 

public class DoodleServer {
	private static ServerSocket ss;
	private static HashMap<String, GroupThread> groups;
	public static int PORT_NUM = 7777;
	
	
	public static void main(String[] args) {
		groups = new HashMap<String, GroupThread>();
		try{
			ss = new ServerSocket(PORT_NUM);
			while(true){
				System.out.println("running...");
				Socket s = ss.accept();
				System.out.println("accepted");
				UserThread thread = new UserThread(s); //we don't save the initial ones, we wait until they join groups
				thread.start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//finds the GroupThread of "name"
	//returns the the GroupThread if it exists, null if not
	public static GroupThread joinGroup(String name){
		synchronized(groups){
			GroupThread gt = groups.get(name);
			return gt;
		}
	}
	
	//attempts to create a GroupThread for "name"
	//returns the new GroupThread if one doesn't already exist
	//returns null if one does already exist
	public static GroupThread createGroup(String name){
		synchronized(groups){
			if(groups.get(name) != null) return null;
			GroupThread gt = new GroupThread(name);
			groups.put(name, gt); //stores it in the hashmap
			return gt;
		}
	}
	
	//helps clean up the mess when users start leaving
	public static void removeGroup(String name){
		synchronized(groups){
			groups.remove(name);
		}
	}
	
	//returns a list of the groups it has in proper protocol syntax
	public static String getGroups(){
		String ret = "";
		synchronized(groups){
			for(GroupThread gt : groups.values()){
				ret += "&" + gt.getGroupName();
			}
		}
		if(ret.length()>0){
			ret = ret.substring(1);
		}
		return "@Groups" + ret;
	}
	
}
