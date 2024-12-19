import java.util.*;


public class ProtocolMP{

    public Session session;

    ProtocolMP(Session s){
        this.session = s;
    }

    public int TryWS(int x, int y){

        int choice = (x*7) + y;

        if(session.getIsInitialized() == false){
            Initialisation(choice);
            session.setIsInitialized(true);
        }

        char value = session.getGameState().get(choice)[0];
        List<char[]> game = session.getGameState();
        if(value == 'B')
        {
            for(int i = 0; i<49; i++){
                game.get(i)[1] = 'S';
            }
            session.setGameState(game);
            return 1;
        }
        else if(game.get(choice)[1] != 'S'){

            if(value == '0'){
                game.get(choice)[1] = 'S';
                revealZeroCell(game, x, y);
                session.setGameState(game);
                if(session.getUnrevealedCell() <= 7)
                    return 2;
                else
                    return 0;
            }
            else{
                session.getGameState().get(choice)[1] = 'S';
                session.setUnrevealedCell(session.getUnrevealedCell() - 1);
                if(session.getUnrevealedCell() <= 7)
                    return 2;
                else
                    return 0;
            }
        }
        else
            return 0;

        
    }

    public void FlagWS(int x, int y){
        int choice = x * 7 + y;

        List<char[]> game = session.getGameState();

        if(game.get(choice)[1] == 'S')
            {
                return ;
            } 
            else if(game.get(choice)[1] == 'H') {
                game.get(choice)[1] = 'F';
                
            }
            else{
                game.get(choice)[1] = 'H';
                
            }
            session.setGameState(game);
    }

    private void Initialisation(int choice) {
        try {
            List<char[]> game = new ArrayList<>();
    
            // Initialisation du jeu : créer un tableau de 49 cellules
            for (int i = 0; i < 49; i++) {
                game.add(new char[]{'0', 'H'}); // Par défaut, chaque case est vide ('0') et cachée ('H')
            }
    
            // Génération des bombes
            Random random = new Random();
            int i = 0;
            while (i < 7) { // 7 bombes
                int nombre = random.nextInt(49);
                if (nombre != choice && game.get(nombre)[0] != 'B') {
                    game.get(nombre)[0] = 'B'; // Placer une bombe
                    i++;
                }
            }
    
            // Mise à jour des cellules adjacentes aux bombes
            for (int j = 0; j < 49; j++) {
                char[] emplacement = game.get(j);
                int BombNumber = 0;
    
                if (emplacement[0] != 'B') { // Si ce n'est pas une bombe
                    // Vérifier les cases adjacentes
                    if (j > 6 && game.get(j - 7)[0] == 'B') BombNumber++; // Haut
                    if (j > 6 && j % 7 < 6 && game.get(j - 6)[0] == 'B') BombNumber++; // Haut droite
                    if (j > 6 && j % 7 > 0 && game.get(j - 8)[0] == 'B') BombNumber++; // Haut gauche
                    if (j % 7 > 0 && game.get(j - 1)[0] == 'B') BombNumber++; // Gauche
                    if (j % 7 < 6 && game.get(j + 1)[0] == 'B') BombNumber++; // Droite
                    if (j < 42 && j % 7 > 0 && game.get(j + 6)[0] == 'B') BombNumber++; // Bas gauche
                    if (j < 42 && game.get(j + 7)[0] == 'B') BombNumber++; // Bas
                    if (j < 42 && j % 7 < 6 && game.get(j + 8)[0] == 'B') BombNumber++; // Bas droite
    
                    if (BombNumber > 0) {
                        emplacement[0] = (char) ('0' + BombNumber); // Mettre le nombre de bombes adjacentes
                    }
                }
            }
    
            // Enregistrer l'état du jeu dans la session
            session.setGameState(game);
    
        } catch (Exception e) {
            System.err.println("Error Try WS Init: " + e);
        }
    }
    
   

    public void revealZeroCell(List<char[]> game, int line, int row){
        

        int cell = line*7 + row;
        
        
        if(game.get(cell)[1] == 'S')
            return;


        if(game.get(cell)[0] != '0')
        {
            game.get(cell)[1] = 'S';
            session.setUnrevealedCell(session.getUnrevealedCell() -1);
            
        }
        else
        {
            game.get(cell)[1] = 'S';
            session.setUnrevealedCell(session.getUnrevealedCell() -1);
            
            //up
            if(line>0)
            revealZeroCell(game, line-1, row);
            //up left
            if(line>0 && row>0)
                revealZeroCell(game,line-1, row-1);
            //up right
            if(line>0 && row < 6)
                revealZeroCell(game,line-1, row+1);

            //left
            if(row>0)
                revealZeroCell(game,line, row-1);
            //right
            if(row <6)
                revealZeroCell(game, line, row+1);
            //down
            if(line < 6)
                revealZeroCell(game, line+1, row);
            //down left
            if(line <6 && row >0)
                revealZeroCell(game, line+1, row-1);
            //down right
            if(line <6 && row < 6)
                revealZeroCell(game, line+1, row+1);
            
        }

    }
}