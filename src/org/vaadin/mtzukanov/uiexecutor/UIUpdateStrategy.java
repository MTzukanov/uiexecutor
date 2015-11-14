package org.vaadin.mtzukanov.uiexecutor;

import com.vaadin.ui.UI;

public interface UIUpdateStrategy {
	void runBeforeBackgroundTask(UI ui);

	void runAfterBackgroundTaskAndUIUpdate(UI ui);
}
