package bgu.spl181.net.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class MoviesHolder {
	
	@SerializedName("movies")
	private List<Movie> movies;
	private String moviesPath;
	private Gson gson;
	private MoviesHolder newHolder;
	private ReadWriteLock moviesLock;
	private Object Mhold ;

	
	public List<Movie> getMovies() {								//getter movie list
		return movies;
	}
	
	public void setMovies(List<Movie> movies) {						//setter movie list
		this.movies = movies;
	}

	public String getMoviesPath() {									//getter movie path
		return moviesPath;
	}

	public void setMoviesPath(String moviesPath) {					//setter movie path
		this.moviesPath = moviesPath;
	}

	public MoviesHolder getNewHolder() {							//getter new holder
		return newHolder;
	}

	public void setNewHolder(MoviesHolder newHolder) {				//setter new holder
		this.newHolder = newHolder;
	}

	public ReadWriteLock getMoviesLock() {							//getter movie lock
		return moviesLock;
	}

	public void setMoviesLock(ReadWriteLock moviesLock) {			//setter movie lock
		this.moviesLock = moviesLock;
	}

	public Object getMhold() {										//getter hold
		return Mhold;
	}

	public void setMHold(Object hold) {								//setter hold

		this.Mhold = hold;
		getNewFromGson();
		newHolder.setNewHolderMHold(new Object());
	}

	public void setNewHolderMHold(Object hold) {					//setter new holder hold
		this.Mhold = hold;
	}

	public Gson getGson() {											//getter gson
		return gson;
	}

	public void setGson(Gson gson) {								//setter gson
		this.gson = gson;
	}

	public boolean hasMoviename(String moviename) {					

		boolean ans = false;
		Movie[] movie = new Movie[movies.size()];
		movies.toArray(movie);
		for(int j = 0; (j < movie.length) && (ans == false); j++ ) {
			if (movie[j].getName().equals(moviename))
				ans = true;
		}
		return ans;	
	}
	
	public String getInfo(String moviename) {

		String info = "";
		synchronized (Mhold) {
			getNewFromGson(); 										//gets updated data to a new Holder
			Movie[] movieArray = new Movie[newHolder.movies.size()];
			newHolder.movies.toArray(movieArray);
			for(int i = 0; i < movieArray.length; i++) {
				if(movieArray[i].getName().equals(moviename))  
					info = "\"" + movieArray[i].getName() + "\"" + 
							" " + movieArray[i].getAvailableAmount() +
							" "+ movieArray[i].getPrice() +
							" "+  movieArray[i].getBannedCountriesString();
			}
		}
		return info;
	}

	public void updateMovies(ReadWriteLock moviesLock){				//updates movies json

		moviesLock.writeLock().lock();
		try (BufferedWriter br = new BufferedWriter(new FileWriter(this.moviesPath))){
			gson.toJson(newHolder , br);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		moviesLock.writeLock().unlock();
	}

	public void getNewFromGson() { 									//reads from movies.Json to a newHolder

		moviesLock.readLock().lock();
		try(BufferedReader br = new BufferedReader(new FileReader(this.moviesPath))) {
			newHolder =  gson.fromJson(br, MoviesHolder.class);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		moviesLock.readLock().unlock();
	}

	public String getAllMoviesInfo() {

		String Allinfo = "";
		synchronized (Mhold) {
			getNewFromGson(); 										//getting updated data to a new Holder
			Movie[] movieArray = new Movie[newHolder.movies.size()];
			newHolder.movies.toArray(movieArray);
			for(int i = 0; i < movieArray.length; i++) 				//creates info
				Allinfo += "\"" + movieArray[i].getName() + "\" "  ;
		}
		return Allinfo.substring(0,Allinfo.length() -1);
	}

	public boolean movieExist(String moviename) {					//checks if movie is exist

		boolean ans = false;
		Movie mov = searchAndGetMovie(moviename);
		if(mov != null)
			ans = true;
		return ans;
	}

	public boolean hasCopies(String moviename) {					//checks there are copies

		boolean ans = false;
		Movie mov = searchAndGetMovie(moviename);
		if(mov != null && Integer.parseInt(mov.getAvailableAmount()) > 0)
			ans = true;
		return ans;
	}

	public int getPrice(String moviename) {							

		int ans = 0;
		Movie mov = searchAndGetMovie(moviename);
		if(mov != null) 
			ans = Integer.parseInt(mov.getPrice());
		return ans;
	}

	public boolean isBanned(String usercountry, String moviename) {		//checks if is banned in user country

		boolean ans = false;
		Movie mov = searchAndGetMovie(moviename);
		if(mov != null && mov.getBannedCountries().contains(usercountry)) 
			ans = true;
		return ans;
	}

	public String getCopies(String moviename) {

		String ans = "";
		Movie mov = searchAndGetMovie(moviename);
		if(mov != null) 
			ans = mov.getAvailableAmount(); 
		return ans;
	}

	public Movie searchAndGetMovie(String moviename) {					//finds the movie

		Movie mov = null;
		boolean found = false;
		synchronized (Mhold) {
			getNewFromGson(); 											//getting updated data to a new Holder
			Movie[] movieArray = new Movie[newHolder.movies.size()];
			newHolder.movies.toArray(movieArray);
			for(int i = 0; i < movieArray.length &&  found == false; i++) {
				if(movieArray[i].getName().equals(moviename)) {
					found = true;
					mov = movieArray[i]; 
				}
			}
		}
		return mov;
	}

	public void rent(Movie movie) {

		movie.addCopies(-1);							//reduces copies by 1
		this.movies = newHolder.getMovies();
		updateMovies(moviesLock);						//updates movie json
	}

	public void returnMovie(Movie movie) {

		movie.addCopies(1);								//increases copies by 1
		this.movies = newHolder.getMovies();
		updateMovies(moviesLock);						//updates movie json
	}

	public boolean addMovie(String moviename, String amount, String price, List<String> banned) {
		
		if(moviename != null && amount != null && price != null && banned != null) {
			if (!hasMoviename(moviename) && Integer.parseInt(amount) > 0 && Integer.parseInt(price) > 0 ){
				synchronized (Mhold) {
					getNewFromGson(); 					//getting updated data to a new Holder
					Movie mov = new Movie (moviename,amount,price,banned,String.valueOf(movies.size()+1));
					newHolder.movies.add(mov);
					this.movies = newHolder.getMovies();
					updateMovies(moviesLock);
				}
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean removeMovie(String moviename) {
		
		if(moviename != null) {
			Movie mov = searchAndGetMovie(moviename);

			if (hasMoviename(moviename)) {
				int amount = Integer.parseInt(mov.getTotalAmount()) - Integer.parseInt(mov.getAvailableAmount());
				if(amount == 0) {								//checks if amount = total amount
					synchronized (Mhold) {
						getNewFromGson(); 						//getting updated data to a new Holder
						newHolder.movies.remove(mov);			//removes the movie
						this.movies = newHolder.getMovies();
						updateMovies(moviesLock);				//updates movie json
					}
					return true;
				}
				return false;
			}
			return false;
		}
		return false;
	}

	public boolean changePrice(String moviename, String newPrice) {
		
		boolean ans = false;
		if (moviename != null && newPrice != null && (Integer.parseInt(newPrice) > 0)) {
			synchronized (Mhold) {
				ans = true;
				Movie mov = searchAndGetMovie(moviename);
				if (mov != null) {
					mov.changePrice(newPrice);						//changes price
					this.movies = newHolder.getMovies();
					updateMovies(moviesLock);						//updates movie json
				}
			}
		}
		return ans;
	}
}
