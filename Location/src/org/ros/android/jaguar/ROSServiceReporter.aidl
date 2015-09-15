package org.ros.android.jaguar;

import com.darpa.location.Location;

interface ROSServiceReporter {
	void reportGPS(in Location location);
}