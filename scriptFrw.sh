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
jar cf StringType.jar mg
jar cf IntType.jar mg
jar cf DoubleType.jar mg
jar cf NotNull.jar mg
jar cf ParamField.jar mg
jar cf ParamObject.jar mg
jar cf InjectionSession.jar mg
jar cf Url.jar mg
jar cf AnnotationClass.jar mg
jar cf AnnotationAttribut.jar mg

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
mv StringType.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv IntType.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv DoubleType.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv NotNull.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv ParamField.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv ParamObject.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv InjectionSession.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv Url.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv AnnotationClass.jar "R:\S4\Mr.Naina\sprint\test\lib"
mv AnnotationAttribut.jar "R:\S4\Mr.Naina\sprint\test\lib"

sleep 60