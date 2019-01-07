package org.hkijena.misa_imagej;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class ProcessStreamToStringGobbler extends Thread {
    InputStream is;
    Consumer<String> consumer;

    public ProcessStreamToStringGobbler(InputStream is, Consumer<String> consumer) {
        this.is = is;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                consumer.accept(line);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
