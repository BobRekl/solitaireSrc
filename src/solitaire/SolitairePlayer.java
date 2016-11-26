/*
 * Solitaire Game - SolitairePlayer class manipulates the stacks of cards.
 */
package solitaire;

import java.util.ArrayList;

/**
 * SolitairePlayer - SolitairePlayer class manipulates the stacks of cards.
 * @author Bob Reklis
 */
public class SolitairePlayer {
    //int stack_hit = 0; //The stack hit by a mouse click
    //int card_hit = 0; //The card hit by a mouse click.
    //public int card;
    public boolean toDisplay = true; //Toggle between the 2 halves of a move.  True when card moves to stack 10.
    public String announcement; //A message displayed at the top of the game panel
    public int last_card; //The card from the first half of the move
    public int last_stack; //The stack from the first half of the move
    public boolean fileExists;
    
    public SolitaireStacks Stacks;
    public ArrayList<SolitaireMove> solitaireMoves;
    public int stack10_display_card; //card to show on display stack
    public boolean prevCardVis;
    
    /**
     * SolitairePlayer - Creates card stacks and then makes, validates and stores the moves for the solitaire game.
     */
    SolitairePlayer(){
        solitaireMoves = new ArrayList<>(); //stack of all the moves made in the game
        int n;
//        int numMoves;
//        int nMoves;
//        int[] intMove  = new int[4];
        
        Stacks = new SolitaireStacks(); //Create new solitaire stacks
        
//        for(n = 1; n <= 14; n++){
//            prevCardVis[n - 1] = false;
//        }
    }
                    
    /**
     * Uses card hit and stack hit from the mouse click to record a move.
     * @param stack_hit_in - The stack clicked on by the mouse
     * @param card_hit_in - The card clicked on by the mouse
     */
    public void makeMove(int stack_hit_in, int card_hit_in){
        //System.out.println("Move: Stack "+stack_hit_in+", card "+card_hit_in);
        SolitaireMove solitaireMove;
        int stack_f; //move from stack_f
        int stack_t; //move to stack_t
        int move_score; //score for the move
        
        
        if(toDisplay){ // moves card to stack 10, the display stack
            if(card_hit_in > 0){
                stack_f = stack_hit_in;
                stack_t = 10;
                last_card = card_hit_in; //card and stack from first half of the move
                last_stack = stack_hit_in;
                prevCardVis = Stacks.Deck.getCardVis(Stacks.getPreviousCard(stack_f, card_hit_in));

                solitaireMove = new SolitaireMove(stack_f, stack_t, last_card, 0);
                solitaireMoves.add(solitaireMove); //records move

                toDisplay = false;
            }
        } else { // moves card from the display stack
            stack_f = 10;
            stack_t = stack_hit_in;
            
            if((last_stack == 8)&&(stack_hit_in == 8)) stack_t = 9;
            
            if(isValidMove(last_card, last_stack, stack_t)){ //checks to make sure it is a valid move
                move_score = scoreMove(last_stack, stack_t); //get score for the move
                solitaireMove = new SolitaireMove(stack_f, stack_t, last_card, move_score); //enters move data into SolitaireMove object
                solitaireMoves.add(solitaireMove); //records move
                setAnnouncement("");
            } else {
                popMovesStack(); //remove first half of the move
                setAnnouncement("Invalid Move");
                if(last_stack == stack_t){
                    setAnnouncement("");
                }
            }
            toDisplay = true;
        }
        executeMoves();
    }
        
    /**
     * Executes all of the moves in the moves stack.
     */
    public void executeMoves(){
        int n;
        int nMoves;
        int stack_f;
        int stack_t;
        int card_f;
        SolitaireMove solitaireMove;
        
        nMoves = solitaireMoves.size();
        //System.out.println("Execute "+ nMoves + " moves");
        Stacks.resetStacks();
        stack10_display_card = 0;
        if(nMoves > 0){
            card_f = 0;
            stack_t = 0;
            for(n = 1; n <= nMoves; n++){
                solitaireMove = solitaireMoves.get(n - 1);
                stack_f = solitaireMove.stackFrom;
                stack_t = solitaireMove.stackTo;
                card_f = solitaireMove.card;

                Stacks.popStack(stack_f, card_f); //stack_f or stack_t == 10 so one of these pairs does nothing
                Stacks.pushStack(10, card_f);

                Stacks.popStack(10, card_f);
                Stacks.pushStack(stack_t, card_f);
            }
            
            if(stack_t == 10) stack10_display_card = card_f; //show card_f on display stack even if it is not the stack top
        }
    }
    
