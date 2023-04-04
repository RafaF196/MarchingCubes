package main;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRender.TextMaster;
import marchingCubes.VolumeModel;
import models.RawModel;
import render.DisplayManager;
import render.Loader;
import render.MasterRenderer;

public class MainTest {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		MasterRenderer renderer = new MasterRenderer(loader);
		TextMaster.init(loader);
		
		Camera camera = new Camera();
		List<Entity> entities = new ArrayList<Entity>();

		FontType font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
		GUIText thres_text = new GUIText("Threshold: 0.0", 1.8f, font, new Vector2f(0.008f, 0.008f), 1f, false);
		thres_text.setColour(1, 0, 0);
		GUIText thres2_text = new GUIText("Thres. Increment: 0.1", 1.6f, font, new Vector2f(0.008f, 0.058f), 1f, false);
		thres2_text.setColour(1, 0, 0);
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat decimalFormat = (DecimalFormat)nf;
		
        String numberAsString;
		
		Integer entityToShow = 0, numModels = 4; // 8 for all models
		Float threshold = 0.0f, thresholdIncrement = 0.1f;
		Boolean objectChangePressed = false, thresholdChangePressed = false;
		
		RawModel model = null;
		Entity entity = null;
		
		VolumeModel[] vModels = new VolumeModel[numModels];
		vModels[0] = new VolumeModel("Blooby", threshold);
		vModels[1] = new VolumeModel("connection3", threshold);
		vModels[2] = new VolumeModel("connection4", threshold);
		vModels[3] = new VolumeModel("connection5", threshold);
		
		// Commented this 4 models to improve the general performance of the application
		/*
		vModels[4] = new VolumeModel("cushin", threshold);
		vModels[5] = new VolumeModel("heart", threshold);
		vModels[6] = new VolumeModel("sphere", threshold);
		vModels[7] = new VolumeModel("tori", threshold);
		*/
		
		vModels[entityToShow].extractModel(loader);
		model = vModels[entityToShow].extractModel(loader);
		entity = new Entity(model, new Vector3f(0, 0, 0), 0, 1f);
		entities.add(entity);
		
		while(!Display.isCloseRequested()) {

			// Change object
			if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
				if (!objectChangePressed) {
					entityToShow = (entityToShow + 1) % numModels;
					
					vModels[entityToShow].actualizeVoxels(threshold);
					vModels[entityToShow].extractModel(loader);
					model = vModels[entityToShow].extractModel(loader);
					entity = new Entity(model, new Vector3f(0, 0, 0), 0, 1f);
					entities.remove(entityToShow);
					entities.add(entityToShow, entity);
					
					objectChangePressed = true;
				}
			} else {
				objectChangePressed = false;
			}
			
			// Modify the Threshold value
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				if (threshold < 500) {
					threshold += thresholdIncrement;
					
					thres_text.remove();
					numberAsString = decimalFormat.format(threshold);
					thres_text = new GUIText("Threshold: " + numberAsString, 1.8f, font, new Vector2f(0.008f, 0.008f), 1f, false);
					thres_text.setColour(1, 0, 0);
					
					// Regenerate mesh
					vModels[entityToShow].actualizeVoxels(threshold);
					model = vModels[entityToShow].extractModel(loader);
					entity = new Entity(model, new Vector3f(0, 0, 0), 0, 1f);
					entities.remove(entityToShow);
					entities.add(entityToShow, entity);
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				if (threshold > -1000) {
					threshold -= thresholdIncrement;
					
					thres_text.remove();
					numberAsString = decimalFormat.format(threshold);
					thres_text = new GUIText("Threshold: " + numberAsString, 1.8f, font, new Vector2f(0.008f, 0.008f), 1f, false);
					thres_text.setColour(1, 0, 0);
					
					// Regenerate mesh
					vModels[entityToShow].actualizeVoxels(threshold);
					model = vModels[entityToShow].extractModel(loader);
					entity = new Entity(model, new Vector3f(0, 0, 0), 0, 1f);
					entities.remove(entityToShow);
					entities.add(entityToShow, entity);
				}
			}
			
			// Modify the Threshold increment value
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				if (thresholdIncrement > 0.02f && !thresholdChangePressed) {
					thresholdIncrement /= 10;
					
					
					thres2_text.remove();
					thres2_text = new GUIText("Thres. Increment: " + thresholdIncrement, 1.6f, font,
							new Vector2f(0.008f, 0.058f), 1f, false);
					thres2_text.setColour(1, 0, 0);
					
					thresholdChangePressed = true;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				if (thresholdIncrement < 9 && !thresholdChangePressed) {
					thresholdIncrement *= 10;
					if (thresholdIncrement < 0.2) thresholdIncrement = 0.1f;
					
					thres2_text.remove();
					thres2_text = new GUIText("Thres. Increment: " + thresholdIncrement, 1.6f, font,
							new Vector2f(0.008f, 0.058f), 1f, false);
					thres2_text.setColour(1, 0, 0);
					
					thresholdChangePressed = true;
				}
			} else {
				thresholdChangePressed = false;
			}
			
			camera.move();
			renderer.renderScene(entities.subList(entityToShow, entityToShow+1), camera);
			TextMaster.render();
			DisplayManager.updateDisplay();
			
		}
		
		TextMaster.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
