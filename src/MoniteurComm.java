/**
 * @author Rémi Grison
 */

// Les imports nécessaires
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;

public class MoniteurComm implements Runnable {
    // Attributs de la classe

    // Le port à interroger pour entrer en communication avec ce petit serveur
    private int port;
    private List<LigneRoutage> tableRoutage;

    /**
     * Constructeur de la classe
     * @param port         le port à interroger pour entrer en communication avec ce petit serveur
     * @param tableRoutage la table de routage du pair qui instancie un MoniteurComm
     */
    public MoniteurComm(int port, List<LigneRoutage> tableRoutage) {
        this.port = port;
        this.tableRoutage = tableRoutage;
    }


    public int getPort() {
        return this.port;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public List<LigneRoutage> getTableRoutage() {
        return this.tableRoutage;
    }

    public void setTableRoutage(Liste<LigneRoutage> tr) {
        this.tableRoutage = tr;
    }



    /*
     * Fonction pour retourner les pairs que l'on connaît (dans tableRoutage)
     * sous la forme hash:hash(succ):ip(succ)
     */
    private List<String> donnerPairs() {
        List<String> lesPairs = new ArrayList<String>();
        Set<Integer> pairsConnus = this.tableRoutage.keySet();

        String tmp;
        Map<Integer, String> mapTmp;
        Integer elem2;
        String elem3;
        for (Integer i: pairsConnus) {
            mapTmp = this.tableRoutage.get(i);

            elem2 = (Integer)mapTmp.keySet().toArray()[0]; // récup du hash du successeur
            elem3 = (String)mapTmp.values().toArray()[0]; // récup de l'IP du successeur

            // Constitution de la chaîne au format correct
            tmp = new String();
            tmp += i + ":" + elem2 + ":" + elem3;

            lesPairs.add(tmp);
        }

        return lesPairs;
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
            )
            {
                String messageRecu = in.readLine();
                if (messageRecu != null && messageRecu.equals("rt?")) {
                    List<String> pairsConnus = this.donnerPairs();
                    for (String strPair: pairsConnus) {
                        out.println(strPair);
                    }
                }
            }
            catch (IOException ioe)
            {
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
        return serv;
    }
}
