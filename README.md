# framwork0

creer une class .java et annote le (exemple: @AnnotationController("TestController"))
metter la dans une package (exemple: package controller)
declarer votre package dans web.xml en utilisant "init-param" 
(exemple:<servlet>
            <servlet-name>FrontController</servlet-name>
            <servlet-class>mg.itu.prom16.FrontController</servlet-class>
            <init-param>
                <param-name>controller-package</param-name>
                <param-value>controller</param-value>
            </init-param>
         </servlet>)
faire une script pour l'envoyer dans webapps si le dossier n'y est pas encore 