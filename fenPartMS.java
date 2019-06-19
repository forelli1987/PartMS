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
	private final String txtCacherSignature="Cacher signature MSDOS";
	private final String txtModifIdentifiant="Modifier/Créer identifiant du disque";
	private final String txtIdDemande="Indiquez l'ID du disque en hexa (Lettre de A à F autorisées et 0 à 9)";
	private final String txtIdDemandeTitre="ID Volume";

	private final String txtErreurFormat="Formalisme de la saisie non respecté. \nOpération annulée.";
	private final String txtErreurFormatTitre="Erreur saisie ID";

	private final String txtTypeTablePartitionTitre="Type partition";
	private final String txtTypeTablePartition="Table de type : ";

	private final String titreFenetre="MS Partition";
	private String titreModifiable=titreFenetre;
	private String cheminFichier="";

	//Texte boîte de dialogue
	private final String txtTitreFormatBN="Formatage bas niveau";
	private final String txtMessageFormatBN="Êtes vous sûr de vouloir formater ? \nAucunes données ne seront récupérables après cette opération";

	//Taille de la fenêtre
	private final int tailleFenX=600;
	private final int tailleFenY=270;

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

	private JMenuItem menuStatVolume;
	private JMenuItem menuCloseVolume;
	private JMenuItem menuRafraichir;

	private JMenuItem menuFormatageBN;
	private JMenuItem menuCacherSignature;

	private JMenuItem menuSupprTable;
	private JMenuItem menuCreationTable;
	private JMenuItem menuModifIdentifiant;

	private JMenuItem menuInfoTable;

	//Gestion menu fichier.
	private static JFileChooser filou=new JFileChooser();

	//Creation des objets pour la gestion de fichier et des panels.
	private operationFichier opFi=new operationFichier();
	private volume volGest=new volume();
	private panneauStat pS=new panneauStat();
	private panneauInfo pI=new panneauInfo();
	private JOptionPane jop=new JOptionPane();
	private remplZeroAutoThread threadFormatageBN;
	private effaceTableThread threadEffacerTable;

	private void initSplit()
	{

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true, pS, pI);
		splitPane.setEnabled(false);

	}


	public fenPartMS()
	{

		fen=new JFrame();
		this.initSplit();
		pan2.setBackground(Color.RED);
		fen.setTitle(titreModifiable);
		fen.setSize(tailleFenX,tailleFenY);
		fen.setLocationRelativeTo(null);
		fen.setJMenuBar(initMenu());

		//Déclaration des listener pour le menu.
		menuStatVolume.addActionListener(this);
		menuCloseVolume.addActionListener(this);
		menuRafraichir.addActionListener(this);
		menuInfoTable.addActionListener(this);

		menuCreationTable.addActionListener(this);
		menuSupprTable.addActionListener(this);
		menuFormatageBN.addActionListener(this);
		menuCacherSignature.addActionListener(this);
		menuModifIdentifiant.addActionListener(this);

		fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(300);
		fen.add(splitPane);
		fen.setVisible(true);
		fen.setResizable(false);
	}

	private JMenuBar initMenu()
	{
		//Gestion du menu
		menuPrincipal=new JMenuBar();
		menuVolume=new JMenu(txtVolume);
		menuStatVolume=new JMenuItem(txtStatVolume);
		menuCloseVolume=new JMenuItem(txtCloseVolume);
		menuRafraichir=new JMenuItem(txtRafraichir);
		menuTablePartition=new JMenu(txtPartition);
		menuSupprTable=new JMenuItem(txtSupprTable);
		menuCreationTable=new JMenuItem(txtCreateTable);
		menuFormatageBN=new JMenuItem(txtFormatageBasNiveau);
		menuCacherSignature=new JMenuItem(txtCacherSignature);
		menuModifIdentifiant=new JMenuItem(txtModifIdentifiant);
		menuInfoTable=new JMenuItem(txtTypeTablePartitionTitre);

		menuVolume.add(menuStatVolume);
		menuVolume.add(menuCloseVolume);
		menuVolume.add(menuRafraichir);
		menuVolume.add(menuTablePartition);
		menuVolume.add(menuFormatageBN);

		//Les ajouts de menu et sous menu.
		menuPrincipal.add(menuVolume);
		menuVolume.add(menuTablePartition);

		//Sous menu table de partition.
		menuTablePartition.add(menuCreationTable);
		menuTablePartition.add(menuSupprTable);
		menuTablePartition.add(menuCacherSignature);
		menuTablePartition.add(menuModifIdentifiant);
		menuTablePartition.add(menuInfoTable);

		menuActivation(false);

		return menuPrincipal;
	}

	public void actionPerformed(ActionEvent arg0)
	{

		//Si on clique sur le menu stat volume.
		if(arg0.getSource()==menuStatVolume)
		{
			cheminFichier=choixFichier();
			titreModifiable=titreFenetre+" "+cheminFichier;
			fen.setTitle(titreModifiable);
			System.out.println(cheminFichier);
			pI.setCheminFichier(cheminFichier);

			if(cheminFichier!="-1")
			{
				menuActivation(true);

				//Envoi du nom de fichier au panneau d'information
				System.out.println("majGraphique");
				pI.setCheminFichier(cheminFichier);
				majou(cheminFichier);

			}
			else
			{
				menuActivation(false);
				System.out.println("Pas de données");
			}

		}

		if(arg0.getSource()==menuCloseVolume)
		{
			fen.setTitle(titreFenetre);

			//Blocage du menu dans le pannel panneauInfo
			pS.effaceGraph();
			pI.closeVolume();
			menuActivation(false);
		}

		if(arg0.getSource()==menuRafraichir)
		{
			System.out.println("Rafraichir : "+cheminFichier);
			this.majou(cheminFichier);
		}

		if(arg0.getSource()==menuCreationTable)
		{
			if(cheminFichier!="-1")
			{
				System.out.println("--- CREATION TABLE ---");
				dialogueId(cheminFichier);
				this.volGest.writeSignature(cheminFichier,true);

			}

		}

		if(arg0.getSource()==menuSupprTable)
		{
			if(cheminFichier!="-1")
			{
				System.out.println("--- SUPPRESION TABLE ---");
				threadEffacerTable=new effaceTableThread(cheminFichier);
				threadEffacerTable.start();
			}

		}

		if(arg0.getSource()==menuFormatageBN)
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

		if(arg0.getSource()==menuCacherSignature)
		{
			this.volGest.writeSignature(cheminFichier,false);
		}

		if(arg0.getSource()==menuModifIdentifiant)
		{

			dialogueId(cheminFichier);

		}

		if(arg0.getSource()==menuInfoTable)
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

			catch (NullPointerException erFile)
			{
				System.out.println("Annulation");
				retourFichier="-1";
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
		menuRafraichir.setEnabled(activation);
		menuTablePartition.setEnabled(activation);
		menuCloseVolume.setEnabled(activation);
		menuFormatageBN.setEnabled(activation);
		menuCacherSignature.setEnabled(activation);
	}

	private void superMenuBlocage(boolean activation)
	{
		menuVolume.setEnabled(activation);
	}

	private void dialogueId(String fichier)
	{
			String idReleve="";
			String pattern="[0-9,A-F,a-f]{1,7}";

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

			catch(NullPointerException erSaisie){System.out.println("ANNULE SAISIE");}
			catch(NumberFormatException erSaisir){System.out.println("saisie non retenue");}
	}

	private void dialogueInfotable(String fichier)
	{
		int typeTable=this.volGest.infoTable(fichier);

		String table[]={"Inconnu","dos","GPT"};
		String texteBoite=txtTypeTablePartition+" "+table[typeTable];

		jop.showMessageDialog(null,texteBoite,txtTypeTablePartitionTitre,JOptionPane.INFORMATION_MESSAGE);

	}






}
