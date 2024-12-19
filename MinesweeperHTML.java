import java.net.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import java.nio.charset.StandardCharsets;

public class MinesweeperHTML{
    private String htmlcode;

    public MinesweeperHTML(){
        htmlcode = "";
    }
    //generate the html code without
    /*public void generateHTML(PrintWriter serverWriter, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;

		
		
		if(acceptGZIP)
		{
			htmlCode = getHeaders() + getTile() + getFooter();
			byteArray = compress(htmlCode);
			chunkEncodingGZIP(byteArray,serverWriter);
			serverWriter.flush();
		}
		else
		{
			htmlCode = getHeaders() + getTile() + getFooter();
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
		}
		
		

	} */
    //generate the html code for a post avec JS
    public void generateHTML(OutputStream serverWriter, Session session, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;
		String username = session.getPlayerName();
		System.out.println("debut génération");

		
			System.out.println("debut generation");
			htmlCode = getHeaders() + getBody(username);
			sendChunkedResponse(serverWriter, htmlCode);
			System.out.println("fin envoie");
			
		
		
		
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
	
	

    public String getHeaders()
	{
		return 	  "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "    <head>\n"
				+ "        <meta charset='utf-8'>\n"
				+ "        <title>Minesweeper</title>\n"
				+ "        <style>\n"
				+ "            " + getFile("play.css") + "\n"
				+ "        </style>\n"
				+ "    </head>\n";
	}


	public String getBody(String username)
	{
		return	"	<body>\n" +
				"		<h1 style=\"text-align: center;\">Minesweeper "+ username +" </h1>\n" +
				"		<div id=\"username-form\">\n" +
				"			<form action=\"/play.html/setUsername\" method=\"POST\">\n" +
				"				<label for=\"username\">Enter your username:</label>\n"+
				"				<input type=\"text\" id=\"username\" name=\"username\" placeholder=\"Your name\" required>\n" +
				"				<button type=\"submit\">Update Username</button>\n" +
				"			</form>\n"+
				"		</div>\n"+
				"		<div id=\"playerName\">\n" +
				generateTable() +
				"		</div>\n" +
				getScript() +
				"	</body>\n" +
				"</html>\r\n\r\n";
	}

	public String generateTable() {
		StringBuilder tableBuilder = new StringBuilder();
	
		// Début de la table
		tableBuilder.append("\t\t\t\t<table class=\"minesweeper\">\n");
	
		for (int row = 0; row < 7; row++) {
			tableBuilder.append("\t\t\t\t\t<tr>\n");
			for (int col = 0; col < 7; col++) {
				String cellId = "cell_" + row + "_" + col;
				tableBuilder.append("\t\t\t\t\t\t<td id=\"")
							.append(cellId)
							.append("\" class=\"hidden\" ")
							.append("onclick=\"handleLeftClick('")
							.append(cellId)
							.append("')\" ")
							.append("oncontextmenu=\"handleRightClick(event, '")
							.append(cellId)
							.append("')\"></td>\n");
			}
			tableBuilder.append("\t\t\t\t\t</tr>\n");
		}
	
		// Fin de la table
		tableBuilder.append("\t\t\t\t</table>\n");
	
		return tableBuilder.toString();
	}

	public String getScript() {
        return "        <script type=\"text/javascript\">\n"
            + "            function handleLeftClick(cellID) {\n"
            + "                sendClickToServer(cellID, \"TRY\");\n"
            + "            }\n"
            + "            function handleRightClick(event, cellID) {\n"
            + "                event.preventDefault();\n"
            + "                sendClickToServer(cellID, \"FLAG\");\n"
            + "            }\n"
            + "            function sendClickToServer(cellId, actionType) {\n"
            + "                var data = { cellId: cellId, actionType: actionType };\n"
            + "                var xhr = new XMLHttpRequest();\n"
            + "                xhr.open(\"POST\", \"/play.html\", true);\n"
            + "                xhr.setRequestHeader(\"Content-Type\", \"application/json\");\n"
            + "                xhr.onload = function () {\n"
            + "                    if (xhr.status === 200) {\n"
            + "                        console.log('Server response:', xhr.responseText);\n"
            + "                    } else {\n"
            + "                        console.log('Error sending data to server');\n"
            + "                    }\n"
            + "                };\n"
            + "                xhr.send(JSON.stringify(data));\n"
            + "            }\n"
            + "        </script>\n";
    }

	

	//ancine code !!!!!!!!!
    public String getTile(Session session){
        List<char[]> state = new ArrayList<>();
		state = session.getGameState();
        int rows = (int)Math.sqrt(state.size());
        String htmlcode = "\t\t\t<table class = 'minesweeper>\n";

		String flagBase64 = convertImageToBase64("bomb.png");
		String bombBase64 = convertImageToBase64("flag.png");

        for(int i = 0; i < rows; i++){
            htmlcode += "\t\t\t\t<tr>\n";

            for(int j = 0; j < rows; j++){
                char[] cell = state.get(i * rows + j); // Accéder à la cellule (ligne * taille + colonne)
                char value = cell[0]; // Valeur de la cellule (B, 0-8, ou espace)
                char status = cell[1]; // État de la cellule (H, F, S)
                String cellId = "cell_" + i + "_" + j; // ID unique pour chaque cellule

                if(status == 'H'){
                    htmlcode += "\t\t\t\t\t<td id = '" + cellId + "' class = 'hidden' onclick = 'handleLeftClick(\"" + cellId + "\")' oncontextmenu = 'handleRightClick(event, \"" + cellId + "\")'></td>\n";
                }else if(status == 'F'){
                    htmlcode += "\t\t\t\t\t<td id ='" + cellId + "' class = 'flag' onclick = 'handleLeftClick(\"" + cellId + "\")' oncontextmenu = 'handleRightClick(event, \"" + cellId + "\")'><img src = '" + flagBase64 + "'/></td>\n";
                }else if(status == 'S'){
                    if(value == 'B'){
                        htmlcode += "\t\t\t\t\t<td id ='" + cellId +"' class = 'mine' onclick = 'handleLeftClick(\"" + cellId + "\")' oncontextmenu = 'handleRightClick(event, \"" + cellId + "\")'><img src = '" + bombBase64 + "'/></td>\n";
                    }else{
                        String display = (value == ' ') ? "" : String.valueOf(value);
                        htmlcode += "\t\t\t\t\t<td id = '" + cellId + "' class = 'revealed'>" + display + "</td>\n";
                    }
                }
            }
            htmlcode += "\t\t\t\t</tr>\n";
        }
        htmlcode += "\t\t\t</table>\n";
		return htmlcode;
    }

	

    public String getTile(){
        int rows = 7;
        int cols = 7;
        htmlcode = "\t\t\t<table class = 'minesweeper'>\n";

        for(int i = 0; i < rows; i++){
            htmlcode += "\t\t\t\t<tr>\n";

            for(int j = 0; j < cols; j++){
                String cellId = "cell_" + i + "_" + j;

                htmlcode += "\t\t\t\t\t<td id ='" + cellId + "' class = 'hidden'></td>\n";
            }
            htmlcode += "\t\t\t\t</tr>\n";
        }
        htmlcode += "\t\t\t</table>\n";

		return htmlcode;
    }

    public String getFooter(){
        return "<script type='text/javascript'>" 
        + getFile("play.js") 
        + "</script>"
        + "</html>" ;
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

    public byte[] compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}

    private void chunckEncoding(String htmlCode, PrintWriter serverWriter) {
    // Ajouter les en-têtes HTTP
    serverWriter.print("HTTP/1.1 200 OK\r\n");
    serverWriter.print("Content-Type: text/html; charset=UTF-8\r\n" );
    serverWriter.print("Transfer-Encoding: chunked\r\n\r\n");
    serverWriter.flush(); // Envoyer les en-têtes immédiatement

    int maxChunkSize = 128;

    // Envoyer les chunks de données
    while (htmlCode.length() > maxChunkSize) {
        String chunk = htmlCode.substring(0, maxChunkSize);
        htmlCode = htmlCode.substring(maxChunkSize);

        // Taille du chunk + contenu + saut de ligne
        serverWriter.print(Integer.toHexString(chunk.length()) + "\r\n");
        serverWriter.print(chunk + "\r\n");
        serverWriter.flush(); // Forcer l'envoi immédiat
    }

    // Envoyer le dernier chunk (le reste des données)
    if (!htmlCode.isEmpty()) {
        serverWriter.print(Integer.toHexString(htmlCode.length()) + "\r\n");
        serverWriter.print(htmlCode + "\r\n");
        serverWriter.flush();
    }

    // Terminer avec un chunk vide
    serverWriter.print("0\r\n\r\n");
    serverWriter.flush();
}

	

	//Chunk encode the byteArray and send it into the output stream
	private void chunkEncodingGZIP(byte[] byteArray, PrintWriter writer) throws IOException {
		byte[] tmpByte = new byte[128];
		int maxChunkSize = 128;
		int i = 0;
	
		// Écrire les chunks complets
		while (byteArray.length - i * 128 > maxChunkSize) {
			// Taille du chunk en hexadécimal suivie de \r\n
			writer.print(Integer.toHexString(128) + "\r\n");
			writer.flush();
	
			// Extraire le chunk actuel
			System.arraycopy(byteArray, i * 128, tmpByte, 0, 128);
	
			// Écrire le chunk
			writer.write(new String(tmpByte, 0, 128));
			writer.print("\r\n");
			writer.flush();
			++i;
		}
	
		// Écrire le dernier chunk (moins de 128 octets)
		int remainingBytes = byteArray.length - i * 128;
		if (remainingBytes > 0) {
			writer.print(Integer.toHexString(remainingBytes) + "\r\n");
			writer.flush();
	
			System.arraycopy(byteArray, i * 128, tmpByte, 0, remainingBytes);
	
			writer.write(new String(tmpByte, 0, remainingBytes));
			writer.print("\r\n");
			writer.flush();
		}
	
		// Terminer avec un chunk de taille 0
		writer.print("0\r\n\r\n");
		writer.flush();
	}

	public static String convertImageToBase64(String imagePath) {
        try {
            // Charger l'image à partir du fichier
            File file = new File(imagePath);
            BufferedImage image = ImageIO.read(file);
            
            // Convertir l'image en un flux de bytes
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);  // Assurez-vous que l'image est en PNG
            byte[] imageBytes = os.toByteArray();
            
            // Convertir le tableau de bytes en base64
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}