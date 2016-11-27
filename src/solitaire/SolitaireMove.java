/*
 * Solitaire Game - Data encapsulating a move.
 */
package solitaire;

/**
 * SolitaireMove - Encapsulates move data, card moved, stack from, stack to, 
 * and score for making the move.
 * @author Bob Reklis
 */
public class SolitaireMove {
    int card = 0;
    int stackFrom = 0;
    int stackTo = 0;
    int score = 0;
    
    /**
     * Inserts move related data into the move object.
     * @param stack_f - Stack from which card moves.
     * @param stack_t - Stack to which card moves.
     * @param card_in - Bottom card of the stack of cards that is moving.  All cards above it move with it.
     * @param score_in - Score for making the move
     */
    SolitaireMove(int stack_f, int stack_t, int card_in, int score_in){
        card = card_in;
        stackFrom = stack_f;
        stackTo = stack_t;
        score = score_in;
    }
}
