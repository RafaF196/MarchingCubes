package marchingCubes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import render.Loader;

public class VolumeModel {
	
	private static final String RES_LOC = "res/";
	private static final String LUT_filename = "taulaMC.txt";
	
	private Float threshold;
	private Integer gridSize;
	private ArrayList<Float> values = new ArrayList<Float>();
	private ArrayList<Voxel> voxels = new ArrayList<Voxel>();
	private ArrayList<ArrayList<Vector3f>> luTable = new ArrayList<ArrayList<Vector3f>>();
	
	// Generate a volume model from the first time
	public VolumeModel (String modelName, Float threshold) {
		this.threshold = threshold;
		
		loadLookUpTable();
		loadModel(modelName);
		generateVoxels(threshold);
	}
	
	// Recompute the mesh with a new threshold value
	public void recomputeVolumeModel(Float threshold) {
		this.threshold = threshold;
		actualizeVoxels(threshold);
	}
	
	// Loads the information of the Look-up Table file to the "luTable" field
	public void loadLookUpTable() {
		FileReader isr = null;
		File objFile = new File(RES_LOC + LUT_filename);
		try {
			isr = new FileReader(objFile);
		} catch (FileNotFoundException e) {
			System.err.println("File not found in res folder!");
			System.exit(-1);
		}
		
		BufferedReader reader = new BufferedReader(isr);
		String line;
		Integer indexLine = 0;
		String[] currentLine = null;
		Vector3f newFace = null;
		
		// Initialize the data structure
		for (int i = 0; i < 256; i++) {
			luTable.add(new ArrayList<Vector3f>());
		}
		
		try {
			line = reader.readLine();
			while (line != null) {
				currentLine = line.split(",");
				if (currentLine.length > 0) {
					for (int i = 0; i < currentLine.length; i+=3) {
						newFace = new Vector3f((float) Float.valueOf(currentLine[i]),
											   (float) Float.valueOf(currentLine[i+1]),
											   (float) Float.valueOf(currentLine[i+2]));
						luTable.get(indexLine).add(newFace);
					}
				}
				indexLine++;
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("Error reading the file");
			System.exit(-1);
		}
	}
	
	// Loads the volume model from a file containing the size of the grid and the scalar values for the vertices
	// No further computation is done here, it will only store the values on the "values" field
	public void loadModel(String modelName) {
		FileReader isr = null;
		File objFile = new File(RES_LOC + modelName + ".txt");
		try {
			isr = new FileReader(objFile);
		} catch (FileNotFoundException e) {
			System.err.println("File not found in res folder!");
			System.exit(-1);
		}
		
		BufferedReader reader = new BufferedReader(isr);
		String line;
		Float vertexValue;
		
		try {
			line = reader.readLine();
			gridSize = (Integer) Integer.valueOf(line);
			line = reader.readLine();
			while (line != null) {
				vertexValue = (Float) Float.valueOf(line);
				values.add(vertexValue);
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("Error reading the file");
			System.exit(-1);
		}
	}
	
	// Generates all the voxels of the volume model for the first time
	public void generateVoxels(Float threshold) {
		// if N is GridSize, we will have (N-1)^3 voxels
		// they will be numbered like the vertex samples (1..N-1, 1..N-1, 1..N-1)
		// Voxel (0,0,0) will have vertices (0,0,0), (0,0,1), (0,1,0), (0,1,1),
		//									(1,0,0), (1,0,1), (1,1,0), (1,1,1)
		Integer N = this.gridSize;
		for (int i = 0; i < gridSize -1; i++) {
			for (int j = 0; j < gridSize -1; j++) {
				for (int k = 0; k < gridSize -1; k++) {
					Vertex[] voxelVertices = getVerticesForVoxel(i*N*N + j*N + k);
					Voxel newVoxel = new Voxel(i,j,k,threshold,gridSize,voxelVertices);
					voxels.add(newVoxel);
				}
			}
		}
	}
	
	// Recalculate some properties of the voxels with the new threshold
	public void actualizeVoxels(Float threshold) {
		for (int i = 0; i < voxels.size(); i++) {
			voxels.get(i).recomputeNewThreshold(threshold);
		}
	}
	
	// Given the index of a voxel, returns the 8 vertices (their values) that form it
	public Vertex[] getVerticesForVoxel(Integer index) {
		Integer N = this.gridSize;
		Vertex[] voxelVertices = new Vertex[8];
		voxelVertices[0] = new Vertex(values.get(index + N*N), threshold);
		voxelVertices[1] = new Vertex(values.get(index + N*N + 1), threshold);
		voxelVertices[2] = new Vertex(values.get(index + N*N + N), threshold);
		voxelVertices[3] = new Vertex(values.get(index + N*N + N + 1), threshold);
		
		voxelVertices[4] = new Vertex(values.get(index), threshold);
		voxelVertices[5] = new Vertex(values.get(index + 1), threshold);
		voxelVertices[6] = new Vertex(values.get(index + N), threshold);
		voxelVertices[7] = new Vertex(values.get(index + N + 1), threshold);
		
		return voxelVertices;
	}
	
	// Gathers the new vertices computed from each of the voxels, and together with the look-up table
	// previously loaded, it will generate the faces(triangles) with their positions and normals.
	public RawModel extractModel(Loader loader) {
		Integer globalIndex = 0; // Amount of vertices already processed

		ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		Vector3f newI = null, normalFace = null, p1 = null, p2 = null;
		Integer index1, index2, index3;
		
		for (int i = 0; i < voxels.size(); i++) {
			if (voxels.get(i).hasNewVertices()) {
				Voxel thisVoxel = voxels.get(i);
				
				Vector3f[] newVertices = thisVoxel.getNewVertices();
				ArrayList<Vector3f> newIndices = luTable.get(thisVoxel.getMask());
				
				for (int j = 0; j < newIndices.size(); j++) {
					newI = newIndices.get(j);
					index1 = (int) newI.x;
					index2 = (int) newI.y;
					index3 = (int) newI.z;
					indices.add(globalIndex);
					indices.add(globalIndex+1);
					indices.add(globalIndex+2);
					vertices.add(newVertices[index1]);
					vertices.add(newVertices[index2]);
					vertices.add(newVertices[index3]);
					
					normalFace = new Vector3f();
					p1 = new Vector3f(newVertices[index2].x - newVertices[index1].x,
							newVertices[index2].y - newVertices[index1].y, newVertices[index2].z - newVertices[index1].z);
					p2 = new Vector3f(newVertices[index3].x - newVertices[index1].x,
							newVertices[index3].y - newVertices[index1].y, newVertices[index3].z - newVertices[index1].z);
					Vector3f.cross(p1, p2, normalFace);
					normals.add(normalFace);
					normals.add(normalFace);
					normals.add(normalFace);
					
					globalIndex += 3;
				}
				
			}
			
		}
		
		// Convert data to arrays
		float[] verticesArray = new float[vertices.size() * 3];
		float[] normalsArray = new float[vertices.size() * 3];
		int[] indicesArray = new int[indices.size()];
		
		for (int i = 0; i < vertices.size(); i++) {
			verticesArray[3*i] = vertices.get(i).x;
			verticesArray[3*i+1] = vertices.get(i).y;
			verticesArray[3*i+2] = vertices.get(i).z;
			normalsArray[3*i] = normals.get(i).x;
			normalsArray[3*i+1] = normals.get(i).y;
			normalsArray[3*i+2] = normals.get(i).z;
		}
		
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		
		return loader.loadToVAO(verticesArray, normalsArray, indicesArray);
		
	}

}
