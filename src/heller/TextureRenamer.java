package heller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureRenamer {
	private static Map<String, String> replace = new HashMap<String, String>();
	
	public static void load(String fileBeingConverted) {
		Path path = Paths.get(fileBeingConverted.substring(0, fileBeingConverted.lastIndexOf('\\')+1) + "tex_replace.txt");
        try {
        	List<String> lines = Files.readAllLines(path);
        	for(String line : lines) {
        		line = line.replaceAll(" ", "");
        		String[] data = line.split("=");
        		replace.put(data[0], data[1]);
        	}
        } catch (IOException ex) {
        	System.err.println("Problem with texture replacement file");
        }
        
        replace.put("TOOLSNODRAW", "INVIS");
        replace.put("TOOLSINVISIBLELADDER", "LADDER");
        replace.put("TOOLSTRIGGER", "TRIGGER");
	}
	
	public static String parse(String input) {
		String noPath = input.substring(input.lastIndexOf('/')+1).replaceAll(" ","");
		for(String re : replace.keySet()) {
			if (re.equals(noPath)) {
				return replace.get(noPath);
			}
		}
		return input;
	}
}
