import java.io.*;
import java.net.*;
import java.util.Scanner;

public interface Client {

    public void communiquer(); 

    public Socket etablirConnexion(); 

    public String lireMessage(BufferedReader entree); 

}
