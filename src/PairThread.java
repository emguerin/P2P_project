import java.io.*;
import java.net.*;
import java.util.*;


public class PairThread implements Runnable {

	private Map<Integer, Map<Integer, String>> tableRoutage;
	private Socket sock;

	public PairThread(Map<Integer, Map<Integer, String>> tableR, Socket so) {
		this.tableRoutage = tableR;
		this.sock = so;
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

 /*   // Obtenir le hash du successeur du pair de hash h dans le réseau
    // Ne doit pas être appelé si le réseau est vide
    private static int getSucc(int h, HashMap<Integer,String> net) {

		TreeSet<Integer> keys = new TreeSet<Integer>(net.keySet());
		Integer h2 = keys.higher(h);
		if (h2 != null)
		    return h2;
		
		return keys.first();

    }*/

    /*
     * Fonction pour retourner les pairs que l'on connaît (dans tableRoutage)
     * sous la forme hash:hash(succ):ip(succ)
     */
    private List<String> getTableRoutage() {
        List<String> lignes_table = new ArrayList<String>();
        Set<Integer> tableKeys = this.tableRoutage.keySet();

        String tmp;
        Map<Integer, String> mapTmp;
        Integer elem2;
        String elem3;
        for (Integer i: tableKeys) {
            mapTmp = this.tableRoutage.get(i);

            elem2 = (Integer)mapTmp.keySet().toArray()[0]; // récup du hash du successeur
            elem3 = (String)mapTmp.values().toArray()[0]; // récup de l'IP du successeur

            // Constitution de la chaîne au format correct
            tmp = new String();
            tmp += i + ":" + elem2 + ":" + elem3;

            lignes_table.add(tmp);
        }

        return lignes_table;
    }


    public void run {
						
		while (true) {
			try (
				 // Tout ce qui est dans ce bloc sera fermé automatiquement à la fin du try				 
				 PrintWriter out = new PrintWriter(this.sock.getOutputStream(), true);
				 BufferedReader in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
				) 
			{
				    
			    // Après avoir accepté une connexion on lit immédiatement un message
			    String inputLine;
			    if ((inputLine = in.readLine()) != null) {
					
					String reponse = "";
					// On parse le message reçu
					String sep = ":";
					String[] words = inputLine.split(sep);			

				    switch (words[0]) {
							
					    case "rt?":
						// demande notre table de routage
								
						    for (String ligne : this.getTableRoutage()) {
						    	reponse += ligne + "\n"
						    }
							out.println(reponse);

							break;
							
					    case "a+":
						// fin de conversation
								
							out.println("Good bye !");
							this.sock.close();   
							exit(1);

							break;
							
					    case "msg":
						// message du pair

					    	if (words.length < 2) {
					    		out.println("Un message doit suivre cette commande. Format -> msg:'message'");
					    	}
					    	else {
					    		System.out.println("Message reçu : " + words[1]);
					    		out.println("message reçu");
					    	}

							break;
							
					    default:
							out.println("Message/Commande incompris(e).");
							break;
							
					}					
					
			    }
				    
			}
		    catch (IOException e) {
				System.err.println("Erreur pendant une communication (PairThread)");
				System.err.println(e.getMessage());
				System.exit(1);
		    }
		}
		
    }  
}