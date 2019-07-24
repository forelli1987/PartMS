/*
***** fat.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Juillet 2019

Description fichier :
Caisse à outil pour la gestion du système de fichier FAT.
Hérite de la classe operationFichier.

*/

public class fat extends operationFichier
{
	//Variables d'instance
	//Chemin
	private String pathDevice;
	
	//Header FAT12/16
	private String nomProg; //Nom programme qui a formaté le volume
	private int nbrOctetSecteur; //Nombre d'octet par secteur
	private int nbrSecteurCluster; //Nombre de secteur par cluster
	private int nbrSecteurReserve; //Nbr de secteur réservé en comptant le secteur de boot (32 pour le FAT32 et 1 pour le 12 et 16)
	private int nbrFat; // Nbr fat sur le disque (de table d'allocation de fichier).
	private int nbrEntree; // Nombre d'entrée dans le répertoire racine. 0 pour le FAT32
	private int nbrSecteur16; // Nombre de secteur 16 bits. 0 pour le FAT32.
	private int typeDisque; // 0xF8 disque dur, 0xF0 disquette
	private int tailleFatSecteur; //Taille d'une table d'allocation de fichier en secteur.
	private int nbrSecteurPiste; //Nombre de secteur par piste.
	private int nbrTete; //Nombre de tête
	private long nbrSecteurCache; //Nombre de secteur caché.
	private long nbrSecteur32; //0 si nbrSecteur16 != 0 sinon contient une valeur.
	
	//BPB spécifique FAT12/16.
	private int idDisque; // 0x00 pour les amovibles, 0x80 pour les disques fixes.
	private int signature; // Signature 0x29 par défaut.
	private long serialNum; // Numéro de série du disque.
	private String nomDisque; // Nom du volume sur 11 caractères. NO NAME si rien.
	private String type16; // FAT,FAT12 ou FAT16.

	//Constructeur
	public fat(String pathParam){
		this.pathDevice=pathParam;
		
		//Lecture Header du fat
		this.nomProg=this.lectAscii(this.pathDevice,0x3L,8);
		this.nbrOctetSecteur=this.lecture2Inverse(this.pathDevice,0x0bL);
		this.nbrSecteurCluster=this.lecture1(this.pathDevice,0x0DL);
		this.nbrSecteurReserve=this.lecture2Inverse(this.pathDevice,0x0EL);
		this.nbrFat=this.lecture1(this.pathDevice,0x10L);
		this.nbrEntree=this.lecture2Inverse(this.pathDevice,0x11L);
		this.nbrSecteur16=this.lecture2Inverse(this.pathDevice,0x13L);
		this.typeDisque=this.lecture1(this.pathDevice,0x15L);
		this.tailleFatSecteur=this.lecture2Inverse(this.pathDevice,0x16L);
		this.nbrSecteurPiste=this.lecture2Inverse(this.pathDevice,0x18L);
		this.nbrTete=this.lecture2Inverse(this.pathDevice,0x1AL);
		this.nbrSecteurCache=this.lecture4Inverse(this.pathDevice,0x1C);
		this.nbrSecteur32=this.lecture4Inverse(this.pathDevice,0x20);
		
		//Lecture du BPB fat12/16
		this.idDisque=this.lecture1(this.pathDevice,0x24L);
		this.signature=this.lecture1(this.pathDevice,0x26L);
		this.serialNum=this.lecture4Inverse(this.pathDevice,0x27);
		this.nomDisque=this.lectAscii(this.pathDevice,0x2BL,11);
		this.type16=this.lectAscii(this.pathDevice,0x36L,8);
	}
	
	//***** Setter et getter *****
	@Override
	public String toString(){
		String retour="";
	
		retour=" --- LECTURE HEADER SYSTÈME FAT ---\n";
		retour=retour+" Prog formatage : \t\t"+this.nomProg+"\n";
		retour=retour+" Octet/secteur : \t\t"+this.nbrOctetSecteur+"\n";
		retour=retour+" Secteur/cluster : \t\t"+this.nbrSecteurCluster+"\n";
		retour=retour+" Secteur(s) réservé(s) \t\t"+this.nbrSecteurReserve+"\n";
		retour=retour+" Nbr table d'allocation : \t"+this.nbrFat+"\n";
		retour=retour+" Nbr d'entrée : \t\t"+this.nbrEntree+"\n";
		retour=retour+" Nbr de secteur 16 bits : \t"+this.nbrSecteur16+"\n";
		retour=retour+" HardDrive[0xF8] Floppy[0xF0] : 0x"+Integer.toHexString(this.typeDisque)+"\n";
		retour=retour+" Fat size : \t\t\t"+this.tailleFatSecteur+" secteur(s)\n";
		retour=retour+" Secteur(s) caché(s) : \t\t"+this.nbrSecteurCache+"\n";
		retour=retour+" Secteur 32bits : \t\t"+this.nbrSecteur32+"\n\n";
		
		retour=retour+" --- BPB FAT12 OU FAT16 ---\n";
		retour=retour+" ID Amovible[0x00] Fixe[0x80] : 0x"+Integer.toHexString(this.idDisque)+"\n";
		retour=retour+" Signature : \t\t\t0x"+Integer.toHexString(this.signature)+"\n";
		retour=retour+" Serial num : \t\t\t0x"+Long.toHexString(this.serialNum)+"\n";
		retour=retour+" Nom : \t\t\t\t"+this.nomDisque+"\n";
		retour=retour+" Type de FS : \t\t\t"+this.type16+"\n";
				
		return retour;
	}
	
	
	//***** Méthodes spécifiques. *****
	
}
