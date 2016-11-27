/*
 * Solitaire Game - Deck of cards.
 */
package solitaire;

/**
 * Solitaire Deck shuffles and sets up the deck of cards for the game.
 * @author Bob Reklis
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;

/**
 * Solitaire Deck shuffles and sets up the deck of cards for the game.
 * @author Bob Reklis
 */
public class SolitaireDeck {
    public static final BufferedImage[] CARD_IMAGES = new BufferedImage[54];
    public int[] NextCards = new int[52]; //For card n-1 the array holds the next card in the deck.  NextCards run 1 to 52
    public int[] NextCardsArchive = new int[52];
    public boolean[] CardsVis = new boolean[52]; //For card n-1 the array holds the visibility.  NextCards run 1 to 52
    public boolean[] CardsVisArchive = new boolean[52]; //For card n-1 the array holds the visibility archive.  NextCards run 1 to 52
    int start_card;
    public boolean fileExists;
    
    /**
     * Creates and shuffles deck, reads card images. 
     */
    SolitaireDeck(){
        int Acard;
        int n;
        
        localShuffleDeck();
        
        readCardImages();
        
        Acard = 1; //A test of the cards in the deck to see if the next cards are correct.
        for(n = 1; n<=52; n++){
            Acard = NextCards[Acard - 1];
            //System.out.println(Acard);
        }
        if(Acard != 1) System.out.println("Solitaire Deck Fault 1");
    }
    
    /**
     * NCard provides a comparable class used to permute card data and shuffle 
     * the solitaire deck.
     */
    class NCard implements Comparable<NCard>{ //class to set measure to allow sorting
        final int choose = 1; //sets card measure to cardData[1]
        public int[] cardData = new int[3]; // 3 main data elements, card number, random number, next card

        NCard(int in0, int in1, int in3){ //Constructor with full data
                this.cardData[0] = in0;
                this.cardData[1] = in1;
                this.cardData[2] = in3;
        }

        NCard(){ //Constructor with no data
                this.cardData[0] = 0;
                this.cardData[1] = 0;
                this.cardData[2] = 0;
        }
        
        @Override
        public int hashCode(){return cardData[0];}

        @Override
        public int compareTo(NCard nc) { //sets data element to sort by
                //int choose = 1; //sets card measure to cardData[1]
                int compare;
                compare = this.cardData[choose] - nc.cardData[choose];
                return compare;
        }
        
//        //@Override
//        public boolean equals(NCard nc) {
//            boolean ret;
//            ret = false;
//            if(this.cardData[choose] == nc.cardData[choose]){
//                return true;
//            }
//            return ret;
//        }
    }
    
    /**
     * Shuffle the card deck arrays assigning a next card to each card in the 
     * deck.
     */
    public void shuffleDeck(){
        localShuffleDeck();
    }
    
    /**
     * Private version of shuffleDeck that is called in the Deck constructor.
     */
    private void localShuffleDeck(){
        int n;
        NCard card;
        NCard nextcard;
        ArrayList<NCard> NCardList = new ArrayList<>(); //ArrayList of NCards containing card data that can be sorted

        start_card = (int)(52.*Math.random()*0.999999999999999) + 1; // random number between 1 and 52
        /*
        *  This is the implementation of Math.random()
        *public static synchronized double random()
        *{
        *       if (rand == null)
        *          rand = new Random();
        *       return rand.nextDouble(); 
        *}
        *
        *  The Random() constructor for class Random "sets the seed of the 
        *  random number generator to a value very likely to be distinct from 
        *  any other invocation of this constructor".
        *
        */

        for (n = 1; n <= 52; n++) { //52 random numbers placed into Ncards to shuffle the deck
                NCardList.add(new NCard(n, (int)(10000000.*Math.random()), 0));
        }
        
        Collections.sort(NCardList); //sorts NCardList by random numbers
        for (n = 1; n <= 51; n++) { //finds next card in random list
                card = NCardList.get(n-1); //List of card data
                nextcard = NCardList.get(n);
                card.cardData[1] = card.cardData[0]; // sets the card number into the slot that is sorted for second sort
                card.cardData[2] = nextcard.cardData[0];  // sets next card data into the NCard
                NCardList.set(n-1, card); //sets card data in NCard list
        }
        card = NCardList.get(51); //wrap around for last card
        nextcard = NCardList.get(0);
        card.cardData[1] = card.cardData[0];
        card.cardData[2] = nextcard.cardData[0];
        NCardList.set(51, card);

        Collections.sort(NCardList); //second sort this time by card number
        
        for (n = 1; n <= 52; n++){ //get data from the NCardList into NextCard array
            //NextCards[n-1] = getNextCardNumLocal(n - 1);
            NextCards[n-1] = NCardList.get(n - 1).cardData[2];
            NextCardsArchive[n-1] = NextCards[n-1]; //sets up the archive so that the orginal can be recovered 
            CardsVis[n-1] = false; //turns off visibility for all cards
        }
    }
    
