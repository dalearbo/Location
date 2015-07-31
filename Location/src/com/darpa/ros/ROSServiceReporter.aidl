package com.darpa.ros;

import com.darpa.location.Location;

interface ROSServiceReporter {
	void reportGPS(in Location location);
}