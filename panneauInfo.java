/*
***** panneauInfo.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Sous JPane, qui contient le menu déroulant pour le numéro de la partition,
le début en MO, la taille de la partition, un champs pour l'ID et une coche pour indiqué si la partition est bootable ou non.

*/


//Les widget
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

//Gestion des évènements
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

//Couleur
import java.awt.Color;

//Les layout
import java.awt.FlowLayout;

//Gestion du dimensionnement
import java.awt.Dimension;


public class panneauInfo extends JPanel implements ActionListener,ItemListener
{
	//private String essaiHtml="<html><h1>Partitions</h1><font color=FF0000>1 : <br></font><font color=0000CC>2 : <br></font><font color=00BB00>3 : <br></font><font color=000000>4 : <br></font></html>";

	//Nom du fichier récupéré grâce à l'ouverture du fichier.
	private String cheminFichierTraitement="";

	//Données de taille des widget
	private final int tailleX=110;
	private final int tailleY=24;

	//Chaînes des composants
	private final String txtCombo="Partition";
	private final String txtTypePartition="Type de partition";
	private final String txtDebutPartition="Debut (mo)";
	private final String txtTaillePartition="Taille (mo)";
	private String [] partitionListe;
	private final String txtBtnEcrireTable="Écrire la table de partition";
	private final String txtCkEcrire="Modifier table";
	private final String txtCkBoot="Bootable ?";
	private final String titre="PartMS";
	private final boolean console=false;

	//Composants
	private JComboBox comboPartition;
	private JButton btnEcrireTable=new JButton(txtBtnEcrireTable);
	private JCheckBox ckEcrire=new JCheckBox(txtCkEcrire);
	private JCheckBox ckBoot=new JCheckBox(txtCkBoot);

	//Information des champs.
	private JLabel labCombo=new JLabel(txtCombo);
	private JLabel labType=new JLabel(txtTypePartition);
	private JLabel labDebut=new JLabel(txtDebutPartition);
	private JLabel labTaille=new JLabel(txtTaillePartition);

	//Champs
	public JTextField fieType=new JTextField();
	public JTextField fieDebutPartition=new JTextField();
	public JTextField fieTaillePartition=new JTextField();

	private FlowLayout fLayout=new FlowLayout(FlowLayout.RIGHT,15,10);
	private operationFichier opeFILE=new operationFichier();
	private volume volGest=new volume(this.titre);
	private panneauStat pS1=new panneauStat();

	//Tableau receuillant les informations du MBR.
	private long[] infoMBR=new long[6];

	public panneauInfo()
	{

		//Initialisation de la comboBox
		partitionListe=new String[8];
		for(int i=0;i<4;i++)
		{
			partitionListe[i]="Partition #"+(i+1);
		}

		for(int i=4;i<8;i++)
		{
			partitionListe[i]="Partition [ETENDUE] #"+(i+1);
		}

		this.comboPartition=new JComboBox(partitionListe);

		//Taille des champs
		this.fieType.setPreferredSize(new Dimension(tailleX,tailleY));
		this.fieDebutPartition.setPreferredSize(new Dimension(tailleX,tailleY));
		this.fieTaillePartition.setPreferredSize(new Dimension(tailleX,tailleY));

		this.setLayout(fLayout);

		this.add(labCombo);
		this.add(comboPartition);

		this.add(labType);
		this.add(fieType);

		this.add(labDebut);
		this.add(fieDebutPartition);

		this.add(labTaille);
		this.add(fieTaillePartition);

		this.add(ckBoot);

		this.add(ckEcrire);
		this.add(btnEcrireTable);

		//Surveillance de l'action des composants
		this.ckEcrire.addActionListener(this);
		this.comboPartition.addItemListener(this);
		this.btnEcrireTable.addActionListener(this);


		//Désactivation par défaut.
		this.ckEcrire.setEnabled(false);
		this.ckBoot.setEnabled(false);
		this.fieType.setEnabled(false);
		this.fieDebutPartition.setEnabled(false);
		this.fieTaillePartition.setEnabled(false);
		this.btnEcrireTable.setEnabled(false);
		this.comboPartition.setEnabled(false);

		//Changement de couleur désactivée
		this.fieType.setDisabledTextColor(Color.BLACK);
		this.fieTaillePartition.setDisabledTextColor(Color.BLACK);
		this.fieDebutPartition.setDisabledTextColor(Color.BLACK);

	}

