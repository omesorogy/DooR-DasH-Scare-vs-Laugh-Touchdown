package game.gui.model;

import game.engine.Game;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.monsters.*;
import game.engine.cards.Card;
import game.engine.exceptions.*;

import java.io.IOException;
import java.util.ArrayList;

public class GameModel {

    private Game game;
    private String lastLogEntry   = "";
    private String lastCardDrawn  = "";
    private String lastCardType   = "";
    private String lastCardEffect = "";
    private int    lastDiceRoll   = 0;
    private boolean multiplayer   = false;

    public GameModel(Role playerRole, boolean multiplayer) throws IOException {
        this.game = new Game(playerRole);
        this.multiplayer = multiplayer;
    }

    public Game    getGame()       { return game; }
    public Monster getPlayer()     { return game.getPlayer(); }
    public Monster getOpponent()   { return game.getOpponent(); }
    public Monster getCurrent()    { return game.getCurrent(); }
    public boolean isMultiplayer() { return multiplayer; }

    public int    getLastDiceRoll()  { return lastDiceRoll; }
    public String getLastLogEntry()  { return lastLogEntry; }
    public String getLastCardDrawn() { return lastCardDrawn; }
    public String getLastCardType()  { return lastCardType; }
    public String getLastCardEffect(){ return lastCardEffect; }

    public void clearCardInfo() {
        lastCardDrawn  = "";
        lastCardType   = "";
        lastCardEffect = "";
    }

    /**
     * Executes one game turn.
     * Returns:
     *   "FROZEN"  - current monster is frozen, turn skipped automatically
     *   "INVALID" - destination occupied, player must re-roll
     *   null      - turn completed normally
     */
    public String playTurn() {
        lastCardDrawn  = "";
        lastCardType   = "";
        lastCardEffect = "";

        Monster current  = game.getCurrent();
        Monster opponent = (current == game.getPlayer())
            ? game.getOpponent() : game.getPlayer();

        // --- Frozen check ---
        if (current.isFrozen()) {
            lastLogEntry = current.getName() + " is FROZEN and skips their turn!";
            try {
                game.playTurn();
            } catch (InvalidMoveException e) {
                // Expected when frozen - turn switches normally
            } catch (Exception e) {
                lastLogEntry += " [" + e.getClass().getSimpleName() + "]";
            }
            return "FROZEN";
        }

        // Snapshot state before turn
        int posBefore       = current.getPosition();
        int energyBefore    = current.getEnergy();
        int oppPosBefore    = opponent.getPosition();
        int oppEnergyBefore = opponent.getEnergy();

        // Reset so a card from a previous turn never bleeds into this one
        game.resetLastDrawnCard();

        try {
            game.playTurn();
        } catch (InvalidMoveException e) {
            // Read the roll the engine actually used before rejecting the move
            lastDiceRoll = game.getLastDiceRoll();
            lastLogEntry = current.getName() + " rolled " + lastDiceRoll
                + " - cell occupied! Roll again.";
            return "INVALID";
        }
        // Any other RuntimeException propagates to controller which handles it safely.

        // Read the single roll that the engine actually used — no separate roll here
        lastDiceRoll = game.getLastDiceRoll();

        int posAfter       = current.getPosition();
        int energyAfter    = current.getEnergy();
        int oppPosAfter    = opponent.getPosition();
        int oppEnergyAfter = opponent.getEnergy();

        // --- Card detection: read the actual card the engine drew, no guessing ---
        game.engine.cards.Card drawnCard = game.getLastDrawnCard();
        // Only count it if it was drawn THIS turn (board resets lastDrawnCard each draw,
        // so we check whether a CardCell was actually landed on by comparing positions
        // and checking if the card is non-null and was the result of landing on a CardCell)
        Cell[][] cells = game.getBoard().getBoardCells();
        boolean cardLanded = false;
        if (drawnCard != null) {
            // Check if the cell at posAfter OR any cell in the plausible movement range
            // is a CardCell — meaning the engine actually triggered a draw this turn
            for (int step = 1; step <= lastDiceRoll * 3; step++) {
                int checkIdx = (posBefore + step) % 100;
                int r = indexToRow(checkIdx), c = indexToCol(checkIdx);
                if (cells[r][c] instanceof CardCell) {
                    cardLanded = true;
                    break;
                }
            }
        }

        if (cardLanded) {
            lastCardDrawn  = drawnCard.getName();
            lastCardType   = inferCardType(lastCardDrawn);
            lastCardEffect = buildCardEffectDescription(
                current, opponent,
                posBefore, posAfter, energyBefore, energyAfter,
                oppPosBefore, oppPosAfter, oppEnergyBefore, oppEnergyAfter);
        }

        lastLogEntry = buildLog(current, posBefore, posAfter, energyBefore, energyAfter);
        return null;
    }

