//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

//each GroupThread can be thought of as an individual canvas
//each keeps track of all points drawn and all threads in the group
public class GroupThread extends Thread {
	private String groupName;
	private HashSet<UserThread> users;
	private ArrayList<DrawnPoint> pointList;
	
	public GroupThread(String name){
		groupName = name;
		users = new HashSet<UserThread>();
		pointList = new ArrayList<DrawnPoint>();
	}
	
	public String getGroupName(){return groupName;}
	
	public void addUser(UserThread user){
		synchronized(users){
			users.add(user);
		}
	}
	
	//sends "msg" to all users in group except the one with id "threadId"
	public void broadcast(String msg, long threadId){
		synchronized(users){
			Iterator<UserThread> i = users.iterator(); 
			while(i.hasNext()){
				UserThread u = i.next();
				if(u.getId() != threadId){
					u.write(msg);
				} //if
			} //while
		} //synchronized
	}
	
	//adds the points to the communal list of points drawn
	public void addToList(ArrayList<DrawnPoint> list){
		synchronized(pointList){
			for(DrawnPoint p: list){
				pointList.add(p);
			} //for
		}//synchronized
	}
	
	//returns all points that have been drawn so far, basically the full picture
	public String getPoints(){
		String ret = "@Point";
		synchronized(pointList){
			if(!pointList.isEmpty()){
				ret += pointList.get(0).toString(); //get the first one so we can split the list
				for(int i = 1; i<pointList.size(); i++){
					ret += "&" + pointList.get(i).toString();
				} //for
			} //if
		} //synchronized
		return ret;
	}
	
	//helps clean up some messes
	public void removeUser(UserThread user){
		synchronized(users){
			users.remove(user);
			if(users.size() == 0){ //if there's no more users, the group disappears
				DoodleServer.removeGroup(groupName);
			}
		}
	}
}
