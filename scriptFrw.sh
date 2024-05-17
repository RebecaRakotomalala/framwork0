#!/bin/bash

# Définir le chemin vers le répertoire où se trouve votre classe FrontController
DIR="R:\S4\Mr.Naina\sprint\framwork0"

# Déplacer dans le répertoire contenant votre classe FrontController
cd "$DIR"

# Compiler le fichier FrontController.java
javac -d . *.java

# Créer un fichier JAR en incluant le fichier compilé FrontController.class
jar cf FrontController.jar mg
jar cf AnnotationController.jar mg
jar cf AnnotationMethode.jar mg

# Déplacer le fichier JAR créé dans le répertoire souhaité
mv FrontController.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv AnnotationController.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv AnnotationMethode.jar "R:\S4\Mr.Naina\sprint\test\lib"

sleep 60