    /**
     * Check move to ensure that the move follows the rules of the game.
     * @param card_from - Card selected in first half of the move.
     * @param stack_from - Stack from which card was taken in first half of the move.
     * @param stack_t - Stack to move card to.
     * @return true if move is valid.
     */
    private boolean isValidMove(int card_from, int stack_from, int stack_to){
        int card_to;
        boolean valid_move;
        
        //valid_move = false;
        card_to = Stacks.getStackTop(stack_to);
        switch (stack_to){ 
            case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                valid_move = false;
                if((Stacks.isEmpty(stack_to))&&(Stacks.Deck.getRank(card_from) == 13)){ //OK to move king to empty lower stack
                    valid_move = true;
                } else {
                    if((Stacks.Deck.getColor(card_to) != Stacks.Deck.getColor(card_from))&&
                            (Stacks.Deck.getRank(card_from) == Stacks.Deck.getRank(card_to) - 1)){ //OK to move card to lower stack of oposite color and lower rank
                        valid_move = true;
                    }
                    if(stack_from == stack_to){ // cannot return card to the same stack 
                        valid_move = false;
                    }
                }
                break;
            case 8:
                valid_move = false; // cannot move to the new card stack
                break;
            case 9:
                valid_move = true;
                if(stack_from != 8){ //cannot move to the discard pile from any other stack than the new card stack
                    valid_move = false;
                }
                break;
            case 11: case 12: case 13: case 14:
                if((Stacks.isEmpty(stack_to))&&(Stacks.Deck.getRank(card_from) == 1)){ //OK to move ace to an empty upper stack
                    valid_move = true;
                } else {
                    valid_move = false;
                    if(Stacks.Deck.getNextCardNumber(card_from) == 0){ //card must be stack top
                        if((Stacks.Deck.getRank(card_from) == Stacks.Deck.getRank(card_to) + 1) && 
                                (Stacks.Deck.getSuite(card_from) == Stacks.Deck.getSuite(card_to))){ //OK to move to a stack of same suite and lower rank 
                            valid_move = true;
                        }
                    }
                }
                break;
            default:
                valid_move = true; //OK to move to display stack.  Mouse clicks to display stack are rejected.
        }
        //valid_move = true;
        return valid_move;
    }
    
    /**
     * Remove move from move stack.
     */
    void popMovesStack(){
        int movesStackSize;
        
        movesStackSize = solitaireMoves.size();
        solitaireMoves.remove(movesStackSize - 1);
    }
    
    /**
     * Set announcement for display at top of game panel.
     * @param text is the string to set in the announcement.
     */
    public void setAnnouncement(String text){
        announcement = text;
    }
    
    /**
     * Determine score for move.
     * @param stack_f - Stack from which card was taken in first half of the move.
     * @param stack_t - Stack to which card is moved in second half of the move.
     * @return the computed score for the move.
     */
    int scoreMove(int stack_f, int stack_t){
        //Note that you lose 100 points if you recycle the deck but your score never goes negative.
        int score;
        score = 0;
        
        switch(stack_t){
            case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                switch(stack_f){
                    case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                        score = 0;
                        break;
                        
                    case 8: case 9:
                        score = 5; //score 5 for a move to the lower stacks from new card stack or discard stack 
                        break;
                        
                    case 11: case 12: case 13: case 14:
                        score = -10; //lose 10 points for a move from the upper stacks to the lower
                        break;
                }
                break;
                
            case 11: case 12: case 13: case 14: 
               switch(stack_f){
                    case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                        score = 10; //gain 10 points for a move from the lower stacks to the upper
                        break;
                        
                    case 8: case 9: //gain 10 points for a move from the new card stack or the discard stack
                        score = 10;
                        break;
                        
                    case 11: case 12: case 13: case 14:
                        score = 0;
                        break;
                }
                break;
                
            default:
                score = 0;
                break;
        }
        if(stack_f == stack_t) score = 0;
        
        return score;
    }
    
