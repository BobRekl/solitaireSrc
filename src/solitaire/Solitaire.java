/*
 * Solitaire Game.
 */
package solitaire;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 *Solitaire - The Solitaire game JFrame, contains the Game Panel and Button Panel.
 * @author Bob Reklis
 * @version 11.23.2016
 */
public class Solitaire extends JFrame {
    int HighScore; // Data for statistics display
    int NumberOfVictories;
    int NumberOfGamesPlayed;
    String LastPlayedDate;
    
    public SolitairePlayer player; //Moves the cards for the game
    boolean protect_flag; //Turns off mouse click responses
    boolean showStatistics_flag; //Flag causes statistics display to be painted
    boolean writeStatistics_flag; //Set false when save and quit button is pushed this preserves game count
    
    
    ArrayList<int[]> solitaireMovesLocal; //A local version of the solitaire moves stack as and ArrayList of int[]
    AutoPlayTimerListener autoPlayTimerListener; //Timer used to animate auto play function
    javax.swing.Timer autoPlayTimer;
    SolitaireFrameListener solitaireFrameListener; //Used to monitor window closure to trigger writing the statistics file
    
    public GamePanel gamePanel; //Panel displays card stacks
    public ButtonPanel buttonPanel; //Panel displays buttons that control game functions
    
    /**
     * Solitaire Constructor - Creates main JPanel. 
     */
    Solitaire(){
        boolean fileExists; //saved game file exists
        
        WindowData windowData = new WindowData(); //gets display parameters
        //solitairePanel = new SolitairePanel(); //container for game panel and button panel
        
        player = new SolitairePlayer(); //moves the cards, sets up the stacks, sets up the deck
            
        fileExists = readGameFile(); //read stored game data if it exists
        if(fileExists) fillSolitaireMoves(); //generate move stack from stored data
        
        autoPlayTimerListener = new AutoPlayTimerListener(); //listener for timer used to animate auto play
        autoPlayTimer = new javax.swing.Timer(windowData.AUTO_PLAY_TIMER_TIC, autoPlayTimerListener);
        autoPlayTimer.stop(); //turn off timer until auto play button pushed
        
        solitaireFrameListener = new SolitaireFrameListener(); //listener to save statistics on window close
        this.addWindowListener(solitaireFrameListener);
        
        this.setSize(windowData.X_WINDOW_SIZE, windowData.Y_WINDOW_SIZE); //set up window
        this.setTitle("Solitaire"); //title for main window
        this.setLocation(windowData.X_WINDOW_LOC, windowData.Y_WINDOW_LOC);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(player.Stacks.Deck.getImage(54));
        this.setVisible(true);
        
        writeStatistics_flag = true; //update statistics file at end of game

        this.setLayout(new BorderLayout());
        //this.setBorder(windowData.BORDER);
        this.setBackground(Color.GREEN);

        gamePanel = new GamePanel();//Displays the game
        buttonPanel = new ButtonPanel();//Displays buttons at the bottom
        this.add(gamePanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.PAGE_END);
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
        DragDropTimerListener dragDropTimerListener;
        javax.swing.Timer dragDropTimer;
        
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
            //this.setPreferredSize(new java.awt.Dimension(windowData.X_BOARD_SIZE, windowData.Y_BOARD_SIZE));
            
            dragDropTimerListener = new DragDropTimerListener(); //listener for timer used to animate drag and drop
            dragDropTimer = new javax.swing.Timer(windowData.DRAG_DROP_TIMER_TIC, dragDropTimerListener);
            dragDropTimer.stop(); //turn off timer until mouse pushed
            
            HighScore = 0;
            NumberOfVictories = 0;
            NumberOfGamesPlayed = 0;
            LastPlayedDate = "";
            showStatistics_flag = false;
            
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
         * DragDropTimerListener - Causes action during drag and drop.
         */
        private class DragDropTimerListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent event) {
                Point panelLocation;
                WindowData windowData;
                
