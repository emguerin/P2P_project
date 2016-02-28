public class AppliClient {

    public static boolean aht = false;    
    public static boolean wrq = false;    
    
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Il faut 4 arguments : le port et l'adresse IP du serveur Welcome, le port et l'IP du serveur Hash");
            System.exit(1);
        }

        int    portWelcome    = Integer.parseInt(args[0]);
        String adresseWelcome = args[1];
        int    portHash       = Integer.parseInt(args[2]);
        String adresseHash    = args[3];

        ClientHashServer clHash    = new ClientHashServer(portHash, adresseHash);
        ClientWelcomeServer clWelcome = new ClientWelcomeServer(portWelcome, adresseWelcome);

        System.out.println("Début de la communication avec le serveur de hash à l'adresse " + adresseHash);
        clHash.communiquer();
        System.out.println("Fin de la communication avec le serveur de hash\n");

        if(aht) {
            System.exit(0);
        }

        System.out.println("Début de la communication avec le serveur Welcome à l'adresse " + adresseWelcome);
        clWelcome.communiquer();
        System.out.println("Fin de la communication avec le serveur Welcome\n");

        if(wrq) {
            System.exit(0);
        }

        //Lancer ensuite un client/Serveur pair avec le bon successeur et le bon prédecesseur
        //Comment récup proprement le successeur et le prédecesseur donnés par WelcomeServer ? (si on cosnidère que les var static c'est pas propre)

    }
}
