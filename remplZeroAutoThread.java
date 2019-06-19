/*
***** operationFichier.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Thread pour le remplissage à 0x0 de tous les octets d'un fichier passé en paramètre.

*/

//*******************************
//Gestion boîte de dialogue
import javax.swing.JOptionPane;

public class remplZeroAutoThread extends Thread
{
	private operationFichier opFi=new operationFichier();
	private JOptionPane boxDial=new JOptionPane();

	private final String txtFinFormatage="Fin de formatage";
    private final String txtFinMessBoite="Formaté en : ";


    private long tpsDebut=0;
    private long tpsFin=0;
    private long tpsCalcule=0;

	public remplZeroAutoThread(String chemin)
	{
		super(chemin);
	}

	public void run()
	{

		tpsDebut=System.currentTimeMillis();
		opFi.remplirOctet(this.getName());
		tpsFin=System.currentTimeMillis();

		tpsCalcule=tpsFin-tpsDebut;
		boxDial.showMessageDialog(null,txtFinMessBoite+String.valueOf(tpsCalcule/1000)+" s",txtFinFormatage,JOptionPane.INFORMATION_MESSAGE);
	}
}