    /**
     * Read card images from disk into an array.
     */
    private void readCardImages(){
    int n;
        int suite;
        int rank;
        BufferedImage img;
        BufferedImage backImg = null;
        BufferedImage icon = null;
        String[] card_suites = {"s", "h", "c", "d"};
        String card_name; 
        
        try { //special images
            backImg = ImageIO.read(new File("CardImages\\back.jpg"));
            icon = ImageIO.read(new File("CardImages\\icon.jpg"));
            //System.out.println("Success");
        } catch (IOException e) {
                System.out.println("Solitaire Deck Image read Failed card_num = "+"back");
        }
        CARD_IMAGES[52] = backImg;
        CARD_IMAGES[53] = icon;
        
        for (n = 1; n <= 52; n++){ //card images
            rank = (n - 1)%13 + 1;
            suite = (int)(((double)n - .001)/13.);
            card_name = card_suites[suite] + rank + ".jpg"; //name of image file
            img = null;

            try {
                img = ImageIO.read(new File("CardImages\\" + card_name));
            } catch (IOException e) {
                System.out.println("Solitaire Deck Image read Failed card_num = "+n+", card name"+card_name);
            }
            CARD_IMAGES[n-1] = img;
        }
    }
    
    /**
     * Gets a card image from an array of card images.
     * @param cardNum - Number of the card for which an image is returned.
     * @return Return the selected image.
     */
    public BufferedImage getImage(int cardNum){
        BufferedImage img; 
        img = null;
        if((cardNum >= 1) && (cardNum <= 54)){
            img = CARD_IMAGES[cardNum - 1];
        } else {
            System.out.println("getImage- No such image "+cardNum);
        }
        return img;
    }
    
    /**
     * Gets the next card that follows the selected card.
     * @param cardNum - Selected card
     * @return - Return the next card
     */
    public int getNextCardNumber(int cardNum){
        int NextCardNumber;
        NextCardNumber = 0;
        if((cardNum > 0) && (cardNum <= 52)){
            NextCardNumber = NextCards[cardNum - 1];
        }
        return NextCardNumber;
    }
    
    /**
     * Changes the next card data for a selected card.
     * @param cardNum - Selected card
     * @param newNextCard - New number for the next card
     */
    public void setNextCardNumber(int cardNum, int newNextCard){
        if((cardNum > 0) && (cardNum <= 52)){
            NextCards[cardNum - 1] = newNextCard;
        }
    }
    
    /**
     * Gets the card visibility status of the selected card.
     * @param cardNum - Selected card
     * @return Return the visibility status.
     */
    public boolean getCardVis(int cardNum){
        boolean Vis;
        Vis = false;
        if((cardNum > 0) && (cardNum <= 52)){
            Vis = CardsVis[cardNum - 1];
        }
        return Vis;
    }
    
    /**
     * Sets the card visibility status for the selected card.
     * @param cardNum - Selected card
     * @param newVis - The new visibility status.
     */
    public void setCardVis(int cardNum, boolean newVis){
        if((cardNum > 0) && (cardNum <= 52)){
            CardsVis[cardNum - 1] = newVis;
        }
    }
    
    /**
     * Resets the next card and visibility data to the archive values.
     */
    public void resetDeck(){
        int n;
        //System.out.println("resetDeck");
        for(n = 0; n <= 51; n++){
            NextCards[n] = NextCardsArchive[n];
            CardsVis[n] = CardsVisArchive[n];
        }
    }
    
    /**
     * Gets the suite of the selected card.
     * @param cardNum - Selected card
     * @return Returns the suite 0 = spades, 1 = hearts, 2 = clubs, 3 = diamonds
     */
    int getSuite(int cardNum){ //integer between 0 and 3
        return (int)(((double)cardNum - .001)/13.); //rounds down
    }
    
    /**
     * Gets the rank of the selected card.
     * @param cardNum - Selected card
     * @return Returns the rank of the selected card.
     */
    int getRank(int cardNum){ //integer between 1 and 13
        return (cardNum - 1)%13 + 1; 
    }
    
    /**
     * Gets the color of the selected card.
     * @param cardNum - Selected card
     * @return Returns the color of the selected card. 0 is black, 1 is red.
     */
    int getColor(int cardNum){ //black is 0 red is 1
        return getSuite(cardNum)%2;
    }
}
