/**
 * 
 */
package org.vaadin.mtzukanov.uiexecutor.test;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.vaadin.mtzukanov.uiexecutor.DefaultUIUpdateStrategy;

import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.PushConfiguration;
import com.vaadin.ui.UI;

/**
 * @author mtzukanov
 */
@SuppressWarnings("serial")
public class DefaultUIUpdateStrategyTest {

	@Test
	public void testStrategyCorrectlySetPollIntervalSimple() {
		UI ui = createUIpushDisabled();

		DefaultUIUpdateStrategy strategy = new DefaultUIUpdateStrategy(250);
		
		strategy.runBeforeBackgroundTask(ui);
		
		verify(ui).setPollInterval(250);
		
		strategy.runAfterBackgroundTaskAndUIUpdate(ui);

		verify(ui).setPollInterval(-1);
	}
	
	@Test
	public void testStrategyCorrectlySetPollIntervalComplex() {
		UI ui = createUIpushDisabled();
		UI ui2 = createUIpushDisabled();

		DefaultUIUpdateStrategy strategy = new DefaultUIUpdateStrategy(250);
		
		strategy.runBeforeBackgroundTask(ui);
		strategy.runBeforeBackgroundTask(ui2);
		strategy.runBeforeBackgroundTask(ui);
		strategy.runBeforeBackgroundTask(ui2);
		
		verify(ui, atLeastOnce()).setPollInterval(250);
		verify(ui2, atLeastOnce()).setPollInterval(250);
		
		strategy.runAfterBackgroundTaskAndUIUpdate(ui);
		strategy.runAfterBackgroundTaskAndUIUpdate(ui2);
		strategy.runAfterBackgroundTaskAndUIUpdate(ui2);
		strategy.runAfterBackgroundTaskAndUIUpdate(ui);

		verify(ui).setPollInterval(-1);
		verify(ui2).setPollInterval(-1);
	}

	private UI createUIpushDisabled() {
		UI ui = mock(UI.class);
		PushConfiguration pushConf = mock(PushConfiguration.class);
		
		when(ui.getPollInterval()).thenReturn(-1);
		when(ui.getPushConfiguration()).thenReturn(pushConf);
		when(pushConf.getPushMode()).thenReturn(PushMode.DISABLED);
		return ui;
	}

}
