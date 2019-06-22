/*
***** fenPartMS.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Construction de la fenêtre principale.
C'est ici que tous les composants sont construits.
C'est par ici que les commandes du programme sont lancées.

*/

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;

//Gestion des actions.
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;

//Barre de menu
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

//Choix d'un fichier
import javax.swing.filechooser.*;
import javax.swing.JFileChooser;
import java.io.File;


import java.io.IOException;

//Gestion des boîtes de dialogue
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;


public class fenPartMS extends JFrame implements ActionListener
{

	//Texte utilisé sur les composants.
	private final String txtInformations="Informations des descripteurs\n";
	private final String txtRafraichir="Rafraîchir";
	private final String txtChampFichier="Path du bloc's file";

	//Texte du menu
	private final String txtVolume="Volume";
	private final String txtStatVolume="Information volume";
	private final String txtCloseVolume="Fermer le volume";
	private final String txtPartition="Gestion table de partition";
	private final String txtSupprTable="Supprimer la table de partition";
	private final String txtCreateTable="Créer une table de partition type MSDOS";
	private final String txtFormatageBasNiveau="Formatage bas niveau";
	private final String txtMagicNumber="Gestion 'MAGIC NUMBER' du MBR";
	private final String txtCacherSignature="Cacher le 'MAGIC NUMBER' du MBR";
	private final String txtRevelerSignature="Révéler le 'MAGIC NUMBER' du MBR";
	private final String txtModifIdentifiant="Modifier/Créer identifiant du disque";
	private final String txtIdDemande="Indiquez l'ID du disque en hexa (Lettre de A à F autorisées et 0 à 9)";
	private final String txtIdDemandeTitre="ID Volume";

	private final String txtErreurFormat="Formalisme de la saisie non respecté. \nOpération annulée.";
	private final String txtErreurFormatTitre="Erreur saisie ID";

	private final String txtTypeTablePartitionTitre="Type partition";
	private final String txtTypeTablePartition="Table de type : ";
	private final String txtApropos="À propos";
	private final String txtLicence="Licence";

	private final String titreFenetre="PartMS";
	private String titreModifiable=titreFenetre;
	private String cheminFichier="";

	//Texte boîte de dialogue
	private final String txtTitreFormatBN="Formatage bas niveau";
	private final String txtMessageFormatBN="Êtes vous sûr de vouloir formater ? \nAucunes données ne seront récupérables après cette opération";

	//Taille de la fenêtre
	private final int tailleFenX=618;
	private final int tailleFenY=260;

	private final int tailleChampX=200;
	private final int tailleChampY=24;

	//Gestion de la fenêtre panel inclu
	private JFrame fen;

	private JPanel pan2=new JPanel();
	private JSplitPane splitPane;
	private JSplitPane splitPane2;

	//Variable global du menu.
	private JMenuBar menuPrincipal;
	private JMenu menuVolume;
	private JMenu menuTablePartition;
	private JMenu menuMagicNumber;
	private JMenu menuAPropos;

	private JMenuItem menuStatVolume;
	private JMenuItem menuCloseVolume;
	private JMenuItem menuRafraichir;

	private JMenuItem menuFormatageBN;
	private JMenuItem menuCacherSignature;
	private JMenuItem menuRevelerSignature;

	private JMenuItem menuSupprTable;
	private JMenuItem menuCreationTable;
	private JMenuItem menuModifIdentifiant;

	private JMenuItem menuInfoTable;
	private JMenuItem menuLicence;

	//Gestion menu fichier.
	private static JFileChooser filou=new JFileChooser();

	//Creation des objets pour la gestion de fichier et des panels.
	private operationFichier opFi=new operationFichier();
	private volume volGest;
	private panneauStat pS=new panneauStat();
	private panneauInfo pI=new panneauInfo();
	private JOptionPane jop=new JOptionPane();
	private remplZeroAutoThread threadFormatageBN;
	private effaceTableThread threadEffacerTable;

	private boolean affichageConsole=false;