                windowData = new WindowData();
                panelLocation = getLocationOnScreen();
                //System.out.println("DragDropTimerListener mouseInfo x = "+(int) MouseInfo.getPointerInfo().getLocation().getX()+", y = "+(int) MouseInfo.getPointerInfo().getLocation().getY());
                point.setLocation(MouseInfo.getPointerInfo().getLocation().getX() - panelLocation.getX() - windowData.GHOST_CURSOR_OFFSET, 
                        MouseInfo.getPointerInfo().getLocation().getY() - panelLocation.getY() - windowData.GHOST_CURSOR_OFFSET);
                repaint();
            }
        }

        /**
         * CtrlZListener - Fires when ctrl z is pressed.
         */
        class CtrlZListener implements KeyListener{
            @Override 
            public void keyPressed(KeyEvent e){
                boolean ctrlDn;
                ctrlDn = e.isControlDown();
                //System.out.println("ctrlzlistener1");
                if((e.getKeyCode()==java.awt.event.KeyEvent.VK_Z)&&ctrlDn){
                    //System.out.println("ctrlzlistener2");
                    showStatistics_flag = false;
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
                dragDropTimer.start();
                respondToMouse(mouseEvent, mousePressed);
            }
            @Override
            public void mouseReleased(MouseEvent mouseEvent){
                mousePressed = false;
                dragDropTimer.stop();
                respondToMouse(mouseEvent, mousePressed);
            }
        }
        
        int lastStack = 0;
        boolean attempt = true;
        boolean mouseDown = false;
        Point point;
        int cardFound = 0;
        int stackFound = 0;
        
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
            int mouseButton = 0;
            //boolean recycleDeck;
            int caseSelector;
            int numClicks = 0;
            
            e1 = player.toDisplay && mousePressed_in; //first half of move - respond to mouse pressed
            e2 = !player.toDisplay && !mousePressed_in; //second half of move - respond to mouse released
            cardFound = 0;
            stackFound = 0;

            showStatistics_flag = false;
            repaint();
            
            caseSelector = 0;
            
            //respond = mousePressed_in;
            respond = e1 || e2;
            if(respond){ //respond to mouse pressed on first half of a move and mouse reseased on the second
                repaint();
                //protect_flag = false;
                if(!protect_flag){ //If protect flag set then don't respond to click
                    protect_flag = true;
                    
                    //System.out.println("numClicks = "+numClicks);
                    mouseDown = false; //The mouse is released.  will later be set true if down
                    if(e1){ //first half of the move
                        attempt = true; //clear flags
                        lastStack = 0;
                        //mouseDown = true; //The mouse is pressed
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
                        cardFound = cardRectangle.cardNum;
                        stackFound = cardRectangle.stackNum;
                    } else {
                        cardFound = 0;
                        stackFound = 0;
                    }
//                        System.out.println("mouseButton = "+mouseButton+", stackNum = "+
//                                stackFound+", cardNum = "+cardFound);
                        if(e1 && (numClicks == 2) &&
                                (stackFound == 8) && 
                                player.Stacks.isEmpty(stackFound)){
                            caseSelector = 2;//recycle deck on double click in stack 8 when it is empty
                            //System.out.println("caseSelector = 1");
                        }
                        
                        if(e1 && (mouseButton > 1) &&
                                (stackFound == 8) && 
                                player.Stacks.isEmpty(stackFound)){
                            caseSelector = 2;//recycle deck on right click in stack 8 when it is empty
                            //System.out.println("caseSelector = 1");
                        }
                        
                        if(e1 && (mouseButton > 1) && 
                                (stackFound <= 9) && 
                                !player.Stacks.isEmpty(stackFound) &&
                                (cardRectangle.cardNum == player.Stacks.getStackTop(stackFound))){
                            caseSelector = 3;//automatically move card to upper stack when right clicked
                            //System.out.println("caseSelector = 2");
                        }

                        if(stackFound == 10){
                            caseSelector = 1; //can't put it on the display stack
                        }
                        
                        switch (caseSelector){
                            case 1://can't put a card on the display stack
                                cardFound = 0; //sets up error situation in makeMove
                                stackFound = 0;
                                player.makeMove(stackFound, cardFound);
                                repaint();
                                break;
                            case 2: //recycle deck on double click in stack 8 when it is empty
                                player.recycleDeck();
                                repaint();
                                break;
                            case 3: //automatically move card to upper stack when right clicked
                                //System.out.println("Case 3");
                                placeFound = player.autoMove(stackFound, cardFound);
                                if(placeFound) {
                                    repaint();
                                    break;
                                }
                            default://normal operation
                                if(e1 && (stackFound == 8)){
                                    player.moveStack8to9();
                                    repaint();
                                } else {
                                    if(e1){
                                        mouseDown = true; //The mouse is pressed
                                    }
                                    attempt = !attempt;
                                    if((stackFound != lastStack)|| attempt){
                                        //respond if it is new stack or if it is not the mouse release on the first mouse press 
                                        attempt = true;
                                        player.makeMove(stackFound, cardFound);
                                        repaint();
                                    }
                                    lastStack = stackFound;
                                }
                                break;
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
            
            requestFocusInWindow();
            
//            if(point != null){
//                System.out.println("paintComponent point x = "+(int) point.getX()+
//                        ", y = "+(int) point.getY()+", mouseDown = "+mouseDown+
//                        ", cardFound = "+cardFound);
//            }
            
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
            if(showStatistics_flag){//Create and draw statistics rectangle
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
            if(mouseDown && (cardFound > 0) && (stackFound != 8) &&(point != null)){ //draw card at cursor location
                paintGhostCard(g2, cardFound, (int)point.getX(), (int)point.getY());
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
         * @param g2 - 2D graphics context for painting the card
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
        
        /**
         * Paints ghost card.
         * @param g2 - graphics context for painting the card
         * @param cardNum - cardNum for card to paint
         * @param cardPosX - x coordinate of card position
         * @param cardPosY - y coordinate of card position
         */
        void paintGhostCard(Graphics2D g2, int cardNum, int cardPosX, int cardPosY){
            Rectangle rectangle;
            BufferedImage img;
            BufferedImage img2;
            int IMGwidth;
            int IMGheight;
            float[] scales = {1.0f, 1.0f, 1.0f, 0.5f};
            float[] offsets = {0.0f, 0.0f, 0.0f, 0.0f};
            
            WindowData windowData = new WindowData(); //display parameters
            
            scales[3] = windowData.GHOST_TRANSPARENCY;
            RescaleOp rop = new RescaleOp(scales, offsets, null);
            
            img = player.Stacks.Deck.getImage(cardNum);
            IMGwidth = player.Stacks.Deck.getImage(cardNum).getWidth();
            IMGheight = player.Stacks.Deck.getImage(cardNum).getHeight();
            //System.out.println("paintCard image width = "+IMGwidth+", image height = "+IMGheight);
            
            img2 = new BufferedImage(IMGwidth, IMGheight, BufferedImage.TYPE_INT_ARGB);
            Graphics gg = img2.createGraphics();
            gg.drawImage(img, 0, 0, null);
            gg.dispose();
            
            g2.drawImage(rop.filter(img2, null), 
                    cardPosX, cardPosY, cardPosX+windowData.X_CARD, cardPosY+windowData.Y_CARD, 
                    0,0,IMGwidth, IMGheight, null);
            rectangle = new Rectangle(cardPosX, cardPosY, windowData.X_CARD, windowData.Y_CARD);
            g2.setColor(windowData.GHOST_BORDER_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.draw(rectangle);
        }
    }
    
    /**
     * ButtonPanel - Panel of control buttons displayed below game panel.
     */
    public class ButtonPanel extends JPanel{
        ButtonPanel(){
            int widthNewGame;
            int widthRestart;
            int widthSave;
            int widthUnDo;
            int widthRecycle;
            int widthAutoPlay;
            int widthStats;
            int widthGap;
            int widthButtons;
            int widthButtonPanel;
            
            WindowData windowData = new WindowData();
            //System.out.println("buttonPanel");
            this.setBorder(windowData.BORDER);
            this.setBackground(Color.GREEN);
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            
            JButton newGameButton = new JButton("New Game");
            NewGameButtonListener newGameButtonListener = new NewGameButtonListener();
            newGameButton.addActionListener(newGameButtonListener);
            widthNewGame = newGameButton.getPreferredSize().width;

            JButton restartButton = new JButton("Restart Game");
            RestartButtonListener restartButtonListener = new RestartButtonListener();
            restartButton.addActionListener(restartButtonListener);
            widthRestart = restartButton.getPreferredSize().width;

            JButton saveButton = new JButton("Save & Quit");
            SaveButtonListener saveButtonListener = new SaveButtonListener();
            saveButton.addActionListener(saveButtonListener);
            widthSave = saveButton.getPreferredSize().width;

            JButton unDoButton = new JButton("Un Do");
            UnDoButtonListener unDoButtonListener = new UnDoButtonListener();
            unDoButton.addActionListener(unDoButtonListener);
            widthUnDo = unDoButton.getPreferredSize().width;

            JButton recycleDeckButton = new JButton("Recycle Deck");
            RecycleDeckButtonListener recycleDeckButtonListener = new RecycleDeckButtonListener();
            recycleDeckButton.addActionListener(recycleDeckButtonListener);
            widthRecycle = recycleDeckButton.getPreferredSize().width;

            JButton autoPlayButton = new JButton("Auto Play");
            AutoPlayButtonListener autoPlayButtonListener = new AutoPlayButtonListener();
            autoPlayButton.addActionListener(autoPlayButtonListener);
            widthAutoPlay = autoPlayButton.getPreferredSize().width;
            
            JButton displayStatsButton = new JButton("Display Statistics");
            DisplayStatsButtonListener displayStatsButtonListener = new DisplayStatsButtonListener();
            displayStatsButton.addActionListener(displayStatsButtonListener);
            widthStats = displayStatsButton.getPreferredSize().width;
            
            widthButtons = widthNewGame + widthRestart + widthSave + widthUnDo + 
                    widthRecycle + widthAutoPlay + widthStats;
            widthGap = 6;
            widthButtonPanel = windowData.X_WINDOW_SIZE - getInsets().left - getInsets().right;
            //System.out.println("containerGap = "+(widthButtonPanel - widthButtons)/2+", inset = "+getInsets().left+", gap = "+widthGap);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addContainerGap((widthButtonPanel - widthButtons - 6*widthGap)/2, 
                            (widthButtonPanel - widthButtons - 6*widthGap)/2)
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
            showStatistics_flag = false;
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
            showStatistics_flag = false;
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
            showStatistics_flag = false;
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
            showStatistics_flag = false;
            if((player.Stacks.isEmpty(8))&&(player.toDisplay)){ //If new card pile is empty and it is the first half of a move then recycle the deck
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
            showStatistics_flag = false;
            if(player.toDisplay){ //if first half of the move cycle
                player.setAnnouncement("Working");
                //repaint();
                protect_flag = true;
                autoPlayTimer.start(); //start the timer.  autoPlayTimerListener will search for moves one at a time as the timer fires 
            } else {
                player.setAnnouncement("Must complet move to run AutoPlay");
                //repaint();
            }
            repaint();
        }
    }
    
    /**
     * AutoPlayTimerListener - Causes action when auto play timer fires.
     */
    private class AutoPlayTimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean found;
            
            found = player.autoPlay(); //search for one move each time the timer fires
            if(found){
                repaint();
            } else { //when no valid moves are found turn off the timer and quit
                autoPlayTimer.stop();
                player.setAnnouncement("");
                repaint();
                protect_flag = false;
            }
        }
    }

    /**
     * DisplayStatsButtonListener - Causes action when display statistics button pressed.
     */
    private class DisplayStatsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            showStatistics_flag = !showStatistics_flag;
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

            File file = new File("SolitaireGameArchive.sga");

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
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
