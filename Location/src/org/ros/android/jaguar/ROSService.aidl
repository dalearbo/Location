package org.ros.android.jaguar;

import org.ros.android.jaguar.ROSServiceReporter;

interface ROSService {
	void add(ROSServiceReporter reporter);
	void remove(ROSServiceReporter reporter);
	void publishCommand(in String commandString);
}