package heller;

import java.util.HashMap;
import java.util.Map;

import info.ata4.bsplib.vector.Vector3f;

public class ArcEntity {

	public String className;

	public Map<String, String> otherData = new HashMap<String, String>();
	
	public void add(String key, String val) {
		otherData.put(key,val);
	}
	
	public void add(String key, Vector3f val) {
		otherData.put(key,val.x+","+val.y+","+val.z);
	}
}
