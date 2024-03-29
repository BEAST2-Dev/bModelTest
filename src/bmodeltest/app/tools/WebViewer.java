package bmodeltest.app.tools;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;


import beast.base.core.BEASTVersion2;
import beast.base.core.Log;

import java.awt.*;


public class WebViewer extends JFrame {

	private static final long serialVersionUID = 1L;
	private final JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;
	static String title = "BEAST " + new BEASTVersion2().getVersionString();
	static String url = System.getProperty("user.dir") +"/js";
	static int instance = 0;

	private final JPanel panel = new JPanel(new BorderLayout());

	public WebViewer() {
		super();
	}

	public WebViewer(String title, String url) {
		WebViewer.title = WebViewer.title + " " + title;
		WebViewer.url = url;
		WebViewer.main(new String[] {});
	}

	public void initComponents() {
		createScene();

		panel.add(jfxPanel, BorderLayout.CENTER);

		getContentPane().add(panel);

		setPreferredSize(new Dimension(1024, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setTitle(title);

	}

	private void createScene() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				WebView view = new WebView();
				engine = view.getEngine();

		        // load the home page        
				engine.load(url);

				jfxPanel.setScene(new Scene(view));
			}
		});
	}


	public static void main(String[] args) {
		WebViewer browser = new WebViewer();
		browser.initComponents();
		browser.setTitle(title);
		browser.setVisible(true);
		Log.info.println("ok");
	}
}
