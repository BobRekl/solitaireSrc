/*
 * Solitaire Game - Card stacks.
 */
package solitaire;


/**
 * SolitaireStacks - Provides card stack set up and manipulation.
 * @author Bob Reklis
 */
public class SolitaireStacks {
    int[] STACKbottoms = new int[14];
    final int[] cardCount;
    public SolitaireDeck Deck;
    
    /**
     * Creates the card deck and then initializes card stacks by distributing 
     * cards.
     */
    SolitaireStacks(){
        int n;
        
        cardCount = new int[14]; //set the number of cards in each stack
        for(n = 1; n <= 7; n++){
            cardCount[n - 1] = n;
        }
        cardCount[7] = 24;
        
        for(n = 9; n <= 14; n++){
            cardCount[n - 1] = 0;
        }
        
        Deck = new SolitaireDeck(); //Create card shuffled card deck;
        localResetStacks(); //Set up card stacks.
    }
    
    /**
     * Adds cards to stack.
     * @param stackNum - The stack where cards are to be added. 
     * @param cardNum  - The bottom card of the card stack that is to be added.
     */
    void pushStack(int stackNum, int cardNum){
        int stackTop;
        
        if(isEmpty(stackNum)){
            STACKbottoms[stackNum-1] = cardNum;
            //Deck.setCardVis(cardNum, true); //turn the card visible
        } else {
            stackTop = getStackTop(stackNum);
            Deck.setNextCardNumber(stackTop, cardNum);
        }
        //System.out.println("pushStack"+ stackNum + "card number "+ cardNum +" New stack top = "+getStackTop(stackNum));
    }
    
    /**
     * Removes a stack of cards from a card stack.
     * @param stackNum - The stack from which cards are to be removed.
     * @param cardNum - The bottom card in the stack that is to be removed.
     */
    void popStack(int stackNum, int cardNum){
        int prevCard;
        
        prevCard = getPreviousCard(stackNum, cardNum); //get the card just under the cards that are to be removed
        switch (prevCard){
            case -1: case -2://error 
                break;
                
            case 0: //pop removes the last card or there are no cards on stack
                STACKbottoms[stackNum - 1] = 0;//stack empty
                //setStackBottom(stackNum, 0); 
                break;
                
            default:
                Deck.setCardVis(prevCard, true); //turn the new stack top visible
                Deck.setNextCardNumber(prevCard, 0); //now top of stack
                break;
        }
        //System.out.println("popStack "+ stackNum + " card number "+ cardNum +" New stack top = "+getStackTop(stackNum));
    }
    
    /**
     * Determine if stack is empty.
     * @param stackNum - Stack that is to be examined
     * @return - Return true if empty.
     */
    boolean isEmpty(int stackNum){
        boolean isempty;
        isempty = true;
        if((stackNum > 0) && (stackNum <= 14)){
            isempty = false;
            if(STACKbottoms[stackNum-1] <= 0) isempty = true;
        }
        return isempty;
    }
    
    /**
     * Find the top of the stack.
     * @param stackNum - Stack that is to be examined
     * @return - Return the top of the stack, 0 if empty, -1 if error
     */
    int getStackTop(int stackNum){
        int cardNum;
        int nextCardNum;
        
        cardNum = -1;
        if((stackNum > 0)&&(stackNum <= 14)){
            cardNum = STACKbottoms[stackNum - 1]; //stack bottom card is 0 if empty
            nextCardNum = Deck.getNextCardNumber(cardNum);
            while(nextCardNum > 0){
                cardNum = nextCardNum;
                nextCardNum = Deck.getNextCardNumber(cardNum);
            }
        }
        return cardNum;
    }
    
    /**
     * Finds the card just under the input card
     * @param stackNum - Stack that is to be examined
     * @param cardNumIn - The card that is to be searched for
     * @return - Return the card just under the input card. 0 if bottom of 
     * stack, -1 if error
     */
    int getPreviousCard(int stackNum, int cardNumIn){
        int nextCardNum;
        int cardNum;
        int switchControl;
        
        cardNum = -1;
        if((stackNum > 0)&&(stackNum <= 14)){
            cardNum = STACKbottoms[stackNum - 1]; //start at stack bottom
            switchControl = 0;
            if(cardNum == 0) switchControl = 1; //stack empty
            if(cardNum == cardNumIn) switchControl = 2; //cardNumIn points at stack bottom
            switch(switchControl){
                case 1: //Stack empty do nothing
                    break;
                case 2: //cardNumIn points at stack bottom return 0
                    cardNum = 0;
                    break;
                default: //search for cardNumIn
                    nextCardNum = Deck.getNextCardNumber(cardNum);
                    while((nextCardNum != cardNumIn)&&(cardNum > 0)){
                        cardNum = nextCardNum;
                        nextCardNum = Deck.getNextCardNumber(cardNum);
                    }
                    if(cardNum == 0) cardNum = -2; //reached stack top without finding cardNumIn
                    break;
            }
        }
        return cardNum;
    }
    
