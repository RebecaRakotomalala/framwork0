Creer une nouvelle classe annotation (ex:Restapi)
Dans la partie FrontController , on doit verifier l'existence de cette annotation
    Si l'annotation n'existe pas ==> continuez comme avant
    Si oui 
        Recuperer la valeur de retour de la methode (gson)
            Si autre que ModelView, transformer en json directement
            Si ModelView, transformer en json la valeur de l'attribut "data"
        Ne pas utiliser DispatchForward mais utiliser directement getWriter() et print(json)
        Ne pas oublier de changer la respone type => text/json