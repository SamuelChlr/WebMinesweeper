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
			htmlCode = getHeaders() + getTile() + getForm() + getFooter();
			byteArray = compress(htmlCode);
			chunckEncoding(byteArray,os);
			os.flush();
		}
		else
		{
			htmlCode = getHeaders() + getTile() + getForm() + getFooter();
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
		}
		
		

	} 
    //generate the html code for a post method
    public void generateHTML(OutputStream os, Game currentGame, boolean acceptGZIP) throws IOException
	{
		byte byteArray[];
		String htmlCode,tmp;
		
		PrintWriter serverWriter = new PrintWriter(os);

		if(acceptGZIP)
		{
			htmlCode = getHeaders() + getTile(currentGame) + getForm() + getFooter(currentGame);
			byteArray = compress(htmlCode);
			chunckEncoding(byteArray,os);
			os.flush();
		}
		else
		{
			htmlCode = getHeaders() + getTile(currentGame) + getForm() + getFooter(currentGame);
			chunckEncoding(htmlCode,serverWriter);
			serverWriter.flush();
		}
		
		
	}

    public String getHeaders()
	{
		return "<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<meta charset='utf-8'>\n\t\t<title>Mastermind Game</title>\n\t</head>\n<style>" + getFile("style.css")+"</style>\n\t<body>\n\t\t<h1>Mastermind Game</h1>\n\t\t<div class='box' style='display: flex; justify-content: center; align-items: center;'>\n";
	}
}