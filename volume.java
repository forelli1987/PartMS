/*
***** volume.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Hérite d'operationFichier pour y ajouter des fonctionnalités,
exclusivements réservée à la gestion des volumes.

*/

//******************************* GESTION PARTITIONNEMENT *******************************
/*
Hérite de operationFichier, contient des méthodes permettant d'intéragir avec le MBR.
*/

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.util.zip.CRC32; //Utilisation du CRC32 (enfin ...)
import java.io.FileNotFoundException;

public class volume extends operationFichier
{
	//Gestion de la table partition DOS
	private final long posBootable=0x1beL;
	private final long posTypePartition=0x1c2L;
	private final long posDebutPartition=0x1c6L;
	private final long posTaillePartition=0x1caL;
	private final long posSignatureMBR=0x1feL;
	private final long posSignature=0x1b8L;

	//Gestion de la table partition GPT
	private final long posSignatureGPT=0x200L; //Doit contenir EFI PART
	private final long posCrcEnTete=0x210L; //CRC de l'entête, ATTENTION, doit être initialisé à 0 pour le calcul
	private final long posDebutDescripteurPartition=0x400L; //Position du premier descripteur de partition
	private final long posCrcHeader1TablePartition=0x258L; //CRC table de partition

	private final long offsetCrcEnTete=0x10L;
	private final long offsetTableFin=0x58L;
	private final long offsetDescripteurPartition=0x80L;
	private final long offsetGUID=0x10L;
	private final long offsetDebutPartition=0x20L;
	private final long offsetFinPartition=0x28L;

	private final long secteurTaille=0x200L;
	private final long MO=0x100000L;
	private final long tailleDescripteurPartition=0x4000L;

        public long [] lecturePartitionMBR(String cheminBlocFichier,int numeroPartition)
        {
			//[0] contiendra le début de la partition en octet
			//[1] contiendra la taille de la partition en octet
			//[2] contiendra le type de la partition
			//[3] contiendra la signature du MBR
			//[4] contiendra la taille du volume
			//[5] Si la partition est bootable
			long retourPartition[]=new long[6];
			long curseur=posBootable;


			//On décrémente pour effectuer plus facilement le calcul d'adresse
			numeroPartition--;

			//Ouverture du fichier en lecture

			//Lecture du début de la partition
			//Calcul de la position du curseur pour obtenir le début de la partition.
			curseur=posDebutPartition+16L*numeroPartition;

			retourPartition[0]=(long)(lecture4Inverse(cheminBlocFichier,curseur));
			retourPartition[0]=retourPartition[0]*512L; //Conversion de secteur à octet.

			//Calcul de la position du curseur pour obtenir la taille.
			curseur=posTaillePartition+16L*numeroPartition;

			retourPartition[1]=lecture4Inverse(cheminBlocFichier,curseur);
			retourPartition[1]=retourPartition[1]*512L; //Conversion de secteur à octet.

			curseur=posTypePartition+16L*numeroPartition;

			retourPartition[2]=(long)lecture1(cheminBlocFichier,curseur);

			curseur=posSignatureMBR;
			retourPartition[3]=(long)lecture2Inverse(cheminBlocFichier,curseur);

			try
			{
				RandomAccessFile tailleFichier=new RandomAccessFile(cheminBlocFichier,"r");
				retourPartition[4]=tailleFichier.length();
			}

			catch (IOException erFile){}
            catch (NullPointerException erFile){}

            curseur=posBootable+16L*numeroPartition;
            retourPartition[5]=(long)lecture1(cheminBlocFichier,curseur);

			return retourPartition;

		}

