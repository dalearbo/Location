package org.ros.android.android_tutorial_project;

import com.darpa.location.Location;

interface JaguarServiceReporter {
	void reportLocation(in Location location);
}