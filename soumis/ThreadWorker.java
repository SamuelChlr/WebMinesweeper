import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.util.*;

public class ThreadWorker extends Thread {
    private Socket socket;
    private InputStream requestStream;
    private PrintWriter responStream;

    public ThreadWorker(Socket s)
    {
        try{
            socket = s;
            requestStream = s.getInputStream();
            responStream = new PrintWriter(s.getOutputStream(), true);

        }
        catch(Exception e)
        {
            System.err.println("Error ThreadWorker Constructor : " + e);
        }
    }


    @Override
    public void run(){

        try{
            String request = readHTTPrequest();
            MinesweeperHTML htmlGenerator = new MinesweeperHTML();
            LeaderboardHTML leaderboardHTML = new LeaderboardHTML();
            

            if(request == null || request.isEmpty())
                return;
            
            System.out.println("requete reçue :");
            System.out.println(request);

            //on verifie quel type de requête c'est
            if(request.contains("Upgrade: websocket")){
                requestWithWebsocket(request);
            }
            else if(request.startsWith("GET"))
            {
                requestGET(request,htmlGenerator, leaderboardHTML);
            } 
            else if(request.startsWith("POST")) {
                System.out.println("debut POST");
                requestPOST(request);
            }
            else{
                sendError(405, "Method Not Allowed");
            }
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
        responStream.close();
        try {
            socket.close();
        } catch (IOException e) {
            
            e.printStackTrace();
        }

    }

    /**
     * fonction qui va lire une requête HTTP
     * @return la requête lue sous forme d'un String
    * @throws IOException 
    */
    private String readHTTPrequest() throws IOException {
        byte[] buffer = new byte[1024];

        StringBuilder requestBuilder = new StringBuilder();
        int len;

            
            //lecture de la requête ou de l'entête en cas de requête POST
            while ((len = requestStream.read(buffer)) != -1) {
                requestBuilder.append(new String(buffer, 0, len));

                if(requestBuilder.length() >= 4 && requestBuilder.substring(requestBuilder.length() -4).equals("\r\n\r\n"))
                    break;
            }

            String header = requestBuilder.toString();
            
            //on verifie que ce n'est pas une requête POST avec un body
            int contentLength = 0;
            String[] lines = header.split("\r\n");
            for (String line : lines) {
                if (line.startsWith("Content-Length:")) {
                    String value = line.substring("Content-Length:".length()).trim();
                    try {
                        contentLength = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        System.err.println("Erreur : Content-Length invalide -> " + value);
                    }
                }
            }


            //on va lire le body s'il y a en a un
            //StringBuilder requestBodyBuilder = new StringBuilder();
            String body = "";
            
            if (contentLength > 0) {
                int totalRead = 0;
                byte[] bodyBuffer = new byte[contentLength];
                while (totalRead < contentLength) {
                    int len2 = requestStream.read(bodyBuffer, totalRead, contentLength - totalRead);
                    if (len2 == -1) break;
                    totalRead += len2;
                }
                body = new String(bodyBuffer, 0, totalRead);
            }
            
           
            return header + "\r\n\r\n" + body;     

    }

    /****************** FONCTION DE GESTION DES DIFFERENTES REQUETES  *****************************/

    private void requestGET(String request, MinesweeperHTML htmlGenerator, LeaderboardHTML htmlLeadGenerator) throws IOException {

        
        String sessionId = readCookies(request, "SESSID");
        Session session;
        Boolean NewSession = false;
        

        if(sessionId == null || !SessionManager.sessionExists(sessionId)) { // on vérifie s'il y a une session déjà connue 
            //si elle n'existe pas on en crée une
            sessionId = SessionManager.createSession();
            session = SessionManager.getSession(sessionId);
            NewSession = true;
            System.out.println("Nouvelle session crée avec succès. SESSID: " + sessionId);
        }
        else {
            session = SessionManager.getSession(sessionId);
            System.out.println("Session en cours, SESSID: " + sessionId);
        }

        
        session.getDuration(); // mettre à jour la durée
        
        if( request.contains("GET / ")){
            
            Redirection();
        }
        else if(request.startsWith("GET /play.html"))
        {
        
            responStream.flush();
            htmlGenerator.generateHTML(socket.getOutputStream(),session, NewSession);
        }
        else if(request.contains("/leaderboard.html"))
        {
            System.out.println("requete GET vers leaderboard.html");
            Boolean acceptGZIP = request.contains("Accept-Encoding: gzip");
            htmlLeadGenerator.generateHTML(socket.getOutputStream(), session, acceptGZIP);
            
        }
        else {
            
            sendError(404, "Not Found");
        }
    }

    /************************ POST ***************$*/
    private void requestPOST(String request) throws IOException {

        
        String[] requestParts = request.split("\r\n\r\n");
        String Header = requestParts[0];
        String Body = "";
        if(requestParts.length > 1)
            Body = requestParts[1];
        //else erreur !!!!

        System.out.println("Header :" + Header);
        System.out.println("Body :" + Body);

        //gestion des cookies pour la session
        String sessionId = readCookies(Header, "SESSID");
        Session session;
        

        if (sessionId == null || !SessionManager.sessionExists(sessionId)) {
            sessionId = SessionManager.createSession();
            session = SessionManager.getSession(sessionId);
            //sendCookie(sessionId);
            System.out.println("Nouvelle session créée : " + sessionId);
        } else {
            session = SessionManager.getSession(sessionId);
            System.out.println("Session existante : " + sessionId);
        }

        session.getDuration(); // mettre à jour la durée
        
        if(request.startsWith("POST /play.html")){ 

            if(Body.contains("username=")){
                int index = Body.indexOf("=");
                String username = Body.substring(index +1);
                System.out.println("POST 1 SESSID:" + session.getSessionId() + " username :" + username);
                session.setPlayerName(username);
                Redirection();
                

            }else if(Body.contains("action=") && Body.contains("row=") && Body.contains("col=")){
                int actionIndex = Body.indexOf("action=");
                int rowIndex = Body.indexOf("row=");
                int colIndex = Body.indexOf("col=");
        
                // Extraire la valeur de "action"
                String action = Body.substring(actionIndex + 7, Body.indexOf("&", actionIndex) == -1 ? Body.length() : Body.indexOf("&", actionIndex));
        
                // Extraire la valeur de "row"
                String rowStr = Body.substring(rowIndex + 4, Body.indexOf("&", rowIndex) == -1 ? Body.length() : Body.indexOf("&", rowIndex));
        
                // Extraire la valeur de "col"
                String colStr = Body.substring(colIndex + 4, Body.indexOf("&", colIndex) == -1 ? Body.length() : Body.indexOf("&", colIndex));

                try{
                    MinesweeperHTML htmlGen = new MinesweeperHTML();
                    int row = Integer.parseInt(rowStr);
                    int col = Integer.parseInt(colStr);

                    if(action.equals("reveal")){
                        System.out.println("revealing" + row + col);
                        ProtocolMP MP = new ProtocolMP(session); 
                        int result = MP.TryWS(row, col);                      
                    }
                    else if(action.equals("flag")){
                        System.out.println("flagging" + row + col);
                        ProtocolMP MP = new ProtocolMP(session);

                    }
                } catch(NumberFormatException e){}
            }
            else
            {
                System.out.println("erreur post username");
                //renvoyer une erreur
            }

        }

    }



    /********************** WEBSOCKET 
         * @throws NoSuchAlgorithmException *********************************/
    
    private void requestWithWebsocket(String request1) throws IOException, NoSuchAlgorithmException{
        WebSocket webSocket = new WebSocket(socket);

        WebSocketHanshake(request1);

        //gestion des cookies pour la session
        String sessionId = readCookies(request1, "SESSID");
        Session session;

        if (sessionId == null || !SessionManager.sessionExists(sessionId)) {
            sessionId = SessionManager.createSession();
            session = SessionManager.getSession(sessionId);
            //sendCookie(sessionId);
            System.out.println("Nouvelle session créée : " + sessionId);
        } else {
            session = SessionManager.getSession(sessionId);
            System.out.println("Session existante : " + sessionId);
        }

        session.getDuration(); // mettre à jour la durée

        try{
            while (true) {
                String request = webSocket.receive();
                
                // faire une fonction pour gerer les requetes
                String response = HandleWebSocketMessage(request, session);
                session.getDuration();
                webSocket.send(response);
            }
        } 
        catch(Exception e) {
            System.err.println("Erreur des echanges webSocket:" + e);
        }

    }

    private void WebSocketHanshake(String request) throws NoSuchAlgorithmException, UnsupportedEncodingException{

        String webSocketKeyClient = null;

        //on recherche la clé du client 
        for(String line : request.split("\r\n")){
            if(line.startsWith("Sec-WebSocket-Key:")){
                webSocketKeyClient = line.substring(19).trim();
                break;
            }
        }

        if(webSocketKeyClient == null ) {
            sendError(400, "No Sec-WebSocket-Key from the Client");
        }

        String partKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        String webSocketKeyServer = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((webSocketKeyClient + partKey).getBytes("UTF-8")));

        //reponse au hanshake
        responStream.println("HTTP/1.1 101 Switching Protocols");
        responStream.println("Connection: Upgrade");
        responStream.println("Upgrade: websocket");
        responStream.println("Sec-WebSocket-Accept: "+ webSocketKeyServer );
        responStream.println();
        responStream.flush();
    }