    private String inferCardType(String name) {
        if (name == null) return "Card";
        switch (name) {
            case "Position Swap":      return "Position Swap";
            case "Contamination Code":
            case "2319 Alert":         return "Start Over";
            case "Small Snatcher":
            case "Sneaky Thief":
            case "Mega Drain":         return "Energy Steal";
            case "Super Shield":       return "Shield";
            case "Mind Scramble":
            case "Total Confusion":    return "Confusion";
            default:                   return "Card";
        }
    }

    private String buildCardEffectDescription(
        Monster current, Monster opponent,
        int pb, int pa, int eb, int ea,
        int oppPb, int oppPa, int oppEb, int oppEa)
    {
        StringBuilder sb = new StringBuilder();

        // Current position change
        if (pa != pb) {
            if (pa == 0)
                sb.append(current.getName()).append(" was sent back to START! ");
            else if (pa < pb)
                sb.append(current.getName()).append(" moved back to cell ").append(pa).append("! ");
            else if (pa > pb + 6)
                sb.append(current.getName()).append(" launched forward to cell ").append(pa).append("! ");
        }

        // Opponent position change
        if (oppPa != oppPb) {
            if (oppPa == 0)
                sb.append(opponent.getName()).append(" was sent back to START! ");
            else if (oppPa < oppPb)
                sb.append(opponent.getName()).append(" moved back to cell ").append(oppPa).append("! ");
        }

        // Current energy change
        if (ea != eb) {
            int diff = ea - eb;
            sb.append(current.getName())
              .append(diff > 0 ? " gained " : " lost ")
              .append(Math.abs(diff)).append(" energy. ");
        }

        // Opponent energy change
        if (oppEa != oppEb) {
            int diff = oppEa - oppEb;
            sb.append(opponent.getName())
              .append(diff > 0 ? " gained " : " lost ")
              .append(Math.abs(diff)).append(" energy. ");
        }

        // Status changes
        if (current.isShielded())   sb.append(current.getName()).append(" now has a SHIELD. ");
        if (current.isConfused())   sb.append(current.getName()).append(" is CONFUSED for ")
            .append(current.getConfusionTurns()).append(" turn(s). ");
        if (opponent.isConfused())  sb.append(opponent.getName()).append(" is CONFUSED for ")
            .append(opponent.getConfusionTurns()).append(" turn(s). ");

        // Position swap detection
        if (pa == oppPb && pb == oppPa && pa != pb)
            sb.append("Positions were SWAPPED! ");

        if (sb.length() == 0) sb.append("Card effect was applied.");
        return sb.toString().trim();
    }

    private String buildLog(Monster m, int pb, int pa, int eb, int ea) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append(" -> Cell ").append(pa);
        if (ea != eb) {
            int diff = ea - eb;
            sb.append(" | Energy ").append(diff > 0 ? "+" : "").append(diff);
            sb.append(" (now ").append(ea).append(")");
        }
        return sb.toString();
    }

    public String usePowerup() {
        try {
            game.usePowerup();
            lastLogEntry = game.getCurrent().getName() + " activated their powerup!";
            return null;
        } catch (OutOfEnergyException e) {
            return "Not enough energy! Need 500 to activate powerup.";
        } catch (Exception e) {
            return "Powerup error: " + e.getMessage();
        }
    }

    public Monster getWinner()               { return game.getWinner(); }
    public Cell[][] getBoardCells()           { return game.getBoard().getBoardCells(); }
    public ArrayList<Monster> getAllMonsters(){ return game.getAllMonsters(); }

    public int indexToRow(int index) {
        return index / 10;
    }

    public int indexToCol(int index) {
        int row = index / 10;
        int col = index % 10;
        return (row % 2 == 0) ? col : (9 - col);
    }

    public int cellIndexAt(int row, int col) {
        int base = row * 10;
        return (row % 2 == 0) ? base + col : base + (9 - col);
    }
}
