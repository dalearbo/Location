package com.darpa.ros;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class ROSServiceManager {

	private boolean disconnected = false;
    private ROSService rosService;
    private static Intent intent = new Intent("org.ros.android.android_tutorial_project");
    private OnConnectedListener onConnectedListener;
    private Service service;
    private Activity activity;

    public static interface OnConnectedListener {
    	void onConnected();
    	void onDisconnected();
    }

	public ROSServiceManager(Service service, OnConnectedListener onConnectedListener) {
    	this.service = service;
		this.onConnectedListener = onConnectedListener;
		service.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	public ROSServiceManager(Activity activity, OnConnectedListener onConnectedListener) {
    	this.activity = activity;
		this.onConnectedListener = onConnectedListener;
		activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	
	public void publishCommand(float velocity, float angle){
		
		String commandString = "MMW !M ";
		
		if(velocity>1){
			Log.e("UVADE","Error! Velocity too large");
			velocity=1;
		}
		
		int jaguarX = 0;
		int jaguarY = 0;
		
		commandString.concat(jaguarX+" "+jaguarY);
		Log.d("ROS",commandString);
		
		try {
			rosService.publishCommand(commandString);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void disconnect() {
		disconnected = true;
		if(service!=null)
			service.unbindService(serviceConnection);
		else
			activity.unbindService(serviceConnection);
	}
	public synchronized void add(ROSServiceReporter reporter) {
		if (disconnected)
			throw new IllegalStateException("Manager has been explicitly disconnected; you cannot call methods on it");
		try {
			rosService.add(reporter);
		} catch (RemoteException e) {
			Log.e("RSM", "add reporter", e);
		}
	}
	public synchronized void remove(ROSServiceReporter reporter) {
		if (disconnected)
			throw new IllegalStateException("Manager has been explicitly disconnected; you cannot call methods on it");
		try {
			rosService.remove(reporter);
		} catch (RemoteException e) {
			Log.e("RSM", "remove reporter", e);
		}
	}

    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override public void onServiceDisconnected(ComponentName name) {
			Log.d("RSM", "onServiceDisconnected");
			if (onConnectedListener != null)
				onConnectedListener.onDisconnected();
			rosService = null;
		}
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			rosService = ROSService.Stub.asInterface(service);
			if (onConnectedListener != null)
				onConnectedListener.onConnected();
		}
	};
	
	
}
