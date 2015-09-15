package com.darpa.location;


import java.util.ArrayList;
import java.util.List;

import org.ros.android.jaguar.ROSServiceManager;
import org.ros.android.jaguar.ROSServiceReporter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class LocationService extends Service{

	//Different apps will use this service differently.  Either you can listen for updates in location
	//through the reporter, or call robotLocation.requestLocation();

		private Location currentLocation;
		private Location organicLocation;
		LocationManager locationManager;
		private ROSServiceManager jaguarManager;
		String tag = "LocationService";
		boolean newLocationFlag = false;

		
		private Location startLocation(){
			Location startLocation=new Location("");
			startLocation.setLatitude(32.996683d);
			startLocation.setLongitude(-79.970303d);
			startLocation.setBearing(0.0f);
			return startLocation;
			
		}
		

		private class onboardLocation implements LocationListener {
			@Override
			public void onLocationChanged(Location location) {
				organicLocation=location;
			}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onProviderDisabled(String provider) {}
			
		}
		


		private class ServiceThread extends Thread {
			@Override public void run() {
				currentLocation = startLocation();
				while(!isInterrupted()) {
					while(!isInterrupted()) {
						List<RobotLocationReporter> targets;
						synchronized (reporters) {
							targets = new ArrayList<RobotLocationReporter>(reporters);
						}
						for(RobotLocationReporter robotLocationReporter : targets) {
							try {
								// currentLocation is CORRECT here
								//Log.d("DALE", "LocationService Lat: " + currentLocation.getLatitude() + " Lon: " + currentLocation.getLongitude());
								robotLocationReporter.report(currentLocation);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
				}}
				Log.d(tag,"RobotLocation interrupted");
			}
		}
		
		private ServiceThread serviceThread;
		
		@Override
		public void onCreate() {
			super.onCreate();
			Log.d(tag,"RobotLocation onCreate");
			//locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			//LocationListener locationListener = new onboardLocation();
			//locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, (float) 0.1, locationListener);
			
			jaguarManager = new ROSServiceManager(this, new ROSServiceManager.OnConnectedListener() {
	    		@Override public void onConnected() {
	    			Log.d(tag, "Jaguar Location connected - adding reporter");
	    			jaguarManager.add(jaguarServiceReporter);
	    			
	    		}
	    		@Override public void onDisconnected() {
	    			Log.d(tag, "Jaguar Location disconnected - removing reporter");
	    			jaguarManager.remove(jaguarServiceReporter);
	    		}
	    	});
			
		}
		/*
		float[] mGravity;
		float[] mGeomagnetic;
		public void onSensorChanged(SensorEvent event) {
		    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		        mGravity = event.values;
		    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		        mGeomagnetic = event.values;
		    if (mGravity != null && mGeomagnetic != null) {
		        float R[] = new float[9];
		        float I[] = new float[9];
		        boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
		                mGeomagnetic);
		        if (success) {
		            float orientation[] = new float[3];
		            SensorManager.getOrientation(R, orientation);
		            float radianHeading = orientation[0];
					float bearing = (float) ((90-radianHeading*180/Math.PI)%360);
					//currentLocation.setBearing(bearing);
					Log.d(tag,"Bearing: "+bearing);
		        }
		    }
		}*/
		
		
		private ROSServiceReporter jaguarServiceReporter = new ROSServiceReporter.Stub() {
			@Override
			public void reportGPS(Location location) throws RemoteException {
				//Log.d("DALE", "LocationService Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
				currentLocation=location;
			}
		};
		

		@Override
		public void onDestroy() {
			if (serviceThread != null) {
				serviceThread.interrupt();
				serviceThread = null;
			}
			Log.d(tag,"RobotLocation onDestroy");
			super.onDestroy();
		}
		
		@Override
		public IBinder onBind(Intent intent) {
			Log.d(tag,"RobotLocation onBind");
			currentLocation=new Location("");
			serviceThread = new ServiceThread();
			serviceThread.start();
			return locationBinder;
		}
		
		RobotLocation robotLocation;
		
		private List<RobotLocationReporter> reporters = new ArrayList<RobotLocationReporter>();

		private RobotLocation.Stub locationBinder = new RobotLocation.Stub() {

			@Override
			public Location requestLocation() {
				return currentLocation;
			}

			@Override
			public void add(RobotLocationReporter reporter) throws RemoteException {
				synchronized (reporters) {
					reporters.add(reporter);
				}
			}

			@Override
			public void remove(RobotLocationReporter reporter) throws RemoteException {
				synchronized (reporters) {
					reporters.remove(reporter);
				}
			}
			
		};


	}

