public class Pair implements Runnable {

    public static boolean aht = false;    
    public static boolean wrq = false;   



    private int hash;
    private Map<Integer, Map<Integer, String>> tableRoutage;
    private int port;

    //Constructeur
    public Pair(int portWelcome, String adresseWelcome, int portHash, String adresseHash) {

        //String myIp =   RECUP l'IP AUTOMATIQUEMENT

        //Communication avec le HashServer
        ClientHashServer clHash = new ClientHashServer(portHash, adresseHash);
        System.out.println("Début de la communication avec le serveur de hash à l'adresse " + adresseHash);
        this.hash = clHash.communiquer(); //METTRE IP EN PARAM 
        System.out.println("Fin de la communication avec le serveur de hash\n");

        if(aht) {
            System.exit(0);
        }

        //Communication avec le WelcomeServer
        ClientWelcomeServer clWelcome = new ClientWelcomeServer(portWelcome, adresseWelcome);
        System.out.println("Début de la communication avec le serveur Welcome à l'adresse " + adresseWelcome);
        this.tableRoutage = clWelcome.communiquer(); //METTRE IP ET HASH EN PARAM
        System.out.println("Fin de la communication avec le serveur Welcome\n");

        if(wrq) {
            System.exit(0);
        }

        this.port = 2016;
    }


    //Modifieurs
    public void setHash(int hash) {
        this.hash = hash;
    }
    public void setTableRoutage(Map<Integer, Map<Integer, String>> tableRoutage) {
        this.tableRoutage = tableRoutage;
    }
    public void setPort(int port) {
        this.port = port;
    }



    //Partie Serveur
    @Override
    public void run() {
        //écoute du port 
        try {
            ServerSocket serverSock = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //récupération des demandes client
        Socket sock;
        while(1) {
            try {
                sock = serverSock.accept();
                //à chaque client accepté, on crée un nouveau PairThread et on le lance (lance sa méthode run())
                Thread th = new Thread(new PairThread(this.tableRoutage, sock));
                th.start(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        Pair pair = new Pair(portWelcome, adresseWelcome, portHash, adresseHash);


        //Création d'un thread d'écoute du MonitorServer
        MoniteurComm moniteurComm = new MoniteurComm(this.port, this.tableRoutage);
        Thread threadMC = new Thread(moniteurComm);
        threadMC.start();

        //Création d'un thread pour la partie serveur
        Thread threadServ = new Thread(pair);
        threadServ.start();



        //Gestion du côté client du pair
        Scanner sc = new Scanner(System.in);
        String ip;
        int h;
        ClientPair clPair;
        while(1) {
            System.out.println("Quelle pair souhaitez-vous contacter (donnez une adresse IP) ?");
            ip = sc.nextLine();
            //récupération du hash du pair destinataire
            h = clHash.communiquer(ip); //il faudra mettre l'ip en paramètre dans la classe ClientHashServer!!!!!!!!!!!

            
            /*
            *Si le dest est le prédecesseur
            *on lance alors la communication avec lui et on fait les envois de messages qu'il faut
            *
            *
            *sinon, on lance la communication avec le successeur et :
            *
            * - soit c'est lui et alors on fait les envois de messages qu'il faut et le résultat de la communication une fois terminée est par
            *exemple "null" ce qui veut dire qu'on a trouvé le dest 
            *
            *- soit ce n'est pas lui et le résultat de la communication une fois terminée est l'ip du successeur du successeur et on boucle ainsi sur les différents succ
            */
            if(h == hash du prédecesseur) {
                clPair = new ClientPair(this.port, ipPred);
            } else {
                while ("dest pas trouvé") {
                    clPair = new ClientPair(this.port, ipSucc);
                    ipSucc = clPair.communiquer(h); //tant qu'on n'est pas tombé sur le dest, on récup le successeur
                    if(ipSucc == null) {
                        "dest trouvé"
                    }
                }
            }
        }

    }
}