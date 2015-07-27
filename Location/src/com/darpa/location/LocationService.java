package com.darpa.location;


import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class LocationService extends Service{

	//Different apps will use this service differently.  Either you can listen for updates in location
	//through the reporter, or call robotLocation.requestLocation();

		private Location currentLocation;
		private Location organicLocation;
		LocationManager locationManager;
		String tag = "Location";

		///Need to change LocationManager.Passive_Provider to LocationManager.GPS_Provider but doesn't work

	////THIS IS THE METHOD TO EDIT TO DETERMINE BEST LOCATION USING OTHER SENSORS///
		private Location DetermineBestLocation(){
			Location bestLocation=new Location("");
			
			//bestLocation.setLatitude(35.0001d);
			//bestLocation.setLongitude(70.0002d);
			//bestLocation.setBearing(15.0f);
			
			if(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)==null){
				bestLocation=organicLocation;
			}else
				bestLocation=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				
			return bestLocation;
			
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
				while(!isInterrupted()) {
					currentLocation=DetermineBestLocation();
					while(!isInterrupted()) {
						List<RobotLocationReporter> targets;
						synchronized (reporters) {
							targets = new ArrayList<RobotLocationReporter>(reporters);
						}
						for(RobotLocationReporter robotLocationReporter : targets) {
							try {
								robotLocationReporter.report(currentLocation);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
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
			locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new onboardLocation();
			locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, (float) 0.1, locationListener);
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(tag, "onStartCommand(" + intent + ", " + flags + ", " + startId + ")");
			return START_STICKY;
		}

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

