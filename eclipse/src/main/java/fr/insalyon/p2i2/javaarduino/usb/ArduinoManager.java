package fr.insalyon.p2i2.javaarduino.usb;

import fr.insalyon.p2i2.javaarduino.util.Console;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import jssc.SerialPortException;

public class ArduinoManager {

    protected final String port;
    protected ArduinoUsbChannel vcpChannel;
    protected Thread readingThread;

    protected boolean readingThreadRunning;

    public ArduinoManager(String port) {
        this.port = port;

        this.readingThreadRunning = false;
    }

    public final void start() throws IOException {

        // Creation d'un flux bidirectionnel entre l'ordi et l'arduino
        this.vcpChannel = new ArduinoUsbChannel(this.port);

        // Creation d'une tache qui va s'exécuter en parallèle du code séquentiel du main
        this.readingThread = new Thread(new Runnable() {

            public void run() {

                ArduinoManager.this.readingThreadRunning = true;

                //création d'un flux bufferisé en entrée à partir de l'Arduino
                BufferedReader vcpInput = new BufferedReader(new InputStreamReader(vcpChannel.getReader()));

                String line;
                try {
                    //lecture et traitement du flux en entrée de l'Arduino
                    while ((line = vcpInput.readLine()) != null) {
                        ArduinoManager.this.onData(line);
                    }

                } catch (IOException ex) {
                    if (ArduinoManager.this.readingThreadRunning) {
                        ex.printStackTrace(System.err);
                    }
                }

                ArduinoManager.this.readingThreadRunning = false;
            }
        });

        this.readingThread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            // Ignore
            ex.printStackTrace(System.err);
        }

        try {
            this.vcpChannel.open();
        } catch (SerialPortException ex) {

            this.readingThread.interrupt();
            this.readingThreadRunning = false;

            throw new IOException("Erreur à l'ouverture du Port VCP", ex);
        }
    }

    public final void stop() throws IOException {

        this.readingThreadRunning = false;

        if (this.vcpChannel != null) {
            //libération du port de communication entre l'arduino et l'ordi
            this.vcpChannel.close();
        }

        if (this.readingThread != null) {
            //Ordre d'interruption de la transmission en entrée
            this.readingThread.interrupt();
            try {
                //attente de la fin du thread (au plus 1000 ms)
                readingThread.join(1000);
            } catch (InterruptedException ex) {
                // Ignore
                ex.printStackTrace(System.err);
            }
        }
    }

    protected void onData(String line) {
        // Cette méthode est à surcharger dans une classe qui hérite de cette classe

        // Affichage de la ligne transmise par l'Arduino
        // System.err.println("Data from Arduino: " + line);
    }

    public final void write(String line) throws IOException {
        if (this.vcpChannel != null) {
            OutputStream writer = this.vcpChannel.getWriter();
            writer.write(line.getBytes("UTF-8"));
            writer.write('\n');
        } else {
            throw new IOException("Erreur: VCP Channel pas encore initialisé");
        }
    }

    public static String searchVirtualComPort(String... exceptions) {

        String myVirtualComPort = null;

        do {

            System.err.println("[ArduinoManager] RECHERCHE d'un port disponible...");

            List<String> virtualComPorts = ArduinoUsbChannel.listVirtualComPorts(exceptions);
            System.err.println("[ArduinoManager] " + virtualComPorts.size() + " port(s) disponible(s)");
            for (String virtualComPort : virtualComPorts) {
                System.err.println("[ArduinoManager] - "  + virtualComPort);
            }

            if (virtualComPorts.size() > 0) {
                myVirtualComPort = virtualComPorts.get(0);
            }

            if (myVirtualComPort == null) {
                System.err.println("[ArduinoManager] Aucun port disponible!");
                System.err.println("[ArduinoManager] Nouvel essai dans 5s");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    // Ignorer l'Exception
                }
            }

        } while (myVirtualComPort == null);

        System.err.println("[ArduinoManager] Port choisi: " + myVirtualComPort);

        return myVirtualComPort;
    }

    public static List<String> listVirtualComPorts(String... exception) {
        return ArduinoUsbChannel.listVirtualComPorts(exception);
    }
}
