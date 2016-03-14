import java.io.*;
import java.net.*;
import java.util.*;
import java.net.InetAddress;


public class PairThread implements Runnable {

	private ArrayList<LigneRoutage> tableRoutage;
	private Socket sock;
	private int hash;

	public PairThread(ArrayList<LigneRoutage> tableR, Socket so) {
		this.tableRoutage = tableR;
		this.sock = so;
		this.hash = this.tableRoutage.get(0).getHash();
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
    private ArrayList<LigneRoutage> getTableRoutage() {
        return this.tableRoutage;
    }


    public void run() {
						
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
							
						// Probablement inutile
					    case "rt?":
						// demande notre table de routage
								
						    for (LigneRoutage ligne : this.getTableRoutage()) {
						    	reponse += ligne.toString() + "\n";
						    }
							out.println(reponse);

							break;
						
						////////////////////
						// INUTILE /////
						////////////////////	
					    /*case "a+":
						// fin de conversation
								
							out.println("Good bye !");
							this.sock.close();   
							System.exit(1);

							break;*/
							
					    case "msg":
						// message du pair

					    	if (words.length < 4) {
					    		out.println("D'autres éléments doivent suivre cette commande msg. Format -> msg:hash(destinataire):IP(emetteur):contenu");
					    	}
					    	else {
					    		// GERER LE MESSAGE OU SA TRANSMISSION
					    		int hdest = PairThread.safeParseInt(words[1]);
					    		int hsucc = this.tableRoutage.get(1).getHashDestinataire();
					    		// cas où on est le destinataire
					    		if (this.hash == hdest) {
					    			out.println("message reçu");
					    			System.out.println("Contenu du message : " + words[3]);
					    		}
					    		// cas où le destinataire n'existe pas
					    		else if ( (this.hash < hdest && hdest < hsucc && this.hash < hsucc) || // cas normal
					    				  (this.hash > hsucc && hdest > this.hash) || // gère un cas comme : msg pour 48, je suis 45 et mon suc 3
					    				  (this.hash > hsucc && hdest < hsucc && this.hash > hdest) // gère un cas comme : msg pour 2, je suis 45 et mon suc 3
					    				)
					    		{
					    			out.println("Aucun membre du réseau n'a pour hash : " + hdest);
					    		}
					    		// cas où on doit transmettre le message
					    		else if (hsucc <= hdest) {
					    			ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(1).getIpDestinataire());
					    			clPair.transmettreMessage(inputLine);
					    			out.println("message transmis au successeur.");
					    		}
					    		// aucun des cas précédents, soit un cas d'erreur
					    		else {
					    			out.println("probleme inconnu dans pairThread:msg");
					    		}
					    		System.out.println("Contenu du message : " + words[3]);
					    		out.println("message reçu");
					    	}

							break;
						
						case "yo":
						// on cherche succ ou pred du pair disant yo
							if (words.length < 3) {
								out.println("D'autres éléments doivent suivre cette commande yo. Format -> msg:hash(emetteur):IP(emetteur)");
							}
							else {
								// on récupère notre ip
								String ip = InetAddress.getLocalHost().getHostAddress();
								// on fait circuler le message si on est pas succ/pred
								int hpred = this.tableRoutage.get(0).getHashDestinataire();
								int hsucc = this.tableRoutage.get(1).getHashDestinataire();
								int hemet = PairThread.safeParseInt(words[1]);
								String ipemet = words[2];

								if (this.hash == hsucc) {
									// cas où il y avait une seule personne sur le réseau
									this.tableRoutage.get(1).setHashDestinataire(hemet);
									this.tableRoutage.get(0).setHashDestinataire(hemet);
									this.tableRoutage.get(1).setIpDestinataire(ipemet);
									this.tableRoutage.get(0).setIpDestinataire(ipemet);
									out.println(this.hash + ":" + ip);
									out.println(this.hash + ":" + ip);
								}
								else if (hemet > this.hash) {
									if (hemet < hsucc) {
										// emetteur entre ce pair et son successeur
										out.println(this.hash + ":" + ip);
										out.println(hsucc + ":" + this.tableRoutage.get(1).getIpDestinataire());
										this.tableRoutage.get(1).setHashDestinataire(hemet);
										this.tableRoutage.get(1).setIpDestinataire(ipemet);

										///////////////////////////////////////////////
										// PREVENIR SUCC DU CHGMT DE SON PREDECESSEUR /
										///////////////////////////////////////////////
									}
									else {
										ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(1).getIpDestinataire());
						    			clPair.transmettreMessage(inputLine);
						    			out.println("message transmis au successeur.");
									}
								}
								else if (hemet < this.hash) {
									if (hemet > hpred) {
										// emetteur entre ce pair et son predecesseur
										out.println(hpred + ":" + this.tableRoutage.get(0).getIpDestinataire());
										out.println(this.hash + ":" + ip);
										this.tableRoutage.get(0).setHashDestinataire(hemet);
										this.tableRoutage.get(0).setIpDestinataire(ipemet);

										///////////////////////////////////////////////
										// PREVENIR pred DU CHGMT DE SON SUCCESSEUR /
										///////////////////////////////////////////////
									}
									else {
										ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(0).getIpDestinataire());
						    			clPair.transmettreMessage(inputLine);
						    			out.println("message transmis au predecesseur.");
									}
								}
							}

					    default:
							out.println("Message/Commande incompris(e).");
							break;
							
					}

					this.sock.close();
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

/*
 *	
 */