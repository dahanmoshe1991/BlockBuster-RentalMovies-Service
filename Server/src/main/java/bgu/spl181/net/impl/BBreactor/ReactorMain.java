package bgu.spl181.net.impl.BBreactor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;

import bgu.spl181.net.impl.BidiMessagingProtocolImpl;
import bgu.spl181.net.impl.MessageEncoderDecoderImpl;
import bgu.spl181.net.impl.MoviesHolder;
import bgu.spl181.net.impl.SharedData;
import bgu.spl181.net.impl.UsersHolder;
import bgu.spl181.net.srv.Server;

public class ReactorMain {

	private static UsersHolder UsersHold;
	private static MoviesHolder MoviesHold;
	private static ReadWriteLock userLock;
	private static ReadWriteLock moviesLock;
	private static SharedData shared;

	public static void main(String[] args){

		String path = System.getProperty("user.dir");
		String userpath = path + "/Database/Users.json";
		String moviespath =path +"/Database/Movies.json";

		userLock = new ReentrantReadWriteLock();
		moviesLock = new ReentrantReadWriteLock();
		shared = new SharedData();
		Gson gson = new Gson();
		try {
			BufferedReader br = new BufferedReader(new FileReader(userpath));
			UsersHold = gson.fromJson(br, UsersHolder.class);				//initialization
			UsersHold.setUserPath(userpath);
			UsersHold.setGson(gson);
			UsersHold.setUserLock(userLock);
			UsersHold.setHold(new Object());
			UsersHold.logUsers(); 									

			br = new BufferedReader(new FileReader(moviespath));
			MoviesHold = gson.fromJson(br, MoviesHolder.class);				//initialization
			MoviesHold.setMoviesPath(moviespath);
			MoviesHold.setGson(gson);
			MoviesHold.setMoviesLock(moviesLock);
			MoviesHold.setMHold(new Object());

			start("7777");
		}
		catch(FileNotFoundException ex){
		}
	}

	public static void start(String s){
							//Runtime.getRuntime().availableProcessors()
		Server.reactor(
				5,
				Integer.parseInt(s), //port
				() -> new BidiMessagingProtocolImpl(UsersHold,MoviesHold,userLock), //protocol factory
				() -> new MessageEncoderDecoderImpl()						//message encoder decoder factory
				).serve();
	}
}