		public void ecriturePartition(String cheminBloc,int numeroDescripteur,long debutPartition,long taillePartition,int type,boolean bootable)
		{
			System.out.println("ECRITURE PARTITION  n° Descripteur : "+numeroDescripteur+"\nDebut partition : "+debutPartition+"\nTaille partition : "+taillePartition);
			long curseur=0;

			//Conversion en int du début de la partition et conversion en secteur
			int intDebutPartitionSecteur=(int)(debutPartition/512L);

			//Conversion en int de la taille de la partition et conversion en secteur
			int intTaillePartitionSecteur=(int)(taillePartition/512L);

			System.out.println("ECRITURE PARTITION [SECTEUR] \nDebut partition secteur: "+intDebutPartitionSecteur+"\nTaille partition : "+intTaillePartitionSecteur);

			//Ecriture du début de la partition.
			ecriture4Inverse(cheminBloc,posDebutPartition+16L*numeroDescripteur,intDebutPartitionSecteur); //Divisé par 512 conversion en secteur et conversion en MO.

			//Ecriture de la taille
			ecriture4Inverse(cheminBloc,posTaillePartition+16L*numeroDescripteur,intTaillePartitionSecteur);

			ecriture1(cheminBloc,posTypePartition+16L*numeroDescripteur,type);

			writeSignature(cheminBloc,true);

			//Lever le flag bootable
			if(bootable)
			{
				ecriture1(cheminBloc,posBootable+16L*numeroDescripteur,0x80);
			}

			//Partition non bootable
		    else
			{
				ecriture1(cheminBloc,posBootable+16L*numeroDescripteur,0x0);
			}


		}

		//Ecriture simple de la signature du MBR.
		public void writeSignature(String cheminBloc,boolean signature)
		{
			if(signature)
			{

				ecriture2Inverse(cheminBloc,posSignatureMBR,0xaa55);
			}

			else if(!signature)
			{
				ecriture2Inverse(cheminBloc,posSignatureMBR,0);
			}

		}

		public void supprTablePart(String cheminBloc)
		{
			File fichier=new File(cheminBloc);
			long taille=fichier.length();
			remplirOctet(cheminBloc,1013760L);
			remplirOctet(cheminBloc,taille-posSignatureGPT,512L);
		}

		public void writeId(String cheminBloc,String valeurHexa)
		{
			int intValeurHexa=Integer.parseInt(valeurHexa,16);
			ecriture4Inverse(cheminBloc,posSignature,intValeurHexa);
		}


		public int infoTable(String cheminBloc)
		{
			//Type de table de partition retournée
			//0 pas de type
			//1 type msdos
			//2 type GPT

			String comp1="";
			String comp2="";
			String operandeComp="EFI PART";
			int sigMbr=0;
			File tailleTest=new File(cheminBloc);
			long taille=tailleTest.length();

			int retourTypeTable=0;

			sigMbr=lecture2Inverse(cheminBloc,posSignatureMBR);

			comp1=lectAscii(cheminBloc,posSignatureGPT,8);
			comp2=lectAscii(cheminBloc,taille-posSignatureGPT,8);

			if(sigMbr==0xaa55)
			{
				if(comp1.equals(operandeComp) && comp2.equals(operandeComp))
				{
					retourTypeTable=2;
				}

				else
				{
					retourTypeTable=1;
				}

			}

			return retourTypeTable;


		}

		public long crc32SignatureDosPartition(String cheminBloc)
		{
			CRC32 crcControl=new CRC32();
			byte[] octetsControl=new byte[65];
			long retourCrc=0;

			try
			{
				RandomAccessFile fichierCrc=new RandomAccessFile(cheminBloc,"r");
				fichierCrc.seek(posBootable);
				crcControl.reset();
				fichierCrc.read(octetsControl);
				fichierCrc.close();
				crcControl.update(octetsControl);
				retourCrc=crcControl.getValue();

			}

			catch (IOException erFile){}
            catch (NullPointerException erFile){}

            return retourCrc;
		}

		//****************************** gestion table GPT ****************************

