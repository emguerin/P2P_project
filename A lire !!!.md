# Communication avec le HashServer

1. Envoyer son adresse IP

2. Retour d'un hash (ou aht si plus de hash)

3. La connexion est ensuite fermée avec le HashServer. 
   Un pair le contacte ensuite au besoin s'il veut obtenir le hachage d'une adresse IP


# Communication avec le WelcomeServer

1. Envoyer un message de la forme yo:hash(ip):ip
   avec hash(ip) obtenu précedemment avec le HashServer.
   WelcomeServer indique alors qui sont succ et pred
   On sera alors dans le réseau P2P
   Une fois entré sur le réseau, le client ne communique plus jamais avec le WelcomeServer ! (sinon ca ne représente pas bien un vrai réseau P2P)

2. Mais il existe quand même des commandes pour communiquer avec lui (donc je comprend rien) :
    - a+  : quitter le réseau P2P
    - rt? : demande d'une table de routage
    - ip? : demande d'IP
    - li? : liste des pairs présents sur le réseau


# Pour chaque pair du réseau P2P

1. Il est un client en ce qu’il peut contacter un autre pair 

2. Il est un serveur en ce qu'il peut être contacté par un autre pair

3. Il écoute en permanence, dans un thread réservé à cette fonction, le port du moniteur pour répondre à ses requêtes


# Echange des plus fructueux avec Cédric Berland 

si un client veut envoyer un truc à un autre client
  - tu donne le hash du client receveur dans le message, et tu l'envois à ton successeur
  - Le successeur le renvois si le message est pas pour lui, et ça fait le tour de la boucle jusqu'à arriver jusqu'au client qui pourra lire le message
oui voila mais est ce qu'on doit vérifier que le client receveur existe ?
est ce que ya une table de la totalité du réseau à maintenir quelque part ?
  - Normalement non
mais du coup ca peut faire le tour pour rien
  - Sur certaine condition, un client peut vérifier si le client existe
  - Du genre, je suis 13, j'ai un message pour 14 mais je connais que 15 comme succésseur, donc 14 n'existe pas
oue mais la c'est un cas particulier
  - T'as moyen de faire des condition qui seront générale
  - Si ton successeur à un hash inférieur au tiens, ça veut dire que tu est au bout du cercle
  - Donc tu peux faire des vérif du genre client receveur > toi = existe pas
  - Et client receveur < succésseur = client existe pas
  - Sinon envoyé au succésseur
oue mais meme faut déjà faire pas mal de passage aux successeurs consécutifs avant d'arriver à la conclusion que le receveur existe pas...
mais surtout que welcomeServer il enregistre tous les pairs
puisque il est capable de te donner une liste de tous les pairs présents en tapant li?
  - Faut pas utiliser Welcome Server, je crois
donc ton truc c'ets utile si un pair s'est barré sans prévenir
bah...
  - On avais demander au prof, t'utilise WelcomeServer pour choper une IP, et tu te barre après
bah prk alors est ce que on peut lui envoyer des commandes
du genre a+ pour signaler qu'on s'en va
  - Bah c'est pour pas récupérer une ip qui c'est barré, je pense
  - Et la liste des clients ça doit être pour monitor server
bon ok, je pense que c'est parce que dans un vrai réseau P2P ya pas de truc centralisé donc on s'oblige à l'utiliser que pour entrer dans le réseau
mais du coup c'est un peu con mais bref
  - Voilà
merci
  - Si tu veux un truc plus opti, il faut faire la finger table
  - Tu fais une table avec plus de succésseur, mais à des endroit plus loin dans la boucle, pour pouvoir aller plus vite à envoyé des messages
  - Mais ça c'est galère
bah on doit pas le faire de base ça ?
on nous demande juste de garder succ et pred ?
  - De base, c'est que succ et pred, mais la continuation c'est rajouté plus de succésseur
okk je vois
merci bcp
ah oui juste
dans le td ya marqué que les clients doivent écouter en permanence le monitor
mais le moniteur c'est un client comme un autre au final, donc si le moniteur fait un socket avec l'ip du client désiré, le client va l'accepter et il pourra y avoir communication
donc pas besoin d'avoir une écoute en permanence non ?
  - Ouais, c'est juste pour envoyé les tables de hashage au moniteur pour qu'il puisse les lire
fin moi je séparerais pas autre client et moniteur en fait
  - Je pense que t'as juste à faire un socket qui écoute le port du moniteur, hein, rien de compliqué
oui mais tu fais pas un thread réservé à l'écoute du moniteur pour chaque client
  - Bah pourquoi pas ?
bah chaque client est également un serveur
et en fait le moniteurServer, ya marqué server mais en fait c'est un client
dans moniteurserver ya pas de fonction accept()
  - Bah non, c'est un server
  - En quelque sorte en faites
  - Il a juste une connexion avec WelcomeServer, je crois
donc puisque chaque client est un serveur, il est constamment en attente d'acceptation d'un client, et ce client ca peut soit être un autre client du réseau, soit le moniteur
mais du coup je ferai pas de truc spécial pour le moniteur, je le considère comme un client comme les autres
ca devrait marcher je pense..
  - Et il se connecte sur un client pour récuperer ses tables de hachage
  - Bah tu fais comme tu veux, aussi, donc bon
ok