package org.vaadin.mtzukanov.uiexecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.vaadin.ui.UI;

public class DefaultBackgroundUIExecutor implements BackgroundUIExecutor {
	private Executor executor;
	private UIUpdateStrategy uiUpdateStrategy;

	public DefaultBackgroundUIExecutor(final Executor executor) {
		this(executor, new DefaultUIUpdateStrategy());
	}

	public DefaultBackgroundUIExecutor(final Executor executor,
			UIUpdateStrategy uiUpdateStrategy) {
		this.executor = executor;
		this.uiUpdateStrategy = uiUpdateStrategy;
	}

	@Override
	public Future<?> execute(final BackgroundUIRunnable r) {
		FutureTask<?> futureTask = createFutureTask(getUI(), r);

		executor.execute(futureTask);

		return futureTask;
	}

	protected UI getUI() {
		return UI.getCurrent();
	}

	class FutureValue {
		Future<?> future;
	}

	protected FutureTask<?> createFutureTask(final UI ui,
			final BackgroundUIRunnable r) {
		if (ui == null)
			throw new IllegalStateException("UI is null");

		FutureValue futureValue = new FutureValue();
		Runnable runnable = createRunnable(ui, r, futureValue);
		FutureTask<Void> futureTask = new FutureTask<Void>(runnable, null);
		futureValue.future = futureTask;

		return futureTask;
	}

	protected Runnable createRunnable(final UI ui,
			final BackgroundUIRunnable r, FutureValue future) {
		return () -> {
			Throwable backgroundThrowed = null;
			try {
				uiUpdateStrategy.runBeforeBackgroundTask(ui);
				ui.access(r::runInUIBefore);
				r.runInBackground(ui);
			} catch (Throwable e) {
				backgroundThrowed = e;
			} finally {
				try {
					if (!future.future.isCancelled() && ui.isAttached()) {
						final Throwable t = backgroundThrowed;
						ui.access(() -> r.runInUIAfter(t));
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				finally {
					uiUpdateStrategy.runAfterBackgroundTaskAndUIUpdate(ui);					
				}
			}
		};
	}
}
