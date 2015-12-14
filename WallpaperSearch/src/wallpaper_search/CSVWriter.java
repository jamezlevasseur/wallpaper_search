package wallpaper_search;

import java.io.FileWriter;
import java.io.IOException;

//NOTE: only enabled for data output
public class CSVWriter
{  
	private FileWriter writer;
	
	public CSVWriter (String filename, String Category1) throws IOException {/*
		writer = new FileWriter(filename);
		writer.append(Category1);
	    writer.append('\n');*/
	}
	
	public void write (String val1) throws IOException {/*
	    writer.append(val1);
        writer.append('\n');*/
	}
	
	public void end() throws IOException {/*
	    writer.flush();
	    writer.close();*/
	}
	
}