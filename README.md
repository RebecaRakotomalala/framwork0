on est une nouvelle class ModelView 
puis on cree un methode qui retournera un ModelView 
(ex: 
    @AnnotationMethode("mycontroller2") 
    public ModelView uneautre() { 
        ModelView mv = new ModelView("/uneautre.jsp"); 
        mv.addObject("nom", "RAKOTOMALALA"); 
        mv.addObject("prenoms", "Ranjaa"); 
        mv.addObject("age", 42); 
        return mv; 
    } 
)