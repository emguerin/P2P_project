import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientHashServer {
    protected int    port;
    protected String adresseServeur;

    public ClientHashServer(int port, String adresseServeur) {
        this.port           = port;
        this.adresseServeur = adresseServeur;
    }

    public int communiquer() {
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
        System.out.println("Rentrez votre adresse IP :");

        String msg;
        String entreeLue;
      
        msg = sc.nextLine();

        // On écrit sur la sortie
        sortie.println(msg);

        entreeLue = this.lireMessage(entree);

        System.out.println("Vous obtenez le hash : "+entreeLue);

        if(entreeLue.equals("aht")) {
            AppliClient.aht = true;
        }

        return this.safeParseInt(entreeLue);
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


    // Parsing d'une chaîne de caractères pour trouver un entier
    // En cas d'erreur l'entier retourné est négatif
    private static int safeParseInt(String i) {

        int res = -1;

        try {
            res = Integer.parseInt(i);
        } finally {
            return res;
        }

    }
}
