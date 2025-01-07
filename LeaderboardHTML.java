import java.io.*;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class LeaderboardHTML{
    private String htmlCode;
	private static List<Player> leaderboard = new ArrayList<>();

    private class Player{
        public String name;
        public int time;

        Player(String name, int time){
            this.name = name.isEmpty()?"":name;
            this.time = time;
        }
    }

	public void addPlayerToLeadeboard(String playerName, int time){
		leaderboard.add(new Player(playerName, time));
		leaderboard.sort(Comparator.comparingInt((Player p) -> p.time));//trie par temps croissant
	}
	
    public void LeaderboardHTML(){
        htmlCode = "";
		Player p = new Player("Jean", 50);
		addPlayerToLeadeboard(p.name, p.time);
    }

    //generate html code for a get method
    /*public void generateHTML(PrintWriter serverWriter, boolean acceptGZIP) throws IOException{
        byte byteArray[];
        String htmlCode, tmp;

        if(acceptGZIP){
            htmlCode = getHeaders() + getBody();
			byteArray = compress(htmlCode);
			sendChunkedResponse(serverWriter,htmlCode);
			serverWriter.flush();
        }else{
            htmlCode = getHeaders() + getBody();
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
        }
    }*/

	//generate the html code for a post avec JS
    public void generateHTML(OutputStream serverWriter, Session session, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;
		System.out.println("debut génération");

		
			System.out.println("debut generation");
			htmlCode = getHeaders() + getBody();
			sendContentWithContentLength(serverWriter, htmlCode);
			System.out.println(htmlCode);
			System.out.println("fin envoie");

		if(session.isGameOver()){
			String playerName = session.getPlayerName();
			long elapsedTime = session.getDuration();

			System.out.println("Partie teminée pour" + playerName);

			this.addPlayerToLeadeboard(playerName, (int) elapsedTime);
		}

			
		
		
		
	}

	private void sendContentWithContentLength(OutputStream outputStream, String content) throws IOException {
		byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
		String headers = "HTTP/1.1 200 OK\r\n" +
						 "Content-Type: text/html; charset=UTF-8\r\n" +
						 "Content-Length: " + contentBytes.length + "\r\n" +
						 "Connection: close\r\n\r\n";
	
		outputStream.write(headers.getBytes(StandardCharsets.UTF_8));
		outputStream.write(contentBytes);
		outputStream.flush();
	}

	private void sendChunkedResponse(OutputStream outputStream, String content) throws IOException {
		
	
		// Taille des chunks
		int chunkSize = 1024;
	
		// Envoi des chunks
		int offset = 0;
		while (offset < content.length()) {
			// Calculer la taille du chunk actuel
			int remaining = content.length() - offset;
			int currentChunkSize = Math.min(chunkSize, remaining);
	
			// Obtenir la portion à envoyer
			String chunk = content.substring(offset, offset + currentChunkSize);
	
			// Envoyer la taille du chunk en hexadécimal, suivie par le contenu du chunk
			String chunkHeader = Integer.toHexString(currentChunkSize) + "\r\n";
			outputStream.write(chunkHeader.getBytes(StandardCharsets.UTF_8));
			outputStream.write(chunk.getBytes(StandardCharsets.UTF_8));
			outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
	
			// Avancer l'offset
			offset += currentChunkSize;
		}
	
		// Envoyer le chunk final pour indiquer la fin de la transmission
		outputStream.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
	
		// Forcer la vidange du flux
		outputStream.flush();
	}

    public String getHeaders(){
    	return  "<!DOCTYPE html>\n" +
        	    "<html>\n" +
        	    "   <head>\n" +
        	    "       <meta charset='utf-8'>\n" +  
        	    "       <title>Leaderboard</title>\n" +
        	    "       <style>\n" +
        	    "			" + getFile("leaderboard.css") + "\n" +
        	    "       </style>\n" +
        	    "   </head>\n";
	}

	public String getBody() {
		StringBuilder body = new StringBuilder();
		body.append("  <body>\n");
		body.append("    <h1 style=\"text-align: center;\">Minesweeper Leaderboard</h1>\n");
		body.append("    <ol id=\"playerList\">\n");
	
		// Ajouter les joueurs triés
		for (Player player : leaderboard) {
			body.append("      <li>")
				.append(player.name)
				.append(" - ")
				.append(player.time)
				.append(" seconds</li>\n");
		}
	
		body.append("    </ol>\n");
		body.append("    <script>").append(getFile("leaderboard.js")).append("</script>\n");
		body.append("  </body>\n");
		body.append("</html>\r\n\r\n");
		return body.toString();
	}

    //Gzip the string into a byte array
	public byte[] compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}

    public String getFile(String path)
	{
		String htmlCode ="",line;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			while((line = br.readLine()) != null)
				htmlCode += line;
		}
		catch(IOException e) { }
		return htmlCode;

	}

    //Chunk encode the byteArray and send it into the output stream
    private void chunckEncoding(byte[] byteArray, OutputStream os) throws IOException
	{
		byte[] tmpByte = new byte[128];
		String tmp;
		int maxChunckSize = 128,i=0;
		while(byteArray.length-i*128 > maxChunckSize)
		{
			tmp = new String(Integer.toHexString(128)+"\r\n");
			os.write(tmp.getBytes());
			System.arraycopy(byteArray, i*128, tmpByte, 0, 128);
			os.write(tmpByte);
			tmp = new String("\r\n");
			os.write(tmp.getBytes());
			++i;
		}

		tmp = new String(Integer.toHexString(byteArray.length-i*128)+"\r\n");
		os.write(tmp.getBytes());
		System.arraycopy(byteArray, i*128, tmpByte, 0, byteArray.length-i*128);
		os.write(tmpByte,0,byteArray.length-i*128);
		tmp = new String("\r\n");
		os.write(tmp.getBytes());
		tmp = new String(Integer.toHexString(0)+"\r\n\r\n");
		os.write(tmp.getBytes());
	}

	private void chunckEncoding(String htmlCode, PrintWriter serverWriter)
	{
		
		int maxChunckSize = 128;
		while(htmlCode.length() > maxChunckSize)
		{
			serverWriter.print(Integer.toHexString(128)+"\r\n");
			serverWriter.print(htmlCode.substring(0,128));
			htmlCode = htmlCode.substring(128);
		}

		serverWriter.print(Integer.toHexString(htmlCode.length())+"\r\n");
		serverWriter.print(htmlCode);
		serverWriter.print(Integer.toHexString(0)+"\r\n\r\n");
	}

}