		//Fourni les CRC32 sous forme d'un tableau regroupant, le CRC de l'entête et le CRC de la table de partition
		public long []crcGPTHeader(String cheminFichier)
		{
			long []crcEntete=new long[3]; //[0]CRC32 de l'entête, [1]CRC32 de la table de partition [2]CRC32 de l'header du backup
			byte []entete=new byte[92];
			byte []tablePartition=new byte[16384];
			byte []crcSauvegarde=new byte[4]; //Permet de sauver le CRC32 de l'header
			byte []crcZero={0x0,0x0,0x0,0x0};

			byte []crcRelecture={0,0,0,0};

			int injectionCrc=0;

			long tailleVolume=0;
			long tailleCalculeeBackupCrc=0;

			//byte []enteteRetournee=new byte[92];

			CRC32 crcEnteteCalcul=new CRC32();

			try
			{
				RandomAccessFile gptCrc=new RandomAccessFile(cheminFichier,"rw");

				System.out.println("---- RAZ CRC ----");

				//Raz du CRC
				gptCrc.seek(posCrcEnTete);
				gptCrc.write(crcZero);

				//On se place au début de l'entête.
				System.out.println("---- Calcul CRC32 en tête et réécriture ----");

				//On se déplace au début de l'entête GPT
				gptCrc.seek(posSignatureGPT);
				//On initialise le CRC32
				crcEnteteCalcul.reset();

				//Lecture de tout l'entête
				gptCrc.read(entete);

				//Calcul du nouveau CRC
				crcEnteteCalcul.update(entete);

				//On range le CRC32 de l'entête dans le tableau
				crcEntete[0]=crcEnteteCalcul.getValue();

				//On injecte le nouveau CRC32 dans le premier entête.
				this.ecriture4Inverse(cheminFichier,posCrcEnTete,(int)crcEntete[0]);

				//On récupère la table de partition
				gptCrc.seek(posDebutDescripteurPartition);
				gptCrc.read(tablePartition);

				crcEnteteCalcul.reset();
				crcEnteteCalcul.update(tablePartition);
				crcEntete[1]=crcEnteteCalcul.getValue();
				this.ecriture4Inverse(cheminFichier,posCrcHeader1TablePartition,(int)(crcEntete[1]));

				System.out.println("CRC32 TABLE : "+Long.toHexString(crcEntete[1]));
				System.out.println("CRC32 en tête GPT : "+Long.toHexString(crcEntete[0]));


				//Récupération de la taille du fichier.
				tailleVolume=new File(cheminFichier).length();
				System.out.println("Adresse fin clé : "+Long.toHexString(tailleVolume));
				System.out.println("Adresse CRC32: "+Long.toHexString(tailleVolume-posSignatureGPT));

				//Réécriture du CRC32 de la table dans le backup de l'entête.
				this.ecriture4Inverse(cheminFichier,(tailleVolume-posSignatureGPT)+offsetTableFin,(int)(crcEntete[1]));

				//On se déplace sur le CRC du backup
				gptCrc.seek((tailleVolume-posSignatureGPT)+offsetCrcEnTete);
				//RAZ du CRC32 du backup l'header.
				gptCrc.write(crcZero);

				//On se déplace au début du backup de l'header
				gptCrc.seek(tailleVolume-posSignatureGPT);
				//Lecture de 92 octets du backup.
				gptCrc.read(entete);

				//Calcul du nouveau CRC32 du backup.
				crcEnteteCalcul.reset();
				crcEnteteCalcul.update(entete);

				//Récupération de la valeur.
				crcEntete[2]=crcEnteteCalcul.getValue();

				System.out.println("CRC32 du backup de l'header : "+Integer.toHexString((int)crcEntete[2]));

				gptCrc.close();

				ecriture4Inverse(cheminFichier,((tailleVolume-posSignatureGPT)+offsetCrcEnTete),(int)crcEntete[2]);


			}

			catch (IOException erFile){}
            catch (NullPointerException erFile){}

            return crcEntete;

		}

