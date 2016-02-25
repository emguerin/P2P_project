/*
 *
 * Moniteur
 *   1. Démarrer le moniteur en indiquant 
 *         . l'hôte et le port du serveur d'accueil
 *         . le port pour la communication avec les pairs
 *         . l'hôte et le port du serveur de hachage
 *   2. Dans l'invite qui s'affiche, taper les commandes à exécuter (help pour la liste)
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class MonitorServer {


    // Invite donnant la main à l'utilisateur
    private static void displayInvit() {
	
	System.out.println("");
	System.out.println(">> Donnez moi un ordre s'il vous plait maître.");

    }

    // Message d'aide
    private static void displayHelp() {
	
	System.out.println("");
	System.out.println("***********************************************************");
	System.out.println("*   Les ordres possibles sont :                           *");
	System.out.println("*      . rout h -> afficher la table de routage du pair   *");
	System.out.println("*                  dont le hash est h                     *");
	System.out.println("*      . brout h -> afficher la table de routage idéale   *");
	System.out.println("*                   du pair dont le hash est h            *");
	System.out.println("*      . list -> afficher la liste des pairs présents     *");
	System.out.println("*      . help -> afficher la liste des commandes          *");
	System.out.println("*      . exit -> quitter                                  *");
	System.out.println("***********************************************************");

    }

    // Message d'erreur
    private static void displayError(String info) {
	
	System.out.println("");
	System.out.println("!!!   Impossible d'exécuter cet ordre");
	System.out.println("!!!      Info : " + info);

    }

    // Message de fin
    private static void displayQuit() {

	System.out.println("");
	System.out.println(">> Adieu.");
	System.out.println("");

    }

    // Étant donné un hash, interroge le serveur d'accueil pour connaître son ip
    // Le serveur d'accueil doit avoir pour hôte wSHost et pour port wSPort
    private static String getHost(int hash, String wSHost, int wSPort) {

	String res = "oups";

	try (
	     Socket s = new Socket(wSHost,wSPort);
	     PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	     ) {

		// Pour demander l'ip correspondant à un hash on envoie un message commençant par « ip?: »		    
		out.println("ip?:"+hash);
		String inputLine;
		if ((inputLine = in.readLine()) != null && !inputLine.equals("ukh") && !inputLine.equals("wrq")) {
		    // Si l'ip est bien reçue et correcte (i.e. le serveur n'a pas répondu « ukh » ou « wrq »)
		    // On l'enregistre pour la retourner
		    res = inputLine;
		} 

	    }
	catch (IOException e) {
	    res = "oups";
	}
	
	// On retourne l'adresse correspondant au hash, ou « oups » en cas de problème
	return res;

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

    // Classe pour stocker les tables de routage
    private static class RoutingTable {

	// Une ligne de table de routage est :
	//   . un entier k indiquant le successeur de quel hash se trouve sur cette ligne
	//   . un entier succ indiquant le hash de ce successeur
	//   . l'ip correspondant à ce successeur
	private static class RTLine {

	    public int k;
	    public int succ;
	    public String ip;

	    public RTLine(int kk, int succsucc, String ipip) {
		this.k = kk;
		this.succ = succsucc;
		this.ip = ipip;
	    }

	}

	public Boolean isOk;
	public LinkedList<RTLine> table;

	public RoutingTable() {
	    this.isOk = false;
	    this.table = new LinkedList<RTLine>();
	}

	public void setOk() {
	    this.isOk = true;
	}

	public void setNOk() {
	    this.isOk = false;
	}

	public void addLine(int k, int succ, String ip) {
	    RTLine rtl = new RTLine(k, succ, ip);
	    this.table.add(rtl);
	}

	// Méthode pour afficher une table de routage
	public void display() {

	    System.out.println("");
	    System.out.println(">   La table de routage demandée est :");
	    System.out.println(">");
	    for (int i = 0; i < this.table.size(); i++ ) {
		RTLine current = table.get(i);
		System.out.println(">         " + current.k + " -> " + current.succ + " (" + current.ip + ")");
	    }
	    
	}


    }


    // Méthode pour demander à un pair dont on indique l'hôte et le port
    // de nous envoyer sa table de hachage
    private static RoutingTable getRoutingTable(String host, int port) {

	RoutingTable rt = new RoutingTable();

	if (host.equals("oups")) {

	    // Si le pair n'a pas été précédemment trouvé on affiche une erreur 
	    rt.setNOk();
	    displayError("pair inconnu dans le réseau");

	} else {

	    // Sinon, on se connecte et on lui demande sa table de routage
	    try (
		 // Tout ceci sera automatiquement fermé à la fin du try
		 Socket s = new Socket(host,port);
		 PrintWriter out = new PrintWriter(s.getOutputStream(), true);
		 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		 ) {	

		    // Pour demander sa table à un pair on lui envoie le message « rt? »
		    out.println("rt?");

		    // On attend ensuite de recevoir la table, ligne par ligne
		    // Après la dernière ligne le pair doit envoyer le message « end »
		    String inputLine;
		    Boolean ok = true;
		    while ((inputLine = in.readLine()) != null && !inputLine.equals("end") && ok) {
			
			String sep = ":";
			String[] words = inputLine.split(sep);

			// On vérifie que la ligne a le format attendu
			// c'est à dire k:succ:succIp avec k et succ des entiers
			if (words.length != 3) {
			 
			    ok = false;

			} else {

			    int k = safeParseInt(words[0]);
			    int succ = safeParseInt(words[1]);
			    String succIp = words[2];

			    ok = !((k < 0) || (succ < 0));

			    rt.addLine(k, succ, succIp);

			}

		    } 

		    // Si tout s'est bien passé on indique que la table est correcte
		    // Sinon on affiche une erreur
		    if (ok) {
			rt.setOk();
		    } else {
			rt.setNOk();
			displayError("table de routage mal transmise par le pair");
		    }

		}
	    catch (IOException e) {
		rt.setNOk();
		displayError("problème de connexion au pair");
	    }
	}

	return rt;

    }

    // Méthode pour demander au serveur d'accueil la meilleur table de routage théorique
    // du pair de hachage hash
    // Le serveur d'accueil a pour hôte host et est accessible par le port port
    // Le fonctionement est presque le même que pour demander sa table à un pair
    private static RoutingTable getRoutingTable(String host, int port, int hash) {

	RoutingTable rt = new RoutingTable();

	try (
	     // Tout ceci sera fermé à la fin du try
	     Socket s = new Socket(host,port);
	     PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	     ) {

		// On spécifie de quel pair on veut la table de routage
		out.println("rt?:"+hash);

		String inputLine;
		Boolean ok = true;
		while ((inputLine = in.readLine()) != null && !inputLine.equals("end") && ok && !inputLine.equals("ukh")) {

		    String sep = ":";
		    String[] words = inputLine.split(sep);
		    
		    if (words.length != 3) {
			
			ok = false;
			
		    } else {
			
			int k = safeParseInt(words[0]);
			int succ = safeParseInt(words[1]);
			String succIp = words[2];
			
			ok = !((k < 0) || (succ < 0));
			
			rt.addLine(k, succ, succIp);
			
		    }
		    
		} 
		if (ok && !inputLine.equals("ukh")) {
		    rt.setOk();
		} else if (inputLine.equals("ukh")) {
		    rt.setNOk();
		    displayError("aucun pair avec ce hash dans le réseau");
		} else {
		    rt.setNOk();
		    displayError("table de routage mal transmise par le serveur d'accueil");
		}
	    }
	catch (IOException e) {
		rt.setNOk();
		displayError("problème de connexion au serveur d'accueil");
	}
	
	return rt;

    }

    // Classe pour stocker les listes de pairs
    private static class PeerList {

	// Une ligne de de liste de pairs est :
	//   . un entier k indiquant le hash du pair
	//   . l'ip du pair
	private static class PLLine {

	    public int k;
	    public String ip;

	    public PLLine(int kk, String ipip) {
		this.k = kk;
		this.ip = ipip;
	    }

	}

	public Boolean isOk;
	public Boolean isEmpty;
	public LinkedList<PLLine> list;

	public PeerList() {
	    this.isOk = false;
	    this.isEmpty = true;
	    this.list = new LinkedList<PLLine>();
	}

	public void setOk() {
	    this.isOk = true;
	}

	public void setNOk() {
	    this.isOk = false;
	}

	public void addLine(int k, String ip) {
	    PLLine pll = new PLLine(k, ip);
	    this.isEmpty = false;
	    this.list.add(pll);
	}

	// Méthode pour afficher une liste de pairs
	public void display() {

	    System.out.println("");
	    if (this.isEmpty) {
		System.out.println(">   Aucun pair dans le réseau");
	    } else {
		System.out.println(">   Les pairs présents dans le réseau sont :");
		System.out.println(">");
		for (int i = 0; i < this.list.size(); i++ ) {
		    PLLine current = list.get(i);
		    System.out.println(">         " + current.k + " (" + current.ip + ")");
		}
	    }
	    
	}

    }

    // Méthode pour demander au serveur d'accueil la liste des pairs présents
    // Le serveur d'accueil a pour hôte host et est accessible par le port port
    private static PeerList getPeerList(String host, int port) {

	PeerList pl = new PeerList();

	try (
	     // Tout ceci sera fermé à la fin du try
	     Socket s = new Socket(host,port);
	     PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	     ) {

		// On envoie la demande
		out.println("li?");

		// On récupère la réponse et on l'enregistre
		String inputLine;
		Boolean ok = true;
		while ((inputLine = in.readLine()) != null && !inputLine.equals("end") && ok) {

		    String sep = ":";
		    String[] words = inputLine.split(sep);
		    
		    if (words.length != 2) {
			
			ok = false;
			
		    } else {
			
			int k = safeParseInt(words[0]);
			String ip = words[1];
			
			ok = !(k < 0);
			
			pl.addLine(k, ip);
			
		    }
		    
		} 
		if (ok) {
		    pl.setOk();
		} else {
		    pl.setNOk();
		    displayError("liste de pairs mal transmise par le serveur d'accueil");
		}
	    }
	catch (IOException e) {
		pl.setNOk();
		displayError("problème de connexion au serveur d'accueil");
	}
	
	return pl;

    }

    // Classe pour décrire les commandes que peut accepter le moniteur
    private static class Command {
	
	public int command;
	public int arg1;
	public Boolean isOk;

	public Command() {
	    this.command = -1;
	    this.isOk = false;
	}

	public void setCommand(int c) {
	    this.command = c;
	}

	public void setArg1(int a) {
	    this.arg1 = a;
	}
	
	public void setOk() {
	    this.isOk = true;
	}

	public void setNOk() {
	    this.isOk = false;
	}
	
    }

    // Lecture et parsing des commandes
    private static Command readCommand(BufferedReader sin) throws IOException {
	
	Command c = new Command();

	// On propose dans un premier temps à l'utilisateur d'entrer une commande
	displayInvit();

	// On lit le résultat et on le parse
	String line = sin.readLine();
	String sep = "[ ]+";
	String[] words = line.split(sep);

	// On vérifie que la commande n'est pas vide
	if (words.length <= 0) {
	    displayError("ordre vide");
	    displayHelp();
	} else {

	    // On vérifie que la commande correspond à quelque chose qui existe
	    // et a au moins autant d'arguments que nécessaire
	    switch (words[0]) {

	    case "help": 
		c.setCommand(0);
		c.setOk();
		break;

	    case "exit":
		c.setCommand(1);
		c.setOk();
		break;

	    case "rout":
		c.setCommand(2);
		if (words.length <= 1) {
		    displayError("nombre d'arguments");
		    c.setNOk();
		} else {
		    int h = safeParseInt(words[1]);
		    if (h >= 0) {
			c.setOk();
			c.setArg1(h);
		    } else {
			displayError("type d'argument");
			c.setNOk();
		    }
		}
		break;

	    case "brout":
		c.setCommand(3);
		if (words.length <= 1) {
		    displayError("nombre d'arguments");
		    c.setNOk();
		} else {
		    int h = safeParseInt(words[1]);
		    if (h >= 0) {
			c.setOk();
			c.setArg1(h);
		    } else {
			displayError("type d'argument");
			c.setNOk();
		    }
		}
		break;

	    case "list":
		c.setCommand(4);
		c.setOk();
		break;

	    default:
		c.setCommand(-1);
		c.setNOk();
		displayError("ordre inconnu");
		break;

	    }

	}

	// On retourne la commande
	// c.isOk est vrai si et seulement si la commande a été parsée avec succés
	return c;

    }

    // Méthode principale qui définit le fonctionement du moniteur
    public static void main(String[] args) {

	Boolean done = false;

	// On vérifie les arguments
	if (args.length != 3) {
	    System.err.println("Usage: java MonitorServer <welcome server host> <welcome server port number> <peer communication port number>");
	    System.exit(1);
	}
	String wSHost = args[0];
	int wSPort = safeParseInt(args[1]);
	int port = safeParseInt(args[2]);

	// On ouvre un buffer pour lire l'entrée standard
	try (
	     BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
	     ) {
		
		// Tant que l'utilisateur ne demande pas d'arrêter
		// on lit une commande puis on l'exécute
		while (!done) {
		    
		    Command c = readCommand(sin);
		    RoutingTable rt;
		    PeerList pl;
		    
		    if (c.isOk) {

			switch (c.command) {

			case 0:
			    displayHelp();
			    break;
			    
			case 1:
			    displayQuit();
			    done = true;
			    break;

			case 2:
			    String peerHost = getHost(c.arg1,wSHost,wSPort);
			    rt = getRoutingTable(peerHost,port);
			    if (rt.isOk)
				rt.display();
			    break;

			case 3:
			    rt = getRoutingTable(wSHost,wSPort,c.arg1);
			    if (rt.isOk)
				rt.display();
			    break;

			case 4:
			    pl = getPeerList(wSHost,wSPort);
			    if (pl.isOk)
				pl.display();
			    break;

			default:
			    displayError("très étrange...");
			    break;

			}
			
		    } else {

			displayHelp();

		    }
		    
		}

	    }
	catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
	
    }
    
}