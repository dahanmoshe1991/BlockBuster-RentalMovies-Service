package bgu.spl181.net.impl;

import java.util.concurrent.ConcurrentHashMap;

public class SharedData {

	private static ConcurrentHashMap<String, Boolean> loggedUsers = new ConcurrentHashMap<>();


	protected static boolean islogged(String user) {			//checks if connected to server
		
		if(loggedUsers.containsKey(user))
			return loggedUsers.get(user);
		return false;
	}

	protected static void register(String user) {				//adds to map new user
		
		if(!loggedUsers.containsKey(user))
			loggedUsers.put(user, false);
	}

	protected static void login(String user) {					//connect to server
		
		if(loggedUsers.containsKey(user)) {
			if (!islogged(user))
				loggedUsers.replace(user, true);
		}
	}
	
	protected static void signout(String user) {				//disconnect from server
		
		if(loggedUsers.containsKey(user)) {
			if (islogged(user))
				loggedUsers.replace(user, false);
		}
	}
}
