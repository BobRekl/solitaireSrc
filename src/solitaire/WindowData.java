/*
 * Solitaire Game - Display parameters.
 */
package solitaire;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * WindowData - Display parameters used in laying out game objects.
 * @author Bob Reklis
 */
public class WindowData {
    public final int X_WINDOW_SIZE; //Size of the main JFrame
    public final int Y_WINDOW_SIZE;
    public final int X_WINDOW_LOC; //Initial location of the main JFrame
    public final int Y_WINDOW_LOC; 
    public final int X_BOARD_SIZE; //Size of the GamePanel within the JFrame/SolitairePanel
    public final int Y_BOARD_SIZE;
    public final int X_CARD; //Nominal number of pixels in a card image
    public final int Y_CARD;
    public final int AUTO_PLAY_TIMER_TIC;
    public final Border BORDER;
    
    public final int OFFSET_INC; //amount visible cards are offset in a stack
    public final int GAP; //gap between stacks in game panel
    public final int CONTAINER_GAP; //vertical displacement of stacks in game pannel

    public final int STATISTICS_H_OFFSET; //distanct to text in statistics display
    public final int X_STATISTICS_POS; //statistics display position
    public final int Y_STATISTICS_POS;
    public final int STATISTICS_HEIGHT; //statistics display size
    public final int STATISTICS_WIDTH;
    public final int STATISTICS_INC; //increment between lines in statistics display
    
    public final int VICTORY_WIDTH;  //victory display size
    public final int VICTORY_HEIGHT;  //victory display size
            
    WindowData(){
        X_BOARD_SIZE = 930;
        Y_BOARD_SIZE = 620;
        X_WINDOW_SIZE = X_BOARD_SIZE + 20;
        Y_WINDOW_SIZE = Y_BOARD_SIZE + 50;
        X_WINDOW_LOC = 200; 
        Y_WINDOW_LOC = 30; 
        X_CARD = 85; //Size of most card images / 4 
        Y_CARD = 132;
        AUTO_PLAY_TIMER_TIC = 300;
        BORDER = BorderFactory.createLineBorder(Color.BLUE, 2); //standard border style
        
        OFFSET_INC = 20;
        GAP = 5;
        CONTAINER_GAP = 10;

        STATISTICS_H_OFFSET = 10;
        X_STATISTICS_POS = 20;
        Y_STATISTICS_POS = 20;
        STATISTICS_HEIGHT = 130;
        STATISTICS_WIDTH = 200;
        STATISTICS_INC = 20;
        
        VICTORY_WIDTH = 200;
        VICTORY_HEIGHT = 100;
    }
}
