import java.lang.*;

public class Session {

    private String sessionID;  // identifiant unique de la session  => cookie
    private String playerName;
    private String gameState;
    private long startTime;
    private long duration;

    public Session(String ID) {
        this.sessionID = ID;
        this.playerName = "";
        this.gameState = "new";
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

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
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
