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

    public CancelableProcessUI(List<ProcessBuilder> processes) {

        setTitle("Working ...");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 100);
        setLayout(new BorderLayout(8, 8));

        add(new JLabel("Please wait until the process is finished ..."), BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.CENTER);

        worker = new Worker(processes);

        JButton cancelButton = new JButton("Cancel");
        add(cancelButton, BorderLayout.SOUTH);
        cancelButton.addActionListener(actionEvent -> {
            worker.cancel(true);
            worker.cancelCurrentProcess();
        } );
    }

    public void start() {
        if(getStatus() != Status.Ready)
            throw new RuntimeException("Worker is not ready!");
        setStatus(Status.Running);
        setModal(false);
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

    public class Worker extends SwingWorker<Integer, Object> {

        private final List<ProcessBuilder> processes;
        private volatile Process currentProcess;

        public Worker(List<ProcessBuilder> processes) {
            this.processes = processes;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            for(ProcessBuilder builder : processes) {
                if(this.isCancelled())
                    return 1;
                currentProcess = builder.start();
                new ProcessStreamToStringGobbler(currentProcess.getInputStream(), s -> MISAModuleRepositoryUI.getInstance().getCommand().getLogService().info(s)).start();
                new ProcessStreamToStringGobbler(currentProcess.getErrorStream(), s -> MISAModuleRepositoryUI.getInstance().getCommand().getLogService().error(s)).start();
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
    }
}
