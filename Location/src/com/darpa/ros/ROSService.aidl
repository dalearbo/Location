package com.darpa.ros;

import com.darpa.ros.ROSServiceReporter;

interface ROSService {
	void add(ROSServiceReporter reporter);
	void remove(ROSServiceReporter reporter);
	void publishCommand(in String commandString);
}