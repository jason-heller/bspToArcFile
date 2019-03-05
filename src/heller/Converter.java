package heller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.compress.utils.IOUtils;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.io.EntityInputStream;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.BrushFlag;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DEdge;
import info.ata4.bsplib.struct.DFace;
import info.ata4.bsplib.struct.DLeaf;
import info.ata4.bsplib.struct.DModel;
import info.ata4.bsplib.struct.DNode;
import info.ata4.bsplib.struct.DPlane;
import info.ata4.bsplib.struct.DTexData;
import info.ata4.bsplib.struct.DTexInfo;
import info.ata4.bsplib.struct.DVertex;
import info.ata4.bsplib.vector.Vector3f;

public class Converter {
	public static final String EXTENSION = ".arc";
	//private static Logger logger = Logger.getLogger(Converter.class.getName());
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("-f")) {
			if (args[1].equals("") || args.length < 2) {
				System.err.println("Error: No file");
				System.exit(-1);
			}
			
			if (args[1].contains(".env")) {
				EnvFileAppender.append(args[1]);
				System.exit(0);
			}
			
			try {
			if (args.length >= 2) MapFileBuilder.GAME_ID = Byte.parseByte(args[2]);
			if (args.length >= 3) MapFileBuilder.MAP_VERSION = Byte.parseByte(args[3]);
			if (args.length >= 4) MapFileBuilder.MAP_NAME = args[4];
			} catch(NumberFormatException e) {
				System.err.println("Error: One of the argument passed is not the correct type");
				System.exit(-1);
			}
			
			try {
				Scanner scanner = new Scanner( System.in );
				if (MapFileBuilder.GAME_ID == -1) {
					System.out.print("Enter game ID: ");
					MapFileBuilder.GAME_ID = scanner.nextByte();
				}
				if (MapFileBuilder.MAP_NAME == "") {
					System.out.print("\nEnter map name: ");
					MapFileBuilder.MAP_NAME = scanner.next();
				}
				if (MapFileBuilder.MAP_VERSION == -1) {
					System.out.print("Enter map version (as a byte) ");
					MapFileBuilder.MAP_VERSION = scanner.nextByte();
				}
				scanner.close();
			} catch (InputMismatchException e) {
				System.err.println("Error: Incorrect input");
				System.exit(-1);
				
			} catch (Exception e) {
				System.err.println("Error: Scanner error");
				System.exit(-1);
			}
			
			TextureRenamer.load(args[1]);
			
			System.out.println();
			convert(args[1]);
		}
	}
	
	private static void convert(String filename) {
		try {
			System.out.println("Beginning conversion");
			// https://developer.valvesoftware.com/wiki/Source_BSP_File_Format
			MapFileBuilder builder = new MapFileBuilder(filename, EXTENSION);
			
			System.out.println("Reading bsp");
			BspFile bspFile = new BspFile(new File(filename));
			// get the BSP reader
			BspFileReader bspReader = bspFile.getReader();
			
			System.out.println("Writing header");
			EntityInputStream eis = new EntityInputStream(bspFile.getLump(LumpType.LUMP_ENTITIES).getInputStream());
			try {
				Entity worldspawn = eis.readEntity();
				
				String skybox = worldspawn.getValue("skyname");
			} finally {
				IOUtils.closeQuietly(eis);
			}
			builder.header();

			// Planes
			System.out.println("Exporting planes");
			bspReader.loadPlanes();
			List<DPlane> planes = bspReader.getData().planes;
			builder.writeInt(planes.size());
			for(DPlane plane : planes) {
				builder.writePlane(plane);
			}
			
			// Vertices
			System.out.println("Exporting vertices");
			bspReader.loadVertices();
			List<DVertex> vertices = bspReader.getData().verts;
			builder.writeInt(vertices.size());
			for(DVertex vertex : vertices) {
				builder.writeVertex(vertex);
			}
			
			// Edges
			System.out.println("Exporting edges");
			bspReader.loadEdges();
			List<DEdge> edges = bspReader.getData().edges;
			builder.writeInt(edges.size());
			for(DEdge edge : edges) {
				builder.writeEdge(edge);
			}
			
			// Surface Edges
			System.out.println("Exporting surface edges");
			bspReader.loadSurfaceEdges();
			List<Integer> surfEdges = bspReader.getData().surfEdges;
			builder.writeInt(surfEdges.size());
			for(Integer edge : surfEdges) {
				builder.writeSurfaceEdge(edge);
			}
			
			// Faces
			System.out.println("Exporting faces");
			bspReader.loadFaces();
			List<DFace> faces = bspReader.getData().faces;
			builder.writeInt(faces.size());
			for(DFace face : faces) {
				builder.writeFace(face);
			}
			
			// Leaves & Nodes (BSP)
			System.out.println("Exporting leafs & nodes");
			bspReader.loadLeaves();
			bspReader.loadNodes();
			bspReader.loadLeafFaces();
			builder.writeInt(bspReader.getData().leafFaces.size());
			for(int i : bspReader.getData().leafFaces) {
				builder.writeShort(i);
			}
			
			List<DNode> nodes = bspReader.getData().nodes;
			List<DLeaf> leaves = bspReader.getData().leaves;
			builder.writeInt(nodes.size());
			for(DNode node : nodes) {
				builder.writeNode(node);
			}
			
			builder.writeInt(leaves.size());
			for(DLeaf leaf : leaves) {
				builder.writeLeaf(leaf);
			}
			
			// Leaffaces
			System.out.println("Exporting leaf-faces");
			bspReader.loadLeafFaces();
			List<Integer> leafFaces = bspReader.getData().leafFaces;
			builder.writeInt(leafFaces.size());
			for(int face : leafFaces) {
				builder.writeShort(face);
			}
			
			// Triggers
			//System.out.println("Exporting models (triggers)");
			bspReader.loadModels();
			List<DModel> models = bspReader.getData().models;
			
			// Entities
			bspReader.loadEntities();
			System.out.println("Exporting entities");
			List<Entity> entities = bspReader.getData().entities;
			List<ArcEntity> outEntities = new ArrayList<ArcEntity>();
			for(Entity entity : entities) {
				ArcEntity e = new ArcEntity();
				if (entity.getClassName().equals("info_player_start") || entity.getClassName().equals("info_player_teamspawn")) {
					e.className = "spawn";
					e.add("pos", yUpSpace(entity.getOrigin(),true));
					e.add("rot", yUpSpace(entity.getAngles()));
					e.add("label", entity.getTargetName());
					outEntities.add(e);
				} else if (entity.getClassName().equals("env_sun")) {
					e.className = "sun";
					String[] rotation = entity.getValue("angles").split(" ");
					e.add("yaw", rotation[0]);
					e.add("pitch", rotation[1]);
					e.add("rgb", entity.getValue("rendercolor"));
					outEntities.add(e);
				} else if (entity.getClassName().equals("env_fire")) {
					e.className = "part_emitter";
					e.add("pos", yUpSpace(entity.getOrigin(),true));
					//e.add("rot", yUpSpace(entity.getAngles()));
					e.add("life", entity.getValue("lifetime"));
					e.add("maxspeed", entity.getValue("maxspeed"));
					e.add("minspeed", entity.getValue("minspeed"));
					e.add("spawnrate", entity.getValue("spawnrate"));
					e.add("startsize", entity.getValue("startsize"));
					e.add("spin", entity.getValue("mindirectedspeed"));
					outEntities.add(e);
				} else if (entity.getClassName().equals("env_smoketrail")) {
					e.className = "part_emitter";
					e.add("pos", yUpSpace(entity.getOrigin(),true));
					//e.add("rot", yUpSpace(entity.getAngles()));
					e.add("life", entity.getValue("lifetime"));
					e.add("maxspeed", entity.getValue("maxspeed"));
					e.add("minspeed", entity.getValue("minspeed"));
					e.add("spawnrate", entity.getValue("spawnrate"));
					e.add("startsize", entity.getValue("startsize"));
					e.add("spin", entity.getValue("mindirectedspeed"));
					outEntities.add(e);
				} else if (entity.getClassName().equals("env_soundscape")) {
					e.className = "ambient_sfx";
					e.add("pos", yUpSpace(entity.getOrigin(),true));
					e.add("radius", ""+(Float.parseFloat(entity.getValue("radius"))/12));
					e.add("sfx", entity.getValue("soundscape"));
					e.add("playing", entity.getValue("StartDisabled").equals("0")?"1":"0");
					outEntities.add(e);
				} else if (entity.getClassName().equals("npc_template_maker")) {
					String npc = entity.getValue("TemplateName");
					if (npc.contains("!")) {
						e.className = "enemy_spawn";
						e.add("pos", yUpSpace(entity.getOrigin(),true));
						e.add("rot", yUpSpace(entity.getAngles()));
						e.add("enemy", ""+npc);
						e.add("radius", ""+(Float.parseFloat(entity.getValue("Radius"))/12));
						e.add("freq", ""+Integer.parseInt(entity.getValue("SpawnFrequency")));
						e.add("max_spawn", ""+Integer.parseInt(entity.getValue("MaxLiveChildren")));
					} else {
						e.className = "npc_spawn";
						e.add("pos", yUpSpace(entity.getOrigin(),true));
						e.add("rot", yUpSpace(entity.getAngles()));
						e.add("character", ""+npc);
						e.add("radius", ""+(Float.parseFloat(entity.getValue("Radius"))/12));
						e.add("freq", ""+Integer.parseInt(entity.getValue("SpawnFrequency")));
						e.add("max_spawn", ""+Integer.parseInt(entity.getValue("MaxLiveChildren")));
					}
					outEntities.add(e);
				} else if (entity.getClassName().equals("trigger_changelevel")) {
					e.className = "warp";
					String[] data = entity.getValue("map").split(",");
					e.add("dest_map", data[0]);
					e.add("dest_spawn", data[1]);
					e.add("style", data[2]);
					DModel model = models.get(entity.getModelNum());
					e.add("min", yUpSpace(model.mins,true));
					e.add("max", yUpSpace(model.maxs,true));
					e.add("node", ""+model.headnode);
					e.add("first_face", ""+model.fstface);
					e.add("num_faces", ""+model.numface);
					outEntities.add(e);
				}
			}
			
			builder.writeInt(outEntities.size());
			for(ArcEntity en : outEntities) {
				builder.writeEntity(en);
			}
			
			// Brushes
			System.out.println("Exporting brushsides & brushes (only clips)");
			bspReader.loadBrushSides();
			List<DBrushSide> bSides = bspReader.getData().brushSides;
			builder.writeShort(bSides.size());
			for(DBrushSide bSide : bSides) {
				builder.writeBrushSide(bSide);
			}
			
			bspReader.loadBrushes();
			List<DBrush> brushes = bspReader.getData().brushes;
			int numBrush = 0;
				for(DBrush brush : brushes) {
				if (brush.isGrate() && brush.isSolid() && brush.isOpaque())
					numBrush++;
				else if (brush.isPlayerClip())
					numBrush++;
				else if (brush.isNpcClip())
					numBrush++;
				else if (brush.isLadder())
					numBrush++;
				else if (brush.isSolid())
					numBrush++;
			}	
			builder.writeShort(numBrush);
			for(DBrush brush : brushes) {
				
				if (brush.isGrate() && brush.isSolid() && brush.isOpaque()) {
					builder.writeBrush(1, brush.fstside, brush.numside);
				}
				
				else if (brush.isPlayerClip()) {
					builder.writeBrush(2, brush.fstside, brush.numside);
				}
				
				else if (brush.isNpcClip()) {
					builder.writeBrush(3, brush.fstside, brush.numside);
				}
				
				else if (brush.isLadder()) {
					builder.writeBrush(4, brush.fstside, brush.numside);
				}
				
				else if (brush.isSolid()) {
					builder.writeBrush(6, brush.fstside, brush.numside);
				}
			}
			
			// Visibility (PVS)
			System.out.println("Exporting VIS");
			Lump lump = bspReader.getBspFile().getLump(LumpType.LUMP_VISIBILITY);

			int numBytes = lump.getLength(), bytePos = 0;
			InputStream is = null;
			try {
				is = lump.getInputStream();
	
				int numClusters = readInt(is);
				int[][] ptrs = new int[numClusters][2];
				byte[] vis;
				for(int i = 0; i < numClusters; ++i) {
					ptrs[i][0] = readInt(is);
					ptrs[i][1] = readInt(is);
				}
				bytePos += 4 + (numClusters*8);
				
				vis = new byte[numBytes-bytePos];
				for(int v = 0; v < vis.length; ++v) {
					vis[v] = (byte)is.read();
					//System.out.println(String.format("%8s", Integer.toBinaryString(vis[v] & 0xFF)).replace(' ', '0'));
				}
				
				builder.writeVis(numClusters, ptrs, vis);
				// The rest of the data is the PVS as a bitvector, 0s are run length encoded
				// every cluster points to a point inside this vector
				
				// Baked lightmap
				lump = bspReader.getBspFile().getLump(LumpType.LUMP_LIGHTING);
				
				numBytes = lump.getLength();
				bytePos = 0;
				is = lump.getInputStream();
	
				builder.writeLighting(is);
				
				// Texture data
				System.out.println("Exporting texture info & Texture data");
				bspReader.loadTexInfo();
				List<DTexInfo> texInfos = bspReader.getData().texinfos;
				bspReader.loadTexData();
				List<DTexData> texDatas = bspReader.getData().texdatas;
				
				int numTexInfos = texInfos.size();
				builder.writeInt(numTexInfos);
				for(int i = 0; i < texInfos.size(); ++i) {
					DTexInfo texInfo = texInfos.get(i);
					builder.writeInt(texDatas.get(texInfo.texdata).texname);
					float[][] texels = texInfo.textureVecsTexels;
					for(float[] f : texels) {
						builder.writeFloat(f[0]/48);// Set to 12 instead of 48 for 1:1 with hammer
						builder.writeFloat(f[2]/48);
						builder.writeFloat(f[1]/48);
						builder.writeFloat(f[3]/48);
					}
				}
				
				System.out.println("Exporting texture list");
				List<String> texNames = bspReader.getData().texnames;
				builder.writeInt(texNames.size());
				System.out.println("\n=====Textures Exported=====");
				for(String s : texNames) {
					s = TextureRenamer.parse(s);
					System.out.println(s);
					builder.writeByte(s.length());
					builder.writeChars(s);
				}
				System.out.println("===========================\n");
			}
			finally {
				System.out.println("Finshing up");
				builder.finish();
				IOUtils.closeQuietly(is);
			}
			
			bspReader.loadLeaves();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String yUpSpace(Vector3f v) {
		return yUpSpace(v,false);
	}
	
	private static String yUpSpace(Vector3f v, boolean scale) {
		if (scale) {
			return (v.x/MapFileBuilder.SCALE_DOWN)+","+(v.z/MapFileBuilder.SCALE_DOWN)+","+(v.y/MapFileBuilder.SCALE_DOWN);
		}
		
		return v.x+","+v.z+","+v.y;
	}

	private static int readInt(InputStream is) {
		byte [] arr = new byte[4];
		try {
			is.read(arr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteBuffer bb = ByteBuffer.wrap(arr);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
}