    /**
     * Compute the total score for all moves.
     * @return Return the computed score.
     */
    public int getTotalScore(){
        int score;
        int n;
        int nMoves;
        SolitaireMove solitaireMove;
        
        score = 0;
        nMoves = solitaireMoves.size();
        for(n = 1; n <= nMoves; n++){
            solitaireMove = solitaireMoves.get(n - 1);
            score = score + solitaireMove.score;
            if(score < 0) score = 0;
        }
        
        return score;
    }
    
    /**
     * Return the cards on the discard stack to the new card stack.
     */
    public void recycleDeck(){
        int stackSize;
        int n;
        int cardNum;
        SolitaireMove solitaireMove;
        
        stackSize = Stacks.getStackSize(9); //get size of the discard stack
        if(stackSize > 0){
            setAnnouncement("");
            for(n = 1; n <= stackSize; n++){
                cardNum = Stacks.getStackTop(9); //get top of discard stack
                solitaireMove = new SolitaireMove(9, 10, cardNum, 0); //move from discard stack to display stack
                solitaireMoves.add(solitaireMove);

                if((n == 1)||(n == stackSize)) { //first and last moves score -50.  This marks the range of moves that recycle the deck.
                    solitaireMove = new SolitaireMove(10, 8, cardNum, -50);
                } else {
                    solitaireMove = new SolitaireMove(10, 8, cardNum, 0);//move from display stack to new card stack
                }
                solitaireMoves.add(solitaireMove);

                executeMoves();
            }
        }
    }
    
    /**
     * Clear the moves stack so game starts over.
     */
    public void restartGame(){
        setAnnouncement("Same Game");
        Stacks.resetStacks();
        
        stack10_display_card = 0;
        toDisplay = true;
        solitaireMoves.clear();
    }
    
    /**
     * Clear the moves stack and shuffle deck so new game starts.
     */
    public void newGame(){
        setAnnouncement("New Game");
        Stacks.Deck.shuffleDeck();
        Stacks.resetStacks();
        stack10_display_card = 0;
        toDisplay = true;
        solitaireMoves.clear();
        
    }
    
    /**
     * Removes the last move from the move stack.
     */
    void unDo(){
        int moveNum;
        int moveCount;
        int stackSize;
        SolitaireMove solitaireMove;
        
        stackSize = solitaireMoves.size(); //get size of moves stack
        
        if(stackSize > 0){ //operate only if there are moves on the stack
            //stackSize = solitaireMoves.size();
            setAnnouncement("");
            solitaireMove = solitaireMoves.get(stackSize - 1); //get the last move
            
            if(solitaireMove.stackTo != 10){ //first half of move cycle not treated
                if(solitaireMove.score == -50){ //move was recycle the deck
                    moveCount = 3;
                    moveNum = stackSize - 1;
                    solitaireMove = solitaireMoves.get(moveNum - 1); //start at top of the stack
                    while(solitaireMove.score != -50){ //work back until the beginning of the deck recycle is reached
                        moveCount++;
                        moveNum--;
                        solitaireMove = solitaireMoves.get(moveNum - 1);
                    }
                    
                    for(moveNum = 1; moveNum <= moveCount; moveNum++){
                        popMovesStack(); //remove the required number of moves
                    }
                } else { //ordinary move
                    popMovesStack();
                    popMovesStack();
                }
                executeMoves();
            } else {
                setAnnouncement("Must complete move before Un Do");
            }
        }
    }
    