	//Les listener

	public void actionPerformed(ActionEvent arg0)
	{


		if(!this.ckEcrire.isSelected())
		{
			verrouComposantListe(false);
		}

		else if(this.ckEcrire.isSelected())
		{
			verrouComposantListe(true);
		}

		if(arg0.getSource()==btnEcrireTable)
		{
			String contenuJournal="";
			boolean bootableSignal=false;
			long rechercheEtendue[];
			boolean etendue=false;

			long calcMODebutPartition=0;
			calcMODebutPartition=Long.parseLong(this.fieDebutPartition.getText());
			calcMODebutPartition=calcMODebutPartition*(1024L*1024L);

			long calcMOTaillePartition=0;
			calcMOTaillePartition=Long.parseLong(this.fieTaillePartition.getText());

			calcMOTaillePartition=calcMOTaillePartition*(1024L*1024L);

			int typeCalcPartition=Integer.parseInt(this.fieType.getText(),16);

			if(ckBoot.isSelected())
			{
				bootableSignal=true;
			}

			else if(!ckBoot.isSelected())
			{
				bootableSignal=false;
			}


			//Récupération des informations pour l'écriture.
			/*
			La condition suivante permet de savoir s'il s'agit de partition "classiques" ou bien étendues.
			Elle contrôle également la présence d'une partition primaire avec l'identifiant 0x5 (étendue)
			*/

			//Si partition étendue
			if(this.comboPartition.getSelectedIndex()>3)
			{
				//Recherche d'une partition primaire de type étendue
				for(int i=0;i<4;i++)
				{
					rechercheEtendue=this.volGest.lecturePartitionMBR(cheminFichierTraitement, i);
					if(rechercheEtendue[2]==0x5L)
					{
						etendue=true;
					}

				}
				
				if(etendue)
				{
					this.volGest.ecriturePartitionEtendue(cheminFichierTraitement,this.comboPartition.getSelectedIndex()-4,calcMODebutPartition,calcMOTaillePartition,typeCalcPartition,bootableSignal);
					this.volGest.writeSignatureEtendue(cheminFichierTraitement, true);
					contenuJournal="\n-- Ecriture partition ETENDUE ["+(this.comboPartition.getSelectedIndex()+1)+"] --\n";
					contenuJournal=contenuJournal+"Bootable : "+bootableSignal+"\nDebut : "+this.fieDebutPartition.getText()+" mo | ";
					contenuJournal=contenuJournal+"Taille : "+this.fieTaillePartition.getText()+" mo";
					this.volGest.journal(contenuJournal,this.console);
				}

			}

			else
			{
				this.volGest.ecriturePartition(cheminFichierTraitement,this.comboPartition.getSelectedIndex(),calcMODebutPartition,calcMOTaillePartition,typeCalcPartition,bootableSignal);
				this.volGest.writeSignature(cheminFichierTraitement, true);
				contenuJournal="\n-- Ecriture partition ["+(this.comboPartition.getSelectedIndex()+1)+"] --\n";
				contenuJournal=contenuJournal+"Bootable : "+bootableSignal+"\nDebut : "+this.fieDebutPartition.getText()+" mo | ";
				contenuJournal=contenuJournal+"Taille : "+this.fieTaillePartition.getText()+" mo";
				this.volGest.journal(contenuJournal,this.console);
			}



		}

	}

