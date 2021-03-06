package com.indoorino.bme5;

import com.badlogic.gdx.ApplicationAdapter;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ORIENTATION;

// alternative: public class extends ApplicationAdapter
// alternative: public class Indoorino extends Activity implements ApplicationListener

public class Indoorino extends ApplicationAdapter implements SensorEventListener{

	private static final float ninetyDeg = 90;
	// UI
	private Stage stage;
	private Button button2;

	// 3D Objects and Handlers
	private Environment lights;
	private PerspectiveCamera cam;

	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;

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
	private PositionCalculator posCalc;

	// Compass
	private SensorManager sensorManager;
	private Sensor compass;
	private float currentDegree = 0f;


	// Nürnberg 0-Punkt an TH BB Gebäude
	final double lat = 49.448380; // aus GoogleMaps
	final double lon = 11.096160; // aus GoogleMaps

	//final double lat = 49.448256; //gemessen mit Smartphone
	//final double lon = 11.095962; // gemessen mit Smartphone
	final double alt = 311; // WGS84 46.87;

	// Nürnberg links vor Parkhaus
	final double latlp = 49.448241;
	final double lonlp = 11.095696;

	// Nürnberg unten neben BB Gebäude
	final double latubb = 49.448073;
	final double lonubb = 11.096225;

	final double lattest = 49.448122;
	final double lontest = 11.095949;

	final double lattest2 = 49.448116;
	final double lontest2 = 11.095942;

	final double lattest3 = 49.448108;
	final double lontest3 = 11.095948;

	final double lattest4 = 49.448100;
	final double lontest4 = 11.095885;


	/*
	Location[gps
	Location[gps ,
	Location[gps ,
	Location[gps ,
	*/

	// App Constructor
	public Indoorino(AndroidApplication myapp) {
		appl = myapp;
	}

	@Override
	public void create() {

		// Object Loader for loading model (schoolarea) into system
		loader = new ObjLoader();
		ground = loader.loadModel(Gdx.files.internal("CityblockMeter5.obj"));
		try {
			groundinstance = new ModelInstance(ground, 0, 0, 0); // places Ground at center of coordinate System
			Log.i("INSTANCE LOADING","Import did work");
		} catch(Exception e) {
			Log.e("INSTANCE LOADING","Did not work because: " + e);
		}
		//groundinstance.transform.scale(0.01f, 0.01f, 0.01f); // Scales the schoolground
		groundinstance.transform.rotate(0,1,0, 25); // rotates the area by 10 percent

		// Initialisation of Coordinate Converter
		utl = new CoordinateUtilities(lat, lon, alt);

		// Initialisation of position Calculator with center values
		double[] ecefbase = utl.geo_to_ecef(lat, lon, alt);
		double[] enuBase = utl.ecef_to_enu(ecefbase[0], ecefbase[1], ecefbase[2],lat,lon,alt);
		//Gdx.app.log("ENU-posCalc-Initialisierung","X : " + ecefbase[0] + ", Y: " + ecefbase[1] + ", Z: " + ecefbase[2]);
		posCalc = new PositionCalculator(enuBase);

		// Initialisation of the compass
		sensorManager = (SensorManager) appl.getSystemService(SENSOR_SERVICE);
		compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (compass != null) {
			sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_GAME);
		}

