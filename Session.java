import java.lang.*;
import java.util.*;

public class Session {

    private String sessionID;  // identifiant unique de la session  => cookie
    private String playerName;
    private List<char[]> gameState;
    private Boolean IsInitialized;
    private int UnrevealedCell;
    private long startTime;
    private long duration;

    public Session(String ID) {
        this.sessionID = ID;
        this.playerName = "";
        IsInitialized = false;
        UnrevealedCell = 49;
        this.gameState = new ArrayList<>();
        for(int i = 0; i < 49; i++) {
            gameState.add(new char[] { '0' , 'H'});
        }
        this.startTime = System.currentTimeMillis();
        this.duration = 0;
    }

    //getters et setters
    public String getSessionId() {
        return sessionID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public  List<char[]> getGameState() {
        return gameState;
    }

    public void setGameState(List<char[] > gameState) {
        this.gameState = gameState;
    }

    public Boolean getIsInitialized() {
        return IsInitialized;
    }

    public void setIsInitialized(Boolean val) {
        IsInitialized = val;
    }

    public int getUnrevealedCell(){
        return UnrevealedCell;
    }

    public void setUnrevealedCell(int val){
        UnrevealedCell = val;
    }

    public long getDuration() {
        updateDuration(); // Mettre à jour la durée avant de la renvoyer
        return duration;
    }

    private void updateDuration() {
        // Calcul de la durée en secondes
        this.duration = (System.currentTimeMillis() - startTime) / 1000;
    }
    
}
