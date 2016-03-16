import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import java.net.*;
import java.util.Scanner;
import java.io.IOException;
import java.net.InetAddress;

public class Pair implements Runnable {

    private int hash;
    List<LigneRoutage> tableRoutage;
    private int port;
    private String ip;

    //Constructeur
    public Pair(ClientHashServer clHash, ClientWelcomeServer clWelcome) {

        //récupération de l'adresse IP publique de la machine
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
            System.err.println(uhe.getMessage());
        }

        //Communication avec le HashServer
        System.out.println("Début de la communication avec le serveur de hash à l'adresse " + clHash.getAdresse());
        this.hash = clHash.communiquer(this.ip);
        if(this.hash == -1) {
            System.out.println("Il n'y a plus de hash disponible, vous ne pouvez pas rejoindre le réseau P2P");
            System.exit(0);
        }
        System.out.println("Fin de la communication avec le serveur de hash\n");

        //Communication avec le WelcomeServer
        System.out.println("Début de la communication avec le serveur Welcome à l'adresse " + clWelcome.getAdresse());
        this.tableRoutage = clWelcome.communiquer(this.ip, this.hash);
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
    public void setTableRoutage(List<LigneRoutage> tableRoutage) {
        this.tableRoutage = tableRoutage;
    }
    public void setPort(int port) {
        this.port = port;
    }

    //Accesseurs
    public List<LigneRoutage> getTableRoutage(){
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
    public String getIp() {
        return this.ip;
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
        List<Thread> threads = new ArrayList<Thread>();
        //écoute du port
        try (ServerSocket serverSock = new ServerSocket(this.port);) {
            //récupération des demandes Client
            while(true) {
                try {
                    Socket sock = serverSock.accept();
                    // à chaque client accepté, on crée un nouveau PairThread et on le lance (lance sa méthode run())
                    // On doit stocker les threads pour qu'ils restent actifs
                    threads.add(new Thread(new PairThread(this.tableRoutage, sock)));
                    threads.get(threads.size() - 1).start();
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
        for (LigneRoutage ligne : pair.getTableRoutage()) {
            System.out.println(ligne.toString());
        }


        //Création d'un thread d'écoute du MonitorServer
        // Port spécifique pour tester sur une seule machine
        MoniteurComm moniteurComm = new MoniteurComm(2010, pair.getTableRoutage());
        Thread threadMC = new Thread(moniteurComm);
        threadMC.start();

        //Création d'un thread pour la partie serveur
        Thread threadServ = new Thread(pair);
        threadServ.start();


        //Gestion du côté client du pair
        int h;
        ClientPair clPair;
        while(true) {
            System.out.println("Quel pair souhaitez-vous contacter (donnez un hash) ?");
            Scanner sc = new Scanner(System.in);
            String hash_str = sc.nextLine();
            h = safeParseInt(hash_str);

            //Si c'est notre prédecesseur que l'on souhaite contacter, alors on lui envoie le message et il va nous recontacter car il sera le destinataire
            if(h == pair.getPredecesseur().getHashDestinataire()) {
                clPair = new ClientPair(pair.port, pair.getPredecesseur().getIpDestinataire());
            } else {
                clPair = new ClientPair(pair.port, pair.getPredecesseur().getIpDestinataire()); //sinon, on contacte notre successeur
            }

            clPair.communiquer(pair.getIp(), h);

        } //S'ASSURER QUE, UNE FOIS COMMUNICATION TERMINEE AVEC UN PAIR, CAPABLE D'EN RECONTACTER UN AUTRE
          // => A priori, ça me semble être le cas là
    }
}
