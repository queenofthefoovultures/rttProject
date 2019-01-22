package com.indoorino.bme5;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;



public class Indoorino extends ApplicationAdapter {

		private Stage stage;

		private Button button2;


		private Environment lights;
		private PerspectiveCamera cam;

		private ModelBatch modelBatch;
		private Model model;
		private ModelInstance instance;


		private Model model2;
		private ModelInstance redBox;



		@Override
		public void create() {
			stage = new Stage(new ScreenViewport());


			// Initiate Light
			lights = new Environment();
			lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 0.4f, 0.4f, 1f));
			lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));



			// Initiate Camera
			cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			cam.position.set(10f, 10f, 10f);
			cam.lookAt(0,0,0);
			cam.near = 1f;
			cam.far = 300f;
			cam.update();

			// Initiate 3D Model Handler Batch
			modelBatch = new ModelBatch();
			ModelBuilder modelBuilder = new ModelBuilder();
			model = modelBuilder.createBox(5f, 5f, 5f,
					new Material(ColorAttribute.createDiffuse(Color.GREEN)),
					VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
			instance = new ModelInstance(model, 2,2, 2);
			instance.transform.translate(1,-2, 1);

			model2 = modelBuilder.createBox(10f, 3f, 6f, new Material(ColorAttribute.createDiffuse(Color.RED)),
					VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
			redBox = new ModelInstance(model2);

			//Nürnberg ZENTRUM vor TH BB Gebäude
			double lat = 49.448256;
			double lon = 11.095962;
			double alt = 46.87;

			CoordinateUtilities utl = new CoordinateUtilities();


			// Neue Koordinaten um Zentrum links drüber versetzt.
			// Erg: Koordinaten links oben sind: x: -7.468874241294032, y: 7.006816722382261, z: -8.217153399048271E-6
			double latp = 49.448319;
			double lonp = 11.095859;
			double altp = 46.87;
			double[] ecef = utl.geo_to_ecef(latp, lonp, altp);
			double[] enu = utl.ecef2enu(ecef[0], ecef[1], ecef[2], lat, lon, alt);
			Gdx.app.log("ENU1", "Koordinaten links oben sind: x: " + enu[0] + ", y: " + enu[1] + ", z: " + enu[2]);

			// Neue Koordinaten um Zentrum rechts darunter versetzt.
			// Erg: Koordinaten rechts unten sind: x: 10.079383055515022, y: -8.452652248178886, z: -1.355316137274798E-5
			double latp2 = 49.448180;
			double lonp2 = 11.096101;
			double altp2 = 46.87;

			double[] ecef2 = utl.geo_to_ecef(latp2, lonp2, altp2);
			double[] enu2 = utl.ecef2enu(ecef2[0], ecef2[1], ecef2[2], lat, lon, alt);
			Gdx.app.log("ENU2", "Koordinaten rechts unten sind: x: " + enu2[0] + ", y: " + enu2[1] + ", z: " + enu2[2]);


			// Neue Korrdinaten um Zentrum links darunter versetzt.
			// Erg.: Koordinaten links unten sind: x: -8.846663549991854, y: -15.125808148317041, z: -2.4074337785506827E-5
			double latp3 = 49.448120;
			double lonp3 = 11.095840;
			double altp3 = 46.87;
			double[] ecef3 = utl.geo_to_ecef(latp3, lonp3, altp3);
			double[] enu3 = utl.ecef2enu(ecef3[0], ecef3[1], ecef3[2], lat, lon, alt);
			Gdx.app.log("ENU3", "Koordinaten links unten sind: x: " + enu3[0] + ", y: " + enu3[1] + ", z: " + enu3[2]);

			double[] enu4 = utl.geo2enu(latp3,lonp3, altp3);
			Gdx.app.log("ENU4", "Koordinaten links unten sind: x: " + enu4[0] + ", y: " + enu4[1] + ", z: " + enu4[2]);

			Skin mySkin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));

			button2 = new TextButton("Text Button", mySkin, "small");
			button2.setSize(300,150);
			button2.setPosition(100,100);
			button2.addListener(new InputListener(){
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					Gdx.app.log("Button	", " has been pressed");

				}

				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					Gdx.app.log("Button	", " has been pressed");
					instance.transform.rotate(1,2,3,3);
					redBox.transform.rotate(1,-2,3,-33);
					return true;
				}
			});
			stage.addActor(button2);

			Gdx.input.setInputProcessor(stage);
		}

		@Override
		public void render() {
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Setsup view for Display.
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			modelBatch.begin(cam);
			modelBatch.render(instance, lights);
			modelBatch.render(redBox, lights);
			modelBatch.end();

			stage.act();
			stage.draw();

			//spriteBatch.begin();

			//font.draw(spriteBatch, "Hello World!", 10, 10);
			//spriteBatch.end();



		}

		@Override
		public void dispose() {
			modelBatch.dispose();
			model.dispose();
			model2.dispose();
			stage.dispose();
		}

		@Override
		public void resize(int width, int height) {
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
		}
	}