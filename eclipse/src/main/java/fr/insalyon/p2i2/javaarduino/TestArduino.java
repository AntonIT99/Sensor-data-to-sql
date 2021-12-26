package fr.insalyon.p2i2.javaarduino;

import fr.insalyon.p2i2.javaarduino.tdtp.BDFlux;
import fr.insalyon.p2i2.javaarduino.usb.ArduinoManager;
import fr.insalyon.p2i2.javaarduino.util.Console;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TestArduino 
{
	static int idMesure = 0;
    
    public static void main(String[] args) {
    	
    	final BDFlux bdFlux = new BDFlux("G222_B_BD2", "G222_B", "G222_B");

        // Objet matérialisant la console d'exécution (Affichage Écran / Lecture Clavier)
        final Console console = new Console();

        // Affichage sur la console
        console.log("DÉBUT du programme TestArduino");

        console.log("TOUS les Ports COM Virtuels:");
        for (String port : ArduinoManager.listVirtualComPorts()) {
            console.log(" - " + port);
        }
        console.log("----");

        // Recherche d'un port disponible (avec une liste d'exceptions si besoin)
        String myPort = ArduinoManager.searchVirtualComPort("COM0", "/dev/tty.usbserial-FTUS8LMO", "<autre-exception>");

        console.log("CONNEXION au port " + myPort);
        
        

        ArduinoManager arduino = new ArduinoManager(myPort) {
        	protected void onData(String line) {
                console.println("ARDUINO >> " + line);
                
                String[] data = line.split(";");
    
                if(data[0].equals("data"))
                {
                	SimpleDateFormat dateFormat= new SimpleDateFormat("yy-MM-dd hh:mm:ss");
                	try {
                		int idMes = bdFlux.recupIdMesure(Integer.parseInt(data[1]));
						bdFlux.ajouterMesure(Integer.parseInt(data[1]), 1+idMes, Double.parseDouble(data[2]), dateFormat.parse(data[3]));
						System.out.println("BD Mise à jour");
                	} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
                }
            }
        };

        try {

            console.log("DÉMARRAGE de la connexion");
            // Connexion à l'Arduino
            arduino.start();

            console.log("BOUCLE infinie en attente du Clavier");
            // Boucle d'ecriture sur l'arduino (execution concurrente au thread)
            boolean exit = false;

            while (!exit) {

                // Lecture Clavier de la ligne saisie par l'Utilisateur
                String line = console.readLine("Envoyer une ligne (ou 'stop') > ");

                if (line.length() != 0) {

                    // Affichage sur l'écran
                    console.log("CLAVIER >> " + line);

                    // Test de sortie de boucle
                    exit = line.equalsIgnoreCase("stop");

                    if (!exit) {
                        // Envoi sur l'Arduino du texte saisi au Clavier
                        arduino.write(line);
                    }
                }
            }

            console.log("ARRÊT de la connexion");
            // Fin de la connexion à l'Arduino
            arduino.stop();

        } catch (IOException ex) {
            // Si un problème a eu lieu...
            console.log(ex);
        }

    }
}
