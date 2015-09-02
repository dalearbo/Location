package org.ros.android.jaguar;

import org.ros.android.jaguar.ROSService;
import org.ros.android.jaguar.ROSServiceReporter;


import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;


public class ROSServiceManager {

	private boolean disconnected = false;
    private static ROSService rosService;
    private static Intent intent = new Intent("com.darpa.ros");
    private OnConnectedListener onConnectedListener;
    private Service service;
    private Activity activity;
    private String tag = "Socket";

    public static interface OnConnectedListener {
    	void onConnected();
    	void onDisconnected();
    }

	public ROSServiceManager(Service service, OnConnectedListener onConnectedListener) {
    	this.service = service;
		this.onConnectedListener = onConnectedListener;
		service.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
			Log.e(tag, "add reporter", e);
		}
	}
	public synchronized void remove(ROSServiceReporter reporter) {
		if (disconnected)
			throw new IllegalStateException("Manager has been explicitly disconnected; you cannot call methods on it");
		try {
			rosService.remove(reporter);
		} catch (RemoteException e) {
			Log.e(tag, "remove reporter", e);
		}
	}

    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override public void onServiceDisconnected(ComponentName name) {
			Log.d(tag, "RosService onServiceDisconnected");
			if (onConnectedListener != null)
				onConnectedListener.onDisconnected();
			rosService = null;
		}
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(tag,"RosService onServiceConnected");
			rosService = ROSService.Stub.asInterface(service);
			
			if (onConnectedListener != null)
				onConnectedListener.onConnected();
		}
	};
	
	
}