	private void initSplit()
	{

		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true, pS, pI);
		this.splitPane.setEnabled(false);

	}


	public fenPartMS(String titre)
	{
		this.titreModifiable=titre;
		this.fen=new JFrame();
		this.initSplit();
		this.pan2.setBackground(Color.RED);
		this.fen.setTitle(this.titreModifiable);
		this.volGest=new volume(this.titreModifiable);
		this.fen.setSize(tailleFenX,tailleFenY);
		this.fen.setLocationRelativeTo(null);
		this.fen.setJMenuBar(initMenu());

		//Déclaration des listener pour le menu.
		this.menuStatVolume.addActionListener(this);
		this.menuCloseVolume.addActionListener(this);
		this.menuRafraichir.addActionListener(this);
		this.menuInfoTable.addActionListener(this);

		this.menuCreationTable.addActionListener(this);
		this.menuSupprTable.addActionListener(this);
		this.menuFormatageBN.addActionListener(this);
		this.menuCacherSignature.addActionListener(this);
		this.menuRevelerSignature.addActionListener(this);
		this.menuModifIdentifiant.addActionListener(this);

		this.fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//splitPane.setResizeWeight(0.5);
		this.splitPane.setDividerLocation(300);
		this.fen.add(splitPane);
		this.fen.setVisible(true);
		this.fen.setResizable(false);
	}

	private JMenuBar initMenu()
	{
		//Gestion du menu
		this.menuPrincipal=new JMenuBar();

		//Sous menu volume
		this.menuVolume=new JMenu(this.txtVolume);
		this.menuAPropos=new JMenu(this.txtApropos);
		this.menuLicence=new JMenuItem(this.txtLicence);
		this.menuStatVolume=new JMenuItem(this.txtStatVolume);
		this.menuCloseVolume=new JMenuItem(this.txtCloseVolume);
		this.menuRafraichir=new JMenuItem(this.txtRafraichir);

		//Sous menu partition.
		this.menuTablePartition=new JMenu(this.txtPartition);
		this.menuSupprTable=new JMenuItem(this.txtSupprTable);
		this.menuCreationTable=new JMenuItem(this.txtCreateTable);
		this.menuFormatageBN=new JMenuItem(this.txtFormatageBasNiveau);
		this.menuCacherSignature=new JMenuItem(this.txtCacherSignature);
		this.menuRevelerSignature=new JMenuItem(this.txtRevelerSignature);
		this.menuModifIdentifiant=new JMenuItem(this.txtModifIdentifiant);
		this.menuInfoTable=new JMenuItem(this.txtTypeTablePartitionTitre);
		this.menuMagicNumber=new JMenu(this.txtMagicNumber);

		//Construction du menu volume.
		this.menuVolume.add(this.menuStatVolume);
		this.menuVolume.add(this.menuCloseVolume);
		this.menuVolume.add(this.menuRafraichir);
		this.menuVolume.add(this.menuTablePartition);
		this.menuVolume.add(this.menuFormatageBN);

		//Les ajouts de menu et sous menu.
		this.menuPrincipal.add(this.menuVolume);
		this.menuPrincipal.add(this.menuAPropos);
		this.menuVolume.add(this.menuTablePartition);
		this.menuMagicNumber.add(this.menuCacherSignature);
		this.menuMagicNumber.add(this.menuRevelerSignature);

		//Sous menu table de partition.
		this.menuTablePartition.add(this.menuCreationTable);
		this.menuTablePartition.add(this.menuSupprTable);
		this.menuTablePartition.add(this.menuMagicNumber);
		this.menuTablePartition.add(this.menuModifIdentifiant);
		this.menuTablePartition.add(this.menuInfoTable);

		this.menuAPropos.add(this.txtLicence);

		menuActivation(false);

		return this.menuPrincipal;
	}

	public void actionPerformed(ActionEvent arg0)
	{

		//Si on clique sur le menu stat volume.
		if(arg0.getSource()==this.menuStatVolume)
		{
			this.cheminFichier=choixFichier();
			this.titreModifiable=this.titreFenetre+" "+this.cheminFichier;
			this.fen.setTitle(this.titreModifiable);
			this.pI.setCheminFichier(this.cheminFichier);

			if(this.cheminFichier!="-1")
			{
				menuActivation(true);

				//Envoi du nom de fichier au panneau d'information
				this.pI.setCheminFichier(cheminFichier);
				majou(cheminFichier);

			}
			else
			{
				menuActivation(false);
			}

		}

		if(arg0.getSource()==this.menuCloseVolume)
		{
			this.fen.setTitle(titreFenetre);

			//Blocage du menu dans le pannel panneauInfo
			this.pS.effaceGraph();
			this.pI.closeVolume();
			menuActivation(false);
		}

		if(arg0.getSource()==this.menuRafraichir)
		{
			this.majou(cheminFichier);
		}

		if(arg0.getSource()==this.menuCreationTable)
		{
			if(cheminFichier!="-1")
			{
				dialogueId(cheminFichier);
				this.volGest.writeSignature(cheminFichier,true);
				this.volGest.writeSignatureEtendue(cheminFichier,true);

			}

		}

		if(arg0.getSource()==this.menuSupprTable)
		{
			if(cheminFichier!="-1")
			{
				this.threadEffacerTable=new effaceTableThread(cheminFichier);
				this.threadEffacerTable.start();
			}

		}

		if(arg0.getSource()==this.menuFormatageBN)
		{
			int choix=-1;
			choix=jop.showConfirmDialog(null,txtMessageFormatBN,txtTitreFormatBN,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);

			if(choix==0)
			{
				fen.setTitle(titreFenetre);
				menuActivation(false);
				pI.superVerrouComposant(false);
				threadFormatageBN=new remplZeroAutoThread(cheminFichier);
				cheminFichier="-1";
				threadFormatageBN.start();

			}

		}

		//Gestion de la signature
		if(arg0.getSource()==this.menuCacherSignature)
		{
			this.volGest.writeSignature(cheminFichier,false);
			this.volGest.writeSignatureEtendue(cheminFichier,false);
			this.volGest.journal("Dissimulation du MAGIC NUMBER.",this.affichageConsole);
		}

		if(arg0.getSource()==this.menuRevelerSignature)
		{
			this.volGest.writeSignature(cheminFichier, true);
			this.volGest.writeSignatureEtendue(cheminFichier, true);
			this.volGest.journal("Restauration du MAGIC NUMBER.",this.affichageConsole);
		}

		if(arg0.getSource()==this.menuModifIdentifiant)
		{

			dialogueId(cheminFichier);

		}

		if(arg0.getSource()==this.menuInfoTable)
		{
			dialogueInfotable(cheminFichier);
		}

	}

	//Méthode qui ouvre une fenêtre pour choisir un fichier
	private String choixFichier()
	{
		String retourFichier="No FILE";

		try
		{
			File f=new File(new File("/dev").getCanonicalPath());
			filou.setCurrentDirectory(f);
		}

		catch(IOException erFile){}

		finally
		{
			try
			{
				filou.showOpenDialog(this);
				File fichierOuvrir=filou.getSelectedFile();
				retourFichier=fichierOuvrir.getAbsolutePath();
			}

			//Bouton annuler.
			catch (NullPointerException erFile)
			{
				retourFichier="";
			}
		}

		return retourFichier;
	}

	//Mise à jour du graph
	private void majou(String pathBloc)
	{
		//Récupération des attributs
		long mbr[]=new long[5];

		mbr=this.volGest.lecturePartitionMBR(pathBloc,1);
		pS.majGraphique(mbr[0],mbr[1],mbr[4],0);

		mbr=this.volGest.lecturePartitionMBR(pathBloc,2);
		pS.majGraphique(mbr[0],mbr[1],mbr[4],1);

		mbr=this.volGest.lecturePartitionMBR(pathBloc,3);
		pS.majGraphique(mbr[0],mbr[1],mbr[4],2);

		mbr=this.volGest.lecturePartitionMBR(pathBloc,4);
		pS.majGraphique(mbr[0],mbr[1],mbr[4],3);

	}

	//Verouilleur du menu (pas de fichier sélectionné par exemple)
	private void menuActivation(boolean activation)
	{
		this.menuRafraichir.setEnabled(activation);
		this.menuTablePartition.setEnabled(activation);
		this.menuCloseVolume.setEnabled(activation);
		this.menuFormatageBN.setEnabled(activation);
		this.menuCacherSignature.setEnabled(activation);
	}

	private void superMenuBlocage(boolean activation)
	{
		this.menuVolume.setEnabled(activation);
	}

	private void dialogueId(String fichier)
	{
			String idReleve="";
			String pattern="[0-9,A-F,a-f]{1,8}";

			try
			{
				idReleve=jop.showInputDialog(fen, txtIdDemande, txtIdDemandeTitre, JOptionPane.WARNING_MESSAGE);

				if(idReleve.matches(pattern) && idReleve!=null)
				{
					this.volGest.writeId(fichier,idReleve);
				}

				else if(!idReleve.matches(pattern) && idReleve!=null)
				{
					jop.showMessageDialog(null,txtErreurFormat,txtErreurFormatTitre,JOptionPane.ERROR_MESSAGE);
				}
			}

			catch(NullPointerException erSaisie){}
			catch(NumberFormatException erSaisir){}
	}

	private void dialogueInfotable(String fichier)
	{
		int typeTable=this.volGest.infoTable(fichier);

		String table[]={"Inconnu","dos","GPT"};
		String texteBoite=txtTypeTablePartition+" "+table[typeTable];

		jop.showMessageDialog(null,texteBoite,txtTypeTablePartitionTitre,JOptionPane.INFORMATION_MESSAGE);

	}






}
