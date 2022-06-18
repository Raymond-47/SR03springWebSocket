package fr.utc.springwebsocket.websocket;


import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Hashtable;

@ServerEndpoint(value = "/chatserver/{channelId}",configurator = ChatServer.EndpointConfigurator.class)
@Component
public class ChatServer {

    private static ChatServer singleton = new ChatServer();

    private ChatServer(){

    }

    public static ChatServer getInstance(){
        return ChatServer.singleton;
    }

    private Hashtable<String, Session> sessions = new Hashtable<>();

    @OnOpen
    public void open(Session session, @PathParam("channelId") String channelId){
        sendMessage(">>> Connection established for channel " + channelId, channelId);
        session.getUserProperties().put("channelId",channelId);
        sessions.put(session.getId(),session);
    }

    @OnClose
    public void close(Session session) {
        String channelId = (String) session.getUserProperties().get("channelId");
        sessions.remove(session.getId());
        sendMessage(">>> Connection closed for "+channelId, channelId);
    }

    @OnError
    public void onError(Throwable error){
        System.out.println("Error: " + error.getMessage());
    }

    @OnMessage
    public void handleMessage(String message, Session session){
        String channelId = (String) session.getUserProperties().get("channelId");
        String fullMessage = " >>> " + message;

        sendMessage(fullMessage,channelId);
    }

    private void sendMessage(String fullMessage, String channelIdDestination){
        System.out.println(fullMessage);

        for (Session session:sessions.values()){
            try{
                if(session.getUserProperties().get("channelId").toString().equals(channelIdDestination)){
                    session.getBasicRemote().sendText(fullMessage);
                }

            }catch (Exception exception){
                System.out.println("ERROR: can't send message to " + session.getId());
            }
        }
    }

    public static class EndpointConfigurator extends ServerEndpointConfig.Configurator{
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getEndpointInstance(Class<T> endpointClass){
            return (T)ChatServer.getInstance();
        }
    }
}
