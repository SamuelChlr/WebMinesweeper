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
                //gerer le get
            } 
            else if(request.startsWith("POST")) {
                //gerer le post
            }
            else if(request.contains("Upgrade: websocket")){
                //gerer le websocket
            }
            else{
                //renvoyer une erreur 405 
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

}
