package org.ros.android.android_tutorial_project;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class JaguarManager {
	private boolean disconnected = false;
    private JaguarService jaguarService;
    private static Intent intent = new Intent("org.ros.android.android_tutorial_project");
    private OnConnectedListener onConnectedListener;
    private Service service;

    public static interface OnConnectedListener {
    	void onConnected();
    	void onDisconnected();
    }

	public JaguarManager(Service service, OnConnectedListener onConnectedListener) {
    	this.service = service;
		this.onConnectedListener = onConnectedListener;
		service.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public synchronized void disconnect() {
		disconnected = true;
		service.unbindService(serviceConnection);
	}
	public synchronized void add(JaguarServiceReporter reporter) {
		if (disconnected)
			throw new IllegalStateException("Manager has been explicitly disconnected; you cannot call methods on it");
		try {
			jaguarService.add(reporter);
		} catch (RemoteException e) {
			Log.e("MapServiceManager", "add reporter", e);
		}
	}
	public synchronized void remove(JaguarServiceReporter reporter) {
		if (disconnected)
			throw new IllegalStateException("Manager has been explicitly disconnected; you cannot call methods on it");
		try {
			jaguarService.remove(reporter);
		} catch (RemoteException e) {
			Log.e("MainActivity", "remove reporter", e);
		}
	}

    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override public void onServiceDisconnected(ComponentName name) {
			Log.d("MapServiceManager", "onServiceDisconnected");
			if (onConnectedListener != null)
				onConnectedListener.onDisconnected();
			jaguarService = null;
		}
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			jaguarService = JaguarService.Stub.asInterface(service);
			if (onConnectedListener != null)
				onConnectedListener.onConnected();
		}
	};
}


