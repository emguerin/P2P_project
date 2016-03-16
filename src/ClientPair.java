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

    public void communiquer(String ip, int h) {
        Socket sock = etablirConnexion();

        OutputStream fluxSortie = null;

        try {
            fluxSortie = sock.getOutputStream();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage() + " 5");
        }

        PrintWriter    sortie = new PrintWriter(fluxSortie, true);

        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez le contenu du message que vous souhaitez envoyer ");

        String msg;
        
        msg = sc.nextLine();

        msg = "msg:"+h+":"+ip+":"+msg;

        // On écrit sur la sortie
        sortie.println(msg);
    }

    /*
     * Méthode appelée pour envoyer le message donné en paramètre à son destinataire
     *
     */
    public void transmettreMessage(String mess) {        
        /*
         * Mise en place des flux et des buffers pour écrire/recevoir les
         * messages + facilement
         */
        try (
            Socket sock = new Socket(this.adresseServeur, this.port);
            PrintWriter sortie = new PrintWriter(sock.getOutputStream(), true);
            )
        {
            sortie.println(mess);
        } 
        catch (IOException ioe) {
            System.err.println(ioe.getMessage() + " 3");
        }
    }
    

    public Socket etablirConnexion() {
	       Socket sock = null;
           try {
               sock = new Socket(this.adresseServeur, this.port);
           } catch (UnknownHostException uhe) {
               System.err.println(uhe.getMessage() + " 4");
           } catch (IOException ioe) {
               System.err.println(ioe.getMessage() + " 6");
           }

           return sock;
    }

    public String lireMessage(BufferedReader entree) {
        String entreeLue = "";
        try {
            entreeLue = entree.readLine();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage() + " 7");
        }
        return entreeLue;
    }


}
