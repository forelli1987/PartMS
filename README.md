# PartMS Partitionnement linux

## Présentation
Ce programme a pour but de gérer son ou ses volumes.

### Fonctionnalités actuelles

* Création d'une table
  * MSDOS
* Identification du type de table :  
  * MSDOS
  * GPT
* Choix de l'identifiant de la partition (exemple 0x83 -> linux).
* Formatage bas niveau de la partition ou du volume entier.
* Cacher la table (suppression ou restauration du magic number du MBR).

## Compilation

Fonctionne avec **java 8** .
> javac main.java

Tous les autres *.java* seront compilés automatiquement

### Construire un .jar

1. Lancer la compilation.
> javac main.java

1. Générer le .jar avec le fichier MANIFEST.MF présent dans le dépôt.
> jar -cvmf MANIFEST.MF ParMS.jar *.class

## Contribuer

### Si vous êtes développeur
1. Créer un fork  
1. Créer une branche (si possible avec le nom de la fonctionalité ajoutée)
1. Faire votre modification
1. Envoyer votre branche : git push origin VotreBranche  
1. Effectuer une pull request

### Si vous avez des idées
Merci de m'envoyer un message privé sur forelli87@gmail.com en indiquant vos coordonnées,  
en objet PartMS_modif,  
que doit-on ajouter et modifier,  
pourquoi doit-on intégrer cette modification.
