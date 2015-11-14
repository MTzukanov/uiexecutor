package org.vaadin.mtzukanov.uiexecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.UI;

public class DefaultUIUpdateStrategy implements UIUpdateStrategy {
	/**
	 * The default poll interval when background work is executing in a
	 * runnable.
	 */
	private static final int DEFAULT_WORK_POLL_INTERVAL = 800;

	private final Logger log = Logger.getLogger(getClass().getName());

	private class UIData {
		int idlePollInterval = 0;
		int pendingTasks = 0;

		public UIData(int pollInterval) {
			this.idlePollInterval = pollInterval;
		}
	}

	private final Map<UI, UIData> uiData = new HashMap<>();
	private final int workPollInterval;
	
	public DefaultUIUpdateStrategy() {
		this(DEFAULT_WORK_POLL_INTERVAL);
	}

	public DefaultUIUpdateStrategy(int workPollInterval) {
		this.workPollInterval = workPollInterval;
	}

	/**
	 * Called when a task is submitted for execution to adjust the polling
	 * interval.
	 */
	@Override
	public synchronized void runBeforeBackgroundTask(UI ui) {
		if (ui.getPushConfiguration().getPushMode() != PushMode.DISABLED)
			return;

		if (!uiData.containsKey(ui))
			uiData.put(ui, new UIData(ui.getPollInterval()));

		ui.setPollInterval(workPollInterval);
		uiData.get(ui).pendingTasks++;

		log.info(String.format(
				"Background task started. There are [%s] pending "
						+ "tasks for UI [%s] with a poll interval of [%s].",
				uiData.get(ui).pendingTasks, ui.getConnectorId(),
				ui.getPollInterval()));
	}

	/**
	 * Called by the complete listener to adjust the polling interval or perform
	 * a manual push.
	 */
	@Override
	public void runAfterBackgroundTaskAndUIUpdate(UI ui) {
		if (ui.getPushConfiguration().getPushMode() != PushMode.DISABLED)
		{
			if (ui.getPushConfiguration().getPushMode() == PushMode.MANUAL)
				ui.push();
			return;
		}
		
		uiData.get(ui).pendingTasks--;
		
		final int pendingTasks = uiData.get(ui).pendingTasks;
		
		if (uiData.get(ui).pendingTasks == 0) {
			ui.setPollInterval(uiData.get(ui).idlePollInterval);
			uiData.remove(ui);
		}
		
		log.info(String.format(
				"Background task finished. There are [%s] pending "
						+ "tasks for UI [%s] with a poll interval of [%s].",
						pendingTasks, ui.getConnectorId(), ui.getPollInterval()));
	}
}
