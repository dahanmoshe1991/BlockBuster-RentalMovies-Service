package bgu.spl181.net.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
	
	private ConcurrentHashMap<Integer , ConnectionHandler<T>> activeClients = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
	private AtomicInteger connectionId = new AtomicInteger(0);
	
	@Override
	public boolean send(int connectionId, T msg) {
		
		if(activeClients.containsKey(connectionId)) {
			activeClients.get(connectionId).send(msg);
			return true;
		}	
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void broadcast(T msg) {
		
		ConnectionHandler<?>[] clientsArray = new ConnectionHandler<?>[activeClients.size()];
		activeClients.values().toArray(clientsArray);
		for(int i = 0; i<clientsArray.length; i++)					//sends message to every client
			((ConnectionHandler<T>)clientsArray[i]).send(msg);
	}

	@Override
	public void disconnect(int connectionId) {
		
		if(activeClients.containsKey(connectionId))
			activeClients.remove(connectionId);
	}

	public int addClient(ConnectionHandler<T> con) {
		
		this.connectionId.incrementAndGet();
		activeClients.put(connectionId.intValue(), con);
		return connectionId.intValue();
	}
}
