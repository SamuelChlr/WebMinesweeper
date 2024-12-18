import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class SessionManager {

    private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>(); 

    //créer une nouvelle session
    public static String createSession(){
        String sessionId = java.util.UUID.randomUUID().toString();

        sessions.put(sessionId, new Session(sessionId));

        return sessionId;
    }

    //récupérer une session existante
    public static Session getSession(String sessionId){
        return sessions.get(sessionId);
    }

    //verifie si une session existe
    public static boolean sessionExists(String sessionId){
        return sessions.containsKey(sessionId);
    }
}
