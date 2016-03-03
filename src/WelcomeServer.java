/*
 *
 * Serveur d'accueil
 *   1. Démarrer le serveur en indiquant 
 *         . le port utilisé
 *         . la taille du réseau (nombre maximum de pairs + données acceptés)
 *   2. Lorsqu'un pair veut entrer dans le réseau il doit
 *         . ouvrir une connexion avec le serveur
 *         . envoyer le message « yo:hash:ip » où hash est le hachage de ip et ip est son ip
 *         . FERMER la connexion
 *   3: Lorsqu'un pair veut quitter le réseau il doit
 *         . ouvrir une connexion avec le serveur
 *         . envoyer le message « a+:hash » où hash est le hachage de son ip
 *         . FERMER la connexion
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class WelcomeServer {

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

    // Obtenir l'adresse d'un élément du réseau pour servir de point d'entrée
    private static String getIp(HashMap<Integer,String> net) {

	Random gen = new Random();
	Object[] vals = net.values().toArray();
	Object aVal = vals[gen.nextInt(vals.length)];
	return (String) aVal;

    }

    // Obtenir le hash du successeur du pair de hash h dans le réseau
    // Ne doit pas être appelé si le réseau est vide
    private static int getSucc(int h, HashMap<Integer,String> net) {

	TreeSet<Integer> keys = new TreeSet<Integer>(net.keySet());
	Integer h2 = keys.higher(h);
	if (h2 != null)
	    return h2;
	
	return keys.first();

    }

    public static void main(String[] args) {
	// Il faut deux arguments : le numéro de port et la taille max du réseau
	if (args.length != 2) {
	    System.err.println("Usage: java WelcomeServer <port number> <size>");
	    System.exit(1);
	}
	int portNumber = Integer.parseInt(args[0]);
	int size = Integer.parseInt(args[1]);
	
	// Table de hachage contenant les infos sur les pairs présents dans le réseau
	// clé : hash, valeur : ip
	HashMap<Integer,String> inNetwork = new HashMap<Integer,String>();
	
	try (
	     // Tout ce qui est dans ce bloc sera fermé automatiquement à la fin du try
	     ServerSocket serverSock = new ServerSocket(portNumber);
	     ) {
		
		while (true) {
		    try (
			 // Tout ce qui est dans ce bloc sera fermé automatiquement à la fin du try
			 Socket s = serverSock.accept();
			 PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			 ) {
			    
			    // Après avoir accepté une connexion on lit immédiatement un message
			    String inputLine;
			    if ((inputLine = in.readLine()) != null) {
				
				// On parse le message reçu
				String sep = ":";
				String[] words = inputLine.split(sep);
				if (words.length < 1) {
				    out.println("wrq");
				} else {
				    
				    int hash;
				    String anIp;

				    switch (words[0]) {
					
				    case "yo":
					// Une arrivée dans le réseau, 
					// on l'ajoute à la table de hachage et 
					// on lui propose un point d'entrée
					// Si le réseau est vide, on envoie au pair
					// le message « yaf » (you are the first)
					if (words.length != 3) {
					    out.println("wrq");
					} else {
					    hash = safeParseInt(words[1]);
					    if (hash < 0 || inNetwork.containsKey(hash)) {
						out.println("wrq");
					    } else {
						anIp = "yaf";
						if (!inNetwork.isEmpty())
						    anIp = getIp(inNetwork);
						inNetwork.put(hash,words[2]);
						out.println(anIp);
					    }
					}
					break;
					
				    case "a+":
					// Un départ du réseau, 
					// on le supprime de la table de hachage
					if (words.length != 2) {
					    out.println("wrq");
					} else {
					    hash = safeParseInt(words[1]);
					    if (hash < 0 || !inNetwork.containsKey(hash)) {
						out.println("wrq");
					    } else {
						inNetwork.remove(hash);
						out.println("ok");
					    }
					}
					break;
					
				    case "rt?":
					// Une demande de table de routage, on la transmet
					if (words.length != 2) {
					    out.println("wrq");
					} else {
					    hash = safeParseInt(words[1]);
					    if (hash < 0) { 
						out.println("wrq");
					    } else if (!inNetwork.containsKey(hash)) {
						out.println("ukh");
					    }else {
						int hash2 = getSucc(hash,inNetwork);
						anIp = inNetwork.get(hash2);
						out.println(hash + ":" + hash2 + ":" + anIp);
						for (int i = 2; i < size; i = i * 2) {
						    int k = (hash + i) % size;
						    hash2 = getSucc(k,inNetwork);
						    anIp = inNetwork.get(hash2);
						    out.println(k + ":" + hash2 + ":" + anIp);
						}
						out.println("end");
					    }
					}
					break;
					
				    case "ip?":
					// Une demande d'ip, on la transmet
					if (words.length != 2) {
					    out.println("wrq");
					} else {
					    hash = safeParseInt(words[1]);
					    if (hash < 0) { 
						out.println("wrq");
					    } else if (!inNetwork.containsKey(hash)) {
						out.println("ukh");
					    } else {
						anIp = inNetwork.get(hash);
						out.println(anIp);
					    }
					}
					break;
					
				    case "li?":
					// Une demande de la liste des présents, on la transmet
					for (Map.Entry<Integer,String> e : inNetwork.entrySet())
					    out.println(e.getKey() + ":" + e.getValue());
					out.println("end");
					break;
					
				    default:
					out.println("wrq");
					break;
					
				    }
				    
				}
				
			    }
			    
			}
		    catch (IOException e) {
			System.err.println("Erreur pendant une communication (WelcomeServer)");
			System.err.println(e.getMessage());
			System.exit(1);
		    }
		}
	    } 
	catch (IOException e) {
	    System.err.println("Erreur à la création (WelcomeServer)");
	    System.err.println(e.getMessage());
	    System.exit(1);
	} 
    }  
}