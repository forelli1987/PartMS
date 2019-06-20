/*
***** operationFichier.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Caisse à outil pour la gestion des fichiers.

*/

//Gestion boîte de dialogue

//La fenêtre de base
import java.awt.FlowLayout;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Gestion des fichiers

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;

//Hérite de JDialog pour pouvoir ajouter des barres de progression ou autre.
public class operationFichier extends JFrame
{

	//Bloc de déclaration des variables communes.
	private int cleCut[]={0,0,0,0};

	private BufferedInputStream fis=null;
	private BufferedOutputStream fos=null;

	private byte buf[]=new byte[1];
	private int n=0;

	private byte tempByteFinal=0;

	private int nbrBaguefile=0;
	private int nbrBagueFor=4;
	private long adresseFichier=0;
	private int tailleDonnee=0;

	private int[] cleSupplementaire={0x19,0x87,0x20,0x16,0x19,0x85,0xAF,0xFF,0xCA,0xFC};
	private String limSeize="-------------------------------------------------------------\n\n";
	private long nbrTraceLigne=14;

	private int tailleBloc=1024;

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


	//Méthode de cryptage en fonction d'une clé et d'un nom de fichier.
	public void cryptageFichier(String nomFichier, int[] cleDecoupe, String nomFichierCrypte)
	{
		int tailleBoucle=cleDecoupe.length;

		try
		{
			fis=new BufferedInputStream(new FileInputStream(new File(nomFichier)));
			fos = new BufferedOutputStream(new FileOutputStream(new File(nomFichierCrypte),true));//Le true signifie append

			//Tant que tous les octets du fichier ne sont pas parcouru, alors on effectue le cryptage.
			while ((n=fis.read(buf))>=0)
			{
				for(byte bit:buf)
				{
					tempByteFinal=(byte)(bit^cleDecoupe[0]);//Dans le cas d'un indici à 0

					for(int i=1;i<tailleBoucle;i++)
					{
						tempByteFinal=(byte)(tempByteFinal^cleDecoupe[i]);
					}

					fos.write(tempByteFinal);
				}


			}

			fis.close();
			fos.close();
		}

		catch (FileNotFoundException erFile)
		{
			erFile.printStackTrace();
		}

		catch (IOException erFile)
		{
			erFile.printStackTrace();
		}


	}

	//Méthode permettant de décrypter le fichier
	public void deCryptageFichier(String nomFichier, int[] cleDecoupe,String nomFichierDecrypte)
	{
		int tailleBoucle=cleDecoupe.length;

		try
		{
			fis=new BufferedInputStream(new FileInputStream(new File(nomFichier)));
			fos = new BufferedOutputStream(new FileOutputStream(new File(nomFichierDecrypte),true));


			while ((n=fis.read(buf))>=0)
			{
				for(byte bit:buf)
				{

					//Boucle de décryptage
					//tempByteFinal=(byte)(bit^cleSupplementaire[5]);

					tempByteFinal=(byte)(bit^cleDecoupe[tailleBoucle-1]);

					for(int i=tailleBoucle-2;i>=0;i--)
					{
						tempByteFinal=(byte)(tempByteFinal^cleDecoupe[i]);
					}

					fos.write(tempByteFinal);

				}


			}

			fis.close();
			fos.close();
		}

		catch (FileNotFoundException erFile)
		{
			erFile.printStackTrace();
		}

		catch (IOException erFile)
		{
			erFile.printStackTrace();
		}


	}

	//Méthode permettant la conversion de la clé en chaîne avec des tirets en quatre valeurs distinctes et interprétables.
	public int[] decoupageCle(String cle,int tailleBoucle)
	{
		int i=0;

		//Les trois chiffres sont découpés afin de leurs appliquer un coeff multiplicateur -> le premier chiffre est multiplié par 100 le second par 10 et le troisième par 1
		int indice1=0;
		int indice2=0;
		int indice3=0;
		int deltaAscii=48;

		int ring[]=new int[tailleBoucle];

		for(i=0;i<tailleBoucle;i++)
		{
			indice1=i*4;
			indice2=i*4+1;
			indice3=i*4+2;

			ring[i]=(int)((cle.charAt(indice1))-deltaAscii)*100;
			ring[i]=(int)((cle.charAt(indice2))-deltaAscii)*10+ring[i];
			ring[i]=(int)((cle.charAt(indice3))-deltaAscii)+ring[i];

			//Si on a une valeur supérieur à 255.
			if(ring[i]>255)
			{
				ring[i]=255;
			}

		}

		return ring;

	}

	//Génère une clé aléatoire.
	public int[] cleAleatoire(int nbrBague)
	{
		int nbr[]=new int[nbrBague];

		//reconstruction clé aléatoire.
		for(int i=0;i<nbrBague;i++)
		{
			nbr[i]=(int)(Math.random()*(256-5)+5);//(max-min)+min
		}

		return nbr;
	}

