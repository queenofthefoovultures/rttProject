package com.indoorino.bme5;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.backends.android.AndroidApplication;
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
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ORIENTATION;

// alternative: public class extends ApplicationAdapter
//public class Indoorino extends Activity implements ApplicationListener
public class Indoorino extends ApplicationAdapter implements SensorEventListener{

	// UI
	private Stage stage;
	private Button button2;

	// 3D Objects and Handlers
	private Environment lights;
	private PerspectiveCamera cam;

	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;

	private Model model2;
	private ModelInstance redBox;

	private Model ground;
	private ModelInstance groundinstance;
	private ModelLoader loader;


	// GPS Retrieval Instance
	public LocationManager locationManager;
	public LocationListener locationListener;
	double locationLat = 0d;
	double locationLon = 0d;
	private AndroidApplication appl;
	private CoordinateUtilities utl;
	private CoordinateConverter conv;
	private PositionCalculator posCalc;

	// Compass
	private SensorManager sensorManager;
	private Sensor compass;
	private float currentDegree = 0f;

	// App Constructor
	public Indoorino(AndroidApplication myapp) {
		appl = myapp;
	}

	@Override
	public void create() {
		// Object Loader for loading model (school) into system
		loader = new ObjLoader();
		ground = loader.loadModel(Gdx.files.internal("CityBlock.obj"));
		try {
			groundinstance = new ModelInstance(ground, 0, 0, 0); // places Ground at center of coordinate System
		} catch(Exception e) {
			Log.e("INSTANCE LOADING","Did not work because: " + e);
		}
		groundinstance.transform.scale(0.03f, 0.03f, 0.03f);
		groundinstance.transform.rotate(0,1,0, 180);

		//Nürnberg ZENTRUM vor TH BB Gebäude
		double lat = 49.448256;
		double lon = 11.095962;
		double alt = 46.87;
		float[] centerPoint = {(float)lat,(float)lon, (float) alt};

		utl = new CoordinateUtilities();
		conv = new CoordinateConverter(centerPoint);

		double[] ecefbase = utl.geo_to_ecef(lat, lon, alt);
		float[] ecefbaseFloat = {(float)ecefbase[0],(float)ecefbase[1],(float)ecefbase[2]};
		posCalc = new PositionCalculator(ecefbaseFloat);


		//
		// Vergleich zwischen Codeschnipseln
		//
		// Neue Koordinaten um Zentrum links drüber versetzt.
		// Erg: Koordinaten links oben sind: x: -7.468874241294032, y: 7.006816722382261, z: -8.217153399048271E-6
		double latp = 49.448319;
		double lonp = 11.095859;
		double altp = 46.87;
		double[] ecef = utl.geo_to_ecef(latp, lonp, altp);
		double[] enu = utl.ecef2enu(ecef[0], ecef[1], ecef[2], lat, lon, alt);
		Gdx.app.log("ENU1", "Alte Berechnung: x: " + enu[0] + ", y: " + enu[1] + ", z: " + enu[2]);
		//Gdx.app.log("ECEF1", "Koordinaten links oben sind: x: " + ecef[0] + ", y: " + ecef[1] + ", z: " + ecef[2]);

		//
		// Vergleich zwischen Codeschnipseln
		//
		float[] currentSignal = {(float)latp,(float)lonp, (float) altp};
		float[] currentLoc = conv.gps2LocalEnu(currentSignal);
		//Vector3 test = new Vector3(currentSignal);
		//Vector3 test2 = conv.gps2ecef(test);
		Gdx.app.log("ENU2", "neue Berechnung x: " + currentLoc[0] + ", y: " + currentLoc[1] +  ", z: " + currentLoc[2]);
		//Gdx.app.log("ECEF2", "Koordinaten links oben sind: x: " + test2.x + ", y: " + test2.y + ", z: " + test2.z);
		Log.i("ENU", "-----------------------------------");


		// Compass
		sensorManager = (SensorManager) appl.getSystemService(SENSOR_SERVICE);
		compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (compass != null) {
			sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_GAME);
		}

		// Aktivieren  des LocationManagers für GPS Abfrage
		locationManager = (LocationManager) appl.getContext().getSystemService(LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				locationLon = location.getLongitude();
				locationLat = location.getLatitude();
				Log.d("GPSLocation","gps is updated lat: " + locationLat + ", lon: " + locationLon);

				double[] enu4 = utl.geo2enu(locationLat,locationLon, 46.87);
				Log.d("GPSLocationDoubl", "Folgende ENU Werte: Lat: " + enu4[0] + " Lon: " + enu4[1] + " Alt: " +  enu4[2]);
				Log.d("GPSLocationFloat", "Folgende ENU Werte: Lat: " + (float)enu4[0] + " Lon: " + (float)enu4[1] + " Alt: " +  (float)enu4[2]);

				float[] movement = {(float)enu4[0],(float)enu4[2], (float)enu4[1]};

				float[] differenceMov = posCalc.giveNewVec(movement);
				instance.transform.translate(differenceMov[0], differenceMov[1],differenceMov[2]); // 3 float werte x, y, z
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) { }

			@Override
			public void onProviderEnabled(String provider) { }

			@Override
			public void onProviderDisabled(String provider) { }
		};

		appl.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(appl, "hey", Toast.LENGTH_SHORT).show();
				// appl.getContext().
				if (appl.getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && appl.getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    Activity#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for Activity#requestPermissions for more details.
					//ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10);
					return;
				}
				try {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
				} catch (Exception e){
					Log.e("GPSAKTIVIERUNGSFEHLER", "" + e);
				}


			}
		});



		stage = new Stage(new ScreenViewport());

		// Initiate Light
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 0.4f, 0.4f, 1f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


		// Initiate Camera
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 100f, -90f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		// Initiate 3D Model Handler Batch
		modelBatch = new ModelBatch();
		ModelBuilder modelBuilder = new ModelBuilder();

		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		instance = new ModelInstance(model, 2,2, 2);
		instance.transform.translate(1,-2, 1);



		model2 = modelBuilder.createBox(10f, 3f, 6f, new Material(ColorAttribute.createDiffuse(Color.RED)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		redBox = new ModelInstance(model2);




		Skin mySkin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));


		button2 = new TextButton("Text Button", mySkin, "small");button2.setSize(300,150);
		button2.setPosition(100,100);
		button2.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

			}

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.log("Button	", " has been pressed");
				instance.transform.rotate(1,2,3,3);
				Gdx.app.log("GPS:", "Lat: " + locationLat + " Lon: " + locationLon);
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
			Gdx.gl.glClearColor(135/255f, 206/255f, 235/255f, 1);
			modelBatch.begin(cam);
			modelBatch.render(instance, lights);
			modelBatch.render(redBox, lights);
			modelBatch.render(groundinstance, lights);
			modelBatch.end();

			stage.act();
			stage.draw();

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
			sensorManager.unregisterListener(this);
		}

		@Override
		public void resume() {
			if (compass != null) {
				sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
			}
		}

		private void loadSetup(){


		}

	// Interface method for actions when Sensor is changed
	@Override
	public void onSensorChanged(SensorEvent event) {
		float degree = Math.round(event.values[0]);
		Log.i("COMPASS","degree: " + degree + ", event.values: " + event.values[0]);
		// create a rotation animation (reverse turn degree degrees)
		currentDegree = -degree;
		//Log.i("COMPASS","Rotation = " + currentDegree + ", -degree = " + -degree);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}