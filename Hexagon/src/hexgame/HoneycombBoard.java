package hexgame;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
/**
 * 
 * @author Yong on 05/22/2013, for coding test.
 * 
 * A 5*4 honeycomb board which contain a alphabet in each cell and the game is done after each cell is clicked.
 * 
 */
public class HoneycombBoard extends Applet implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;

    private static int BOARD_WIDTH = 5;
    private static int BOARD_HEIGHT = 4;

    //Initially cells are on, would be OFF if clicked or designated Char is hit on keyboard
    private static final int L_ON = 1;
    private static final int L_OFF = 2;

    private static final int NUM_HEX_CORNERS = 6;
    private static final int CELL_RADIUS = 40;

    class UserInput {
    	public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public char getAlphabet() {
			return alphabet;
		}
		public void setAlphabet(char alphabet) {
			this.alphabet = alphabet;
		}
		int status;
    	char alphabet;
    }
    //  game board cells array
    private UserInput[][] mCells;

    private int[] mCornersX = new int[NUM_HEX_CORNERS];
    private int[] mCornersY = new int[NUM_HEX_CORNERS];

    private static HexGridCell mCellMetrics = new HexGridCell(CELL_RADIUS);
    
    private static Font font = new Font("Serif", Font.PLAIN, 20);
    private List<Character> allAlphabets = new ArrayList<Character> ( 
    		Arrays.asList('A','B','C','D','E','F','G',
    		'H','I','J','K','L','M','N',
    		'O','P','Q','R','S','T',
    		'U','V','W','X','Y','Z') );
    private List<Character> candidates = new ArrayList<Character>();
    private List<Character> userInput = new LinkedList<Character>();
    
    @Override
    public void init() {
    	this.setSize(400,400);
    	loadProperties();
    	resetGame();
    	
    	setFocusable(true);
        //requestFocus();
        requestFocusInWindow();
        addMouseListener(this);
        addKeyListener(this);
    }

    /**
     * Randomly pick BOARD_HEIGHT*BOARD_WIDTH alphabets out of the 26.
     * 
     * @param total 
     */
    private void pickCandidates(int total) {
    	Collections.shuffle(allAlphabets);
    	candidates.addAll(allAlphabets.subList(0, total));
    }
    
    @Override
    public void paint(Graphics g) {
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            for (int i = 0; i < BOARD_WIDTH; i++) {
                mCellMetrics.setCellIndex(i, j);
                if (mCells[j][i].getStatus() != 0) {
                    mCellMetrics.computeCorners(mCornersX, mCornersY);

                    g.setColor((mCells[j][i].getStatus() == L_ON) ? Color.ORANGE : Color.GRAY);
                    g.fillPolygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
                    g.setColor(Color.BLACK);
                    g.drawPolygon(mCornersX, mCornersY, NUM_HEX_CORNERS);
                    
                    g.setFont(font);
                    g.drawString(Character.toString(mCells[j][i].getAlphabet()), mCellMetrics.getCenterX(), mCellMetrics.getCenterY());
                }
            }
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Returns true if cell is inside the game board.
     * 
     * @param i cell's horizontal index
     * @param j cell's vertical index
     */
    private boolean isInsideBoard(int i, int j) {
        return i >= 0 && i < BOARD_WIDTH && j >= 0 && j < BOARD_HEIGHT
                && mCells[j][i].getStatus() != 0;
    }

    /**
     * Toggles the cell OFF.
     */
    private void toggleCell(int i, int j) {
    	mCells[j][i].setStatus(L_OFF);
    	userInput.add(mCells[j][i].getAlphabet());
    }

    /**
     * Returns true if all cells have been set OFF.
     */
    private boolean isWinCondition() {
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            for (int i = 0; i < BOARD_WIDTH; i++) {
                if (mCells[j][i].getStatus() == L_ON) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Resets the game to the initial position.
     */
    private void resetGame() {
    	pickCandidates(BOARD_WIDTH * BOARD_HEIGHT);
    	if (mCells == null) {
    		mCells = new UserInput[BOARD_HEIGHT][BOARD_WIDTH];
    	}
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            for (int i = 0; i < BOARD_WIDTH; i++) {
	            if (mCells[j][i] == null) {
	            	mCells[j][i] = new UserInput();
	            }
                mCells[j][i].setStatus(L_ON);
                mCells[j][i].setAlphabet(candidates.remove(0));
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        mCellMetrics.setCellByPoint(arg0.getX(), arg0.getY());
        int clickI = mCellMetrics.getIndexI();
        int clickJ = mCellMetrics.getIndexJ();

        if (isInsideBoard(clickI, clickJ)) {
            // toggle the clicked cell
            toggleCell(clickI, clickJ);
            arg0.consume();

        }
        repaintAfterHit();
    }

	private void repaintAfterHit() {
		repaint();

        if (isWinCondition()) {
            JOptionPane.showMessageDialog(new JFrame(), "Well done!\nInput in order of " + userInput.toString());
            resetGame();
            repaint();
        }
	}
    
    @Override
    public void keyTyped( KeyEvent e ) {
       char c = e.getKeyChar();
       if ( c != KeyEvent.CHAR_UNDEFINED ) {

          for (int j = 0; j < BOARD_HEIGHT; j++) {
              for (int i = 0; i < BOARD_WIDTH; i++) {
            	  if (mCells[j][i].getAlphabet() == Character.toUpperCase(c)) {
            		  toggleCell(i, j);
            		  e.consume();
            	  }
              }
          }

          repaintAfterHit();
        }
    }
 
    private void loadProperties() {
        Properties p = new Properties();
        try {
          p.load((new URL(getCodeBase(), "user.props")).openStream());
          int width = Integer.valueOf(p.getProperty("width"));
          int height = Integer.valueOf(p.getProperty("height"));
          //accept valid setting only, otherwise stick with 5*4
          if (width * height <= allAlphabets.size()) {
        	  BOARD_WIDTH = width;
        	  BOARD_HEIGHT = height;
          }
        	  
        } catch (MalformedURLException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
        }
    	
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }
    
    @Override
    public void keyPressed( KeyEvent e ) { 
    }
    
    @Override
    public void keyReleased( KeyEvent e ) { 	
    }
    
}
