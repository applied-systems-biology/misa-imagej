package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for threaded and cancelable saving of an Attachment
 */
public class MISAAttachmentSaverDialogUI extends JDialog {

    private Path exportPath;
    private List<MISAAttachment> attachments;
    private MISAAttachmentDatabase database;
    private int[] databaseIds;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private Worker worker;
    private volatile boolean isDone = false;


    public MISAAttachmentSaverDialogUI(Path exportPath, MISAAttachment attachment) {
        this.exportPath = exportPath;
        this.attachments = Arrays.asList(attachment);
        initialize();
    }

    public MISAAttachmentSaverDialogUI(Path exportPath, List<MISAAttachment> attachments) {
        this.exportPath = exportPath;
        this.attachments = attachments;
        initialize();
    }

    public MISAAttachmentSaverDialogUI(Path exportPath, MISAAttachmentDatabase database, int[] databaseIds) {
        this.database = database;
        this.exportPath = exportPath;
        this.databaseIds = databaseIds;
        initialize();
    }

    private void initialize() {
        setTitle("Saving as JSON");

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JTextArea warningLabel = new JTextArea();
        warningLabel.setBorder(null);
        warningLabel.setLineWrap(true);
        warningLabel.setWrapStyleWord(true);
        warningLabel.setEditable(false);
        warningLabel.setText("Please wait until the process is finished. This operation can take some time if there is a lot of data.");
        add(warningLabel);

        add(Box.createVerticalGlue());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("- / -");
        add(progressBar);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(Box.createVerticalStrut(4));

        cancelButton = new JButton("Cancel", UIUtils.getIconFromResources("remove.png"));
        cancelButton.addActionListener(e -> cancelOperation());
        buttonPanel.add(cancelButton);

        add(buttonPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if(isDone) {
                    SwingUtilities.invokeLater(() -> setVisible(false));
                }
            }
        });
    }

    private void cancelOperation() {
        if(worker != null) {
            worker.cancel(false);
        }
        else {
            setVisible(false);
            isDone = true;
        }
    }

    public void startOperation() {
        worker = new Worker(exportPath, attachments, database, databaseIds);
        worker.getEventBus().register(this);
        worker.execute();
    }

    @Subscribe
    public void handleProgressEvent(ProgressEvent event) {
        progressBar.setMaximum(event.getMaximum());
        progressBar.setValue(event.getProgress());
        progressBar.setString(event.getProgress() + " / " + event.getMaximum());
    }

    @Subscribe
    public void handleWorkDoneEvent(WorkDoneEvent event) {
        worker = null;
        setVisible(false);
        isDone = true;
    }

    private static class Worker extends SwingWorker {

        private Path exportPath;
        private EventBus eventBus = new EventBus();

        private List<MISAAttachment> attachments;
        private MISAAttachmentDatabase database;
        private int[] databaseIds;

        private Worker(Path exportPath, List<MISAAttachment> attachments, MISAAttachmentDatabase database, int[] databaseIds) {
            this.exportPath = exportPath;
            this.attachments = attachments;
            this.database = database;
            this.databaseIds = databaseIds;
        }

        @Override
        protected Object doInBackground() throws Exception {
            Gson gson = GsonUtils.getGson();

            final int count = attachments != null ? attachments.size() : databaseIds.length;

            SwingUtilities.invokeLater(() -> eventBus.post(new ProgressEvent(0, count)));
            try(JsonWriter writer = new JsonWriter(new FileWriter(exportPath.toFile()))) {
                writer.setIndent("    ");
                writer.setSerializeNulls(true);
                writer.beginObject();

                for(int i = 0; i < count; ++i) {

                    MISAAttachment attachment;
                    if(attachments != null)
                        attachment = attachments.get(i);
                    else
                        attachment = database.queryAttachmentAt(databaseIds[i]);

                    writer.name(attachment.getAttachmentFullPath());
                    gson.toJson(attachment.getFullJson(), JsonObject.class, writer);
                    // Update progress
                    int finalI = i + 1;
                    SwingUtilities.invokeLater(() -> eventBus.post(new ProgressEvent(finalI, count)));
                }
                writer.endObject();
            }

            return null;
        }

        @Override
        protected void done() {
            super.done();
            getEventBus().post(new WorkDoneEvent(isCancelled()));
        }

        public EventBus getEventBus() {
            return eventBus;
        }
    }

    public static class ProgressEvent {
        private int progress;
        private int maximum;

        private ProgressEvent(int progress, int maximum) {
            this.progress = progress;
            this.maximum = maximum;
        }

        public int getProgress() {
            return progress;
        }

        public int getMaximum() {
            return maximum;
        }
    }

    public static class WorkDoneEvent {
        private boolean canceled;

        public WorkDoneEvent(boolean canceled) {
            this.canceled = canceled;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
