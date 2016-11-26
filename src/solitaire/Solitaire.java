/*
 * Solitaire Game.
 */
package solitaire;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 *Solitaire - The Solitaire game JFrame.
 * @author Bob Reklis
 * @version 11.23.2016
 */
public class Solitaire extends JFrame {

    int HighScore; // Data for statistics display
    int NumberOfVictories;
    int NumberOfGamesPlayed;
    String LastPlayedDate;
    
    SolitairePanel solitairePanel; //Main solitaire game JPanel
    public SolitairePlayer player; //Moves the cards for the game
    boolean protect_flag; //Turns off mouse click responses
    boolean showStatistics; //Flag causes statistics display to be painted
    boolean writeStatistics_flag; //Off when save and quit button pushed to preserve game count
    boolean fileExists; //saved game file exists
    
    ArrayList<int[]> solitaireMovesLocal; //A local version of the solitaire moves stack as and ArrayList of int[]
    AutoPlayTimerListener autoPlayTimerListener;
    SolitaireFrameListener solitaireFrameListener;
    javax.swing.Timer autoPlayTimer;
    
    /**
     * Solitaire Constructor - Creates main JPanel. 
     */
    Solitaire(){
        solitairePanel = new SolitairePanel(); //container for game panel and button panel
        
        fileExists = readGameFile(); //read stored game data if it exists
        if(fileExists) fillSolitaireMoves(); //generate move stack from stored data
        
        autoPlayTimerListener = new AutoPlayTimerListener(); //listener for timer used to animate auto play
        autoPlayTimer = new javax.swing.Timer(300, autoPlayTimerListener);
        autoPlayTimer.stop(); //turn off timer until auto play button pushed
        
        solitaireFrameListener = new SolitaireFrameListener(); //listener to save statistics on window close
        this.addWindowListener(solitaireFrameListener);
        
        WindowData windowData = new WindowData(); //gets display parameters
        this.setSize(windowData.X_WINDOW_SIZE, windowData.Y_WINDOW_SIZE); //set up window
        this.setTitle("Solitaire"); //title for main window
        this.setLocation(windowData.X_WINDOW_LOC, windowData.Y_WINDOW_LOC);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(player.Stacks.Deck.getImage(54));
        this.setVisible(true);
        
        this.add(solitairePanel);
    }
    
    /**
     * SolitairePanel - The main JPanel contains the Game Panel and Button Panel.
     */
    class SolitairePanel extends JPanel{
        public GamePanel gamePanel;
        public ButtonPanel buttonPanel;
        
        /**
         * SolitairePanel - Creates player mechanism to move cards then adds 
         * game and button panels to JFrame for display and control.
         */
        SolitairePanel() {
            WindowData windowData = new WindowData(); //package of display parameters
            
            player = new SolitairePlayer(); //moves the cards
            
            writeStatistics_flag = true; //update statistics file at end of game
            
            this.setLayout(new BorderLayout());
            this.setBorder(windowData.BORDER);
            this.setBackground(Color.GREEN);
            
            gamePanel = new GamePanel();//Displays the game
            buttonPanel = new ButtonPanel();//Displays buttons at the bottom
            this.add(gamePanel, BorderLayout.CENTER);
            this.add(buttonPanel, BorderLayout.PAGE_END);
        }
                
    }
    
