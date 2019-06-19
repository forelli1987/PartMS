/*
***** effaceTableParitionThread.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Il s'agit d'un thread java, pour effacer la table de partition.

*/

//*******************************
//Gestion boîte de dialogue
import javax.swing.JOptionPane;

public class effaceTableThread extends Thread
{
	private volume volGest=new volume();
	private JOptionPane boxDial=new JOptionPane();

	private final String txtFinFormatage="Fin de suppression de la table";
    private final String txtFinMessBoite="Effectué en : ";


    private long tpsDebut=0;
    private long tpsFin=0;
    private long tpsCalcule=0;

	public effaceTableThread(String chemin)
	{
		super(chemin);
	}

	public void run()
	{

		tpsDebut=System.currentTimeMillis();
		this.volGest.supprTablePart(this.getName());
		tpsFin=System.currentTimeMillis();

		tpsCalcule=tpsFin-tpsDebut;
		boxDial.showMessageDialog(null,txtFinMessBoite+String.valueOf(tpsCalcule/1000)+" s",txtFinFormatage,JOptionPane.INFORMATION_MESSAGE);
	}
}
