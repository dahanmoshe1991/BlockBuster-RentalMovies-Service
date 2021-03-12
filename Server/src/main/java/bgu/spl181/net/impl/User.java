package bgu.spl181.net.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;



public class User {

	@SerializedName("username")
	private String username;
	@SerializedName("type")
	private String type;
	@SerializedName("password")
	private String password;
	@SerializedName("country")
	private String country;
	@SerializedName("movies")
	private List<Movie> movies;
	@SerializedName("balance")
	private String balance;

	
	public User(String username, String password, String datablock) {		//constructor

		this.movies = new ArrayList<Movie>();
		this.username = username;
		this.password = password;
		this.country = datablock;
		this.type = "normal";
		this.balance = "0";
	}

	public String getUsername() {						//getter user name
		return username;
	}

	public String getPassword() {						//getter password
		return password;
	}
	public String getBalance() {						//getter balance
		return balance;
	}

	public String getCountry() {						//getter countries
		return country;
	}

	public List<Movie> getMovies() {					//getter movies list
		return movies;
	}
	
	public String getType() {							//getter type
		return type;
	}

	public String addBalance(int balanceAdd) {			//adds to balance

		int old = Integer.parseInt(this.balance);
		int newB = old + balanceAdd;
		this.balance = String.valueOf(newB);
		return balance;
	}

	public boolean hasMovies(String moviename) {		//checks if already rent the movie

		boolean found = false;
		if(!movies.isEmpty()) {
			Movie[] movieArray = new Movie[movies.size()];
			movies.toArray(movieArray);
			for(int i = 0; i < movieArray.length &&  found == false; i++) {
				if(movieArray[i].getName().equals(moviename)){
					found = true;
				}
			}
		}
		return found;
	}
}
