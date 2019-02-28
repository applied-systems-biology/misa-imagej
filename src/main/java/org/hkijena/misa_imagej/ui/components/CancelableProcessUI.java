package org.hkijena.misa_imagej.ui.components;

import org.hkijena.misa_imagej.ui.repository.MISAModuleRepositoryUI;
import org.hkijena.misa_imagej.utils.ProcessStreamToStringGobbler;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CancelableProcessUI extends JDialog {

    public enum Status {
        Ready,
        Running,
        Done,
        Canceled,
        Failed
    }

    private Worker worker;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Status status = Status.Ready;
    private JTextArea statusLabel;
    private JProgressBar currentTaskProgress;
    private JProgressBar processProgress;
    private int currentTask = 0;

    private static final Pattern percentagePattern = Pattern.compile("<(\\d+)%>.*");

    public CancelableProcessUI(List<ProcessBuilder> processes) {
        setTitle("Working ...");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout(8, 8));

        JPanel centerPanel = new JPanel(new BorderLayout(8,8));

        statusLabel = new JTextArea("Please wait ...");
        statusLabel.setEditable(false);
        statusLabel.setBorder(null);
        statusLabel.setLineWrap(true);
        statusLabel.setWrapStyleWord(true);
        statusLabel.setOpaque(false);
        centerPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        currentTaskProgress = new JProgressBar();
        currentTaskProgress.setIndeterminate(true);
        progressPanel.add(currentTaskProgress);

        processProgress = new JProgressBar();
        processProgress.setMaximum(processes.size());
        processProgress.setValue(0);
        processProgress.setString("0 / " + processes.size());
        processProgress.setStringPainted(true);
        progressPanel.add(processProgress);

        centerPanel.add(progressPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        worker = new Worker(this, processes);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        cancelButton.addActionListener(actionEvent -> {
            worker.cancel(true);
            worker.cancelCurrentProcess();
        } );

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void start() {
        if(getStatus() != Status.Ready)
            throw new RuntimeException("Worker is not ready!");
        setStatus(Status.Running);
        setModal(false);
        pack();
        setSize(500,400);
        setVisible(true);
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(Status status) {
        Status old = this.status;
        this.status = status;
        propertyChangeSupport.firePropertyChange("status", old, status);
        if(status == Status.Canceled || status == Status.Done || status == Status.Failed) {
            setVisible(false);
            dispose();
        }
        else if(status == Status.Running) {
            worker.execute();
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    private void updateProgressAndStatus(String stdout, int currentProcess, int numProcesses) {
        statusLabel.setText(stdout.replace("\t", "  "));

        processProgress.setValue(currentProcess);
        processProgress.setString(currentProcess + " / " + numProcesses);

        if(currentTask != currentProcess) {
            currentTaskProgress.setMaximum(100);
            currentTaskProgress.setValue(0);
            currentTaskProgress.setIndeterminate(true);
        }

        Matcher percentageMatch = percentagePattern.matcher(stdout.trim());
        if(percentageMatch.matches()) {
            try {
                int percentage = Integer.parseInt(percentageMatch.group(1));
                currentTaskProgress.setIndeterminate(false);
                currentTaskProgress.setMaximum(100);
                currentTaskProgress.setValue(percentage);
            }
            catch(NumberFormatException e) {
            }
        }

    }

    public class Worker extends SwingWorker<Integer, Object> {

        private final List<ProcessBuilder> processes;
        private volatile Process currentProcess;
        private volatile int currentProcessIndex;
        private final CancelableProcessUI ui;

        public Worker(CancelableProcessUI ui, List<ProcessBuilder> processes) {
            this.ui = ui;
            this.processes = processes;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            for(int i = 0; i < processes.size(); ++i) {
                if(this.isCancelled())
                    return 1;
                currentProcess = processes.get(i).start();
                currentProcessIndex = i;
                new ProcessStreamToStringGobbler(currentProcess.getInputStream(), this::processInput).start();
                new ProcessStreamToStringGobbler(currentProcess.getErrorStream(), this::processError).start();
                int ret = currentProcess.waitFor();
                if(ret != 0)
                    return ret;
            }
            return 0;
        }

        @Override
        protected void done() {
            try {
                if(get() == 0) {
                    setStatus(Status.Done);
                }
                else {
                    setStatus(Status.Failed);
                }
            }
            catch (InterruptedException | ExecutionException | CancellationException e) {
                setStatus(Status.Canceled);
            }
        }

        public void cancelCurrentProcess() {
            if(currentProcess != null)
                currentProcess.destroy();
        }

        private void processInput(String s) {
            MISAModuleRepositoryUI.getInstance().getCommand().getLogService().info(s);
            SwingUtilities.invokeLater(() -> {
                ui.updateProgressAndStatus(s, currentProcessIndex, processes.size());
            });
        }

        private void processError(String s) {
            MISAModuleRepositoryUI.getInstance().getCommand().getLogService().error(s);
        }
    }
}
