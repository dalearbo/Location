package org.ros.android.android_tutorial_project;

import org.ros.android.android_tutorial_project.JaguarServiceReporter;

interface JaguarService {
	void add(JaguarServiceReporter reporter);
	void remove(JaguarServiceReporter reporter);
	void move(in String moveString);
}