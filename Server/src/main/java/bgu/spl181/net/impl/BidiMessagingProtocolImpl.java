package bgu.spl181.net.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.impl.SharedData;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String> {

	boolean isLogged;
	private int connectionId;
	private String username;
	private String userType;
	private Connections<String> connections;
	private UsersHolder usersHold;
	private MoviesHolder moviesHold;
	private ReadWriteLock userLock;
	private boolean shouldTerminate = false;
	private String response;
	private ConcurrentHashMap<String, String> combinations;
	private String broadcast;


	public BidiMessagingProtocolImpl(UsersHolder usersHold, MoviesHolder moviesHold, 
			ReadWriteLock userLock) {      //constructor

		this.userLock = userLock;
		this.usersHold = usersHold;
		this.moviesHold = moviesHold;
		this.combinations = usersHold.getUserMap();
	}

	@Override
	public void start(int connectionId, Connections<String> connections) {  //initialization

		this.connectionId = connectionId;
		this.connections = connections;
		this.isLogged = false;
		this.username = null;
		this.userType = null;
	}

	@Override
	public void process(String message) {

		String[] arg = split((String)(message));					//splits the string
		response = null;
		broadcast = null;
		
		switch(arg[0]) {
		case("REGISTER"):											//Register
			register(arg[1],arg[2],arg[3]);
			break;

		case("LOGIN"):												//Login
			login(arg[1],arg[2]);
			break;

		case("SIGNOUT"):											//Signout
			signout();
			break;	

		case("REQUEST"):											//Request
			request(arg[1]);
			break;
		}

		if (response != null)										//sends ack or error							
			connections.send(connectionId, response);

		if (broadcast != null)										
			connections.broadcast("BROADCAST "+ broadcast);			//sends broadcast
	}

	private void register(String username, String password, String datablock) {

		boolean valid = ((isUserNameValid(username)) && (password != null) && (isDatablockValid(datablock)));
		
		if(valid) {
			this.username = username;
			this.userType = "normal";
			datablock = datablock.substring(9, datablock.length() -1);			//sets country
			User newUser = new User(username,password,datablock);				//creates new user
			usersHold.addUsers(newUser);										//adds the user to holder
			combinations.put(username, password);								//adds user-pass combination
			SharedData.register(username);										//adds to shared data
			response = "ACK registration succeeded";
		}
		else 
			response = "ERROR registration failed";
	}

	private void login(String username, String password) {

		boolean valid = ((username != null) && (!isLogged) && (password != null) && isCombined(username, password) && !SharedData.islogged(username));
		
		if(valid) {
			User user = usersHold.searchAndGetUser(username);					//searches the user and sets his fields
			this.userType = user.getType();
			this.isLogged = true;
			this.username = username;
			SharedData.login(username);
			response = "ACK login succeeded";
		}
		else 
			response = "ERROR login failed";
	}
	
	private void signout() {

		boolean valid = isLogged;
		
		if(valid) {											
			isLogged = false;
			response = "ACK signout succeeded";
			connections.send(connectionId, response);
			((ConnectionsImpl<String>)connections).disconnect(connectionId);	//disconnect from server
			SharedData.signout(this.username);									//removes the user from shared data
			response = null;
			shouldTerminate = true;
		}
		else
			response = "ERROR signout failed";
	}

	private void request(String parameters) {

		String[] argRequest = splitRequest(parameters);							//splits the parameters
		
		switch(argRequest[0]) {
		case("balance"):														//Balance
			if(argRequest[1].equals("info")) {									//balance info
				if(isLogged) 
					response = "ACK balance " +	usersHold.getBalance(username);
				else
					response = "ERROR request" + "balance info" +"failed";
			}
			else if(argRequest[1].equals("add")) {								//balance add
				if(isLogged) {
					if(argRequest[2] != null) {									//checks if the addition is valid
						String newBalance = usersHold.addBalance(username, argRequest[2]);	//adds to balance
						response = "ACK balance " + newBalance +" added " + argRequest[2];
					}
				}
				else
					response = "ERROR request" + "balance add" +"failed";
			}
			else 
				response = "ERROR request" + "balance "+ argRequest[1] +"failed";
			break;

		case("info"):														//Info
			if(isLogged) {
				if(argRequest[1] != null) { 								//checks if movie name was given 
					boolean found = false;
					String moviename = "";
					for(int i = 1; i < argRequest.length && found == false; i++) {	//creates movie name
						if(argRequest[i] == null) 
							found = true;
						else 
							moviename = moviename + argRequest[i] + " ";
					}
					response = "ACK info "+ moviesHold.getInfo(moviename.substring(1, moviename.length()-2));
					if(response == "")
						response = "ERROR request " + "info " +"failed"; 
				}
				else  														//print info of all movies		
					response ="ACK info "+ moviesHold.getAllMoviesInfo();
			}
			else 															//user is not logged in
				response = "ERROR request " + "info " +"failed";
			break;

		case("rent"):														//Rent
			if(isLogged) {
				if(argRequest[1] != null) { 								//checks if movie name was given 
					boolean found = false;
					String moviename = "";
					for(int i = 1; i < argRequest.length && found == false; i++) {	//creates movie name
						if(argRequest[i] == null) 
							found = true;
						else  
							moviename = moviename + argRequest[i] + " ";
					}
					moviename = moviename.substring(1,moviename.length()-2);
					
					if(moviesHold.movieExist(moviename) &&					//checks all the conditions
							moviesHold.hasCopies(moviename) &&
							usersHold.hasMoney(username, moviesHold.getPrice(moviename)) &&
							!moviesHold.isBanned(usersHold.getCountry(username) ,moviename) &&
							!usersHold.isAllreadyRented(username, moviename)) {
						
						rent(username, moviesHold.searchAndGetMovie(moviename));
						response = "ACK rent "+ moviename +" success";
						broadcast = "movie "+ moviename+ " "+ moviesHold.getCopies(moviename)+ " "+ moviesHold.getPrice(moviename);	
					}
					else													
						response = "ERROR request " + "rent " +"failed";
				}
				else														//there is no movie name							
					response = "ERROR request " + "rent " +"failed";
			}
			else															//user is not logged
				response = "ERROR request " + "rent " +"failed";
			break;


		case("return"):														//Return
			if(isLogged) {
				if(argRequest[1] != null) { 								//checks if movie name was given
					boolean found = false;
					String moviename = "";
					for(int i = 1; i < argRequest.length && found == false; i++) {	//creates movie name
						if(argRequest[i] == null) 
							found = true;
						else  
							moviename = moviename + argRequest[i] + " ";
					}
					moviename = moviename.substring(1,moviename.length()-2);
					
					if(moviesHold.movieExist(moviename) &&							//checks all the conditions			
							usersHold.isAllreadyRented(username, moviename)) {
						
						returnMovie(username, moviesHold.searchAndGetMovie(moviename));
						response = "ACK return "+ moviename +" success";
						broadcast = "movie "+ moviename+ " "+ moviesHold.getCopies(moviename)+ " "+ moviesHold.getPrice(moviename);	
					}
					else
						response = "ERROR request " + "return " +"failed";
				}
				else																//there is no movie name
					response = "ERROR request " + "reutrn " +"failed";
			}
			else																	//user is not logged
				response = "ERROR request " + "return " +"failed";
		break;


		case("addmovie"):															//Add Movie
			if(isLogged && (userType.equals("admin"))) {			
				if(argRequest[1] != null) { 										//checks if movie name was given
					boolean found = false;
					String moviename = "";
					int i = 0;
					for(i = 1; i < argRequest.length && found == false; i++) {		//creates movie name
						if(argRequest[i].charAt(argRequest[i].length()-1)==('"')) {
							found = true;
						}
						moviename = moviename + argRequest[i] + " ";
					}

					if (i == argRequest.length)										//there are no countries price amount 
						response = "ERROR request " + "addmovie " +"failed";

					else {
						moviename = moviename.substring(1,moviename.length()-2);	//creates a new movie 
						String amount = argRequest[i];
						String price = argRequest[i+1];
						List<String> banned = new ArrayList<String>();
						if(argRequest[i+2] != null) { 								//checks if country name was given
							String countryName = "";
							found = false;
							for(int j = i+2; j < argRequest.length && found == false; j++) {	//creates the list of countries
								if(argRequest[j] == null) 
									found = true;
								else {  
									countryName = countryName + argRequest[j]+" ";
									if(argRequest[j].charAt(argRequest[j].length()-1)==('"')) {
										banned.add(countryName.substring(1, countryName.length()-2));
										countryName = "";
									}
								}
							}

							boolean ack = moviesHold.addMovie(moviename,amount,price,banned);
							if(ack) {
								response = "ACK addmovie "+ moviename +" success";
								broadcast = "movie "+ moviename+ " "+ moviesHold.getCopies(moviename)+ " "+ moviesHold.getPrice(moviename);	
							}
							else
								response = "ERROR request " + "addmovie " +"failed";
						}													
						else
							response = "ERROR request " + "addmovie " +"failed";
					}
				}								//there is no movie name			
				else
					response = "ERROR request " + "addmovie " +"failed";
			}									
			else								//not logged or not admin
				response = "ERROR request " + "addmovie " +"failed";
			break;

			
		case("remmovie"):														//Remove Movie
			if(isLogged && (userType.equals("admin"))) {			
				if(argRequest[1] != null) { 									//checks if movie name was given
					boolean found = false;
					String moviename = "";
					for(int i = 1; i < argRequest.length && found == false; i++) {	//creates movie name
						if(argRequest[i] ==(null)) 
							found = true;
						else  
							moviename = moviename + argRequest[i] + " ";
					}
					moviename = moviename.substring(1,moviename.length()-2);

					boolean ack = moviesHold.removeMovie(moviename);
					if(ack) {
						response = "ACK remmovie "+ moviename +" success";
						broadcast = "movie "+ moviename+ " removed";	
					}
					else
						response = "ERROR request " + "remmovie " +"failed";
				}																
				else						//there is no movie name
					response = "ERROR request " + "remmovie " +"failed";
			}
			else							//not logged or not admin
				response = "ERROR request " + "remmovie " +"failed";
			break;


		case("changeprice"):												//Changes Price
			if(isLogged && (userType.equals("admin"))) {			
				if(argRequest[1] != null) { 								//checks if movie name was given
					boolean found = false;
					String moviename = "";
					int i = 0;
					for(i = 1; i < argRequest.length && found == false; i++) {
						if(argRequest[i].charAt(argRequest[i].length()-1)==('"')) 
							found = true;
						moviename = moviename + argRequest[i] + " ";
					}

					if (i == argRequest.length)								//there is no price
						response = "ERROR request " + "addmovie " +"failed";
					else {
						moviename = moviename.substring(1,moviename.length()-2);
						String newPrice = argRequest[i];
						boolean ack = moviesHold.changePrice(moviename, newPrice);
						if(ack) {
							response = "ACK changeprice "+ moviename +" success";
							broadcast = "movie "+ moviename+ " "+ moviesHold.getCopies(moviename)+ " "+ moviesHold.getPrice(moviename);	
						}
						else
							response = "ERROR request " + "changeprice " +"failed";
					}
				}
				else									//there is no movie name
					response = "ERROR request " + "changeprice " +"failed";
			}
			else										//not logged or not admin
				response = "ERROR request " + "changeprice " +"failed";
			break;
		}
	}

	@Override
	public boolean shouldTerminate() {						//getter shouldTerminate
		return this.shouldTerminate;
	}

	public String[] split(String arg) {						//split

		String[] ans = new String[4];
		if(arg.substring(0,7).equals("REQUEST")) {
			ans[0] = "REQUEST";
			ans[1] = arg.substring(8);
		}
		else {
			int i = 0;
			for(int j = 0; j < 4 && i != -1; j++) {
				if(j == 3) 
					ans[j] = arg;
				else {
					i = arg.indexOf(" ");
					if(i != -1) {
						ans[j] = arg.substring(0, i);
						arg = arg.substring(i+1);
					}
					else 
						ans[j] = arg;
				}
			}
		}
		return ans;
	}

	private String[] splitRequest(String parameters) {		//splits request

		String[] ans = new String[20];
		int i = 0;
		for(int j = 0; j < 20 && i != -1; j++) {
			i = parameters.indexOf(" ");
			if(i != -1) {
				ans[j] = parameters.substring(0, i);
				parameters = parameters.substring(i+1);
			}
			else 
				ans[j] = parameters;
		}
		return ans;
	}

	private boolean isUserNameValid(String username) {		

		usersHold.setUsers(usersHold.getUpdatedList(userLock));
		return ((username != null) && (!isLogged) && (!this.usersHold.hasUsername(username)));
	}

	private boolean isDatablockValid(String datablock) {

		boolean ans = false;
		if(datablock!=null && datablock.substring(0,9).equals("country=\"") &&
				datablock.substring(datablock.length() - 1).equals("\""))
			ans = true;

		return ans;
	}

	private boolean isCombined(String username, String password) {

		boolean ans = false;
		String pass = combinations.get(username);
		if(pass != null && pass.equals(password))
			ans = true;
		return ans;
	}

	private void rent(String username, Movie movie) {

		moviesHold.rent(movie);
		usersHold.rent(username, movie);
	}

	private void returnMovie(String username, Movie movie) {

		moviesHold.returnMovie(movie);
		usersHold.returnMovie(username, movie);
	}
}
