import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;


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

    public Map<Integer, Map<Integer, String>> communiquer() {
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

        System.out.println("Message retourné par le serveur Welcome : "+entreeLue);

        if(entreeLue.equals("wrq")) {
            AppliClient.wrq = true;
        }

        /*
        * Si on recoit le msg yaf, alors on dit que succ est lui-même
        *
        * Sinon, on recoit l'IP du successeur comme message (au lieu de yaf) et on doit la conserver pour créer la
        * table de routage du pair
        */

        // Instanciation de la table de routage
        Map<Integer, Map<Integer, String>> tr = new HashMap<Integer, Map<Integer, String>>();
        if (entree.readLine().equals("yaf")) {
            Map<Integer, String> soi = new HashMap<Integer, String>();

            /*
             * On ajoute la 2ème partie de la ligne dans la map
             */
            try {
                soi.put(this.hash, InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            }

            tr.put(this.hash, soi);
        } else {
            /*
             * On reçoit une IP d'une personne du réseau
             * on doit le communiquer et lui dire qu'on veut rentrer sur
             * le serveur avec un message de la forme : "TRUC"
             * ensuite on aura la table de routage
             */
            String ipSucc = entree.readLine();
            ClientHashServer chs = new ClientHashServer(LE_PORT, this.adresseServeur);
            // Passer l'IP voulue au HashServer
            int hashSucc = chs.communiquer();
            Map<Integer, String> succ = new HashMap<Integer, String>();
            succ.put(hashSucc, ipSucc);
            tr.put(this.hash, succ);
        }

        return tr;
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
