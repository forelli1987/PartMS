/*
***** main.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Entrée du programme, c'est par ici que tout commence.

*/

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

public class main
{

	public static void main(String args[])
	{
		//***** VARIABLES *****
		String messageRoot="Il faut être root pour lancer le programme.";
		String messageOs="Le programme fonctionne sur GNU/Linux uniquement.";
		String titre="PartMS";

		//Test si l'OS tourne avec un noyau Linux ET s'il s'agit du super utilisateur.
		if(System.getProperty("os.name").equals("Linux"))
		{
			if(System.getProperty("user.name").equals("root"))
			{
				//Lancement de la fenêtre principale.
				new fenPartMS(titre);
			}

			else
			{
				JOptionPane.showMessageDialog(null, messageRoot,titre,JOptionPane.ERROR_MESSAGE);
			}
		}

		else
		{
			JOptionPane.showMessageDialog(null, messageOs,titre,JOptionPane.ERROR_MESSAGE);
		}
		
	}
}
