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
    private int    hash;
    final static int LE_PORT = 8001;

    public ClientWelcomeServer(int port, String adresseServeur, int hash) {
        this.port           = port;
        this.adresseServeur = adresseServeur;
        this.hash           = hash;
    }

    public List<LigneRoutage> communiquer() {
        Socket sock = etablirConnexion();

        /*
         * Mise en place des flux et des buffers pour écrire/recevoir les
         * messages + facilement
         */
        InputStream  fluxEntree = null;
        OutputStream fluxSortie = null;

        try {
            fluxEntree = sock.getInputStream();
            fluxSortie = sock.getOutputStream();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        BufferedReader entree = new BufferedReader(new InputStreamReader(fluxEntree));
        PrintWriter    sortie = new PrintWriter(fluxSortie, true);

        Scanner sc = new Scanner(System.in);
        System.out.println("Demandez à entrer dans le réseau P2P en envoyant un message de la forme yo:hash(ip):ip");

        String msg;
        String entreeLue;

        msg = sc.nextLine();

        // On écrit sur la sortie
        sortie.println(msg);

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

        // Instanciation de la table de routage
        List<LigneRoutage> tr = new ArrayList<LigneRoutage>();
        String notreIP = null;
        try {
            notreIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
            System.err.println(uhe.getMessage());
        }

        if (entree.readLine().equals("yaf")) {
            tr.add(new LigneRoutage(this.hash, this.hash, notreIP));
        } else {
            /*
             * On reçoit une IP d'une personne du réseau
             * on doit le communiquer et lui dire qu'on veut rentrer sur
             * le serveur avec un message de la forme : yo:hash(soi):ip(soi)
             * ensuite on aura la table de routage. On nous envoie d'abord
             * la ligne concernant le prédecesseur puis la ligne de notre
             * successeur
             */
            String ipMembre = entree.readLine();

            /*
             * On récupère les infos nécessaires sur notre successeur et
             * notre prédecesseur au membre dont on a l'IP
             */
            LigneRoutage succ = null;
            LigneRoutage pred = null;

            // succ et pred sont instanciés dans la fonctions
            this.recupererPairs(succ, pred, ipMembre);
            tr.add(pred);
            tr.add(succ);
        }

        return tr;
    }


    private int recupererPairs(LigneRoutage succ, LigneRoutage pred, String ip) {
        // 1. Mise en place des objets pour communiquer
        Socket sock;
        try {
            sock = new Socket(ip, 2016); // port 2016
        } catch (UnknownHostException uhe) {
            System.out.println(uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }

        InputStream  fluxEntree = null;
        OutputStream fluxSortie = null;

        try {
            fluxEntree = sock.getInputStream();
            fluxSortie = sock.getOutputStream();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        BufferedReader entree = new BufferedReader(new InputStreamReader(fluxEntree));
        PrintWriter    sortie = new PrintWriter(fluxSortie, true);

        /*
         * 2. La communication. On nous renverra :
         *   - pour le prédécesseur : hash_pred:mon_hash:mon_ip
         *   - pour le successeur   : hash_moi:hash_lui:ip_lui
         *
         * On récupère
         */
        String predecesseur = entree.readLine();
        String successeur   = entree.readLine();

        pred = new LigneRoutage(predecesseur);
        succ = new LigneRoutage(successeur);
    }


    public Socket etablirConnexion() {
	       Socket sock = null;
           try {
               sock = new Socket(this.adresseServeur, this.port);
           } catch (UnknownHostException uhe) {
               System.err.println(uhe.getMessage());
           } catch (IOException ioe) {
               System.err.println(ioe.getMessage());
           }

           return sock;
    }


    public String lireMessage(BufferedReader entree) {
        String entreeLue = "";
        try {
            entreeLue = entree.readLine();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return entreeLue;
    }


}
