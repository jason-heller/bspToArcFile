package heller;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DEdge;
import info.ata4.bsplib.struct.DFace;
import info.ata4.bsplib.struct.DLeaf;
import info.ata4.bsplib.struct.DNode;
import info.ata4.bsplib.struct.DPlane;
import info.ata4.bsplib.struct.DVertex;
import info.ata4.bsplib.vector.Vector3f;

public class MapFileBuilder  {

	public static final float SCALE_DOWN = 6f;
	public static int VERSION_IDENTIFIER = 2;
	public static int GAME_ID = -1;
	public static int MAP_VERSION = -1;
	public static String MAP_NAME = "";
	DataOutputStream dos;
	
	public MapFileBuilder(String filename, String extension) {
		String outFilename = filename.substring(0, filename.lastIndexOf('\\')+1) + MAP_NAME + extension;
		System.out.println("Exporting to "+outFilename);
		try {
			dos = new DataOutputStream(new FileOutputStream(outFilename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void writePlane(DPlane plane) throws IOException {
		writeVec3(plane.normal);
		writeFloat(plane.dist/SCALE_DOWN);
	}

	private void writeVec3(Vector3f v) throws IOException {
		writeFloat(v.x);
		writeFloat(v.z);
		writeFloat(v.y);
	}
	
	private void writeVec3s(short[] s) throws IOException {
		writeShort((short) (s[0]/SCALE_DOWN));
		writeShort((short) (s[2]/SCALE_DOWN));
		writeShort((short) (s[1]/SCALE_DOWN));
	}

	public void writeVertex(DVertex vertex) throws IOException {
		writeVec3(Vector3f.div(vertex.point, SCALE_DOWN));
	}

	public void writeEdge(DEdge edge) throws IOException {
		writeInt(edge.v[0]);
		writeInt(edge.v[1]);
	}
	
	public void writeSurfaceEdge(Integer edge) throws IOException {
		writeInt(edge);
	}

	public void writeFace(DFace face) throws IOException {
		writeByte(face.onnode);
		writeShort(face.pnum); // id of plane
		writeInt(face.fstedge);
		writeShort(face.numedge);
		writeShort(face.texinfo);
		writeInt(face.lightofs); // index in lightmap arr
		writeInt(face.lightmapTextureMinsInLuxels[0]);
		writeInt(face.lightmapTextureMinsInLuxels[1]);
		writeInt(face.lightmapTextureSizeInLuxels[0]);
		writeInt(face.lightmapTextureSizeInLuxels[1]);
		writeByte(face.styles[0]);
		writeByte(face.styles[1]);
		writeByte(face.styles[2]);
		writeByte(face.styles[3]);
	}

	public void writeNode(DNode node) throws IOException {
		writeInt(node.planenum);
		writeInt(node.children[0]);
		writeInt(node.children[1]);
		writeVec3s(node.mins);
		writeVec3s(node.maxs);
		writeShort(node.fstface); // index into leaffaces
		writeShort(node.numface);
	}

	public void writeLeaf(DLeaf leaf) throws IOException {
		writeShort(leaf.cluster);
		writeVec3s(leaf.mins);
		writeVec3s(leaf.maxs);
		writeShort(leaf.fstleafface); // index into leaffaces
		writeShort(leaf.numleafface);
		writeShort(leaf.leafWaterDataID);
	}

	public void writeVis(int numClusters, int[][] ptrs, byte[] vis) throws IOException {
		writeInt(numClusters);
		for(int[] ptr : ptrs) {
			writeInt(ptr[0]);
			writeInt(ptr[1]);
		}
		
		writeInt(vis.length);
		for(int i = 0; i < vis.length; ++i) {
			writeByte(vis[i]);
		}
	}

	public void finish() {
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeShort(int s) throws IOException {
		dos.writeShort(s);
	}
	
	public void writeInt(int i) throws IOException {
		dos.writeInt(i);
	}
	
	public void writeByte(int i) throws IOException {
		dos.writeByte(i);
	}
	
	public void writeFloat(float f) throws IOException {
		dos.writeFloat(f);
	}

	public void header() throws IOException {
		dos.writeByte('A');
		dos.writeByte('R');
		dos.writeByte('C');
		dos.writeByte(VERSION_IDENTIFIER);
		dos.writeByte(GAME_ID);
		dos.writeByte(MAP_VERSION);
		dos.writeByte(MAP_NAME.length());
		dos.writeChars(MAP_NAME);
	}

	public void writeChars(String s) throws IOException {
		dos.writeChars(s);
	}

	public void writeEntity(ArcEntity e) throws IOException {
		dos.writeByte(e.className.length());
		dos.writeChars(e.className);
		dos.writeByte(e.otherData.keySet().size());
		
		for(String key : e.otherData.keySet()) {
			dos.writeByte(key.length());
			dos.writeChars(key);
			String val = e.otherData.get(key);
			if (val != null) {
				dos.writeByte(val.length());
				dos.writeChars(val);
			} else {
				dos.writeByte(0);
			}
		}
	}

	public void writeBrushSide(DBrushSide bSide) throws IOException {
		dos.writeShort(bSide.pnum);
		dos.writeShort(bSide.texinfo);
	}

	public void writeBrush(int i, int fstside, int numside) throws IOException {
		dos.writeByte(i);
		dos.writeInt(fstside);
		dos.writeInt(numside);
	}

	public void writeLighting(InputStream is) throws IOException {
		// Regurgitate the lump
		byte[] regurgitateBytes = new byte[is.available()];
		dos.writeInt(regurgitateBytes.length);
		dos.write(regurgitateBytes);
	}

	
}
