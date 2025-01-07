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
    
    //generate the html code 
    public void generateHTML(OutputStream serverWriter, Session session, boolean NewSession) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;
		String username = session.getPlayerName();
		System.out.println("debut génération");

		
			System.out.println("debut generation");
			htmlCode = getHeaders() + getBody(username,session);
			if(NewSession)
				sendContentWithContentLength(serverWriter, htmlCode,session.getSessionId());
			else
				sendContentWithContentLength(serverWriter, htmlCode,"");
			System.out.println("fin envoie");

	}

	private void sendContentWithContentLength(OutputStream outputStream, String content, String sessionId) throws IOException {
		byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
		String headers;
		if(sessionId == ""){
			headers = "HTTP/1.1 200 OK\r\n" +
							"Content-Type: text/html; charset=UTF-8\r\n" +
							"Content-Length: " + contentBytes.length + "\r\n" +
							"Connection: close\r\n\r\n";
		}
		else
		{
			 headers = "HTTP/1.1 200 OK\r\n" +
							"Content-Type: text/html; charset=UTF-8\r\n" +
							"Set-Cookie: SESSID=" + sessionId + "; Path=/; Max-Age=600\r\n" +
							"Content-Length: " + contentBytes.length + "\r\n" +
							"Connection: close\r\n\r\n";
		}
	
		outputStream.write(headers.getBytes(StandardCharsets.UTF_8));
		outputStream.write(contentBytes);
		outputStream.flush();
	}
	
	

	private void sendChunkedResponse(OutputStream outputStream, String content, String sessionID) throws IOException {
		String headers;
		if(sessionID == ""){
			headers = "HTTP/1.1 200 OK\r\n" +
							"Content-Type: text/html; charset=UTF-8\r\n" +
							"Transfer-Encoding: chunked\r\n" +
							"Connection: close\r\n\r\n";
		}
		else
		{
			 headers = "HTTP/1.1 200 OK\r\n" +
							"Content-Type: text/html; charset=UTF-8\r\n" +
							"Set-Cookie: SESSID=" + sessionID + "; Path=/; Max-Age=600\r\n" +
							"Transfer-Encoding: chunked\r\n" +
							"Connection: close\r\n\r\n";
		}
		// Envoyer les en-têtes
		outputStream.write(headers.getBytes(StandardCharsets.UTF_8));
		
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


	public String getBody(String username, Session session) {
		return "	<body>\n" +
			   "		<h1 style=\"text-align: center;\">Minesweeper " + username + " </h1>\n" +
			   "		<div id=\"username-form\">\n" +
			   "			<form action=\"/play.html/setUsername\" method=\"POST\">\n" +
			   "				<label for=\"username\">Enter your username:</label>\n" +
			   "				<input type=\"text\" id=\"username\" name=\"username\" placeholder=\"Your name\" required>\n" +
			   "				<button type=\"submit\">Update Username</button>\n" +
			   "			</form>\n" +
			   "		</div>\n" +
			   "		<div id=\"playerName\">\n" +
			   generateTable(session) +
			   "		</div>\n" +
			   getScript() +
			   getNoScript() +
			   "	</body>\n" +
			   "</html>\r\n\r\n";
	}
	

	public String generateTable(Session session) {
		StringBuilder tableHtml = new StringBuilder();
		tableHtml.append("        <table class=\"minesweeper\">\n");
	
		// Récupérer l'état du jeu de la session
		List<char[]> gameState = session.getGameState(); // Supposons que chaque cellule est un tableau [valeur, statut]
		int rows = 7; // Taille de la grille
		int cols = 7;
	
		for (int row = 0; row < rows; row++) {
			tableHtml.append("            <tr>\n");
			for (int col = 0; col < cols; col++) {
				// Récupérer la cellule actuelle
				char[] cell = gameState.get(row * cols + col); // Supposons que l'état est stocké linéairement
				char value = cell[0]; // Valeur : 'B', '1'-'8', ou espace
				char status = cell[1]; // Statut : 'H', 'S', ou 'F'
	
				// Construire la cellule HTML en fonction de l'état
				tableHtml.append("                <td data-x=\"")
						 .append(row)
						 .append("\" data-y=\"")
						 .append(col)
						 .append("\" class=\"");
	
				if (status == 'H') { // Hidden
					tableHtml.append("hidden");
				} else if (status == 'F') { // Flagged
					tableHtml.append("flag\" style=\"background-image: url('data:image/png;base64,")
							 .append(convertImageToBase64("flag.png"))
							 .append("'); background-size: cover;");
				} else if (status == 'S') { // Revealed
					if (value == 'B') { // Bomb
						tableHtml.append("mine\" style=\"background-image: url('data:image/png;base64,")
								 .append(convertImageToBase64("bomb.png"))
								 .append("'); background-size: cover;");
					} else { // Number or empty
						tableHtml.append("revealed");
					}
				}
	
				tableHtml.append("\" onclick=\"handleLeftClick('")
						 .append(row).append("_").append(col)
						 .append("')\" oncontextmenu=\"handleRightClick(event, '")
						 .append(row).append("_").append(col)
						 .append("')\">");
	
				// Ajouter le contenu de la cellule (numéro ou vide)
				if (status == 'S' && value != 'B' && value != ' ') {
					tableHtml.append(value);
				}
	
				tableHtml.append("</td>\n");
			}
			tableHtml.append("            </tr>\n");
		}
	
		tableHtml.append("        </table>\n");
		return tableHtml.toString();
	}
	
	
	
	public String getScript() {
		String bombBase64 = convertImageToBase64("bomb.png");
		String flagBase64 = convertImageToBase64("flag.png");
	
		return "        <script type=\"text/javascript\">\n"
			 + "            const bombImageBase64 = \"data:image/png;base64," + bombBase64 + "\";\n"
			 + "            const flagImageBase64 = \"data:image/png;base64," + flagBase64 + "\";\n"
			 + "            const socket = new WebSocket(\"ws://\" + window.location.host + \"/ws\");\n"
			 + "\n"
			 + "            socket.onmessage = (event) => {\n"
			 + "                console.log(\"Message received from server:\", event.data);\n"
			 + "                const message = event.data.trim();\n"
			 + "\n"
			 + "                if (message === \"GAME LOST\") {\n"
			 + "                    alert(\"You lost the game!\");\n"
			 + "                } else if (message === \"GAME WIN\") {\n"
			 + "                    alert(\"You won the game!\");\n"
			 + "                } else {\n"
			 + "                    // Update the grid\n"
			 + "                    const rows = message.split(\"\\n\");\n"
			 + "                    rows.forEach((row, x) => {\n"
			 + "                        row.split(\"\").forEach((cellContent, y) => {\n"
			 + "                            const cell = document.querySelector(`[data-x='${x}'][data-y='${y}']`);\n"
			 + "                            if (cell) {\n"
			 + "                                if (cellContent === \"#\") {\n"
			 + "                                    cell.className = \"hidden\";\n"
			 + "                                    cell.style.backgroundImage = \"\";\n"
			 + "                                    cell.textContent = \"\";\n"
			 + "                                } else if (cellContent === \"F\") {\n"
			 + "                                    cell.className = \"flag\";\n"
			 + "                                    cell.style.backgroundImage = `url(${flagImageBase64})`;\n"
			 + "                                    cell.style.backgroundSize = \"cover\";\n"
			 + "                                    cell.textContent = \"\";\n"
			 + "                                } else if (cellContent === \"B\") {\n"
			 + "                                    cell.className = \"mine\";\n"
			 + "                                    cell.style.backgroundImage = `url(${bombImageBase64})`;\n"
			 + "                                    cell.style.backgroundSize = \"cover\";\n"
			 + "                                    cell.textContent = \"\";\n"
			 + "                                } else {\n"
			 + "                                    cell.className = \"revealed\";\n"
			 + "                                    cell.textContent = cellContent;\n"
			 + "                                    cell.style.backgroundImage = \"\";\n"
			 + "                                }\n"
			 + "                            } else {\n"
			 + "                                console.warn(`Cell not found for x=${x}, y=${y}`);\n"
			 + "                            }\n"
			 + "                        });\n"
			 + "                    });\n"
			 + "                }\n"
			 + "            };\n"
			 + "\n"
			 + "            socket.onopen = () => {\n"
			 + "                console.log(\"WebSocket connected.\");\n"
			 + "            };\n"
			 + "\n"
			 + "            socket.onerror = (error) => {\n"
			 + "                console.error(\"WebSocket error:\", error);\n"
			 + "            };\n"
			 + "\n"
			 + "            socket.onclose = () => {\n"
			 + "                console.warn(\"WebSocket connection closed.\");\n"
			 + "            };\n"
			 + "\n"
			 + "            function handleLeftClick(cellID) {\n"
			 + "                if (socket.readyState === WebSocket.OPEN) {\n"
			 + "                    const [x, y] = cellID.split(\"_\").map(Number);\n"
			 + "                    console.log(`Sending TRY for x=${x}, y=${y}`);\n"
			 + "                    socket.send(`TRY:${x},${y}`);\n"
			 + "                } else {\n"
			 + "                    console.error(\"WebSocket is not open. Cannot send TRY message.\");\n"
			 + "                }\n"
			 + "            }\n"
			 + "\n"
			 + "            function handleRightClick(event, cellID) {\n"
			 + "                event.preventDefault();\n"
			 + "                if (socket.readyState === WebSocket.OPEN) {\n"
			 + "                    const [x, y] = cellID.split(\"_\").map(Number);\n"
			 + "                    console.log(`Sending FLAG for x=${x}, y=${y}`);\n"
			 + "                    socket.send(`FLAG:${x},${y}`);\n"
			 + "                } else {\n"
			 + "                    console.error(\"WebSocket is not open. Cannot send FLAG message.\");\n"
			 + "                }\n"
			 + "            }\n"
			 + "        </script>\n";
	}
	
	
	public String getNoScript() {
		return "        <noscript>\n"
			 + "            <div id=\"game-action-container\" style=\"display: flex; justify-content: center; align-items: center; height: 100vh;\">\n"
			 + "                <form id=\"game-action-form\" method=\"POST\" action=\"/play.html\">\n"
			 + "                    <label for=\"action\">Action:</label>\n"
			 + "                    <select name=\"action\" id=\"action\">\n"
			 + "                        <option value=\"reveal\">Reveal</option>\n"
			 + "                        <option value=\"flag\">Flag</option>\n"
			 + "                    </select>\n"
			 + "                    <br/>\n"
			 + "                    <label for=\"row\">Row:</label>\n"
			 + "                    <input type=\"number\" name=\"row\" id=\"row\" min=\"0\" max=\"6\" required>\n"
			 + "                    <br/>\n"
			 + "                    <label for=\"col\">Column:</label>\n"
			 + "                    <input type=\"number\" name=\"col\" id=\"col\" min=\"0\" max=\"6\" required>\n"
			 + "                    <br/>\n"
			 + "                    <button type=\"submit\">Submit</button>\n"
			 + "                </form>\n"
			 + "            </div>\n"
			 + "        </noscript>\n";
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