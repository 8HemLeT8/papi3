package MyFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import GIS.Box;
import GIS.Fruit;
import GIS.Ghost;
import GIS.Packman;
import GIS.Types;
import Geom.MyCoords;
import Geom.Point3D;
import Robot.Play;

public class myPanel extends JPanel implements MouseListener {
	BufferedImage backgroundImage, packmanImage, fruitImage, ghostImage, barrierImage, playerImage, playerImageCpy;
	JMenuItem menuItem;
	JMenuBar menuBar;
	ArrayList<Types> types;
	Color colorsArr[];
	Map map = new Map();
	Game game =new Game();
	Iterator<Packman> itPac = game.packmans.iterator();
	Iterator<Fruit> itFru = game.fruits.iterator();
	Iterator<Ghost> itGhost = game.ghosts.iterator();
	Iterator<Box> itBox = game.boxes.iterator();
	Dimension dimensionSize = new Dimension();
	MyCoords mc = new MyCoords();
	Play play;
	Point3D directionPoint = new Point3D(0, 0, 0);
	boolean addPlayer, playerExist ,fileLoaded;
	int x = -1, y = -1;
	double rotationRequired = 90;
	private BufferedImage myImage = null;

	public myPanel() {
		this.addMouseListener(this);

		try {
			myImage = ImageIO.read(new File("Ariel1.png"));
			packmanImage = ImageIO.read(new File("packman1.png"));
			fruitImage = ImageIO.read(new File("fruit1.png"));
			ghostImage = ImageIO.read(new File("Ghost.jpg"));
			playerImage = ImageIO.read(new File("Player_Packman.png"));
			playerImageCpy = playerImage;

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(myImage, 0, 0, this.getWidth(), this.getHeight(), this);
		if (x != -1 && y != -1) {
			int r = 20;
			x = x - (r / 2 + 10);
			y = y - (r / 2 + 10);


			// print boxes
			synchronized (game) {

				itBox = game.boxes.iterator();
				while (itBox.hasNext()) {
					Box box = itBox.next();
					int deltaX = (int) (box.getMaxInPixels().x() - box.getMinInPixels().x());
					int deltaY = (int) (box.getMinInPixels().y() - box.getMaxInPixels().y());

					g.fillRect((int) (box.getMinInPixels().x() * GUI.ratioWidth),
							(int) (box.getMaxInPixels().y() * GUI.ratioHeight), (int) (deltaX * GUI.ratioWidth),
							(int) (deltaY * GUI.ratioHeight));
				}
				if (addPlayer && !playerExist) { // if the addPlayer has been pressed&&the player isnt in the map yet
					Point3D point = map.pixels2polar(x, y);
					game.player.setPoint(point);
					g.drawImage(playerImage, (int) (game.player.getLocationInPixels().x()),
							(int) game.player.getLocationInPixels().y(), (int) (2 * r * GUI.ratioWidth),
							(int) (2 * r * GUI.ratioHeight), this);
					playerExist = true;
				}

				// if the player already exists we want to draw him again
				if (playerExist) {
					//System.out.println(rotationRequired); // calculate angle in degrees
					AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(rotationRequired),
							playerImage.getWidth()*GUI.ratioWidth/ 2, playerImage.getHeight()*GUI.ratioHeight / 2); // set up image rotation configuration
					AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
					// Drawing the rotated image at the required drawing locations
					g.drawImage((op.filter(playerImage, null)), (int) (game.player.getLocationInPixels().x() * GUI.ratioWidth),
							(int) (game.player.getLocationInPixels().y() * GUI.ratioHeight), (int) (2 * r * GUI.ratioWidth),
							(int) (2 * r * GUI.ratioHeight), this);

				}

				// print fruits
				itFru = game.fruits.iterator();
				while (itFru.hasNext()) {
					Fruit fTemp = itFru.next();
					g.drawImage(fruitImage, (int) (fTemp.getLocationInPixels().x() * GUI.ratioWidth),
							(int) (fTemp.getLocationInPixels().y() * GUI.ratioHeight), (int) (2 * r * GUI.ratioWidth),
							(int) (2 * r * GUI.ratioHeight), this);
				}
				// print packmans
				itPac = game.packmans.iterator(); // for the repaint we need to draw every packman again
				while (itPac.hasNext()) {
					Packman pTemp = itPac.next();
					g.drawImage(packmanImage, (int) (pTemp.getLocationInPixels().x() * GUI.ratioWidth),
							(int) (pTemp.getLocationInPixels().y() * GUI.ratioHeight), (int) (2 * r * GUI.ratioWidth),
							(int) (2 * r * GUI.ratioHeight), this);
				}
				// print ghost
				itGhost = game.ghosts.iterator();
				while (itGhost.hasNext()) {
					Ghost pGhost = itGhost.next();
					g.drawImage(ghostImage, (int) (pGhost.getLocationInPixels().x() * GUI.ratioWidth),
							(int) (pGhost.getLocationInPixels().y() * GUI.ratioHeight), (int) (2 * r * GUI.ratioWidth),
							(int) (2 * r * GUI.ratioHeight), this);
				}
			}

		}
	}

	public double orientation(Point3D current, Point3D next) {
		return mc.azimuth_elevation_dist(current, next)[0];
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("(" + e.getX() + "," + e.getY() + ")");
		x = e.getX();
		y = e.getY();
		if (playerExist) {
			directionPoint = new Point3D(e.getX(), e.getY(), 0);
			rotationRequired = 360
					- (orientation(game.player.getPoint(), Map.pixels2polar(directionPoint.ix(), directionPoint.iy())));
			System.out.println(rotationRequired);
		}
		repaint();

	}
	public void clear() {
		x = -1;
		y = -1;
		addPlayer = false;
		playerExist = false;
		fileLoaded=false;
		game.packmans.clear(); // not sure what to fill inside the
		game.fruits.clear();
		game.boxes.clear();
		game.ghosts.clear();
		repaint();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void loadFile(GUI gui) {
		// try read from the file (Copied code from Elizabeth )
		FileDialog fd = new FileDialog(gui, "Open text file", FileDialog.LOAD);
		fd.setFile("*.csv");
		fd.setDirectory("C:");
		fd.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		fd.setVisible(true);
		String folder = fd.getDirectory();
		String fileName = fd.getFile();
		if (fileName != null) {
			System.out.println("The file that opened is: " + folder + fileName);
			play = new Play(folder + fileName);
			game.buildAgame(play.getBoard());
			x = 1;
			y = 1;
			this.repaint();

		}

	}
	
	//pop up message for the game results
	//credit for https://stackoverflow.com/questions/7080205/popup-message-boxes
	public void popUp(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
	}


	public void startPlay(GUI gui) {
		if (playerExist) {
			new myThread(this).start();
		}
	}
	public void startSimu(GUI gui) {
		if (playerExist) {
			new MyThreadSimu(this).start();
		}
	}

}
