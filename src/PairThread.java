import java.io.*;
import java.net.*;
import java.util.*;
import java.net.InetAddress;


public class PairThread implements Runnable {

	private List<LigneRoutage> tableRoutage;
	private Socket sock;
	private int hash;

	public PairThread(List<LigneRoutage> tableR, Socket so) {
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
     */
    private List<LigneRoutage> getTableRoutage() {
        return this.tableRoutage;
    }


    public void run() {
		try (
		// Tout ce qui est dans ce bloc sera fermé automatiquement à la fin du try
		PrintWriter out = new PrintWriter(this.sock.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
		) {
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

					List<LigneRoutage> lignesRoutage = this.getTableRoutage();
					for (LigneRoutage ligne : lignesRoutage) {
						out.println(ligne.toString());
					}

					break;

					////////////////////
					// INUTILE /////
					////////////////////
					/*case "a+":
					// fin de conversation

					out.println("Good bye !");
					this.sock.close();
					System.exit(1);
					/
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
						out.println("D'autres éléments doivent suivre cette commande yo. Format -> yo:hash(emetteur):IP(emetteur)");
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
							System.out.println("je suis là 1");
							// cas où il y avait une seule personne sur le réseau
							this.tableRoutage.get(1).setHashDestinataire(hemet);
							this.tableRoutage.get(0).setHashDestinataire(hemet);
							this.tableRoutage.get(1).setIpDestinataire(ipemet);
							this.tableRoutage.get(0).setIpDestinataire(ipemet);
							out.println(this.hash + ":" + ip);
							out.println(this.hash + ":" + ip);
							System.out.println(this.hash + ":" + ip);
						}
						else if (hemet > this.hash) {
							if (hemet < hsucc || (hemet > hsucc && this.hash > hsucc)) {
								System.out.println("je suis là 2");
								// emetteur entre ce pair et son successeur
								out.println(this.hash + ":" + ip);
								out.println(hsucc + ":" + this.tableRoutage.get(1).getIpDestinataire());

								///////////////////////////////////////////////
								// PREVENIR SUCC DU CHGMT DE SON PREDECESSEUR /
								///////////////////////////////////////////////
								String msg = "ctr:0:" + hemet + ":" + ipemet;
								ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(1).getIpDestinataire());
								clPair.transmettreMessage(msg);

								this.tableRoutage.get(1).setHashDestinataire(hemet);
								this.tableRoutage.get(1).setIpDestinataire(ipemet);
							}
							else {
								System.out.println("je suis là 3");
								ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(1).getIpDestinataire());
								String msg = inputLine;
								clPair.transmettreMessage(msg);
								// out.println("message transmis au successeur.");
							}
						}
						else if (hemet < this.hash) {
							if (hemet > hpred || (hemet < hpred && this.hash < hpred)) {
								System.out.println("je suis là 4");
								// emetteur entre ce pair et son predecesseur
								out.println(hpred + ":" + this.tableRoutage.get(0).getIpDestinataire());
								out.println(this.hash + ":" + ip);

								///////////////////////////////////////////////
								// PREVENIR PREDECESSEUR DU CHGMT DE SON SUCCESSEUR /
								///////////////////////////////////////////////
								String msg = "ctr:1:" + hemet + ":" + ipemet;
								ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(0).getIpDestinataire());
								clPair.transmettreMessage(msg);

								this.tableRoutage.get(0).setHashDestinataire(hemet);
								this.tableRoutage.get(0).setIpDestinataire(ipemet);
							}
							else {
								System.out.println("je suis là 5");
								ClientPair clPair = new ClientPair(this.sock.getPort(), this.tableRoutage.get(0).getIpDestinataire());
								String msg = inputLine;
								clPair.transmettreMessage(msg);
								// out.println("message transmis au predecesseur.");
							}
						}
					}
					break;

					case "ctr":
					if (words.length < 4) {
						out.println("D'autres éléments doivent suivre cette commande ctr.");
						out.println("Format -> ctr:lignetableroutage(0->pred,1->succ):newhash:newip");
					}
					else {
						int ligne = PairThread.safeParseInt(words[1]);
						this.tableRoutage.get(ligne).setHashDestinataire(PairThread.safeParseInt(words[2]));
						this.tableRoutage.get(ligne).setIpDestinataire(words[3]);
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

/*
 *
 */
