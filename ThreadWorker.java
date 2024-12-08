import java.net.*;
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

            if(request == null || request.isEmpty())
                return;
            
            System.out.println("requete reçue :");
            System.out.println(request);

            //on verifie quel type de requête c'est
            if(request.startsWith("GET"))
            {
                requestGET(request);
            } 
            else if(request.startsWith("POST")) {
                requestPOST(request);
            }
            else if(request.contains("Upgrade: websocket")){
                //gerer le websocket
            }
            else{
                sendError(405, "Method Not Allowed");
            }
        }
        catch(Exception e)
        {
            System.err.println(e);
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
            while ((len = requestStream.read(buffer)) != 1) {
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
                int start = header.indexOf("Content-Lenght:") + "Conten-Lenght:".length(); 
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

    private void requestGET(String request) throws IOException {

        if( request.contains("GET /")){
            Redirection();
        }
        else if(request.contains("/play.html"))
        {
            //renvoyer la page html de play (idealement avec chunk)
        }
        else if(request.contains("/leaderboard.html"))
        {
            //envoyer la page html du leaderboard (idealement avec chunk)
        }
        else {
            sendError(404, "Not Found");
        }
    }

    private void requestPOST(String request) throws IOException {

        String[] requestParts = request.split("\r\n\r\n");
        String Header = requestParts[0];
        String Body = "";
        if(requestParts.length > 1)
            Body = requestParts[1];

        System.out.println("Header :" + Header);
        System.out.println("Body :" + Body);

        //ajouter une fonction pour la gestion de la requête post qui va envoyer le nouveau fichier html modifier

        //renvoyer le nouveau fichier html modifer (en chunk)

    }

    private void requestWithWebsocket() throws IOException{
        WebSocket webSocket = new WebSocket(socket);

        while (true) {
            String request = webSocket.receive();
            // faire une fonction pour gerer les requetes
            String response = "fonction a faire";
            webSocket.send(response);
        }

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

}
