#VERSION 2

#1 - L'émetteur
  On instancie un ClientPair avec l'IP de notre successeur puis on utilise la méthode communiquer() pour le contacter

#2 - Le successeur (et, s'il n'est pas le destinataire, ses successeurs "successifs")
  Son "côté serveur" dans Pair.java accepte le pair émetteur puis, dans le PairThread :
  -Soit il est le destinataire du message et donc il instancie un ClientPair (dans PairThread) qui contacte l'émetteur puis communication entre les deux, qui se termine par un "a+" écrit par l'un des deux. 
  -Soit il n'est pas le destinataire du message et donc il instancie un ClientPair (dans PairThread) qui contacte son propre successeur puis ainsi de suite entre les différents successeurs. 
  N.B. : les PairThread de chaque successeur "successifs" vont se fermer car à chaque fois qu'un successeur a fini de contacter son propre successeur (par l'instanciation d'un ClientPair), il n'y aura plus rien à faire pour ce PairThread qui va donc se terminer naturellement

#3 - Retour vers l'émetteur
  L'émetteur est pendant ce temps en train d'attendre de recevoir une connexion d'un autre pair qui est soit son destinataire, auquel cas communication entre les deux, soit un pair quelconque pour indiquer le destinaire n'existe pas.
  Une fois que la communication avec notre destinataire est terminée, ou que l'on sait qu'il n'existe pas, on boucle de nouveau pour proposer d'envoyer un message à un autre pair du réseau.
  Pour gérer les éventuels bugs du réseau, on attend un certain nombre de secondes prédéfini à l'avance de recevoir une connexion d'un autre pair puis si ce pair est le destinataire (on peut soit le vérifer avec le premier message qu'il nous envoie, soit en vérifiant son hash), alors on communique avec lui. 

#N.B. :
  Il faudra penser à gérer le fait que le pair qui se connecte à nous n'est pas simplement un pair qui veut nous contacter mais un pair qui est soit notre destinataire soit un pair quelconque qui veut nous informer qu'il n'existe pas.



#VERSION 1

On considère ici qu'on ajoute dans Pair.java une variable booléenne "waitForReceiverResponse" qui indique si on est ou non dans l'attente de la réponse de notre destinataire

#1 - L'émetteur
  On instancie un ClientPair avec l'IP de notre successeur puis on utilise la méthode communiquer() (qui renvoie un booléen qu'on affecte à waitForReceiverResponse, voir après) pour le contacter

#2 - Le successeur (et, s'il n'est pas le destinataire, ses successeurs "successifs")
  Son "côté serveur" dans Pair.java accepte le pair émetteur puis, dans le PairThread :
  -Soit il est le destinataire du message et donc communication entre les deux, qui se termine par un "a+" écrit par l'un des deux. Une fois terminée, il faut penser à trouver un moyen pour que la méthode communiquer() de ClientPair de l'émetteur retourne false
  -Soit il n'est pas le destinataire du message et donc il instance un ClientPair (dans PairThread) qui contacte son propre successeur puis ainsi de suite entre les différents successeurs. La méthode communiquer() de ClientPair de l'émetteur doit retourner true
  N.B. : les PairThread de chaque successeur "successifs" vont se fermer car à chaque fois qu'un successeur a fini de contacter son propre successeur (par l'instanciation d'un ClientPair), il n'y aura plus rien à faire pour ce PairThread qui va donc se terminer naturellement

#3 - Retour vers l'émetteur
  Que son successeur soit ou non le destinataire, la communication est finie avec lui. A ce moment, on regarde ce qui été retourné par la méthode communiquer() de ClientPair :
  -Soit false, alors le successeur était notre destinataire, le boulot est terminé, et on peut boucler de nouveau pour proposer d'envoyer un message à un autre pair du réseau
  -Soit true, alors on attend un certain nombre de secondes prédéfini à l'avance de recevoir une connexion d'un autre pair (on attend une réponse de notre destinataire) puis si ce pair est le destinataire (on peut soit le vérifer avec le premier message qu'il nous envoie, soit en vérifiant son hash), alors on communique avec lui. La communication terminée, , on pense à remettre waitForReceiverResponse à false.

#N.B. :
  On ajoute dans la partie serveur de Pair.java une ligne pour checker la valeur de waitForReceiverResponse car si elle est à true c'est qu'on attend la connexion d'un pair bien précis (le destinataire) et sinon on peut accepter n'importe qui. => A étudier


