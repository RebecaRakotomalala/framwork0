On derait savoir si le methode est GET ou POST, L'url depant aussi de ca
Avant l'url est associé avec Classe + action (methode dans controlleur)
mais maintenant il faut ajouter une autre attirbut (VERB) qui est soit GET soit POST
Si il y a yn cas comme ca qui devrait elever une exception
: methode na controlleur : @GET @url("/getemp") getEmp .... 
alors que l'url "getemp" est appler en verb (method) POST !!