	//Permet de générer un fichier uniquement composé d'octet aléatoire.
	//En arg on a le chemin du fichier, la taille et l'unité de taille : 2,'m' 2mo
	public void remplAleatoire(String pathFichier,int tailleFichier,char uniteTaille)
	{
		//Initialisation des variables
		long adresseFichier=0;
		long tailleBloc=0;

		int j=0;
		long i=0;

		//Nombre de cycle d'écriture calculé.
		long nbrIteration=0;

		//On calcule la taille en octet désirée. En utilisant la fonction calcOctet.
		adresseFichier=calcOctet(tailleFichier,uniteTaille);

		//Calculer la taille des blocs
		tailleBloc=calcBloc(adresseFichier);
		nbrIteration=adresseFichier/tailleBloc;

		byte donneesInjection[]=new byte[(int)tailleBloc];

		try
		{
			//Ajouter
			//Si le fichier existe, le supprimer pour recommencer à 0.

			RandomAccessFile fos = new RandomAccessFile(pathFichier,"rw");

			//Tableau aleatoire

			while(j<nbrIteration)
			{
				//Création d'un tableau aleatoire de 'tailleBloc'
				for(i=0;i<tailleBloc;i++)
				{
					donneesInjection[(int)i]=(byte)(Math.random()*256);
				}

				//Injection du bloc dans le fichier.
				fos.write(donneesInjection);
				j++;
			}

			fos.close();
		}

		catch (FileNotFoundException erFile)
		{
			erFile.printStackTrace();
		}

		catch (IOException erFile)
		{
			erFile.printStackTrace();
		}

	}

	//Fonction de calcul en fonction de la valeur et de l'unité
	public long calcOctet(int valeur, char unite)
	{
		long tailleCalculee=0;

		switch(unite)
		{
			case 'o':
			{
				tailleCalculee=(long)(valeur);
				break;
			}

			case 'k':
			{
				tailleCalculee=(long)(valeur*1024);
				break;

			}

			case 'm':
			{
				tailleCalculee=(long)(valeur*1024*1024);
				break;

			}

			case 'g':
			{
				tailleCalculee=(long)(valeur*1024*1024*1024);
				break;

			}
		}

		return tailleCalculee;
	}

	//Méthode permettant de ranger des données sous forme d'un tableau d'octet dans un fichier à partir d'une certaine adresse.
	public void rangementFichierCrypt(String pathFichier,int tab[],long adresseDebut)
	{

		int i=0;
		tailleDonnee=tab.length;
		int tabRecuperation[]={};


		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rw");
			fInjection.seek(adresseDebut);

			for(i=0;i<tailleDonnee;i++)
			{
				fInjection.writeByte((byte)tab[i]);
			}

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}


	}

	//Lecture d'un fichier, retourne un tableau d'octet.
	//En argument on a besoin du fichier, de l'adresse de début et de la taille que l'on souhaite extraire.
	public int[] lectFichierCrypt(String pathFichier,long adresseDebut)
	{


		File fichTempo=new File(pathFichier);
		int taillo=Long.valueOf(fichTempo.length()).intValue();

		int tabRecup[] = new int[taillo];

		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"r");
			long taille=fInjection.length();
			tabRecup=new int[(int)(taille)];

			fInjection.seek(adresseDebut);

			for(int i=0;i<taille;i++)
			{
				tabRecup[i]=(int)(fInjection.readByte());
			}

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

		return tabRecup;

	}

	//Renvoi les valeurs binaires de la clé pour la stéganographie de celle ci dans le fichier aléatoire.
	public String[] decoupageBinaire(int[] tabCle)
	{
		int tailleBoucle=tabCle.length;
		String retourCle[]=new String[tailleBoucle];

		for(int i=0;i<tailleBoucle;i++)
		{
			retourCle[i]=Integer.toBinaryString(tabCle[i]);

			//Boucle de construction forçant le nombre de bit à 8.

			while(retourCle[i].length()<8)
			{
				retourCle[i]="0"+retourCle[i];

			}

		}

		return retourCle;
	}

