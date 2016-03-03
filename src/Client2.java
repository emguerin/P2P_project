import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;


public class Client {
	private Socket sock;
	private InputStream fluxEntree;
	private OutputStream fluxSortie;
	private BufferedReader entree;
	private PrintWriter sortie;

	public Client()
	{
		try {
			this.sock = new Socket("localhost",8001);	
			this.fluxEntree = this.sock.getInputStream();
			this.fluxSortie = this.sock.getOutputStream();
			this.entree = new BufferedReader(new InputStreamReader(fluxEntree));
			this.sortie = new PrintWriter(fluxSortie, true);
		}
		catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	

	public BufferedReader getEntree() {
		return this.entree;
	}

	public PrintWriter getSortie() {
		return this.sortie;
	}
	
	public void setSocket(Socket soc) {
		this.sock = soc;
	}

	public void deconnexion() {
		try {
			this.fluxEntree.close();
			this.fluxSortie.close();
			this.sock.close();
			
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		// Client clicli = new Client();
		// Scanner sc = new Scanner(System.in);
		String action = "";
		String hashServerResp = "";
		Client clicli = new Client();
		try (
			
			Scanner sc = new Scanner(System.in);
			)
		{
			String entreeLue = "";
			boolean ok = false;

			if (sc.hasNextLine()) {
				action = sc.nextLine();
				clicli.getSortie().println(action);
			}

			while (true) {
				entreeLue = clicli.getEntree().readLine();
				if (!entreeLue.equals("")) {
					break;
				}
			}
			hashServerResp = entreeLue;
			System.out.println(hashServerResp);

			clicli.setSocket(new Socket("localhost",8000));
			System.out.println("Client (hash = " + hashServerResp + ") parle : ");
			if (sc.hasNextLine()) {
				System.out.println("Client (hash = " + hashServerResp + ") parle : ");
				action = sc.nextLine();
				clicli.getSortie().println(action);
			}

			while (true) {
				while (!entreeLue.equals("ok")) {
					if (clicli.getEntree().hasNextLine()) {
						entreeLue = clicli.getEntree().readLine();
					}
					if (!entreeLue.equals("")) {
						System.out.println(entreeLue);
						break;
					}
					System.out.println(entreeLue);
				}
			}


			// clicli.deconnexion();

			// while (true) {
			// 	if (!ok) {
			// 		entreeLue = clicli.getEntree().readLine();
			// 		System.out.println(entreeLue);
			// 	}
			// 	if (entreeLue.equals("ok")) {
			// 		ok = true;
			// 	}
				
			// 	System.out.println("Client message : ");
			// 	if (sc.hasNextLine()) {
			// 		action = sc.nextLine();
			// 		clicli.getSortie().println(action);
			// 		if (action.equals("end")) {
			// 			clicli.deconnexion();
			// 			break;
			// 		}
			// 	}
			// }
			// clicli.deconnexion();
			
		}
		catch (IOException e){
			System.out.println(e.getMessage());
		}
		
	}

}
