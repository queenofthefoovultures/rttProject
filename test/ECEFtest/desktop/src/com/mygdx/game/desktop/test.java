package com.mygdx.game.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;




public class test extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	// Nürnberg 0-Punkt an TH BB Gebäude
	final double lat = 49.448420;
	final double lon = 11.096092;
	//final double lat = 49.448256;
	//final double lon = 11.095962;
	final double alt = 311; // WGS84 46.87;

	// Nürnberg links vor Parkhaus
	final double latlp = 49.448259;
	final double lonlp = 11.095791;

	//  Nürnberg unten bei BB Gebäude
	final double latubb = 49.448149;
	final double lonubb = 11.096159;

	CoordinateUtilities cooUtl;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		cooUtl = new CoordinateUtilities(lat, lon, alt);
		// Input sind Koordinaten links vor Parkhaus.
		double[] enu = cooUtl.geo2enu(latlp, lonlp, alt);
		Gdx.app.log("cooUtl.geo2enu","enu[] = X: "  + enu[0] + ", Y: " + enu[1] + ", Z= " + enu[2]);
		double[] referenzPunkt = cooUtl.geo2enu(lat, lon, alt);
		Gdx.app.log("cooUtl.referenzPunkt","enu[] = X: "  + referenzPunkt[0] + ", Y: " + referenzPunkt[1] + ", Z= " + referenzPunkt[2]);

		double[] referenzPunkt2 = cooUtl.geo2enu(lat, lon-0.002, alt);
		Gdx.app.log("cooUtl.referenzPunkt2","enu[] = X: "  + referenzPunkt2[0] + ", Y: " + referenzPunkt2[1] + ", Z= " + referenzPunkt2[2]);

		double[] referenzPunkt3 = cooUtl.geo2enu(lat, lon-0.002, alt+10);
		Gdx.app.log("cooUtl.referenzPunkt2","enu[] = X: "  + referenzPunkt3[0] + ", Y: " + referenzPunkt3[1] + ", Z= " + referenzPunkt3[2]);

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
