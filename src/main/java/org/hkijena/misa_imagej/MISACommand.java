package org.hkijena.misa_imagej;

import javax.swing.SwingUtilities;

import io.scif.services.DatasetIOService;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import org.hkijena.misa_imagej.ui.repository.MISAModuleRepositoryUI;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

@Plugin(type = Command.class, menuPath = "Plugins>MISA++ for ImageJ ...")
public class MISACommand implements Command {

	@Parameter
	OpService ops;

	@Parameter
	LogService log;

	@Parameter
	UIService ui;

	@Parameter
	CommandService cmd;

	@Parameter
	StatusService status;

	@Parameter
	ThreadService thread;

	@Parameter
    DatasetIOService datasetIO;

	@Parameter
	DisplayService display;

	@Parameter
    DatasetService datasetService;
	/**
	 * show a dialog and give the dialog access to required IJ2 Services
	 */
	@Override
	public void run() {
		SwingUtilities.invokeLater(() -> {
			MISAModuleRepositoryUI.getInstance(this).setVisible(true);
		});
	}

	public LogService getLogService() {
		return log;
	}

	public StatusService getStatusService() {
		return status;
	}

	public ThreadService getThreadService() {
		return thread;
	}

	public UIService getUiService() {
		return ui;
	}

	public DatasetIOService getDatasetIOService() {
		return datasetIO;
	}

	public DisplayService getDisplayService() {
		return display;
	}

	public DatasetService getDatasetService() {
		return datasetService;
	}

	public static void main(final String... args) {
//		fiji.Debug.runPlugIn(MISACommand.class.getRemovedSampleName(), null, false);
//		fiji.Debug.run("MISA ImageJ", "");
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ij.command().run(MISACommand.class, true);
	}
}

