import java.net.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;

public class MinesweeperHTML{
    private String htmlcode;

    public MinesweeperHTML(){
        htmlcode = "";
    }
    //generate the html code for a get method
    public void generateHTML(OutputStream os, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;

		PrintWriter serverWriter = new PrintWriter(os);
		
		if(acceptGZIP)
		{
			htmlCode = getHeaders() + getTile() + getFooter();
			byteArray = compress(htmlCode);
			chunckEncoding(byteArray,os);
			os.flush();
		}
		else
		{
			htmlCode = getHeaders() + getTile() + getFooter();
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
		}
		
		

	} 
    //generate the html code for a post method
    public void generateHTML(OutputStream os, Session session, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;
		
		PrintWriter serverWriter = new PrintWriter(os);

		if(acceptGZIP)
		{
			htmlCode = getHeaders() + getTile(session) + getForm() + getFooter();
			byteArray = compress(htmlCode);
			chunckEncoding(byteArray,os);
			os.flush();
		}
		else
		{
			htmlCode = getHeaders() + getTile(session) + getForm() + getFooter();
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
		}
		
		
	}

    public String getHeaders()
	{
		return "<!DOCTYPE html>\n"
            + "<html>\n"
            + "\t<head>\n"
            + "\t\t<meta charset='utf-8'>\n"
            + "\t\t<title>Minesweeper</title>\n"
            + "\t\t<style>"
            + getFile("play.css")
            + "\t\t</style>"
            + "\t</head>\n"
            + "\t<body>\n"
            + "\t\t<h1 style = 'text-align: center;'>Minesweeper</h1>\n"
            + "\t\t<div id = 'playerName'>\n";
	}

    public String getTile(Session session){
        list<char[]> state = session.getGameState()
        int rows = (int)Math.sqrt(state.size());
        htmlcode = "\t\t\t<table class = 'minesweeper>\n";

        for(int i = 0; i < rows; i++){
            htmlCode += "\t\t\t\t<tr>\n";

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
                        String display = (value == ' ') ? "" : String.getValueOf(value);
                        htmlcode += "\t\t\t\t\t<td id = '" + cellId + "' class = 'revealed'>" + display + "</td>\n";
                    }
                }
            }
            htmlcode += "\t\t\t\t</tr>\n"
        }
        htmlcode += "\t\t\t</table>\n"
    }

    public String getTile(){
        int rows = 7;
        int cols = 7;
        htmlcode = "\t\t\t<table class = 'minesweeper'>\n";

        for(int i = 0; i < rows; i++){
            htmlcode += "\t\t\t\t<tr>\n";

            for(int j = 0; j < cols; j++){
                String cellId = "cell_" + i + "_" + j;

                htmlcode += "\t\t\t\t\t<td id ='" + cellId + "' class = 'hidden'></td>\n"
            }
            htmlcode += "\t\t\t\t</tr>\n";
        }
        htmlcode += "\t\t\t</table>\n"
    }

    public String getFooter(){
        return "<script type='text/javascript'>" 
        + getFile("play.js") 
        + "</script>"
        + "</html>" 
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
}