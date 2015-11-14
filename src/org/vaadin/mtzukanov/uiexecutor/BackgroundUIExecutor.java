package org.vaadin.mtzukanov.uiexecutor;

import java.util.concurrent.Future;

public interface BackgroundUIExecutor {
	Future<?> execute(BackgroundUIRunnable r);
}
