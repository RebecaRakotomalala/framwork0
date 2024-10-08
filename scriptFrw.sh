#!/bin/bash

# Définir le chemin vers le répertoire où se trouve votre classe FrontController
DIR="R:\S4\Mr.Naina\sprint\framwork0"

# Déplacer dans le répertoire contenant votre classe FrontController
cd "$DIR"

# Compiler le fichier FrontController.java
javac -d . *.java

# Créer un fichier JAR en incluant le fichier compilé FrontController.class
jar cf AnnotationController.jar mg
jar cf GetAnnotation.jar mg
jar cf FrontController.jar mg
jar cf Mapping.jar mg
jar cf ModelView.jar mg
jar cf Post.jar  mg
jar cf Param.jar  mg
jar cf RequestBody.jar mg
jar cf FormParam.jar mg
jar cf CustomSession.jar mg
jar cf Restapi.jar mg

# Déplacer le fichier JAR créé dans le répertoire souhaité
mv FrontController.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv AnnotationController.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv GetAnnotation.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv Mapping.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv ModelView.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv Post.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv Param.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv RequestBody.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv FormParam.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv CustomSession.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv Restapi.jar "R:\S4\Mr.Naina\sprint\test\lib"

sleep 60