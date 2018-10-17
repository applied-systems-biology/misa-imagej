package org.hkijena.misa_imagej;/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import javax.swing.SwingUtilities;

import ij.plugin.ImagesToStack;
import io.scif.services.DatasetIOService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.text.TextService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

@Plugin(type = Command.class, menuPath = "Plugins>MISA>MISACommand")
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

	private static MISADialog dialog = null;
	/**
	 * show a dialog and give the dialog access to required IJ2 Services
	 */
	@Override
	public void run() {

		SwingUtilities.invokeLater(() -> {
			if (dialog == null) {
				dialog = new MISADialog();
			}
			dialog.setVisible(true);
			dialog.setLog(log);
			dialog.setStatus(status);
			dialog.setThread(thread);
			dialog.setUi(ui);
			dialog.setDatasetIO(datasetIO);
		});
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		ij.command().run(MISACommand.class, true);
	}
}
