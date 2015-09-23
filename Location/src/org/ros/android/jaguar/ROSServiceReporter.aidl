package org.ros.android.jaguar;

import com.darpa.location.Location;

interface ROSServiceReporter {
	void reportGPS(in Location location);
	void reportWheel(in double forward, in double right);
}