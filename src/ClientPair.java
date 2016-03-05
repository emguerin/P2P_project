import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientPair {
    protected int    port;
    protected String adresseServeur;

    public ClientPair(int port, String adresseServeur) {
        this.port           = port;
        this.adresseServeur = adresseServeur;
    }


///////////////////////////////////////////////////////////////////////////////
/*Code de merde à suivre, c'est celui de ClientWelcomeServer
*Ici, il va falloir checker si on est connecté avec le destinataire voulu et
*
*si oui : 
*on communique avec lui puis quand c'est terminé on retourne null
*
*si non:
*on retourne l'ip du successeur de notre successeur*
*/
///////////////////////////////////////////////////////////////////////////////    
    public void communiquer() {
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