    /**
     * GamePanel - JPanel with card stacks to play solitaire.
     */
    public class GamePanel extends JPanel{
        JLabel announcement; //Displays announcements at top of game
        JLabel gameScore; //Displays game score
        JLabel victory; //Victory display when game is won
        JLabel highScoreL; //parts of statistics display
        JLabel numberOfVictoriesL;
        JLabel numberOfGamesPlayedL;
        JLabel proportionOfVictoriesL;
        JLabel standardDeviationL;
        JLabel dateL;
        double proportionOfVictories;
        double standardDeviation;
        BufferedImage backImg; //image of back of a card
        ArrayList<CardRectangle> cardRectangle_s; //Stack of card rectangles enables mouse click response
        
        
        /**
         * GamePanel - Constructor sets up main game panel displays with card stacks painted in paintComponent override.
         */
        GamePanel(){
            WindowData windowData = new WindowData(); //Display parameters
            cardRectangle_s = new ArrayList<>();
                        
            GamePanelListener gamePanelListener = new GamePanelListener();
            this.addMouseListener(gamePanelListener);
            CtrlZListener ctrlZListener = new CtrlZListener();
            this.addKeyListener(ctrlZListener);
            this.setBorder(windowData.BORDER);
            this.setBackground(Color.GREEN);
            this.setPreferredSize(new java.awt.Dimension(windowData.X_BOARD_SIZE, windowData.Y_BOARD_SIZE));
            
            HighScore = 0;
            NumberOfVictories = 0;
            NumberOfGamesPlayed = 0;
            LastPlayedDate = "";
            showStatistics = false;
            
            readStatistics(); //read data from statistics file
            //System.out.println("GamePanel Number of Victories = "+NumberOfVictories);
            
            protect_flag = false;
            
            backImg = player.Stacks.Deck.getImage(52); //back of card image
            
            announcement = new JLabel();
            announcement.setForeground(Color.RED);
            announcement.setFont(new java.awt.Font(announcement.getName(), java.awt.Font.PLAIN, 18));
            announcement.setHorizontalAlignment(JLabel.CENTER);
            announcement.setText(player.announcement);
            
            gameScore = new JLabel();
            gameScore.setForeground(Color.BLACK);
            gameScore.setHorizontalAlignment(JLabel.CENTER);
            gameScore.setText("Score = " + Integer.toString(player.getTotalScore()));
            
            victory = new JLabel();
            victory.setForeground(Color.RED);
            victory.setBackground(Color.WHITE);
            victory.setBorder(windowData.BORDER);
            victory.setOpaque(true);
            victory.setFont(new java.awt.Font(victory.getName(), java.awt.Font.PLAIN, 36));
            victory.setHorizontalAlignment(JLabel.CENTER);
            victory.setText("  Victory!  ");
            
            highScoreL = new JLabel();
            highScoreL.setForeground(Color.BLACK);
            highScoreL.setHorizontalAlignment(JLabel.CENTER);
            highScoreL.setText("Score = " + Integer.toString(HighScore));
            
            numberOfVictoriesL = new JLabel();
            numberOfVictoriesL.setForeground(Color.BLACK);
            numberOfVictoriesL.setHorizontalAlignment(JLabel.CENTER);
            numberOfVictoriesL.setText("Number of Victories = "+Integer.toString(NumberOfVictories));

            numberOfGamesPlayedL = new JLabel();
            numberOfGamesPlayedL.setForeground(Color.BLACK);
            numberOfGamesPlayedL.setHorizontalAlignment(JLabel.CENTER);
            numberOfGamesPlayedL.setText("Number of Games Played = "+Integer.toString(NumberOfGamesPlayed));
    
            proportionOfVictoriesL = new JLabel();
            proportionOfVictories = (double)NumberOfVictories/(double)NumberOfGamesPlayed;
            proportionOfVictoriesL.setForeground(Color.BLACK);
            proportionOfVictoriesL.setHorizontalAlignment(JLabel.CENTER);
            proportionOfVictoriesL.setText(String.format("Proportion of Victories = %5.2f", proportionOfVictories));

            standardDeviationL = new JLabel();
            standardDeviation = Math.sqrt(proportionOfVictories*(1.0 - proportionOfVictories)/(double)NumberOfGamesPlayed);
            standardDeviationL.setForeground(Color.BLACK);
            standardDeviationL.setHorizontalAlignment(JLabel.CENTER);
            standardDeviationL.setText(String.format("Standard Deviation = %5.2f", standardDeviation));
            
            dateL = new JLabel();
            dateL.setForeground(Color.BLACK);
            dateL.setHorizontalAlignment(JLabel.CENTER);
            dateL.setText(String.format("Last Game played ", LastPlayedDate));
        }
        
        /**
         * CtrlZListener - Fires when ctrl z is pressed.
         */
        class CtrlZListener implements KeyListener{
            @Override 
            public void keyPressed(KeyEvent e){
                boolean ctrlDn;
                ctrlDn = e.isControlDown();
                
                if((e.getKeyCode()==java.awt.event.KeyEvent.VK_Z)&&ctrlDn){
                    showStatistics = false;
                    player.unDo();
                    repaint();
                }
                else{
                }
            }
            @Override
            public void keyReleased(KeyEvent e){}
            @Override
            public void keyTyped(KeyEvent e){}
        }
        
        /**
         * GamePanelListener - Fires when mouse clicks on a card. Determines which card has mouse click and responds.
         */
        class GamePanelListener implements MouseListener {
            private boolean mousePressed;
            
            @Override
            public void mouseClicked(MouseEvent mouseEvent){}
            @Override
            public void mouseEntered(MouseEvent mouseEvent){}
            @Override
            public void mouseExited(MouseEvent mouseEvent){}
            @Override
            public void mousePressed(MouseEvent mouseEvent){
                mousePressed = true;
                respondToMouse(mouseEvent, mousePressed);
            }
            @Override
            public void mouseReleased(MouseEvent mouseEvent){
                mousePressed = false;
                respondToMouse(mouseEvent, mousePressed);
            }
        }
        