		// Initialisation of the LocationManager/Listener for GPS retrieval
		locationManager = (LocationManager) appl.getContext().getSystemService(LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// Actions when location changed
				locationLon = location.getLongitude();
				locationLat = location.getLatitude();
				// Convert latest GPS Data to ENU Coordinates
				double[] enu4 = utl.geo_to_enu(locationLat,locationLon, alt);
				// Give 'em to differenceCalcualtor
				//double[] differenceMov = posCalc.giveNewVec(enu4);
				//Gdx.app.log("posCalc.giveNewVec Verschiebung: ","x = " + differenceMov[0] + ", y = " + differenceMov[1] + ", z = " + differenceMov[2]);
				// Movement of the playerobject
				//instance.transform.translate((float)differenceMov[0], 1, -(float)differenceMov[1]); // 3 float werte x, y, z = (float)differenceMov[2]
				//Vector3 dfdf = instance.transform.getTranslation(new Vector3());
				//Gdx.app.log("Würfel Position: ","X = " + dfdf.x + ", Y = " + dfdf.y + ", Z = " + dfdf.z);
				// X = X, Y = -Z, Z = 0Y
				Quaternion df = new Quaternion();
				instance.transform.getRotation(df);
				instance.transform.set(new Vector3((float)enu4[0],1,(float)enu4[1]*(-1)), df);
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
				if (appl.getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && appl.getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    Activity#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for Activity#requestPermissions for more details.
					// ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10);
					return;
				}
				try {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
					Toast.makeText(appl, "GPS Activated", Toast.LENGTH_LONG).show();
				} catch (Exception e){

					Log.e("GPSAKTIVIERUNGSFEHLER", "" + e);
					Toast.makeText(appl, "GPS Activation failed, turn it on!", Toast.LENGTH_LONG).show();
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
		cam.position.set(-20f, 40f, 60f);
		cam.lookAt(-20f,0f,20f);
		cam.near = 1f;
		cam.far = 200f;
		cam.update();

		// Initiate 3D Model Handler Batch
		modelBatch = new ModelBatch();
		ModelBuilder modelBuilder = new ModelBuilder();

		model = modelBuilder.createBox(0.5f, 2f, 0.5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

		//ENU Werte: X: -15.373529894298624 Y: -30.252863196922828 Z: -9.029922470915608E-5
		instance = new ModelInstance(model, 0,1, 0);

		// Adding Button to Stage for UI = Skin for Button, Button, Actions for Button
		Skin mySkin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));
		button2 = new TextButton("Text Button", mySkin, "small");button2.setSize(300,150);
		button2.setPosition(100,100);
		button2.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) { }

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.log("Button	", " has been pressed");
				instance.transform.rotate(1,2,3,3);
				//Gdx.app.log("GPS:", "Lat: " + locationLat + " Lon: " + locationLon);

				double[] enu43 = utl.geo_to_enu(lattest,lontest, alt);
				Log.d("ENUTEST", "Folgende ENU Werte: X: " + enu43[0] + " Y: " + enu43[1] + " Z: " +  enu43[2]);

				double[] enu44 = utl.geo_to_enu(lattest2,lontest2, alt);
				Log.d("ENUTEST", "Folgende ENU Werte: X: " + enu44[0] + " Y: " + enu44[1] + " Z: " +  enu44[2]);

				double[] enu45 = utl.geo_to_enu(lattest3,lontest3, alt);
				Log.d("ENUTEST", "Folgende ENU Werte: X: " + enu45[0] + " Y: " + enu45[1] + " Z: " +  enu45[2]);

				double[] enu46 = utl.geo_to_enu(lattest4,lontest4, alt);
				Log.d("ENUTEST", "Folgende ENU Werte: X: " + enu46[0] + " Y: " + enu46[1] + " Z: " +  enu46[2]);



				//Log.d("GPSLocationFloat", "Folgende ENU Werte: Lat: " + (float)enu4[0] + " Lon: " + (float)enu4[1] + " Alt: " +  (float)enu4[2]);

				//double[] differenceMov = posCalc.giveNewVec(enu43);
				//Gdx.app.log("posCalc.giveNewVec Verschiebung: ","x = " + differenceMov[0] + ", y = " + differenceMov[1] + ", z = " + differenceMov[2]);
				// Movement of the playerobject
				//instance.transform.translate((float)differenceMov[0], 1, -(float)differenceMov[1]); // 3 float werte x, y, z = (float)differenceMov[2]
				Vector3 dfdf = instance.transform.getTranslation(new Vector3());
				Gdx.app.log("Würfel Position: ","X = " + dfdf.x + ", Y = " + dfdf.y + ", Z = " + dfdf.z);

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
			modelBatch.render(groundinstance, lights);
			modelBatch.end();
			stage.act();
			stage.draw();
		}

		@Override
		public void dispose() {
			// When app is closed, all instances here listed will be disposed.
			modelBatch.dispose(); // Model handler
			model.dispose(); // green Cube Model
			ground.dispose(); // Area-load-In Model
			stage.dispose(); // Frame for UI with Button
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
		float degree = Math.round(event.values[0]) + ninetyDeg; // app only landscape mode, therefore 90 Deg rotation
		//Log.i("COMPASS","degree: " + degree + ", event.values: " + event.values[0]);
		// create a rotation animation (reverse turn degree degrees)
		currentDegree = -degree;
		//Log.i("COMPASS","Rotation = " + currentDegree + ", -degree = " + -degree);
		float rotationDiff = posCalc.rotateDiff(-degree);
		instance.transform.rotate(0,1,0, rotationDiff);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}