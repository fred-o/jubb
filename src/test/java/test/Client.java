package test;

import java.net.URL;

import jubb.client.DirectJubbClient;
import jubb.client.JubbClient;

public class Client {
    
	public static void main(String argv[]) throws Exception {
		JubbClient cl = new DirectJubbClient(new URL("http://localhost:8081/queue/test"));
		cl.post(10, "{\"message\":\"hello\")");
	}

}