        int lastStack = 0;
        boolean attempt = true;
        
        
        /**
         * Responds to mouse click.
         * @param mouseEvent - The mouse click.
         * @param mousePressed_in - True if mousePressed event false if mouseReleased
         */
        private void respondToMouse(MouseEvent mouseEvent, boolean mousePressed_in){
            CardRectangle cardRectangle;
            int num_cards;
            boolean pointFound;
            boolean respond;
            boolean e1;
            boolean e2;
            boolean placeFound;
            Point point;
            int mouseButton = 0;
            //boolean recycleDeck;
            int caseSelector;
            int numClicks = 0;
            
            e1 = player.toDisplay && mousePressed_in; //first half of move - respond to mouse pressed
            e2 = !player.toDisplay && !mousePressed_in; //second half of move - respond to mouse released

            requestFocusInWindow();
            
            showStatistics = false;
            repaint();
            
            caseSelector = 0;
            
            //respond = mousePressed_in;
            respond = e1 || e2;
            if(respond){ //respond to mouse pressed on first half of a move and mouse reseased on the second
                //protect_flag = false;
                if(!protect_flag){ //If protect flag set then don't respond to click
                    protect_flag = true;
                    
                    //System.out.println("numClicks = "+numClicks);
                    if(e1){ //first half of the move
                        attempt = true; //clear flags
                        lastStack = 0;
                        numClicks = mouseEvent.getClickCount();
                        mouseButton = mouseEvent.getButton();
                    }
//                    if(e1) System.out.println("e1");
//                    if(e2) System.out.println("e2");
//                    System.out.println("numClicks = "+numClicks+", mouseButton = "+mouseButton);
//                    System.out.println("attempt = "+attempt+", lastStack = "+lastStack);
//                    System.out.println("");
                    
                    point = mouseEvent.getPoint();
                    num_cards = cardRectangle_s.size();
                    
                    cardRectangle = cardRectangle_s.get(num_cards - 1);
                    pointFound = cardRectangle.isHit(point); //check last card drawn for a hit
                    while((num_cards > 0)&&(!pointFound)){ //Work down thought card rectangel stack until hit is found
                        cardRectangle = cardRectangle_s.get(num_cards - 1);
                        pointFound = cardRectangle.isHit(point);                    
                        num_cards--;
                    }
                     
                    if(pointFound){
//                        System.out.println("mouseButton = "+mouseButton+", stackNum = "+
//                                cardRectangle.stackNum+", cardNum = "+cardRectangle.cardNum);
                        if(e1 && (numClicks == 2) &&
                                (cardRectangle.stackNum == 8) && 
                                player.Stacks.isEmpty(cardRectangle.stackNum)){
                            caseSelector = 2;//recycle deck on double click in stack 8 when it is empty
                            //System.out.println("caseSelector = 1");
                        }
                        
                        if(e1 && (mouseButton > 1) &&
                                (cardRectangle.stackNum == 8) && 
                                player.Stacks.isEmpty(cardRectangle.stackNum)){
                            caseSelector = 2;//recycle deck on right click in stack 8 when it is empty
                            //System.out.println("caseSelector = 1");
                        }
                        
                        if(e1 && (mouseButton > 1) && 
                                ((cardRectangle.stackNum <= 8) || (cardRectangle.stackNum == 9)) && 
                                !player.Stacks.isEmpty(cardRectangle.stackNum) &&
                                (cardRectangle.cardNum == player.Stacks.getStackTop(cardRectangle.stackNum))){
                            caseSelector = 3;//automatically move card to upper stack when right clicked
                            //System.out.println("caseSelector = 2");
                        }

                        if(cardRectangle.stackNum == 10){
                            caseSelector = 1; //can't put it on the display stack
                        }
                        
                        switch (caseSelector){
                            case 1://can't put a card on the display stack
                                //do nothing
                                break;
                            case 2: //recycle deck on double click in stack 8 when it is empty
                                player.recycleDeck();
                                repaint();
                                break;
                            case 3: //automatically move card to upper stack when right clicked
                                //System.out.println("Case 3");
                                placeFound = player.autoMove(cardRectangle.stackNum, cardRectangle.cardNum);
                                if(placeFound) {
                                    repaint();
                                    break;
                                }
                            default://normal operation
                                attempt = !attempt;
                                //attempt++; //number of attempts to
                                //if((cardRectangle.stackNum != lastStack)||(attempt > 1)){
                                if((cardRectangle.stackNum != lastStack)|| attempt){
                                    //respond if it is new stack or if it is not the mouse release on the first mouse press 
                                    attempt = true;
                                    player.makeMove(cardRectangle.stackNum, cardRectangle.cardNum);
                                    repaint();
                                }
                                lastStack = cardRectangle.stackNum;
                                break;
                        }
                    }
                    protect_flag = false;
                }
            }
        }
        
        /**
         * Overrides paintComponent to paint card stacks.
         * @param g Provides graphics context for card image.
         */
        @Override
        public void paintComponent(Graphics g){ //paintComponent for main display
            //System.out.println("paint game panel");
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g); //paint standard background
            
            WindowData windowData = new WindowData(); //display parameters
            
            int stackNum;
            int gpHeight; //Game panel height
            gpHeight = this.getHeight();
            int gpWidth;
            gpWidth = this.getWidth();
            
            final int STACK_WIDTH; //Display parameters
            final int UPPER_RECTANGLE_HEIGHT;
            final int LOWER_RECTANGLE_HEIGHT;
            final int LOWER_RECTANGLE_POS_Y;
            final int UPPER_RECTANGLE_POS_Y;
            