	//Gestion du basculement des tables de partitions.
	public void itemStateChanged(ItemEvent arg0)
	{

		if(comboPartition.getSelectedIndex()<4)
		{
			this.infoMBR=this.volGest.lecturePartitionMBR(cheminFichierTraitement,comboPartition.getSelectedIndex()+1);

			this.fieDebutPartition.setText(Long.toString(infoMBR[0]/(1024*1024))+" mo");
			this.fieTaillePartition.setText(Long.toString(infoMBR[1]/(1024*1024))+" mo");
			this.fieType.setText("0x"+Long.toHexString(infoMBR[2]));
	
			//Coche la case bootable quand
			if(infoMBR[5]==0x80)
			{
				this.ckBoot.setSelected(true);
			}
	
			else if(infoMBR[5]==0x0)
			{
				this.ckBoot.setSelected(false);
			}
		}

		else
		{
			this.infoMBR=this.volGest.lecturePartitionMBREtendue(cheminFichierTraitement,comboPartition.getSelectedIndex()-3);

			this.fieDebutPartition.setText(Long.toString(infoMBR[0]/(1024*1024))+" mo");
			this.fieTaillePartition.setText(Long.toString(infoMBR[1]/(1024*1024))+" mo");
			this.fieType.setText("0x"+Long.toHexString(infoMBR[2]));
	
			//Coche la case bootable quand
			if(infoMBR[5]==0x80)
			{
				this.ckBoot.setSelected(true);
			}
	
			else if(infoMBR[5]==0x0)
			{
				this.ckBoot.setSelected(false);
			}
		}



	}


	//Mutateur pour être utilisé à l'extérieur de la classe.
	public void setCheminFichier(String fichierRecupere)
	{
		if(fichierRecupere!="-1")
		{
			cheminFichierTraitement=fichierRecupere;

			ckEcrire.setEnabled(true);
			ckEcrire.setSelected(false);
			comboPartition.setEnabled(true);
			verrouComposantListe(false);

			//Force la combo à sélectionner le premier index.
			comboPartition.setSelectedIndex(0);

			//Ecriture des données récupérées dans les champs.
			infoMBR=this.volGest.lecturePartitionMBR(cheminFichierTraitement,1);

			fieDebutPartition.setText(Long.toString(infoMBR[0]/(1024*1024))+" mo");
			fieTaillePartition.setText(Long.toString(infoMBR[1]/(1024*1024))+" mo");
			fieType.setText("0x"+Long.toHexString(infoMBR[2]));

			//Coche la case bootable quand
			if(infoMBR[5]==0x80)
			{
				ckBoot.setSelected(true);
			}

			else if(infoMBR[5]==0x0)
			{
				ckBoot.setSelected(false);
			}
		}

		else if(fichierRecupere=="-1")
		{
			//Désactivation par défaut.
			ckEcrire.setEnabled(false);
			verrouComposant(false);
		}

		else if(fichierRecupere=="No FILE")
		{
			//Désactivation par défaut.
			ckEcrire.setEnabled(false);
			verrouComposant(false);
		}

		//Mise à jour du graphe.



	}

	public void closeVolume()
	{
		//On verouille les composants.
		ckEcrire.setEnabled(false);
		verrouComposant(false);

		//Vidage des champs.
		fieDebutPartition.setText("");
		fieType.setText("");
		fieTaillePartition.setText("");

		cheminFichierTraitement="";
	}

	//Verrouille tous les composants.

	public void superVerrouComposant(boolean activation)
	{
		//On verouille les composants.
		ckEcrire.setEnabled(activation);
		fieType.setEnabled(activation);
		fieDebutPartition.setEnabled(activation);
		fieTaillePartition.setEnabled(activation);
		btnEcrireTable.setEnabled(activation);
		comboPartition.setEnabled(activation);
		ckBoot.setEnabled(activation);
	}

	public void verrouComposant(boolean activation)
	{
		//On verouille les composants.
		fieType.setEnabled(activation);
		fieDebutPartition.setEnabled(activation);
		fieTaillePartition.setEnabled(activation);
		btnEcrireTable.setEnabled(activation);
		comboPartition.setEnabled(activation);
		ckBoot.setEnabled(activation);
	}

	//Verouille tous les composants sauf la combo.
	public void verrouComposantListe(boolean activation)
	{
		//On verouille les composants.
		fieType.setEnabled(activation);
		fieDebutPartition.setEnabled(activation);
		fieTaillePartition.setEnabled(activation);
		btnEcrireTable.setEnabled(activation);
		ckBoot.setEnabled(activation);
	}

}
