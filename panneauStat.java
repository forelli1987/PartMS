/*
***** panneauStat.java *****

Auteur : Anthony Fernandez
Licence : GPL3

Date de début : Mars 2018

Description fichier :
Gestion du camembert qui donne les proportions des partitions dans le volume

*/

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JButton;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class panneauStat extends JPanel //implements MouseListener
{
	public Graphics G;

	private final int taille=160;
	private final int posX=150;
	private final int posY=100;

	private final int posXT=150;
	private final int posYT=150;

	private long debutPartition=0L;
	private long tailleTotale=0L;
	private long taillePartitionSd=0L;

	private int[] angles=new int[2];
	private int[] angles1=new int[2];
	private int[] angles2=new int[2];
	private int[] angles3=new int[2];

	private Color[] couleur={Color.BLUE,Color.RED,Color.GREEN,Color.BLACK};

	public panneauStat()
	{
		//addMouseListener(this);
		angles=this.calcAngles(0,0,0);
		angles1=this.calcAngles(0,0,0);
		angles2=this.calcAngles(0,0,0);
		angles3=this.calcAngles(0,0,0);
	}

	public void paint(Graphics G)
	{

		//G.drawString("Essai",posX,posY);

		G.setColor(couleur[3]);
		G.drawOval(posX-(taille/2),posY-(taille/2),taille,taille);

		G.setColor(couleur[0]);
		G.fillArc(posX-(taille/2),posY-(taille/2),taille,taille,angles[0],angles[1]);

		G.setColor(couleur[1]);
		G.fillArc(posX-(taille/2),posY-(taille/2),taille,taille,angles1[0],angles1[1]);

		G.setColor(couleur[2]);
		G.fillArc(posX-(taille/2),posY-(taille/2),taille,taille,angles2[0],angles2[1]);

		G.setColor(couleur[3]);
		G.fillArc(posX-(taille/2),posY-(taille/2),taille,taille,angles3[0],angles3[1]);
	}




	/*public void mouseClicked(MouseEvent event)
	{
		/*debutPartition=1073741824L;
		taillePartitionSd=2147483648L;
		tailleTotale=2415919104L;

		angles=this.calcAngles(debutPartition,taillePartitionSd,tailleTotale);

		this.repaint();

	}

	public void mouseEntered(MouseEvent event) {}

	public void mouseExited(MouseEvent event) { }

	public void mousePressed(MouseEvent event) { }

	public void mouseReleased(MouseEvent event) { }*/

	public int [] calcAngles(long debutPartition, long taillePartition, long tailleTotale)
	{
		//Conversion en double des données d'entrées
		double debut=(double)(debutPartition);
		double taillePortion=(double)(taillePartition);
		double tailleVolume=(double)(tailleTotale);

		//Délaration du tableau qui sera retourné
		int angleCalcules[]=new int[2];

		//Calcule des angles
		angleCalcules[0]=(int)((debut/tailleVolume)*360D);
		angleCalcules[1]=(int)((taillePortion/tailleVolume)*360D);

		System.out.println("Angles : "+angleCalcules[0]+" "+angleCalcules[1]+" "+tailleTotale/(1024*1024));

		return angleCalcules;

	}


	public void majGraphique(long debut,long taillePart,long tailleVolume,int descripteur)
	{

		switch(descripteur)
		{
			case 0:
			{
				angles=this.calcAngles(debut,taillePart,tailleVolume);
				break;
			}

			case 1:
			{
				angles1=this.calcAngles(debut,taillePart,tailleVolume);
				break;
			}

			case 2:
			{
				angles2=this.calcAngles(debut,taillePart,tailleVolume);
				break;
			}

			case 3:
			{
				angles3=this.calcAngles(debut,taillePart,tailleVolume);
				break;
			}
		}


		this.repaint();
	}

	public void effaceGraph()
	{
		//On trace les angles à Zéro.
		angles[0]=0;
		angles[1]=0;

		angles1[0]=0;
		angles1[1]=0;

		angles2[0]=0;
		angles2[1]=0;

		angles3[0]=0;
		angles3[1]=0;

		//On repeind le graph.
		this.repaint();
	}

}