            int stackRectPosX;
            int stackRectPosY;
            int cardPosX;
            int cardPosY;
            int numHiddenPosX;
            int numHiddenPosY;
            int rectangleHeight;
            int statisticsOffset;
            Dimension size;
            Graphics gg;
            
            Rectangle rectangle;
            Insets insets = this.getInsets();
            int cardNum = 0;
            boolean showBack = true;
            int offset; //offset for card position increments as other cards land on top
            CardRectangle cardRectangle;
            String numHidden;
            int countNotVis;
            
            //update announcement
            announcement.setText(player.announcement);
            size = announcement.getPreferredSize();
            announcement.setBounds(60 + insets.left, 1 + insets.top, size.width, size.height);
            //gg = g.create(gpWidth/2 - 40 + insets.left, 1 + insets.top,size.width, size.height);
            gg = g.create(gpWidth/2 - size.width/2 + insets.left, 1 + insets.top,size.width, size.height);
            announcement.paint(gg);
            
            //updateGameScore();
            gameScore.setText("Score = " + Integer.toString(player.getTotalScore()));
            size = gameScore.getPreferredSize();
            gameScore.setBounds(60 + insets.left, 1 + insets.top,size.width, size.height);
            gg = g.create(windowData.GAP + insets.left, 1 + insets.top, size.width, size.height);
            gameScore.paint(gg);
            
            
            STACK_WIDTH = ((10*(gpWidth - 6*windowData.GAP - 2*windowData.CONTAINER_GAP))/7 + 5)/10; //integer rounding corrected
            UPPER_RECTANGLE_POS_Y = windowData.CONTAINER_GAP + 30;
            UPPER_RECTANGLE_HEIGHT = windowData.Y_CARD + 10;
            
            LOWER_RECTANGLE_POS_Y = UPPER_RECTANGLE_POS_Y + UPPER_RECTANGLE_HEIGHT + 18;
            LOWER_RECTANGLE_HEIGHT = gpHeight - (windowData.CONTAINER_GAP + 30) - UPPER_RECTANGLE_HEIGHT -18 -windowData.CONTAINER_GAP;
            
            cardRectangle_s.clear(); //clear stack of card rectangles used by listener to determine which card was hit
            for(stackNum = 1; stackNum <= 14; stackNum++){ //Set up display parameters for stacks
                if(stackNum <= 7){ //lower stack panel
                        stackRectPosX = windowData.CONTAINER_GAP + (stackNum-1)*(STACK_WIDTH + windowData.GAP);
                        stackRectPosY = LOWER_RECTANGLE_POS_Y;
                        rectangleHeight = LOWER_RECTANGLE_HEIGHT;
                } else { //upper stack panel
                        stackRectPosX = windowData.CONTAINER_GAP + (stackNum-8)*(STACK_WIDTH + windowData.GAP);
                        stackRectPosY = UPPER_RECTANGLE_POS_Y;
                        rectangleHeight = UPPER_RECTANGLE_HEIGHT;
                }
                
                switch (stackNum){
                    case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                        cardNum = player.Stacks.getStackBottomVisible(stackNum); //first card to be displayed
                        showBack = false;
                        if((stackNum == player.last_stack) && 
                                (cardNum == player.Stacks.getStackTop(stackNum)) &&
                                !player.prevCardVis &&
                                !player.toDisplay) showBack = true; //show card back on stack from which last card was removed
                        
                        break;
                    case 8:
                        cardNum = player.Stacks.getStackTop(stackNum); //show back on new card pile
                        showBack = true;
                        
                        break;
                    case 10:
                        cardNum = player.stack10_display_card;  
                        showBack = false;
                        
                        break;
                    case 9: case 11: case 12: case 13: case 14:
                        cardNum = player.Stacks.getStackTop(stackNum);
                        showBack = false;
                        
                        break;
                }
                
                //Create and draw stack rectangle
                rectangle = new Rectangle(stackRectPosX, stackRectPosY, STACK_WIDTH, rectangleHeight);
                g2.setColor(Color.GREEN);
                if(stackNum == 10) g2.setColor(Color.YELLOW);
                g2.fill(rectangle);
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(rectangle);
                
                //Write the number of hidden cards above card image
                numHiddenPosX = stackRectPosX + 60;
                numHiddenPosY = stackRectPosY - 5;
                
                //count of cards not displayed to be printed above stack
                countNotVis = player.Stacks.getNotVisibleStackSize(stackNum);
                if(stackNum > 7) {
                    if(player.Stacks.isEmpty(stackNum)) countNotVis = 0;
                    else countNotVis = player.Stacks.getStackSize(stackNum) - 1;
                }
                numHidden = Integer.toString(countNotVis);
                g2.setColor(Color.BLACK);
                g2.drawString(numHidden, numHiddenPosX, numHiddenPosY);
                
                //Create and draw empty card rectangle
                cardPosX = stackRectPosX + (STACK_WIDTH - windowData.X_CARD)/2;
                cardPosY = stackRectPosY + 5;
                rectangle = new Rectangle(cardPosX, cardPosY, windowData.X_CARD, windowData.Y_CARD);
                g2.setColor(new Color(0, 127, 63));
                g2.fill(rectangle);
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(rectangle);
                
                //Create and draw filled card rectangles
                if(cardNum == 0){//add empty rectangle at bottom of every stack
                    cardRectangle = new CardRectangle(rectangle, stackNum, cardNum);
                    cardRectangle_s.add(cardRectangle); //add rectangle to stack.  
                }else{
                    if(stackNum <= 7){
                        offset = 0;
                        while(cardNum > 0){
                            rectangle = paintCard(g2, cardNum, cardPosX, cardPosY + offset, showBack);
                            cardRectangle = new CardRectangle(rectangle, stackNum, cardNum);
                            cardRectangle_s.add(cardRectangle);
                            offset = offset + windowData.OFFSET_INC;
                            cardNum = player.Stacks.Deck.getNextCardNumber(cardNum);
                        }
                    } else {
                        rectangle = paintCard(g2, cardNum, cardPosX, cardPosY, showBack);
                        cardRectangle = new CardRectangle(rectangle, stackNum, cardNum);
                        cardRectangle_s.add(cardRectangle);
                    }
                }
            }
            
