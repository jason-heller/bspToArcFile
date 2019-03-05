package heller;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EnvFileAppender {

	public static void append(String f) {
		Path path = Paths.get(f.substring(0, f.lastIndexOf('\\') + 1) + "arc_data_for_env.txt");
		float ax = 0, ay = 0, az = 0;
		try {
			List<String> lines = Files.readAllLines(path);
			System.out.println("Grabbing data..");

			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.length() > 0 && line.charAt(0) == '*')
					continue;
				if (line.contains("arc_offset")) {
					String[] data = line.replaceAll(" ", "").split("=")[1].split(",");
					ax = Float.parseFloat(data[0]);
					ay = Float.parseFloat(data[1]);
					az = Float.parseFloat(data[2]);
				}
			}
			System.out.println("Appending..");
			try (DataOutputStream o = new DataOutputStream(new FileOutputStream(f, true))) {
				System.out.println("Writing arc offset");
				o.writeFloat(ax);
				o.writeFloat(ay);
				o.writeFloat(az);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done!");
	}

	public static byte buildByte(boolean... data) {
		String str = "00000000";
		char[] chars = str.toCharArray();
		for (int i = 0; i < data.length; i++) {
			chars[7 - i] = (data[i] ? '1' : '0');
		}
		return Byte.parseByte(String.valueOf(chars), 2);
	}

	public static byte writeByte(int... data) {
		String str = "00000000";
		char[] chars = str.toCharArray();
		for (int i = 0; i < data.length; i++) {
			chars[7 - i] = ((data[i] == 1) ? '1' : '0');
		}
		return Byte.parseByte(String.valueOf(chars), 2);
	}
}
