package org.hkijena.misa_imagej;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Callable;
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

    private Process process;
    private SwingWorker<Integer, Object> worker;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Status status = Status.Ready;

    public CancelableProcessUI(Process process) {

        this.process = process;

        setTitle("Working ...");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 100);
        setLayout(new BorderLayout(8, 8));

        add(new JLabel("Please wait until the process is finished ..."), BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.CENTER);

        worker = new SwingWorker<Integer, Object>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return process.waitFor();
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
        };

        JButton cancelButton = new JButton("Cancel");
        add(cancelButton, BorderLayout.SOUTH);
        cancelButton.addActionListener(actionEvent -> process.destroy());
    }

    public void showDialog() {
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

}
