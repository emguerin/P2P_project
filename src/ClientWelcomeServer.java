import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class ClientWelcomeServer {
    private int    port;
    private String adresseServeur;
    final static int LE_PORT = 8001;

    public ClientWelcomeServer(int port, String adresseServeur) {
        this.port           = port;
        this.adresseServeur = adresseServeur;
    }

    public String getAdresse() {
        return this.adresseServeur;
    }

    /*
     * Le type de retour est List et non ArrayList car c'est l'interface
     * C'est une bonne pratique à adopter et si on veut changer de classe
     * (LinkedList par ex.) ce sera plus facile
     */
    public List<LigneRoutage> communiquer(String notreIP, int hash) {
        // Instanciation de la table de routage
        List<LigneRoutage> tr = new ArrayList<LigneRoutage>();

        try (
        /*
         * Tout ce qui est dans ces parenthèses sera fermé automatiquement
         * à la fin du bloc try
         */
        Socket sock = etablirConnexion();
        BufferedReader entree = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        PrintWriter    sortie = new PrintWriter(sock.getOutputStream(), true);
        ){
            Scanner sc = new Scanner(System.in);
            System.out.println("Demandez à entrer dans le réseau P2P en envoyant un message de la forme yo:hash(ip):ip");

            String msg;
            String entreeLue;

            msg = sc.nextLine();

            // On écrit sur la sortie
            sortie.println(msg);

            // Cette méthode gère le try / catch
            entreeLue = this.lireMessage(entree);

            System.out.println("Message retourné par le serveur Welcome : " + entreeLue);

            if (entreeLue.equals("wrq")) {
                System.err.println("Mauvaise requête");
                return null;
            }

            /*
             * Si on recoit le msg yaf, alors on dit que notre successeur est
             * nous-même
             *
             * Sinon, on recoit l'IP de quelqu'un d'autre du réseau
             */
            if (entreeLue.equals("yaf")) {
                tr.add(new LigneRoutage(hash, hash, notreIP));
                tr.add(new LigneRoutage(hash, hash, notreIP));
            } else {
                /*
                 * On reçoit une IP d'une personne du réseau
                 * on doit le communiquer et lui dire qu'on veut rentrer sur
                 * le serveur avec un message de la forme : yo:hash(soi):ip(soi)
                 * ensuite on aura la table de routage. On nous envoie d'abord
                 * la ligne concernant le prédecesseur puis la ligne de notre
                 * successeur
                 */
                String ipMembre = entreeLue;

                /*
                 * On récupère les infos nécessaires sur notre successeur et
                 * notre prédecesseur au membre dont on a l'IP
                 */
                LigneRoutage succ = null;
                LigneRoutage pred = null;

                // succ et pred sont instanciés dans la fonctions
                List<LigneRoutage> liste = this.recupererPairs(succ, pred, ipMembre, notreIP, hash);
                System.out.println("Dans ClientWelcomeServer : communiquer() :");
                System.out.println("pred : " + liste.get(0));
                System.out.println("succ : " + liste.get(1));
                tr.add(liste.get(0));
                tr.add(liste.get(1));
            }
        } catch (UnknownHostException uhe) {
            System.err.println(uhe.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage() + " dans communiquer ClientWelcomeServer");
        }

        return tr;
    }


    private List<LigneRoutage> recupererPairs(LigneRoutage succ, LigneRoutage pred, String ip, String notreIP, int hash) {
        List<LigneRoutage> retour = new ArrayList<LigneRoutage>();

        try (
        Socket sock = new Socket(ip, 2016);
        BufferedReader entree = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        PrintWriter    sortie = new PrintWriter(sock.getOutputStream(), true);
        ) {
            /*
             * La communication. On nous renverra :
             *   - pour le prédécesseur : hash_pred:mon_hash:mon_ip
             *   - pour le successeur   : hash_moi:hash_lui:ip_lui
             *
             * On récupère
             */
            sortie.println("yo:" + hash + ":" + notreIP);
            String predecesseur = this.lireMessage(entree);
            System.out.println("string predecesseur : " + predecesseur);
            String successeur   = this.lireMessage(entree);
            System.out.println("string successeur : " + successeur);

            pred = new LigneRoutage(hash + ":" + predecesseur);
            succ = new LigneRoutage(hash + ":" + successeur);

            retour.add(pred);
            retour.add(succ);
        } catch (UnknownHostException uhe) {
            System.out.println(uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage() + " dans ClientWelcomeServer recupererPairs");
        }

        return retour;
    }


    public Socket etablirConnexion() {
	       Socket sock = null;
           try {
               sock = new Socket(this.adresseServeur, this.port);
           } catch (UnknownHostException uhe) {
               System.err.println(uhe.getMessage());
           } catch (IOException ioe) {
               System.err.println(ioe.getMessage() + " dans ClientWelcomeServer etablirConnexion");
           }

           return sock;
    }


    public String lireMessage(BufferedReader entree) {
        String entreeLue = "";
        try {
            entreeLue = entree.readLine();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage() + " lireMessage de ClientWelcomeServer");
        }
        return entreeLue;
    }


}
