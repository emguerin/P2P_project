public class AppliClientLambda {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Il faut 4 arguments : le port et l'adresse IP du serveur Welcome, le port et l'IP du serveur Hash");
            System.exit(1);
        }

        int    portWelcome    = Integer.parseInt(args[0]);
        String adresseWelcome = args[1];
        int    portHash       = Integer.parseInt(args[2]);
        String adresseHash    = args[3];

        ClientLambda clHash    = new ClientLambda(portHash, adresseHash);
        ClientLambda clWelcome = new ClientLambda(portWelcome, adresseWelcome);

        System.out.println("Début de la communication avec le serveur à l'adresse " + adresse);
        cl.communiquer();
        System.out.println("Fin de la communication");
    }
}