		public long []gptTableInfo(String cheminFichier,int numeroDescripteur)
		{
			//Calcul de l'adresse du descripteur
			numeroDescripteur--; //On décrémente le descripteur pour le calcul.
			long adresseDescripteur=posDebutDescripteurPartition+numeroDescripteur*offsetDescripteurPartition;
			long adresseDebutPartition=adresseDescripteur+0x10L;
			long adresseFinPartition=adresseDescripteur+0x20L;

			long []tailleRecuperee=new long[3];//[0] Début partition [1] fin partition [2] taille

			//Récupération des informations de la partition
			tailleRecuperee[0]=(lecture8Inverse(cheminFichier,adresseDescripteur+0x20L))*secteurTaille;
			tailleRecuperee[1]=(lecture8Inverse(cheminFichier,adresseDescripteur+0x28L))*secteurTaille;

			tailleRecuperee[2]=tailleRecuperee[1]-tailleRecuperee[0]+MO;

			return tailleRecuperee;

		}

		public byte []lectGUID(String cheminFichier,int numDescripteur)
		{
			byte[] retourGUID=new byte[16]; //[0] partie 1 du GUID, partie 2 du GUID
			numDescripteur--;

			long adresseGUID=(posDebutDescripteurPartition+numDescripteur*offsetDescripteurPartition)+offsetGUID;

			try
			{
				RandomAccessFile blocUUID=new RandomAccessFile(cheminFichier,"r");
				blocUUID.seek(adresseGUID);

				blocUUID.read(retourGUID);
				blocUUID.close();
			}

			catch (IOException erFile){}
            catch (NullPointerException erFile){}

			return retourGUID;

		}

		public void ecritureGPT(String cheminFichier,int numeroDescripteur,long debutPartition,long taillePartition)
		{
			numeroDescripteur--;

			//Conversion des tailles en secteurs
			long debutSecteur=debutPartition/secteurTaille;
			long finSecteur=(taillePartition+debutPartition)/secteurTaille;

			//Calcul de toutes les adresses necessaire à l'écriture.
			long tailleVolume=new File(cheminFichier).length();

			long adresseDebut=posDebutDescripteurPartition+offsetDescripteurPartition*numeroDescripteur;
			adresseDebut=adresseDebut+offsetDebutPartition;

			long adresseDebutBackup=tailleVolume-secteurTaille-tailleDescripteurPartition;
			adresseDebutBackup=adresseDebutBackup+offsetDescripteurPartition*numeroDescripteur;
			adresseDebutBackup=adresseDebutBackup+offsetDebutPartition;

			long adresseFin=posDebutDescripteurPartition+offsetCrcEnTete*numeroDescripteur;
			adresseFin=adresseFin+offsetFinPartition;

			long adresseFinBackup=tailleVolume-secteurTaille-tailleDescripteurPartition;
			adresseFinBackup=adresseFinBackup+offsetDescripteurPartition*numeroDescripteur;
			adresseFinBackup=adresseFinBackup+offsetFinPartition;

			System.out.println("Taille : "+"0x"+Long.toHexString(tailleVolume));
			System.out.println("Adresse début : "+"0x"+Long.toHexString(adresseDebut));
			System.out.println("Adresse début backup : "+"0x"+Long.toHexString(adresseDebutBackup));
			System.out.println("Adresse fin : "+"0x"+Long.toHexString(adresseFin));
			System.out.println("Adresse fin backup : "+"0x"+Long.toHexString(adresseFinBackup));
			System.out.println("Début descripteur backup : "+"0x"+Long.toHexString(tailleVolume-secteurTaille-tailleDescripteurPartition));

			System.out.println("Début : "+"0x"+Long.toHexString(debutSecteur));
			System.out.println("Fin : "+"0x"+Long.toHexString(finSecteur));

			//Ecriture de la table de partition.
			//Ecriture du début de la partition sur les descripteurs standards et backup
			ecriture8Inverse(cheminFichier,adresseDebut,debutSecteur);
			ecriture8Inverse(cheminFichier,adresseDebutBackup,debutSecteur);

			//Ecriture de fin de la partition sur les descripteurs standards et backup
			ecriture8Inverse(cheminFichier,adresseFin,finSecteur);
			ecriture8Inverse(cheminFichier,adresseFinBackup,finSecteur);

			crcGPTHeader(cheminFichier);
			crcGPTHeader(cheminFichier);


		}