//Conversion d'un tableau de chaîne contenant des octets en tableau d'entier.
//Conversion binaire / entier
	public int[] binChaineToInteger(String[] tabBinaire)
	{
		int longueurChaine=tabBinaire.length;
		int i=0;
		int j=0;
		int k=0;
		char caracTempo='0';
		int[] tabSortie=new int[longueurChaine];

		for(i=0;i<longueurChaine;i++)
		{
			k=0;
			for(j=7;j>=0;j--)
			{
				caracTempo=tabBinaire[i].charAt(j);

				if(caracTempo=='1')
				{
					tabSortie[i]=(int)(tabSortie[i]+Math.pow(2,k));
				}
				k++;

			}

		}

		return tabSortie;

	}

	//écriture dans un fichier en utilisant la stéganogrphie.
	//String donneeSteg : tableau contenant des données binaires-> donneeSteg[0]="00011100".
	//String pathFichier : chemin du fichier à traiter.
	//long adresseDebut : adresse de départ pour l'écriture.
	//long taille : nombre d'octet à écrire.

	public void steganoFichierEcriture(String donneeSteg[],String pathFichier,long adresseDebut)
	{
		int donneeTempo=0;
		byte donneeByteTempo=0;
		long posTemp=0; //Garde la position du curseur temporairement.
		char caractereTempo='A';
		int tailleBoucle=donneeSteg.length;

		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rw");
			fInjection.seek(adresseDebut);

			for(int i=0;i<tailleBoucle;i++)
			{

				for(int j=0;j<8;j++)
				{

					caractereTempo=donneeSteg[i].charAt(j);

					//on sauvegarde la position courante du curseur.
					posTemp=fInjection.getFilePointer();

					//On lit l'occurence
					donneeTempo=fInjection.read();

					//Modification de la valeur dans le cas où le bit de poids faible ne correspond pas à celui dans la boucle
					if(caractereTempo=='0' && donneeTempo%2!=0 || caractereTempo=='1' && donneeTempo%2==0)
					{
						//On incrémente afin de rendre la valeur impaire ou paire selon ce que l'on désire.
						donneeByteTempo=(byte)donneeTempo;

						donneeByteTempo++;

						//On recule notre curseur de fichier pour écraser la valeur précédemment lue.
						fInjection.seek(posTemp);

						//Ecriture de l'octet modifié
						fInjection.writeByte(donneeByteTempo);
					}

					posTemp=0;

				}

			}

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	//reconstruction d'un tableau de chaîne représentant des bits.
	//Argument le nom de fichier à lire et la position du curseur.

	//ex String retour[0]="11100011"
	public String[] steganoFichierLecture(String pathFichier,long adresseDebut,int tailleBoucle)
	{
		String retourBinaire[]=new String[tailleBoucle];

		int donneeTempo=0;
		byte donneeByteTempo=0;
		long posTemp=0; //Garde la position du curseur temporairement.
		char caractereTempo='A';

		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rw");
			fInjection.seek(adresseDebut);

			//Boucle de parcours de la chaîne
			for(int i=0;i<tailleBoucle;i++)
			{

				//sous boucle permettant de reconstruire caractère par caractère la chaîne.
				for(int j=0;j<8;j++)
				{
					donneeByteTempo=fInjection.readByte();

					//Dans le cas où le bit de poid faible vaut un.
					if(donneeByteTempo%2!=0)
					{
						//Pour les première occurence quand la chaîne est vide
						if(retourBinaire[i]==null)
						{
							retourBinaire[i]="1";
						}

						else
						{
							retourBinaire[i]=retourBinaire[i]+"1";
						}

					}

					else if(donneeByteTempo%2==0)
					{
						if(retourBinaire[i]==null)
						{
							retourBinaire[i]="0";
						}

						else
						{
							retourBinaire[i]=retourBinaire[i]+"0";
						}
					}
				}



			}

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

		return retourBinaire;

	}

	//Utilisation : renseigner le tableau contenant la clé sur 1 octet.
	//True pour chiffrer false pour déchiffrer la clé.
	public int[] chiffrageCleTab(int[] cleAcrypter,boolean cryptDecrypt)
	{
		int i=0;
		int j=0;
		int tailleBoucle=cleAcrypter.length;
		int retourChiffrage[]=new int[tailleBoucle];

		int tempoChiffrage=0;

			//Chiffrage
		if(cryptDecrypt==true)
		{
			for(i=0;i<tailleBoucle;i++)
			{
					retourChiffrage[i]=cleAcrypter[i]^cleSupplementaire[0];
					for(j=1;j<10;j++)
					{
						retourChiffrage[i]=retourChiffrage[i]^cleSupplementaire[j];
					}
			}
		}


			//Déchiffrage
		else if(cryptDecrypt==false)
		{
			for(i=0;i<tailleBoucle;i++)
			{
					retourChiffrage[i]=cleAcrypter[i]^cleSupplementaire[9];
					for(j=8;j>=0;j--)
					{
						retourChiffrage[i]=retourChiffrage[i]^cleSupplementaire[j];
					}

			}
		}

		return retourChiffrage;

	}

	public void remplirOctetBlockLess(String pathFichier,long taille)
	{

		long i=0;

		//Début d'écriture.
		try
		{

			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rws");

			i=0;
			while(i<taille)
			{
				fInjection.writeByte((byte)(0x00));
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	public void remplirOctetBlockLess(String pathFichier,long curseur,long taille)
	{

		long i=0;

		//Début d'écriture.
		try
		{

			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rw");

			fInjection.seek(curseur);
			i=0;
			while(i<taille)
			{
				fInjection.writeByte((byte)(0x0));
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	public void remplirOctet(String pathFichier,long taille)
	{

		long i=0;

		long tailleRecup=0;
		int tailleBloc=0;


		//Début d'écriture.
		try
		{

			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rws");
			tailleRecup=taille;

			tailleBloc=calcBloc(tailleRecup);
			byte ecriture[]=new byte[tailleBloc];



		//Initialisation d'un tableau
			for(i=0;i<(long)(tailleBloc);i++)
			{
				ecriture[(int)(i)]=(byte)(0);
			}

			tailleRecup=tailleRecup/(long)(tailleBloc);

			i=0;

			while(i<tailleRecup)
			{
				fInjection.write(ecriture);
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}


	}

	public void remplirOctet(String pathFichier,long curseur,long taille)
	{
		long i=0;

		long tailleRecup=0;
		int tailleBloc=0;


		//Début d'écriture.
		try
		{

			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rws");
			tailleRecup=taille;

			tailleBloc=calcBloc(tailleRecup);
			byte ecriture[]=new byte[tailleBloc];



		//Initialisation d'un tableau
			for(i=0;i<(long)(tailleBloc);i++)
			{
				ecriture[(int)(i)]=(byte)(0);
			}

			tailleRecup=tailleRecup/(long)(tailleBloc);

			i=0;
			fInjection.seek(curseur);
			while(i<tailleRecup)
			{
				fInjection.write(ecriture);
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	public void remplirOctet(String pathFichier)
	{

		long i=0;

		long tailleRecup=0;
		int tailleBloc=0;


		//Début d'écriture.
		try
		{

			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rws");
			tailleRecup=fInjection.length();

			tailleBloc=calcBloc(tailleRecup);
			byte ecriture[]=new byte[tailleBloc];



		//Initialisation d'un tableau
			for(i=0;i<(long)(tailleBloc);i++)
			{
				ecriture[(int)(i)]=(byte)(0);
			}

			tailleRecup=tailleRecup/(long)(tailleBloc);

			i=0;

			while(i<tailleRecup)
			{
				fInjection.write(ecriture);
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}


	public void remplirOctet(String pathFichier,long taille,byte valOctet)
	{

		long i=0;
		int tailleBloc=calcBloc(taille);
		long tailleRecup=taille/(long)(tailleBloc);


		byte ecriture[]=new byte[tailleBloc];

		//Initialiser le tableau
		for(i=0;i<tailleBloc;i++)
		{
			ecriture[(int)(i)]=(byte)(valOctet);
		}

		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(pathFichier,"rws");

			//On impose la taille du fichier

			i=0;
			while(i<tailleRecup)
			{
				fInjection.write(ecriture);
				i++;
			}

			fInjection.close();

		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}


	//Méthode de comparaison des octets.
	//Si true, écriture de toutes les valeurs d'octet différente à 0x0 dans le fichier
	//Si false, écriture de tous les octets.
	public void compareFichier(String pathLecture,String pathEnregistrement,boolean compZero)
	{
		long i=0;
		int j=0;
		long curseurTempo=0;
		byte tempo=0;
		String tempoSt="";
		String blocInjection="";
		long tailleBlocEcriture=0;
		//int operandeLogique=0;

		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(pathEnregistrement,"rws");
			RandomAccessFile fLecture=new RandomAccessFile(pathLecture,"r");

			//Titre dans le fichier d'enregistrement.

			if(compZero)
			{
				fInjection.writeBytes("---- COMPARAISON -> 0x00---- \n\n");
			}

			else if(!compZero)
			{
				fInjection.writeBytes("---- LECTURE "+pathLecture+" ----\n\n");
			}


			fInjection.writeBytes("Offset\t|\tOctet diff\t|\tCHAR\n");

			tailleBlocEcriture=calcBloc(fLecture.length());

			while(i<fLecture.length())
			{
				tempo=fLecture.readByte();
				//Suppression des 0xffffff en trop.
				if(Integer.toHexString((int)(tempo)).length()>2)
				{
					tempoSt=Integer.toHexString((int)(tempo));
					tempoSt=tempoSt.substring(6,8);
				}

				else
				{
					tempoSt=Integer.toHexString((int)(tempo));
				}


				//Si true
				if(compZero && tempo!=0x00)
				{

					//Affichage des caractère ascii ou non dans le fichier de retour.
					if(tempo<0x21 || tempo>0x7e)
					{
						//fInjection.writeBytes("0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+"!?!"+"\n");
						blocInjection=blocInjection+"0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+"|?|"+"\n";
					}

					else
					{
						//fInjection.writeBytes("0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+(char)(tempo)+"\n");
						blocInjection=blocInjection+"0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+(char)(tempo)+"\n";
					}


				}

				//Si false
				//Bufferisation
				else if(!compZero)
				{


					//Construction d'un tableau
					//Affichage des caractère ascii ou non dans le fichier de retour.
					if(tempo<0x21 || tempo>0x7e)
					{
						blocInjection=blocInjection+"0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+"|?|"+"\n";
					}

					else
					{
						blocInjection=blocInjection+"0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+(char)(tempo)+"\n";
					}

				}

				i++;
				j++;

				//Ecriture
				if(j==tailleBlocEcriture)
				{
					fInjection.writeBytes(blocInjection);
					j=0;
					blocInjection="";
				}
			}


		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}


	//Comparaison d'octet à partir d'un opérande.
	public void compareFichier(String pathLecture,String pathEnregistrement,int octetOperande)
	{
		long i=0;
		int j=0;
		long curseurTempo=0;
		byte tempo=0;
		String tempoSt="";
		String octetSt="";
		//int operandeLogique=0;

		//Suppression des 0xffffff en trop.
		if(Integer.toHexString((int)(octetOperande)).length()>2)
		{
			octetSt=Integer.toHexString((int)(octetOperande));
			octetSt=octetSt.substring(6,8);
		}

		else
		{
			octetSt=Integer.toHexString((int)(octetOperande));
		}

		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(new File(pathEnregistrement),"rws");
			RandomAccessFile fLecture=new RandomAccessFile(new File(pathLecture),"rws");



			//Titre dans le fichier d'enregistrement.

			fInjection.writeBytes("---- COMPARAISON  ----\n");
			fInjection.writeBytes("Offset\t|\tOctet diff\t|\tCHAR\n");

			while((tempo=(byte)(fLecture.readByte()))!=-1)
			{
				//Suppression des 0xffffff en trop.
				if(Integer.toHexString((int)(tempo)).length()>2)
				{
					tempoSt=Integer.toHexString((int)(tempo));
					tempoSt=tempoSt.substring(6,8);
				}

				else
				{
					tempoSt=Integer.toHexString((int)(tempo));
				}

				//Si true
				if(tempo!=octetOperande)
				{

					//Affichage des caractère ascii ou non dans le fichier de retour.
					if(tempo<0x21 || tempo>0x7e)
					{
						fInjection.writeBytes("0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+"!?!"+"\n");
					}

					else
					{
						fInjection.writeBytes("0x"+Long.toHexString(i)+"\t|\t"+"0x"+tempoSt+"\t\t |\t"+(char)(tempo)+"\n");
					}

				}

				i++;

			}


		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	//Surcharge de la méthode.
	//Permet les comparaisons inter-fichiers et d'enregistrer les différences.

	public void compareFichier(String pathLecture1,String pathLecture2,String pathEnregistrement)
	{
		long i=0;
		long curseurTempo=0;
		byte tempo1=0;
		byte tempo2=0;
		String tempoSt1="";
		String tempoSt2="";
		String sTi="";
		int j=0;
		//int operandeLogique=0;

		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(new File(pathEnregistrement),"rws");
			RandomAccessFile fLecture1=new RandomAccessFile(new File(pathLecture1),"rws");
			RandomAccessFile fLecture2=new RandomAccessFile(new File(pathLecture2),"rws");

			//Titre dans le fichier d'enregistrement.

			fInjection.writeBytes("---- COMPARAISON inter fichiers ---- \nFichier1 : "+pathLecture1+"\nFichier2 : "+pathLecture2+"\n\n");
			fInjection.writeBytes("Offset\t|\tOctet fichier1\t|\tOctet fichier2\n");



			while((tempo1=(byte)(fLecture1.readByte()))!=-1)
			{
				sTi=Long.toHexString(i);
				//Lecture du second octet.
				tempo2=fLecture2.readByte();

				//Suppression des 0xffffff en trop.
				if(Integer.toHexString((int)(tempo1)).length()>2)
				{
					tempoSt1=Integer.toHexString((int)(tempo1));
					tempoSt1=tempoSt1.substring(6,8);
				}

				else
				{
					tempoSt1=Integer.toHexString((int)(tempo1));
				}

				//Suppression des 0xffffff en trop.
				if(Integer.toHexString((int)(tempo2)).length()>2)
				{
					tempoSt2=Integer.toHexString((int)(tempo2));
					tempoSt2=tempoSt2.substring(6,8);
				}

				else
				{
					tempoSt2=Integer.toHexString((int)(tempo2));
				}


				if(tempo1!=tempo2)
				{
					fInjection.writeBytes("0x"+sTi+"\t|\t"+"0x"+tempoSt1+"\t\t|\t"+"0x"+tempoSt2+"\n");
				}


				i++;

			}


		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}


	public void sauvFichier(String pathLecture,String pathEnregistrement)
	{
		//Découpage en bloc d'écriture BUFFERISATION

		long i=0;
		long curseurTempo=0;

		long fin=0;
		int tailleBloc=0;
		long tailleFichier=0;

		String tempoSt="";
		//int operandeLogique=0;

		i=0;
		//fInjection.seek(debut);

		tailleFichier=this.calcTaille(pathLecture);

		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(pathEnregistrement,"rw");
			RandomAccessFile fLecture=new RandomAccessFile(pathLecture,"r");

			fLecture.seek(0);

			//Calcul des itérations de boucle en fonction de la taille du bloc.

			tailleBloc=calcBloc(tailleFichier);


			byte tempo[]=new byte[tailleBloc];
			fin=tailleFichier/(long)(tailleBloc);

			i=0;
			while(i<fin)
			{
				fLecture.read(tempo);
				//Suppression des 0xffffff en trop.
				fInjection.write(tempo);

				i++;
			}


		}

		catch (FileNotFoundException erFile)
		{
			erFile.printStackTrace();
		}

		catch (IOException erFile)
		{
			erFile.printStackTrace();
		}

	}

	public void sauvFichier(String pathLecture,String pathEnregistrement,long debut)
	{
		//Découpage en bloc d'écriture BUFFERISATION

		long i=0;
		long curseurTempo=0;

		long fin=0;
		int tailleBloc=0;

		String tempoSt="";
		//int operandeLogique=0;

		i=0;



		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(pathEnregistrement,"rw");
			RandomAccessFile fLecture=new RandomAccessFile(pathLecture,"r");
			fInjection.seek(debut);

			fLecture.seek(0);

			//Calcul des itérations de boucle en fonction de la taille du bloc.
			tailleBloc=calcBloc(fLecture.length());

			byte tempo[]=new byte[tailleBloc];
			fin=fLecture.length()/(long)(tailleBloc);

			i=0;
			while(i<fin)
			{
				fLecture.read(tempo);
				//Suppression des 0xffffff en trop.
				fInjection.write(tempo);

				i++;
			}


		}

		catch (FileNotFoundException erFile)
		{

		}

		catch (IOException erFile)
		{

		}

	}


	//Surcharge de la méthode pour sauver les fichiers.
	//On renseigne le curseur de début et de fin de la copie
	public void sauvFichier(String pathLecture,String pathEnregistrement,long debut,long taille)
	{

		long i=0;
		long curseurTempo=0;
		int tailleBloc=calcBloc(taille);
		long fin=taille/(long)(tailleBloc);
		byte tempo[]=new byte[tailleBloc];

		String tempoSt="";
		//int operandeLogique=0;

		i=0;
		//fInjection.seek(debut);


		try
		{
			//Ouverture du fichier de lecture et du fichier d'écriture.
			RandomAccessFile fInjection=new RandomAccessFile(pathEnregistrement,"rw");
			RandomAccessFile fLecture=new RandomAccessFile(pathLecture,"r");

			fLecture.seek(debut);


			while(i<fin)
			{
				fLecture.read(tempo);
				//Suppression des 0xffffff en trop.
				fInjection.write(tempo);

				i++;
			}


		}

		catch (FileNotFoundException erFile)
		{
		}

		catch (IOException erFile)
		{
		}

	}

	public String lectAscii(String pathFichier,long debut,int taille)
	{
		String retourLecture="";
		byte tamponLecture[]=new byte[taille];
		char tabChar[]=new char[taille];

		try
		{
			RandomAccessFile fLectureAscii=new RandomAccessFile(pathFichier,"r");
			fLectureAscii.seek(debut);


			fLectureAscii.read(tamponLecture);

			for(int i=0;i<taille;i++)
			{
				tabChar[i]=(char)(tamponLecture[i]);
			}

			retourLecture=new String().valueOf(tabChar);


			fLectureAscii.close();

		}


		catch (FileNotFoundException erFile)
		{}

		catch (IOException erFile)
		{}

		return retourLecture;

	}

	public void injectionAscii(String pathFichier,String asciiInjection,long position)
	{

		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(new File(pathFichier),"rw");
			fInjection.seek(position);


			fInjection.writeBytes(asciiInjection);


			fInjection.close();
		}


		catch (FileNotFoundException erFile)
		{}

		catch (IOException erFile)
		{}


	}

	public void injectionHexa(String pathFichier,String valHexa,long position)
	{
		try
		{
			RandomAccessFile fInjection=new RandomAccessFile(new File(pathFichier),"rw");
			fInjection.seek(position);


			fInjection.writeByte((byte)(Integer.parseInt(valHexa,16)));


			fInjection.close();
		}


		catch (FileNotFoundException erFile)
		{}

		catch (IOException erFile)
		{}


	}

	//Permet de calculer la taille du buffer en fonction d'une taille de fichier.
	private int calcBloc(long taille)
	{
		long tailleTab[]={2048L,104857600L,314572800L,4294967296L};
		int tailleRetour=0;

		if(taille<=tailleTab[0])
		{
			tailleRetour=256;
		}

		//2048 octets
		else if(taille>tailleTab[0] &&taille<=tailleTab[1])
		{
			tailleRetour=2048;
		}

		//1 mo
		else if(taille>tailleTab[1] && taille<=tailleTab[2])
		{
			tailleRetour=1048576;
		}

		//2 mo
		else if(taille>tailleTab[2] /*&& taille<=tailleTab[3]*/)
		{
			tailleRetour=2097152;
		}

		return tailleRetour;

	}

	public long calcTaille(String cheminFichier)
	{
		long i=0;

		try
		{
			RandomAccessFile fTaille=new RandomAccessFile(cheminFichier,"r");
			fTaille.seek(0);

			//fTaille.readByte(byte[] b);

			i=fTaille.length();
			fTaille.close();

		}


		//catch (FileNotFoundException erFile){}
		catch (IOException erFile){}
		catch (NullPointerException erFile){}


		//catch (EOFException erFile){}
		return i;

	}

//***** Gestion du LittleEndian

        public long lecture8Inverse(String fichierALire,long position)
        {
            byte[] retourLecture=new byte[8];
            long retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fLecture=new RandomAccessFile(fichierALire,"r");
                fLecture.seek(position);
                fLecture.read(retourLecture);
                fLecture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

            retourCalcule= (retourLecture[7]<<56)&0xff00000000000000L|(retourLecture[6]<<48)&0x00ff000000000000L|(retourLecture[5]<< 40)&0x0000ff0000000000L|(retourLecture[4]<< 32)&0x000000ff00000000L|(retourLecture[3]<<24)&0x00000000ff000000L|(retourLecture[2]<<16)&0x0000000000ff0000L|(retourLecture[1]<< 8)&0x000000000000ff00L|(retourLecture[0]<< 0)&0x00000000000000ffL;


            return retourCalcule;


        }

        public double lecture8InverseD(String fichierALire,long position)
        {
            byte[] retourLecture=new byte[8];
            double retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fLecture=new RandomAccessFile(fichierALire,"r");
                fLecture.seek(position);
                fLecture.read(retourLecture);
                fLecture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

            retourCalcule= (retourLecture[7]<<56)&0xff00000000000000L|(retourLecture[6]<<48)&0x00ff000000000000L|(retourLecture[5]<< 40)&0x0000ff0000000000L|(retourLecture[4]<< 32)&0x000000ff00000000L|(retourLecture[3]<<24)&0x00000000ff000000L|(retourLecture[2]<<16)&0x0000000000ff0000L|(retourLecture[1]<< 8)&0x000000000000ff00L|(retourLecture[0]<< 0)&0x00000000000000ffL;


            return retourCalcule;


        }

        public void ecriture8Inverse(String fichierAEcrire,long position,long dataWrite)
        {
            byte[] reconstructionByte = new byte[8];

            //Inversion pour l'écriture dans le fichier
            reconstructionByte[7]=(byte)((dataWrite & 0xFF00000000000000L) >> 56);
            reconstructionByte[6]=(byte)((dataWrite & 0x00FF000000000000L) >> 48);
            reconstructionByte[5]=(byte)((dataWrite & 0x0000FF0000000000L) >> 40);
            reconstructionByte[4]=(byte)((dataWrite & 0x000000FF00000000L) >> 32);
            reconstructionByte[3]=(byte)((dataWrite & 0x00000000FF000000L) >> 24);
            reconstructionByte[2]=(byte)((dataWrite & 0x0000000000FF0000L) >> 16);
            reconstructionByte[1]=(byte)((dataWrite & 0x000000000000FF00L) >> 8);
            reconstructionByte[0]=(byte)((dataWrite & 0x00000000000000FFL) >> 0);

            try
            {
                RandomAccessFile fEcriture=new RandomAccessFile(fichierAEcrire,"rw");
                fEcriture.seek(position);
                fEcriture.write(reconstructionByte);
                fEcriture.close();
            }

            catch (IOException erFile){}
            catch (NullPointerException erFile){}

        }


        //Lecture taille dans le MBR qui tient sur 4 octets.
        public byte[] lectureByteInverse(byte [] retourLecture)
        {
			int nbr=retourLecture.length; //Récupération de la taille du tableau.
            byte[] lectureRetournee=new byte[nbr];
            int j=0;

            for(int i=nbr-1;i>=0;i--)
            {
				lectureRetournee[j]=retourLecture[i];
				j++;
			}

            return lectureRetournee;

        }

        public long lecture4Inverse(String fichierALire,long position)
        {
            byte[] retourLecture=new byte[4];
            long retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fLecture=new RandomAccessFile(fichierALire,"r");
                fLecture.seek(position);
                fLecture.read(retourLecture);
                fLecture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

            retourCalcule= (retourLecture[3]<<24)&0x00000000ff000000|(retourLecture[2]<<16)&0x0000000000ff0000|(retourLecture[1]<< 8)&0x000000000000ff00|(retourLecture[0]<< 0)&0x00000000000000ff;


            return retourCalcule;


        }

        //Ecriture sur 4 octets
        public void ecriture4Inverse(String fichierAEcrire,long position,int dataWrite)
        {
            byte[] reconstructionByte = new byte[4];

            //Inversion pour l'écriture dans le fichier
            reconstructionByte[3]=(byte)((dataWrite & 0xFF000000) >> 24);
            reconstructionByte[2]=(byte)((dataWrite & 0x00FF0000) >> 16);
            reconstructionByte[1]=(byte)((dataWrite & 0x0000FF00) >> 8);
            reconstructionByte[0]=(byte)((dataWrite & 0x000000FF) >> 0);

            try
            {
                RandomAccessFile fEcriture=new RandomAccessFile(fichierAEcrire,"rw");
                fEcriture.seek(position);
                fEcriture.write(reconstructionByte);
                fEcriture.close();
            }

            catch (IOException erFile){}
            catch (NullPointerException erFile){}

        }

        //Lecture 2 octets
        public int lecture2Inverse(String fichierALire,long position)
        {
            byte[] retourLecture=new byte[2];
            int retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fLecture=new RandomAccessFile(fichierALire,"r");
                fLecture.seek(position);
                fLecture.read(retourLecture);
                fLecture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

            retourCalcule=(retourLecture[1]<< 8)&0x0000ff00|(retourLecture[0]<< 0)&0x000000ff;

            return retourCalcule;

        }

        //Ecriture 2 octets
        public void ecriture2Inverse(String cheminFichier,long position,int dataWrite)
        {
            byte[] reconstructionByte = new byte[2];

            //Inversion pour l'écriture dans le fichier
            reconstructionByte[1]=(byte)((dataWrite & 0x0000FF00) >> 8);
            reconstructionByte[0]=(byte)((dataWrite & 0x000000FF) >> 0);

            try
            {
                RandomAccessFile fEcriture=new RandomAccessFile(cheminFichier,"rw");
                fEcriture.seek(position);
                fEcriture.write(reconstructionByte);
                fEcriture.close();
            }

            catch (IOException erFile){}
            catch (NullPointerException erFile){}
        }

        //Lecture 1 octet
        public int lecture1(String fichierALire,long position)
        {
            byte []retourLecture=new byte[1];
            int retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fLecture=new RandomAccessFile(fichierALire,"r");
                fLecture.seek(position);
                fLecture.read(retourLecture);
                fLecture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

            //retourCalcule=(retourLecture[0]<< 0)&0x00ff;
            retourCalcule=retourLecture[0] & 0x000000ff;

            return retourCalcule;

        }

		 //Lecture 1 octet
        public void ecriture1(String fichierALire,long position,int dataWrite)
        {
            byte []reconstructionByte=new byte[1];
			reconstructionByte[0]=(byte)((dataWrite & 0x000000FF)>>0);

            int retourCalcule=0;
            int i=0;
            try
            {
                RandomAccessFile fEcriture=new RandomAccessFile(fichierALire,"rw");
                fEcriture.seek(position);
                fEcriture.write(reconstructionByte);
                fEcriture.close();

            }


            catch (IOException erFile){}
            catch (NullPointerException erFile){}

		}

		//Le journal
		public void journal(String contenu, String nomFichier, boolean console)
		{
			//Récupération de la date courante.
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();

			if(console)
			{
				System.out.println("["+date+"]"+contenu);
			}

			else
			{
				try
				{
					String nomComplet="/var/log/"+nomFichier+".log";
					RandomAccessFile fichier=new RandomAccessFile(nomComplet,"rw");
										
					contenu="["+date+"]"+"\t"+contenu+"\n";

					fichier.writeBytes(contenu);
				}

				catch (IOException erFile){}
				catch (NullPointerException erFile){}
			}
		}
	

}
