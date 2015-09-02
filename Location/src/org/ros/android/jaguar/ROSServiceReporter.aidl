package org.ros.android.jaguar;

import org.ros.android.jaguar.Location;

interface ROSServiceReporter {
	void reportGPS(in Location location);
}