/**
 * @author Rémi Grison
 */

// Les imports nécessaires
import java.util.Map;
import java.util.HashMap;

public class MoniteurComm implements Runnable {
    // Attributs de la classe

    // Le port à interroger pour entrer en communication avec ce petit serveur
    private int port;

    /*
     * Une Map pour représenter la table de routage
     * les infos sont organisées comme ceci :
     *   hash(pair) : hash(successeur) : IP successeur
     */
    private Map<Integer, Map<Integer, String>> tableRoutage;


    /**
     * Constructeur de la classe
     * @param port         le port à interroger pour entrer en communication avec ce petit serveur
     * @param tableRoutage la table de routage du pair qui instancie un MoniteurComm
     */
    public MoniteurComm(int port, Map<Integer, Map<Integer, String>> tableRoutage) {
        this.port = port;
        this.tableRoutage = tableRoutage;
    }


    /**
     * Implémentation de la méthode run() de l'interface Runnable
     * permet de gérer la communication avec le MonitorServer
     */
    @Override
    public void run() {
        try (
        ServerSocket serveur = new ServerSocket(this.port);
        ) {
            try (
            Socket s = serveur.accept();

            // Déclaration des canaux de sortie et d'entrée
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ) {
                
            } catch (IOException) {
                System.err.println("Erreur pendant la communication avec le MonitorServer.");
                System.err.println(ioe.getMessage());
            }
        } catch (IOException ioe) {
            System.err.println("Exception à la création du SocketServer");
            System.err.println(ioe.getMessage());
        }
    }

    private ServerSocket obtenirServeur(int port) {
        ServerSocket serv = null;
        try {
            serv = new ServerSocket(port);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