            size.setSize(windowData.VICTORY_WIDTH, windowData.VICTORY_HEIGHT); //draw victory display
            victory.setPreferredSize(size);
            victory.setBounds(60 + insets.left, 1 + insets.top,size.width, size.height);
            gg = g.create(gpWidth/2 - windowData.VICTORY_WIDTH/2 + insets.left, windowData.VICTORY_HEIGHT/2 + insets.top, size.width, size.height);
            if(player.isVictory()) victory.paint(gg);
            //victory.paint(gg);
            
            statisticsOffset = 5;
            if(showStatistics){//Create and draw statistics rectangle
                rectangle = new Rectangle(windowData.X_STATISTICS_POS, windowData.Y_STATISTICS_POS, windowData.STATISTICS_WIDTH, windowData.STATISTICS_HEIGHT);
                g2.setColor(Color.WHITE);
                g2.fill(rectangle);
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(rectangle);
                
                highScoreL.setText("High Score = " + Integer.toString(HighScore));
                numberOfVictoriesL.setText("Number of Victories = "+Integer.toString(NumberOfVictories));
                numberOfGamesPlayedL.setText("Number of Games Played = "+Integer.toString(NumberOfGamesPlayed));
                proportionOfVictories = (double)NumberOfVictories/(double)NumberOfGamesPlayed;
                proportionOfVictoriesL.setText(String.format("Proportion of Victories = %6.1f", 100.*proportionOfVictories)+"%");
                standardDeviation = Math.sqrt(proportionOfVictories*(1.0 - proportionOfVictories)/(double)NumberOfGamesPlayed);
                standardDeviationL.setText(String.format("Standard Deviation = %6.1f", 100.*standardDeviation)+"%");
                dateL.setText(String.format("Last Game Played "+ LastPlayedDate));
                
                size = highScoreL.getPreferredSize();
                highScoreL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                highScoreL.paint(gg);
                
                statisticsOffset = statisticsOffset + windowData.STATISTICS_INC;
                size = numberOfVictoriesL.getPreferredSize();
                numberOfVictoriesL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                numberOfVictoriesL.paint(gg);
                
                statisticsOffset = statisticsOffset + windowData.STATISTICS_INC;
                size = numberOfGamesPlayedL.getPreferredSize();
                numberOfGamesPlayedL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                numberOfGamesPlayedL.paint(gg);
                
                statisticsOffset = statisticsOffset + windowData.STATISTICS_INC;
                size = proportionOfVictoriesL.getPreferredSize();
                proportionOfVictoriesL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                proportionOfVictoriesL.paint(gg);
                
                statisticsOffset = statisticsOffset + windowData.STATISTICS_INC;
                size = standardDeviationL.getPreferredSize();
                standardDeviationL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                standardDeviationL.paint(gg);
                
                statisticsOffset = statisticsOffset + windowData.STATISTICS_INC;
                size = dateL.getPreferredSize();
                dateL.setBounds(1 + insets.left, 1 + insets.top,size.width, size.height);
                gg = g.create(windowData.X_STATISTICS_POS + insets.left + windowData.STATISTICS_H_OFFSET, windowData.X_STATISTICS_POS + insets.top + statisticsOffset, size.width, size.height);
                dateL.paint(gg);
            }
        }
        
        /**
         * CardRectangle - Rectangle with stack number and card number data for use by mouse 
         * listener to determine what was hit by mouse click.
         */
        class CardRectangle {
            Rectangle rectangle;
            public int stackNum;
            public int cardNum;
            CardRectangle(Rectangle rectIn, int stackNumIn, int cardNumIn){
                rectangle = rectIn;
                stackNum = stackNumIn;
                cardNum = cardNumIn;
            }
            
            boolean isHit(Point point){
                return rectangle.contains(point);
            }
        }
                
