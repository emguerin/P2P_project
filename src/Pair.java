import java.util.Map;
import java.net.Socket;
import java.net.*;
import java.util.Scanner;
import java.io.IOException;
import java.net.InetAddress;

public class Pair implements Runnable {

    private int hash;
    ArrayList<LigneRoutage> tableRoutage;
    private int port;
    private String ip;

    //Constructeur
    public Pair(ClientHashServer clHash, ClientWelcomeServer clWelcome) {

        //récupération de l'adresse IP publique de la machine
        ip = InetAddress.getLocalHost().getHostAddress();

        //Communication avec le HashServer
        System.out.println("Début de la communication avec le serveur de hash à l'adresse " + clHash.getAdresse());
        this.hash = clHash.communiquer(); //METTRE ip EN PARAM 
        if(this.hash == null) {
            System.out.println("Il n'y a plus de hash disponible, vous ne pouvez pas rejoindre le réseau P2P");
            System.exit(0);
        }
        System.out.println("Fin de la communication avec le serveur de hash\n");

        //Communication avec le WelcomeServer
        System.out.println("Début de la communication avec le serveur Welcome à l'adresse " + clWelcome.getAdresse());
        this.tableRoutage = clWelcome.communiquer(); //METTRE IP ET HASH EN PARAM
        if(this.tableRoutage == null) {
            System.out.println("Il y a eu un problème lors de l'obtention de votre table de routage. Vous allez être déconnecté");
            System.exit(0);
        }
        System.out.println("Fin de la communication avec le serveur Welcome\n");

        this.port = 2016;
    }


    //Modifieurs
    public void setHash(int hash) {
        this.hash = hash;
    }
    public void setTableRoutage(ArrayList<LigneRoutage> tableRoutage) {
        this.tableRoutage = tableRoutage;
    }
    public void setPort(int port) {
        this.port = port;
    }

    //Accesseurs
    public ArrayList<LigneRoutage> getTableRoutage(){
        return this.tableRoutage;
    }
    //Ip est à envoyer au serveur de hachage si on veut récupérer le hash du successeur
    public LigneRoutage getSuccesseur() {
        return this.tableRoutage.get(1);
    }
    public LigneRoutage getPredecesseur() {
        return this.tableRoutage.get(0);
    }
    public int getPort(){
        return this.port;
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


    //Partie Serveur
    @Override
    public void run() {
        //écoute du port 
        try {
            ServerSocket serverSock = new ServerSocket(this.port);
            //récupération des demandes Client
            Socket sock;
            while(true) {
                try {
                    sock = serverSock.accept();
                    //à chaque client accepté, on crée un nouveau PairThread et on le lance (lance sa méthode run())
                    Thread th = new Thread(new PairThread(this.tableRoutage, sock));
                    th.start(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
    } 



    //Partie Client ( + Thread d'écoute du MonitorServer + Thread de la partie serveur )
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Il faut 4 arguments : le port et l'adresse IP du serveur Welcome, le port et l'IP du serveur Hash");
            System.exit(1);
        }
        int    portWelcome    = Integer.parseInt(args[0]);
        String adresseWelcome = args[1];
        int    portHash       = Integer.parseInt(args[2]);
        String adresseHash    = args[3];

        ClientHashServer clHash = new ClientHashServer(portHash, adresseHash);
        ClientWelcomeServer clWelcome = new ClientWelcomeServer(portWelcome, adresseWelcome);

        Pair pair = new Pair(clHash, clWelcome);


        //Création d'un thread d'écoute du MonitorServer
        MoniteurComm moniteurComm = new MoniteurComm(pair.getPort(), pair.getTableRoutage());
        Thread threadMC = new Thread(moniteurComm);
        threadMC.start();

        //Création d'un thread pour la partie serveur
        Thread threadServ = new Thread(pair);
        threadServ.start();


        //Gestion du côté client du pair
        Scanner sc = new Scanner(System.in);
        int h;
        ClientPair clPair;
        while(true) {
            System.out.println("Quelle pair souhaitez-vous contacter (donnez un hash) ?");
            h = safeParseInt(sc.nextLine());
               
            //Si c'est notre prédecesseur que l'on souhaite contacter, alors on lui envoie le message et il va nous recontacter car il sera le destinataire
            if(h == this.getPredecesseur().getHashDestinaire()) {
                clPair = new ClientPair(this.port, this.getPredecesseur().getIpDestinaire());
            } else {
                clPair = new ClientPair(this.port, this.getPredecesseur().getIpDestinaire()); //sinon, on contacte notre successeur
            }

            clPair.communiquer();

        } //S'ASSURER QUE, UNE FOIS COMMUNICATION TERMINEE AVEC UN PAIR, CAPABLE D'EN RECONTACTER UN AUTRE

    }
}