package bgu.spl181.net.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import bgu.spl181.net.impl.SharedData;
public class UsersHolder {

	@SerializedName("users")
	private List<User> users;
	private String userPath;
	private Gson gson;
	private UsersHolder newHolder;
	private ReadWriteLock userLock;
	private Object hold ;

	
	public void setHold(Object hold) {						//setter hold
		
		this.hold = hold;
		getNewFromGson();
		newHolder.setNewHolderHold(new Object());
	}
	
	public void setNewHolderHold(Object hold) {				//setter new holder hold
		this.hold = hold;
	}
	
	public List<User> getUsers() {							//getter users
		return users;
	}

	public void setUsers(List<User> users) {				//setter users
		this.users = users;
	}
	
	public void logUsers() {								//adds new user to server
		
		User[] usersArray = new User[users.size()];
		users.toArray(usersArray);
		for(int i = 0; i< usersArray.length; i++ ) {
			SharedData.register(usersArray[i].getUsername());
		}
	}
	
	public synchronized boolean hasUsername(String user) {		//checks if user is exist

		boolean ans = false;
		User[] usersArray = new User[users.size()];
		users.toArray(usersArray);
		for(int i = 0; (i< usersArray.length) && (ans == false); i++) {
			if (usersArray[i].getUsername().equals(user))
				ans = true;
		}
		return ans;	
	}

	public List<User> getUpdatedList(ReadWriteLock userLock){				//gets new updated user.json

		List<User> users = null;
		userLock.readLock().lock();
		try(BufferedReader br = new BufferedReader(new FileReader(this.userPath))) {
			users =  gson.fromJson(br, UsersHolder.class).getUsers();
		} 

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		userLock.readLock().unlock();
		return users;
	}

	public void updateUsers(ReadWriteLock userLock){							//updates user json

		userLock.writeLock().lock();
		newHolder.hold = null;
		try (BufferedWriter br = new BufferedWriter(new FileWriter(this.userPath))){
			gson.toJson(newHolder , br);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		userLock.writeLock().unlock();
	}

	public String getUserPath() {						//getter user path
		return userPath;
	}

	public void setUserPath(String userPath) {			//setter user path
		this.userPath = userPath;
	}


	public void getNewFromGson() { 						//reading from Users.Json to a newHolder

		userLock.readLock().lock();
		try(BufferedReader br = new BufferedReader(new FileReader(this.userPath))) {
			newHolder =  gson.fromJson(br, UsersHolder.class);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		userLock.readLock().unlock();
	}

	public void setUserLock(ReadWriteLock userLock) {	//setter user lock
		this.userLock = userLock;
	}

	public Gson getGson() {								//getter gson
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public ConcurrentHashMap<String, String> getUserMap() {		//getter user map

		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
		User[] userArray = new User[users.size()];
		users.toArray(userArray);
		for(int i = 0; i < userArray.length; i++)
			map.put(userArray[i].getUsername(), userArray[i].getPassword());
		return map;
	}

	public String getBalance(String username) {					//gets balance by name

		User[] userArray = new User[users.size()];
		users.toArray(userArray);
		for(int i = 0; i < userArray.length; i++) {
			if(userArray[i].getUsername().equals(username))
				return userArray[i].getBalance();
		}

		return null;
	}

	public String addBalance(String username, String toAdd) {	//adds to user balance

		String newBalance = "";
		int balanceAdd = Integer.parseInt(toAdd);
		User user = searchAndGetUser(username);
		if(user != null) {
			newBalance = user.addBalance(balanceAdd);
			this.users = newHolder.getUsers();
			updateUsers(userLock);								//updates user json
		}
		return newBalance;
	}

	protected void addUsers(User toAdd) {						//adds new user
		
		synchronized (hold) {									
			getNewFromGson(); 									//getting updated data to a new Holder
			newHolder.users.add(toAdd);							//adds user
			this.users = newHolder.getUsers();
			updateUsers(userLock);								//updates user json
		}
	}

	public boolean hasMoney(String username, int price) {		//checks if user has enough money

		boolean ans = false;
		User user = searchAndGetUser(username);
		if(user != null && Integer.parseInt(user.getBalance()) > price) 
			ans = true;
		return ans;
	}

	public String getCountry(String username) {					//changes from list to string

		String ans = "";
		User user = searchAndGetUser(username);
		if(user != null)
			ans = user.getCountry();
		return ans;
	}

	public boolean isAllreadyRented(String username, String moviename) {	

		boolean ans = false;
		User user = searchAndGetUser(username);
		if(user != null && user.hasMovies(moviename))
			ans = true;
		return ans;
	}

	public User searchAndGetUser(String username) {						//finds the user

		User user = null;
		boolean found = false;
		synchronized (hold) {
			getNewFromGson(); 											//getting updated data to a new Holder
			newHolder.setNewHolderHold(new Object());
			User[] userArray = new User[newHolder.users.size()];
			newHolder.users.toArray(userArray);
			for(int i = 0; i < userArray.length && found == false; i++) {
				if(userArray[i].getUsername().equals(username)) { 
					found = true;
					user = userArray[i];
				}
			}
		}
		return user;
	}
	
	public void rent(String username, Movie movie) {					

		User user = searchAndGetUser(username);
		synchronized (hold) {
			if(user != null) {
				user.addBalance(-Integer.parseInt(movie.getPrice()));		//reduces balance by price
				user.getMovies().add(movie);								//adds movie to the list
				this.users = newHolder.getUsers();
				updateUsers(userLock);										//updates user json
			}
		}
	}

	public void returnMovie(String username, Movie movie) {
	
		User user = searchAndGetUser(username);
		synchronized (hold) {
			if(user != null) {
				user.getMovies().remove(movie);								//removes movie from list
				this.users = newHolder.getUsers();
				updateUsers(userLock);										//updates user json
			}
		}
	}
}