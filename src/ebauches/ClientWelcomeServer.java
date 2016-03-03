import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientWelcomeServer implements Client {
    protected int    port;
    protected String adresseServeur;

    public ClientWelcomeServer(int port, String adresseServeur) {
        this.port           = port;
        this.adresseServeur = adresseServeur;
    }

    @Override
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
        * table de routage du pair (création du pair dans ApplicationClient une fois la communication terminée avec WelcomeServer)
        */
    }

    @Override
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

    @Override
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