    /**
     * Searches for a place in the upper stacks to move the selected card and 
     * if found moves it.
     * @param stackNum - the stack holding the selected card
     * @param cardNum - the selected card
     * @return Returns true if a move is found, false otherwise
     */
    boolean autoMove(int stackNum, int cardNum){
        boolean found;
        int n;
        int top_stack;
        int best_top_stack;
        int top_top_stack;
        int top_rank;
        int top_suite;
        int card_rank;
        int card_suite;
        SolitaireMove solitaireMove;
        
        //System.out.println("stackNum = "+stackNum+", cardNum + "+cardNum);
        
        card_rank = Stacks.Deck.getRank(cardNum);
        card_suite = Stacks.Deck.getSuite(cardNum);
        //System.out.println("card_rank = "+card_rank+", card_suite = "+card_suite);
        
        found = false;
        if(cardNum == Stacks.getStackTop(stackNum)){
            top_suite = -1;
            top_stack = 10;
            while((top_stack < 14)&&!found){
                top_stack++;

                top_top_stack = Stacks.getStackTop(top_stack);
                top_rank = Stacks.Deck.getRank(top_top_stack);
                top_suite = Stacks.Deck.getSuite(top_top_stack);
                //System.out.println("top_rank = "+top_rank+", top_suite = "+top_suite);

                found = (top_rank == (card_rank - 1)) && (top_suite == card_suite) ||
                        ((card_rank == 1) && (Stacks.isEmpty(top_stack)));
            }

            if(found){
                best_top_stack = card_suite + 11;
                if((Stacks.isEmpty(best_top_stack)) && (card_rank == 1)){
                    top_stack = best_top_stack;
                }
                //System.out.println("card_rank = "+card_rank+", top_stack = "+top_stack+", best_top_stack = "+best_top_stack+", empty = "+Stacks.isEmpty(best_top_stack));
                solitaireMove = new SolitaireMove(stackNum, 10, cardNum, 0); //set up move and add to the move stack
                solitaireMoves.add(solitaireMove);
                solitaireMove = new SolitaireMove(10, top_stack, cardNum, 10);
                solitaireMoves.add(solitaireMove);

                executeMoves();
                setAnnouncement("");
            }
        }
        
        return found;
    }
    
    /**
     * Searches for and if found, executes a move to the upper scoring stacks.
     */
    boolean autoPlay(){
        boolean found;
        int nCase;
        int n;
        int top_stack;
        int bottom_stack;
        final int[] bottom_stacks = {1, 2, 3, 4, 5, 6, 7, 9}; //will check these stacks for posible cards to move
        int top_top_stack;
        int top_bottom_stack;
        int top_rank;
        int top_suite;
        int bottom_rank;
        int bottom_suite;
        SolitaireMove solitaireMove;
        
        n = 1;
        found = false;
        setAnnouncement("");
        while((n <= 2*8*4)&&!found){ //2 cases to check, 8 candidate stacks to move from, 4 candidate stacks to move to
            top_stack = (n - 1)%4 + 11; //decode n to determine candidate stack to move to
            bottom_stack = bottom_stacks[((n - 1)/4)%8]; //decode n to determine candidate stack to move from
            nCase = ((n - 1)/(4*8))%2 + 1; //decode n to determne the case number
            
            n++;
            
            top_top_stack = Stacks.getStackTop(top_stack);//get rank and suite of target card to move onto
            top_rank = Stacks.Deck.getRank(top_top_stack);
            top_suite = Stacks.Deck.getSuite(top_top_stack);

            top_bottom_stack = Stacks.getStackTop(bottom_stack);//get rank and suite for target card to move up
            bottom_rank = Stacks.Deck.getRank(top_bottom_stack);
            bottom_suite = Stacks.Deck.getSuite(top_bottom_stack);
            
            if(nCase == 1){ //case 1 search posibility of moving ace up to an empty spot
                if((top_top_stack == 0)&&(bottom_rank == 1)){ //ace can move to empty slot
                    found = true;
                    solitaireMove = new SolitaireMove(bottom_stack, 10, top_bottom_stack, 0); //set up move and add to the move stack
                    solitaireMoves.add(solitaireMove);
                    solitaireMove = new SolitaireMove(10, top_stack, top_bottom_stack, 10);
                    solitaireMoves.add(solitaireMove);

                    executeMoves();
                }
            }else{ //case 2 search for card to move to stack that already has a card
                if((top_suite == bottom_suite)&&(top_rank == bottom_rank - 1)){ //can move card up
                    found = true;
                    solitaireMove = new SolitaireMove(bottom_stack, 10, top_bottom_stack, 0);
                    solitaireMoves.add(solitaireMove);
                    solitaireMove = new SolitaireMove(10, top_stack, top_bottom_stack, 10);
                    solitaireMoves.add(solitaireMove);

                    executeMoves();
                }
            }
        }
        return found;
    }
    
    /**
     * Determines if victory has been achieved.
     * @return true if victorious.
     */
    boolean isVictory(){
        boolean victory;
        int stackNum;
        
        victory = true; //victory means that only the upper stacks have cards
        for(stackNum = 1; stackNum <= 10; stackNum++){
            if(!Stacks.isEmpty(stackNum)) victory = false;
        }
        return victory;
    }
}
