public class LigneRoutage {
    private int hash; // hash du pair actuel
    private int hashDestinataire; // le hash d'un autre pair
    private String ipDestinaire;  // l'IP d'un autre pair

    public LigneRoutage(int hash, int hashDestinataire, String ipDestinaire) {
        this.hash = hash;
        this.hashDestinataire = hashDestinataire;
        this.ipDestinaire = ipDestinaire;
    }

    /**
     * Constructeur avec une chaîne de caractère au format habituel :
     *   hash:hash_succ:IP
     */
    public LigneRoutage(String lesInfos) {
        String[] infos = lesInfos.split(":");

        this.hash = Integer.parseInt(infos[0]);
        this.hashDestinataire = Integer.parseInt(infos[1]);
        this.ipDestinaire = infos[2];
    }

    public void setHash(int newHash) {
        this.hash = hash;
    }

    public void setHashDestinataire(int newHash) {
        this.hashDestinataire = newHash;
    }

    public void setIpDestinataire(String newIp) {
        this.ipDestinaire = newIp;
    }

    public int getHash() {
        return this.hash;
    }

    public int getHashDestinataire() {
        return this.hashDestinataire;
    }

    public String getIpDestinataire() {
        return this.ipDestinaire;
    }

    public String toString() {
        return "" + this.hash + ":" + this.hashDestinataire + ":" + this.ipDestinaire;
    }
}