		//********** MÉTHODES CRÉÉES POUR LES DISQUETTES UNIX DU CO3 DE CHOOZ B **********
		public void LectFichierParametre(String NomFichier)
	  {
	    long tailleFichier;
	    long curseur;
	    float valeurParametre;
	    int valeurParametreEntier;

	    String paramInjection;
	    String trigrammeTranche;

	    byte val4octets[]=new byte[4];

	    String nomParametre;
	    char caractereParametre[]=new char[15];
	    boolean entier;

	    RandomAccessFile fichier;
	    RandomAccessFile fichierSortie;

	    int i;

	    //DEBUT
	    try
	    {
	      fichier=new RandomAccessFile(NomFichier,"r");
	      fichierSortie=new RandomAccessFile(NomFichier+".txt","rw");

	      //Récupération du Trigramme de la tranche
	      trigrammeTranche=this.lectAscii(NomFichier,0x0E,3);

	      fichierSortie.writeBytes("----- "+NomFichier+" SITE : "+trigrammeTranche+" -----\n\n");

		  //On se positionne sur le premier paramètre.
	      curseur=0x20;
	      fichier.seek(curseur);

	      tailleFichier=fichier.length();

	      while(curseur<tailleFichier)
	      {
	        nomParametre=this.lectAscii(NomFichier,curseur,15);

	        //MAJ du curseur, la méthode lectAscii, ne bouge le curseur qu'à l'intérieur de l'objet opFi, qui est fermé, il faut donc bouger le curseur 'manuellement' pour prendre en compte la lecture.
	        curseur=curseur+15;
	        fichier.seek(curseur);

	        //On lit un paramètre en le codant avec la norme IEEE754
	        valeurParametre=fichier.readFloat();
	        curseur=fichier.getFilePointer();
	        curseur=curseur-4;
	        fichier.seek(curseur);

	        //On revient 4 octets en arrière pour relire la même valeur mais en INT
	        valeurParametreEntier=fichier.readInt();
	        curseur=fichier.getFilePointer();
	        curseur=curseur-4;
	        fichier.seek(curseur);

	        //On revient en arrière une dernière fois, afin de lire et de ranger les octets dans un tableau de byte.
	        //Si la valeur contenu dans le premier octet est inférieur à 127, on en déduit qu'il s'agit d'un entier, sinon il s'agit d'un float.
	        fichier.read(val4octets);

	        //Si le premier octet est à 0x00 c'est qu'il ne s'agit pas d'un float.
	        if(val4octets[0]==0)
	        {
	          fichierSortie.writeBytes(nomParametre);

	          //Conversion en String de la valeur du paramètre.
	          paramInjection=new String().valueOf(valeurParametreEntier);
	          paramInjection=paramInjection+"\n";

	          fichierSortie.writeBytes(paramInjection);
	        }

	        else
	        {
	          fichierSortie.writeBytes(nomParametre);

	          //Conversion en String de la valeur du paramètre.
	          paramInjection=new String().valueOf(valeurParametre);
	          paramInjection=paramInjection+"\n";

	          fichierSortie.writeBytes(paramInjection);
	        }

	        curseur=fichier.getFilePointer();

	      }

	      fichier.close();
	      fichierSortie.close();

	    }

	    catch (FileNotFoundException erFile)
	    {
	      System.out.println("Erreur de fichier [INTROUVABLE] ...");
	    }

	    catch (IOException erFile)
	    {
	      System.out.println("Erreur de fichier [I/O] ...");
	    }

	  }
}
