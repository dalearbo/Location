package com.darpa.location;
import com.darpa.location.Location;
import com.darpa.location.RobotLocationReporter;

interface RobotLocation{
	Location requestLocation();
	void add(RobotLocationReporter reporter);
	void remove(RobotLocationReporter reporter);
}
