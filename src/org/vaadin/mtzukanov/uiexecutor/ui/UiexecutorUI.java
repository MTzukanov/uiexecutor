package org.vaadin.mtzukanov.uiexecutor.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.annotation.WebServlet;

import org.vaadin.mtzukanov.uiexecutor.BackgroundUIExecutor;
import org.vaadin.mtzukanov.uiexecutor.BackgroundUIRunnable;
import org.vaadin.mtzukanov.uiexecutor.DefaultBackgroundUIExecutor;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("uiexecutor")
public class UiexecutorUI extends UI {

//	private final Logger log = Logger.getLogger(getClass().getName());
	private static ExecutorService pool = Executors.newFixedThreadPool(1);

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = UiexecutorUI.class)
	public static class Servlet extends VaadinServlet {
	}
	
	@Override
	public void setPollInterval(int intervalInMillis) {
		super.setPollInterval(intervalInMillis);
		System.out.println("setPollingInterval " + intervalInMillis);
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		BackgroundUIExecutor bexec = new DefaultBackgroundUIExecutor(pool);

		layout.addComponent(new Button("Show poll interval", e -> Notification.show("Poll interval: " + this.getPollInterval())));
		
		Button button1 = new Button("Click Me 2");
		button1.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				final Future<?> future = bexec
						.execute(new BackgroundUIRunnable() {
							@Override
							public void runInUIBefore() {
								layout.addComponent(new Label("before exec"));
							}

							@Override
							public void runInBackground(UI ui) throws Throwable {
								backgroundTask();
							}

							@Override
							public void runInUIAfter(Throwable ex) {
								layout.addComponent(new Label("after exec "
										+ ex));
							}
						});
				layout.addComponent(new Button("canel exec", e -> future
						.cancel(true)));
			}
		});
		layout.addComponent(button1);
	}

	private void backgroundTask() {
		System.out.println("ui in back: " + UI.getCurrent());
		
		for (int i = 0; i < 5; i++) {
			System.out.println("background thread " + i + "  "
					+ Thread.currentThread().getId());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				System.out.println("thread interrupted " + Thread.interrupted()
						+ " " + Thread.currentThread().getId());
				return;
			}
		}
	}
}