    private String HandleWebSocketMessage(String request, Session session){
        if(request.startsWith("TRY")) {
            return TRY(request, session);
        }
        else if( request.startsWith("FLAG")){
            if(session.getIsInitialized())
                return FLAG(request, session);
            else
                return "WRONG: la partie n'a pas encore commence";
        }
        else {
            return "Erreur: Commande WebSocket non reconnue";
        }

    }

    private String TRY(String request, Session session) {
        try {
            System.out.println("TRY request received: " + request); // Debug
            ProtocolMP MP = new ProtocolMP(session);
            String[] parts = request.split(":");
            String[] position = parts[1].split(",");
            int x = Integer.parseInt(position[0]);
            int y = Integer.parseInt(position[1]);
    
            int result = MP.TryWS(x, y); // Résultat du clic
            if (result == 1) {
                System.out.println("Game lost");
                return "GAME LOST";
            } else if (result == 2) {
                System.out.println("Game won");
                return "GAME WIN";
            } else {
                System.out.println("Game continues");
                return formatGrid(session.getGameState());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing TRY command: " + e.getMessage();
        }
    }
    

    private String FLAG(String request, Session session) {
        ProtocolMP MP = new ProtocolMP(session);
        String[] parts = request.split(":");
        String[] position = parts[1].split(",");
        MP.FlagWS(Integer.parseInt(position[0]), Integer.parseInt(position[0]));
        return formatGrid(session.getGameState());
        
    }

    private String formatGrid(List<char[]> grid) {
        StringBuilder builder = new StringBuilder();
        char[] flatGrid = grid.get(0); // Supposons que la grille est stockée comme une seule ligne dans le premier élément de la liste.
    
        for (int i = 0; i < flatGrid.length; i += 14) { // Parcours de la grille (chaque ligne contient 7 cases * 2 caractères par case)
            for (int j = i; j < i + 14 && j < flatGrid.length; j += 2) { // Parcours des cases dans la ligne actuelle
                char content = flatGrid[j];
                char visibility = flatGrid[j + 1];
    
                if (visibility == 'H') {
                    builder.append("#"); // Case cachée
                } else if (visibility == 'F') {
                    builder.append("F"); // Case marquée comme drapeau
                } else if (visibility == 'S') {
                    if (content == 'B') {
                        builder.append("B"); // Bombe
                    } else {
                        builder.append(content); // Chiffre ou autre contenu affichable
                    }
                }
            }
            builder.append("\n"); // Nouvelle ligne pour chaque rangée de 7 cases
        }
    
        return builder.toString().trim(); // Supprimer les espaces inutiles en fin de chaîne
    }
    


    /**
     * fonction qui va envoyer la response pour redigirer le client sur la bonne page 
    */
    private void Redirection(){
        System.out.println("!!! REDIRECTION !!!");

        String headers = "HTTP/1.1 303 See Other\r\n" +
                            "location: /play.html\r\n" +
                            "Content-Lenght: 0\r\n\r\n";
                responStream.write(headers);
                responStream.flush();

    }

    /**
     * fonction qui va envoyer le bon code de retour en cas d'erreur
     * @param code : code de l'erreur associé
     * @param message : message explicant l'erreur
    */
    private void sendError(int code, String message){
        responStream.println("HTTP/1.1 "+code+" "+message);
        responStream.println("Content-Type: text/plain");
        responStream.println();
        responStream.println(message);
        responStream.flush();
    }


    /********************************* GESTION DES COOKIES DE SESSION *************************************/

    /**
     * fonction qui extrait le SESSID des cookies d'une requête
     * @param request    : chaine de caractères contenant la requête reçue
     * @param cookieName : pour rechercher un cookie en particulier ( exemple SESSID)
     * @return           : la valeur du cookie recherché ou null s'il n'est pas trouvé
    */
    private String readCookies(String request, String cookieName) {
       
        for (String line : request.split("\r\n")) {
            if (line.toLowerCase().startsWith("cookie:")) {
                // Extraire tous les cookies
                String[] cookies = line.substring(7).split(";");
                for (String cookie : cookies) {
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && parts[0].trim().equals(cookieName)) {
                        return parts[1].trim(); 
                    }
                }
            }
        }
        return null;
    }
    


    /**
     * fonction qui envoie le SESSID au client
     * @param request    : un string contenant le SESSID
    */
    private void sendCookie(String sessionId) {
        responStream.println("HTTP/1.1 200 OK\r\n");
        responStream.println("Set-Cookie: SESSID=" + sessionId + "; Path=/; Max-Age=600\r\n");
        responStream.println("\r\n");
        responStream.flush();
    }
}