        /**
         * Paints card.
         * @param g2 - graphics context for painting the card
         * @param cardNum - cardNum for card to paint
         * @param cardPosX - x coordinate of card position
         * @param cardPosY - y coordinate of card position
         * @param showBack - true if back of card is to be painted 
         * @return rectangle - the rectangle that is painted with the card.  
         * The rectangle will be used to determine if the card is hit by a mouse click.
         */
        private Rectangle paintCard(Graphics2D g2, int cardNum, int cardPosX, int cardPosY, boolean showBack){
            Rectangle rectangle;
            BufferedImage img;
            int IMGwidth;
            int IMGheight;
            
            WindowData windowData = new WindowData(); //display parameters
            
            img = player.Stacks.Deck.getImage(cardNum);
            IMGwidth = player.Stacks.Deck.getImage(cardNum).getWidth();
            IMGheight = player.Stacks.Deck.getImage(cardNum).getHeight();
            //System.out.println("paintCard image width = "+IMGwidth+", image height = "+IMGheight);
            
            if(showBack) {
                img = player.Stacks.Deck.getImage(53);
                IMGwidth = img.getWidth();
                IMGheight = player.Stacks.Deck.getImage(53).getHeight();
            }
                        
            //IMGheight = img.getHeight();
            g2.drawImage(img, 
                    cardPosX, cardPosY, cardPosX+windowData.X_CARD, cardPosY+windowData.Y_CARD, 
                    0,0,IMGwidth, IMGheight, null);
            rectangle = new Rectangle(cardPosX, cardPosY, windowData.X_CARD, windowData.Y_CARD);
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2));
            g2.draw(rectangle);
            
            return rectangle;
        }
    }
    
    /**
     * ButtonPanel - Panel of control buttons displayed below game panel.
     */
    public class ButtonPanel extends JPanel{
        ButtonPanel(){
            WindowData windowData = new WindowData();
            //System.out.println("buttonPanel");
            this.setBorder(windowData.BORDER);
            this.setBackground(Color.GREEN);
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            
            JButton newGameButton = new JButton("New Game");
            NewGameButtonListener newGameButtonListener = new NewGameButtonListener();
            newGameButton.addActionListener(newGameButtonListener);

            JButton restartButton = new JButton("Restart Game");
            RestartButtonListener restartButtonListener = new RestartButtonListener();
            restartButton.addActionListener(restartButtonListener);

            JButton saveButton = new JButton("Save & Quit");
            SaveButtonListener saveButtonListener = new SaveButtonListener();
            saveButton.addActionListener(saveButtonListener);

            JButton unDoButton = new JButton("Un Do");
            UnDoButtonListener unDoButtonListener = new UnDoButtonListener();
            unDoButton.addActionListener(unDoButtonListener);

            JButton recycleDeckButton = new JButton("Recycle Deck");
            RecycleDeckButtonListener recycleDeckButtonListener = new RecycleDeckButtonListener();
            recycleDeckButton.addActionListener(recycleDeckButtonListener);

            JButton autoPlayButton = new JButton("Auto Play");
            AutoPlayButtonListener autoPlayButtonListener = new AutoPlayButtonListener();
            autoPlayButton.addActionListener(autoPlayButtonListener);

            JButton displayStatsButton = new JButton("Display Statistics");
            DisplayStatsButtonListener displayStatsButtonListener = new DisplayStatsButtonListener();
            displayStatsButton.addActionListener(displayStatsButtonListener);
            
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addContainerGap(100, 100)
                    .addComponent(newGameButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(restartButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(saveButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(unDoButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(recycleDeckButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(autoPlayButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(displayStatsButton)
            );
            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newGameButton)
                    .addComponent(restartButton)
                    .addComponent(saveButton)
                    .addComponent(unDoButton)
                    .addComponent(recycleDeckButton)
                    .addComponent(autoPlayButton)
                    .addComponent(displayStatsButton)
            );
        }
    }
    
    /**
     * NewGameButtonListener - Causes action when new game button pressed.
     */
    private class NewGameButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            //System.out.println("newGameButton");
            showStatistics = false;
            writeStatistics();
            readStatistics();
            player.newGame();
            repaint();
        }
    }

    /**
     * RestartButtonListener - Causes action when restart button pressed.
     */
    private class RestartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            //System.out.println("restartButton");
            showStatistics = false;
            player.restartGame();
            repaint();
        }
    }

    /**
     * SaveButtonListener - Causes action when save and quit button pressed.
     */
    private class SaveButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            boolean writeSuccess;
            //SolitaireGameArchive solitaireGameArchive;

            //solitaireGameArchive = new SolitaireGameArchive();
            //writeGameFile(solitaireGameArchive);
            writeSuccess = writeGameFile();
            repaint();
            if(writeSuccess){
                writeStatistics_flag = false;
                closeSolitaire();
            }
        }
    }

    /**
     * UnDoButtonListener - Causes action when undo button pressed.
     */
    private class UnDoButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            showStatistics = false;
            player.unDo();
            repaint();
        }
    }

    /**
     * RecycleDeckButtonListener - Causes action when recycle deck  button pressed.
     */
    private class RecycleDeckButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            //System.out.println("RecycleDeckListener 8 is empty-"+Moves.Stacks.isEmpty(8)+", toDisplay-"+Moves.toDisplay);
            showStatistics = false;
            if((player.Stacks.isEmpty(8))&&(player.toDisplay)){
                player.recycleDeck();
                repaint();
            }
        }
    }

    /**
     * AutoPlayButtonListener - Causes action when auto play button pressed.
     */
    private class AutoPlayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            //System.out.println("AutoPlayButtonListener toDisplay = " + Moves.toDisplay);
            showStatistics = false;
            if(player.toDisplay){
                player.setAnnouncement("Working");
                //repaint();
                protect_flag = true;
                autoPlayTimer.start();
            } else {
                player.setAnnouncement("Must complet move to run AutoPlay");
                //repaint();
            }
            repaint();
        }
    }


    /**
     * DisplayStatsButtonListener - Causes action when display statistics button pressed.
     */
    private class DisplayStatsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            showStatistics = !showStatistics;
            repaint();
        }
    }
    
    /**
     * SolitaireFrameListener - Causes action when main window is closed.
     */
    private class SolitaireFrameListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {}
        @Override
        public void windowClosing(WindowEvent e) {
                //System.out.println("Closing");
                if(writeStatistics_flag) writeStatistics();
        }
        @Override
        public void windowClosed(WindowEvent e) {}
        @Override
        public void windowIconified(WindowEvent e) {}
        @Override
        public void windowDeiconified(WindowEvent e) {}
        @Override
        public void windowActivated(WindowEvent e) {}
        @Override
        public void windowDeactivated(WindowEvent e) {}
    }
    
    
    /**
     * AutoPlayTimerListener - Causes action when auto play timer fires.
     */
    private class AutoPlayTimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean found;
            
            found = player.autoPlay();
            if(found){
                repaint();
            } else {
                autoPlayTimer.stop();
                player.setAnnouncement("");
                repaint();
                protect_flag = false;
            }
        }
    }
    
    /**
     * Gets current date for statistics display.
     */
    private String getDate(){
        String DATE_FORMAT = "MM-dd-yyyy";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    } 

    /**
     * Writes statistics data to disk file.
     */
    private void writeStatistics(){
        String string;
        int len;
        int ans;
        PrintWriter out;
        
        try{
            out = new PrintWriter("SolitaireStats.txt");

            ans = HighScore;
            if(player.getTotalScore() > HighScore)ans = player.getTotalScore();
            string = "HighScore " + Integer.toString(ans);
            out.println(string);

            ans = NumberOfVictories;
            if(player.isVictory()) ans = NumberOfVictories + 1;
            string = "NumberOfVictories " + Integer.toString(ans);
            out.println(string);

            ans = NumberOfGamesPlayed + 1;
            string = "NumberOfGamesPlayed " + Integer.toString(ans);
            out.println(string);

            string = "LastGamePlayed " + getDate();
            out.println(string);
            
            out.close();

        }
        catch(IOException e){
            System.out.println("writeStatistics IO Error");
        }
    }
    
    /**
     * Reads statistics data from disk file.
     */
    private void readStatistics(){
        int n;
        Scanner s = null;
        ArrayList<String> strings;
        String string;
        
        strings = new ArrayList<>();

        java.io.File file = new java.io.File("SolitaireStats.txt");
        if(file.exists()){
            try {
                s = new Scanner(new BufferedReader(new FileReader("SolitaireStats.txt")));
                //s.useDelimiter("=");

                while (s.hasNext()) {
                    string = s.next();
                    //System.out.println(string);
                    strings.add(string);
                    n = strings.size();
                    //System.out.println("n = "+n+" "+strings.get(n-1));
                }
                //System.out.println("readStatistics successful");
            } catch(IOException e) {
                System.out.println("readStatistics IO Error");

            } finally {
                if (s != null) {
                    s.close();
                }
            }
            HighScore = Integer.parseInt(strings.get(1));
            NumberOfVictories = Integer.parseInt(strings.get(3));
            NumberOfGamesPlayed = Integer.parseInt(strings.get(5));
            LastPlayedDate = strings.get(7);
            //System.out.println(LastPlayedDate);
        } else {
            HighScore = 0;
            NumberOfVictories = 0;
            NumberOfGamesPlayed = 0;
            LastPlayedDate = "";
            //System.out.println("readStatistics no file found");
        }
    }
    
    /**
     * Converts solitaire move to integer array.
     * @param solitaireMove - move that is to be converted
     * @return in form of integer array
     */
    private int[] move2int(SolitaireMove solitaireMove){
        int[] moveContent = new int[4];

        moveContent[0] = solitaireMove.card;
        moveContent[1] = solitaireMove.score;
        moveContent[2] = solitaireMove.stackFrom;
        moveContent[3] = solitaireMove.stackTo;
        
        return moveContent;
    }
    
    /**
     * Converts integer array to solitaire move.
     * @param integer array to be converted
     * @return solitaireMove form of array data 
     */
    private SolitaireMove int2move(int[] moveContent){
        SolitaireMove solitaireMove;
        
        solitaireMove = new SolitaireMove(moveContent[2], moveContent[3], moveContent[0], moveContent[1]);
        
        return solitaireMove;
    }

    /**
     * Fills solitaire moves stack from data read from disk.
     */
    private void fillSolitaireMoves(){
        int numMoves;
        int nMoves;
        int[] intMove;
        
        player.solitaireMoves.clear();
        numMoves = solitaireMovesLocal.size();
        for(nMoves = 1; nMoves <= numMoves; nMoves++){
            intMove = solitaireMovesLocal.get(nMoves - 1);
            player.solitaireMoves.add(int2move(intMove));
        }
        player.executeMoves();
    }
    
    /**
     * Fills local solitaire moves data to be written to disk.
     */
    private void fillSolitaireMovesLocal(){
        int n;
        int nmx;
        int[] intMove;
        
        solitaireMovesLocal = new ArrayList<>();
        nmx = player.solitaireMoves.size();
        for(n = 1; n <= nmx; n++){
            intMove = move2int(player.solitaireMoves.get(n - 1));
            solitaireMovesLocal.add(intMove);
        }
    }
    
    /**
     * Reads saved game data from disk.
     * @return true if there is a file to read
     */
    private boolean readGameFile(){
        int n;
        //int nmx;
        boolean fileExistsLocal;
        //SolitaireGameArchive solitaireGameArchive;
        ObjectInputStream in;
        File file = new File("SolitaireGameArchive.sga");
        
        fileExistsLocal = false;
        if(file.exists()) {
            try {
                try {
                    in = new ObjectInputStream(new FileInputStream(file));
                    
                    player.Stacks.Deck.NextCards = (int[]) in.readObject();
                    player.Stacks.Deck.NextCardsArchive = (int[]) in.readObject();
                    player.Stacks.Deck.CardsVis = (boolean[]) in.readObject();
                    player.Stacks.Deck.CardsVisArchive = (boolean[]) in.readObject();
                    player.Stacks.Deck.start_card = (int) in.readObject();
                    solitaireMovesLocal = (ArrayList<int[]>) in.readObject();
                    
                    fileExistsLocal = true;
                    
                    in.close();
                    file.delete();
                } catch(IOException e) {
                    System.out.println("readGameFile IO error");
                }
            } catch(ClassNotFoundException ee) {
                System.out.println("readGameFile Class not found error");
            }
                    
//            nmx = Moves.solitaireMoves2.size();
//            for(n = 1; n <= nmx; n++){
//                System.out.println("readGameFile card = "+Moves.solitaireMoves2.get(n-1)[0]+
//                        ", score = "+Moves.solitaireMoves2.get(n-1)[1]+
//                        ", stack from = "+Moves.solitaireMoves2.get(n-1)[2]+
//                        ", stack to = "+Moves.solitaireMoves2.get(n-1)[3]);
//            }
        }
        return fileExistsLocal;
    }
    

        
    /**
     * Writes game data to disk.
     */
    private boolean writeGameFile(){
        ObjectOutputStream out;
        int n;
        boolean result;
//        int nmx;
        result = false;
        if(player.toDisplay){
            fillSolitaireMovesLocal();
//        try {
//            //nmx = Moves.solitaireMoves2.size();
//            nmx = solitaireMovesLocal.size();
//            //nmx = solitaireMovesAr.size();
//            for(n = 1; n <= nmx; n++){
//                System.out.println("writeGameFile card = "+Moves.solitaireMoves2.get(n-1)[0]+
//                        ", score = "+Moves.solitaireMoves2.get(n-1)[1]+
//                        ", stack from = "+Moves.solitaireMoves2.get(n-1)[2]+
//                        ", stack to = "+Moves.solitaireMoves2.get(n-1)[3]);
//            }
//            System.out.println("writeGameFile nmx = "+nmx);
//        }
//        catch(NullPointerException e){}

            File file = new File("SolitaireGameArchive.sga");

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
                //out.writeObject(outputArchive);
                //out.writeObject(solitaireGameArchive);
                out.writeObject(player.Stacks.Deck.NextCards);
                out.writeObject(player.Stacks.Deck.NextCardsArchive);
                out.writeObject(player.Stacks.Deck.CardsVis);
                out.writeObject(player.Stacks.Deck.CardsVisArchive);
                out.writeObject(player.Stacks.Deck.start_card);
                out.writeObject(solitaireMovesLocal);

                out.close();
                result = true;
            } catch(IOException e) {
                System.out.println("writeGameFile IO error");
        }
        } else {
            player.setAnnouncement("Must Complete Move Before Save");
        }
        return result;
    }
    
    /**
     * Closes the main window and ends the game. Called by save & quit.
     */
    private void closeSolitaire(){
        this.dispose();
    }
        
    
    
    /**
     * Runs solitaire game.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Solitaire solitaire = new Solitaire();
                //System.out.println("run");
            }
        });
    }
}
