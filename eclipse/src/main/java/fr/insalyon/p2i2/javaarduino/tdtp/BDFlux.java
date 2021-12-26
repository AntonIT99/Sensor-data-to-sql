package fr.insalyon.p2i2.javaarduino.tdtp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import java.sql.*;

public class BDFlux {

    private Connection conn;
    private PreparedStatement insertMesureStatement;
    private PreparedStatement selectMesuresStatement;

//Constructeur
    public BDFlux(String bd, String compte, String motDePasse) {
    	try {

            //Enregistrement de la classe du driver par le driverManager
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("[MySQL] Driver trouvé...");

            //Création d'une connexion sur la base de donnée
            this.conn = DriverManager.getConnection("jdbc:mysql://PC-TP-MYSQL.insa-lyon.fr:3306/" + bd, compte, motDePasse);
            System.out.println("[MySQL] Connexion établie...");

            // Prepared Statement
            this.insertMesureStatement = this.conn.prepareStatement("INSERT INTO Mesure (idCapteur, idMesure, valeur, dateMesure) VALUES (?,?,?,?) ;");
            this.selectMesuresStatement = this.conn.prepareStatement("SELECT valeur,idMesure numInventaire,dateMesure FROM Mesure WHERE idMesure = ? AND dateMesure >= ? AND dateMesure < ? ;");

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
    }

/**
 * Methode qui permet d'ajouter une valeur dans la BD
 * @param idCapteur : int (identite du capteur)
 * @param idMesure : int (identite de la mesure)
 * @param valeur : double (valeur mesuree par le capteur)
 * @param datetime : date (date + heure a laquelle la mesure a ete faite)
 * @return le nombre de valeur ajoutee dans la BD
 */

    public int ajouterMesure(int idCapteur, int idMesure, double valeur, Date datetime) {
        try {
        	this.insertMesureStatement.setInt(1, idCapteur);
            this.insertMesureStatement.setInt(2, idMesure);
            this.insertMesureStatement.setDouble(3, valeur);
            this.insertMesureStatement.setTimestamp(4, new Timestamp(datetime.getTime())); // DATETIME
            return this.insertMesureStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }
    }
    

/**@param idCapteur: int (numero du capteur en question)
 * @return idMes : int (dernier idMesure enregistre dans la BD
 */
    public int recupIdMesure (int idCapteur) {
    	int idMes=0;
    	try {      
            String sqlStr = "select max(idMesure) from Mesure Where idCapteur="+idCapteur;
            Statement stmt = conn.createStatement();
            
            //execution de la requete
            ResultSet res = stmt.executeQuery(sqlStr);
            System.out.println("Requete executee");
            
            while (res.next()) {
                idMes = res.getInt(1);
            }
        } catch(Exception e){
            //si une erreur se produit, affichage du message correspondant
            System.out.println(e.getMessage());
            System.exit(0);
        }
		return idMes;
    }
}
