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

            if(request == null || request.isEmpty())
                return;
            
            System.out.println("requete reçue :");
            System.out.println(request);

            //on verifie quel type de requête c'est
            if(request.startsWith("GET"))
            {
                requestGET(request,htmlGenerator);
            } 
            else if(request.startsWith("POST")) {
                System.out.println("debut POST");
                requestPOST(request);
            }
            else if(request.contains("Upgrade: websocket")){
                requestWithWebsocket(request);
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
            // TODO Auto-generated catch block
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
            int contentLenght = 0;
            if(header.contains("Content-Lenght:"))
            {
                //on extrait la taille du body
                int start = header.indexOf("Content-Length:") + "Conten-Lenght:".length(); 
                int end = header.indexOf("\r\n", start);
                contentLenght = Integer.parseInt(header.substring(start, end).trim());
            }

            //on va lire le body s'il y a en a un
            StringBuilder requestBodyBuilder = new StringBuilder();
            String body = "";
            if(contentLenght >0)
            {
                int totalread = 0;
                byte[] bodyBuffer = new byte[contentLenght];
                while (totalread < contentLenght) {
                    int len2 = requestStream.read(bodyBuffer,totalread,contentLenght-totalread);
                    
                    if(len2 == -1)
                        break;
                    
                    totalread +=len2;
                    
                }

                body = new String(bodyBuffer,0,totalread);
                
            }
           
            return header + "\r\n\r\n" + body;     

    }

    /****************** FONCTION DE GESTION DES DIFFERENTES REQUETES  *****************************/

    private void requestGET(String request, MinesweeperHTML htmlGenerator) throws IOException {

        
        String sessionId = readCookies(request, "SESSID");
        Session session;
        Boolean NewSession = false;
        System.out.println("je suis dans le GET");

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
        System.out.println("test 1");
        session.getDuration(); // mettre à jour la durée
        System.out.println("TEST 2");
        if( request.contains("GET / ")){
            System.out.println("TEST 3");
            Redirection();
        }
        else if(request.startsWith("GET /play.html"))
        {
            
            System.out.println("requete GET vers play.html");
            //renvoyer la page html de play (idealement avec chunk)
            
            Boolean acceptGZIP = request.contains("Accept-Encoding: gzip");
            if(NewSession){
                String headers = "HTTP/1.1 200 OK\r\n" +
                            "Connection: close\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Set-Cookie: SESSID=" + sessionId + "; Path=/; Max-Age=600\r\n" +
                            "Transfer-Encoding: chunked\r\n\r\n";
                responStream.write(headers);
            }
            else {
                String headers = "HTTP/1.1 200 OK\r\n" +
                            "Connection: close\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Transfer-Encoding: chunked\r\n\r\n";
                responStream.write(headers);
            }
            responStream.flush();
            htmlGenerator.generateHTML(socket.getOutputStream(),session, acceptGZIP);
        }
        else if(request.contains("/leaderboard.html"))
        {
            //envoyer la page html du leaderboard (idealement avec chunk)
            System.out.println("TEST 4");
        }
        else {
            System.out.println("TEST 5");
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
        /* 
         else {
            // Lire le corps si non inclus dans "request" (requis si le header arrive séparément)
            body = readRequestBody(reader, header);
        }
        */

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
        
        if(request.startsWith("POST /play.html/setUsername")){

            if(Body.contains("username=")){
                int index = Body.indexOf("=");
                String username = Body.substring(index +1);


            }
            else
            {
                System.out.println("erreur post username");
                //renvoyer une erreur
            }

        }



        //renvoyer le nouveau fichier html modifer (en chunk)

    }

    /*// Lire le corps de la requête POST si nécessaire
    private String readRequestBody(BufferedReader reader, String header) throws IOException {
        int contentLength = getContentLength(header);
        char[] buffer = new char[contentLength];
        reader.read(buffer, 0, contentLength);
        return new String(buffer);
    }

    // Extraire Content-Length depuis l'en-tête
    private int getContentLength(String header) {
        for (String line : header.split("\r\n")) {
            if (line.startsWith("Content-Length:")) {
                return Integer.parseInt(line.substring(15).trim());
            }
        }
        return 0;
    }*/ //poura etre utile si le header arrive séparément du body


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
            sendCookie(sessionId);
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
            if(line.startsWith("Sec-WebSocket-key:")){
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

    private String TRY(String request, Session session){
        return "";
    }

    private String FLAG(String request, Session session) {
        return "";
    }


    /**
     * fonction qui va envoyer la response pour redigirer le client sur la bonne page 
    */
    private void Redirection(){
        responStream.println("HTTP/1.1 303 See Other");
        responStream.println("location: /play.html");
        responStream.println("Content-Lenght: 0");
        responStream.println();
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
            if (line.startsWith("Cookie:")) {
                String[] cookies = line.substring(7).split("; ");
                for (String cookie : cookies) {
                    String[] parts = cookie.split("=");
                    if (parts.length == 2 && parts[0].equals(cookieName)) {
                        return parts[1];
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
