package org.vaadin.mtzukanov.uiexecutor;

import com.vaadin.ui.UI;

public interface BackgroundUIRunnable {
	default void runInUIBefore() {
	};

	void runInBackground(UI ui) throws Throwable;

	default void runInUIAfter(Throwable ex) {
	};
}