    /**
     * Find the number of cards in the stack.
     * @param stackNum - Stack that is to be examined
     * @return - Return the number of cards in the stack. 0 if empty, 
     * -1 if error
     */
    int getStackSize(int stackNum){
        int cardNum;
        int nextCardNum;
        int stackCount;
        
        stackCount = -1;
        if((stackNum > 0)&&(stackNum <= 14)){
            cardNum = STACKbottoms[stackNum - 1]; //start at bottom of the stack
            stackCount = 0;
            if(cardNum > 0) { //stack is not empty
                stackCount = 1;
                nextCardNum = Deck.getNextCardNumber(cardNum);
                while(nextCardNum > 0){ //search for stack top
                    cardNum = nextCardNum;
                    nextCardNum = Deck.getNextCardNumber(cardNum);
                    stackCount++;
                }
            }
        }
        return stackCount;
    }
    
    /**
     * Find the first visible card searching up from the bottom
     * @param stackNum - Stack that is to be examined
     * @return - Return the first visible card, 0 if stack empty, -1 if error, 
     * the top card must be visible
     */
    int getStackBottomVisible(int stackNum){
        int cardNum;
        boolean cardVis;
        int nextCardNum;
        
        cardNum = -1;
        if((stackNum > 0)&&(stackNum <= 14)){
            cardNum = STACKbottoms[stackNum - 1]; //start search at bottom
            if(cardNum > 0){ //stack is not empty
                if(stackNum > 7){ //upper stacks
                    cardNum = getStackTop(stackNum); //the top card is the only one showing
                } 
                else {//lower stacks
                    cardVis = Deck.getCardVis(cardNum); //determine card visibility
                    nextCardNum = Deck.getNextCardNumber(cardNum);
                    while((nextCardNum > 0)&&!cardVis){
                        cardNum = nextCardNum;
                        nextCardNum = Deck.getNextCardNumber(cardNum);
                        cardVis = Deck.getCardVis(cardNum);
                    }
                }
            }
        }
        return cardNum;
    }
    
    /**
     * Find the number of cards on a stack that are not visible.
     * @param stackNum - The stack to be examined
     * @return - Return the number of invisible cards, -1 if error
     */
    int getNotVisibleStackSize(int stackNum){
        int cardNum;
        boolean cardVis;
        int nextCardNum;
        int stackCount;
        
        stackCount = -1;
        if((stackNum > 0)&&(stackNum <= 14)){
            cardNum = STACKbottoms[stackNum - 1]; //start search at bottom
            stackCount = 0;
            if(cardNum > 0){ //stack not empty
                cardVis = Deck.getCardVis(cardNum);
                nextCardNum = Deck.getNextCardNumber(cardNum);
                while((nextCardNum > 0)&&!cardVis){
                    cardNum = nextCardNum;
                    nextCardNum = Deck.getNextCardNumber(cardNum);
                    cardVis = Deck.getCardVis(cardNum);
                    stackCount++;
                }
            }
        }
        return stackCount;
    }
    
    /**
     * Resets the stacks to condition at the start of the game and does not 
     * reshuffle the deck.
     */
    public void resetStacks(){
        localResetStacks();
    }
    
    /**
     * Private version of reset stacks that is called in the SolitaireStacks 
     * constructor.
     */
    private void localResetStacks(){
        int n;
        int k;
        int card;
        int nextCard;
        //System.out.println("resetStacks");
        Deck.resetDeck(); //Resets deck to version in the archive
        
        
        card = Deck.start_card;
        STACKbottoms[0] = card; //load stack 1 with start_card
        Deck.setCardVis(card, true);//set stack top visible
        
        nextCard = Deck.getNextCardNumber(card); //next card will be bottom of stack 2
        Deck.setNextCardNumber(card, 0); //set top of stack 1
        card = nextCard;
        for(n = 2; n<=8; n++){
            STACKbottoms[n-1] = card; //repeat for stacks 2 through 8
            for(k = 2; k <= cardCount[n-1]; k++){ //flip through number of cards in cardCount to find the next stack top
                card = Deck.getNextCardNumber(card);
            }
            Deck.setCardVis(card, true);//set stack top visible
            nextCard = Deck.getNextCardNumber(card); //nextCard will be the bottom of the next stack
            Deck.setNextCardNumber(card, 0);
            card = nextCard;
        }
        for(n = 9; n<=14; n++){ //Stacks 9 through 14 are empty
            STACKbottoms[n-1] = 0;
        }
    }
}
