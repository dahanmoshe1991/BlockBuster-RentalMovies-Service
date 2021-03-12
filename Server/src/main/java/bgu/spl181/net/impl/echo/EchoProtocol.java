package bgu.spl181.net.impl.echo;

import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;

import java.time.LocalDateTime;

public class EchoProtocol implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate = false;

 /*   @Override
    public String process(String msg) {
        shouldTerminate = "bye".equals(msg);
        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        return createEcho(msg);
    }
*/
    
    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

	@Override
	public void start(int connectionId, Connections<String> connections) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(String message) {
		
		
	}
}
