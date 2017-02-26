package cybercycles;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AI {

    /* Configuration */
    public final String ROOM = "password";
    public final String TEAM = "CRosemont";

    /* Déplacement de l'A.I. */
    public final char[] directions = {'u', 'l', 'd', 'r'};
    public char direction;

    Random random = new Random();
    
    int me;

    /* Grille */
    char[][] grille;

    /*Positions des joueurs*/
    int[][] oldPos = new int[5][2];
    int[][] newPos = new int[5][2];

    /**
     * Fonction appelée en début de partie.
     *
     * @param config Configuration de la grille de jeu
     * @throws org.json.JSONException
     */
    public void start(JSONObject config) throws JSONException {
        InitialisationOfGame(config);
    }

    /**
     * Fonction appelée à chaque tour de jeu.
     *
     * @param prevMoves Mouvements précédents des joueurs
     * @return Mouvement à effectuer
     * @throws org.json.JSONException
     */
    public char next(JSONArray prevMoves) throws JSONException {
        System.out.print("Mouvements précédents : ");
        
        for (int i = 0; i < prevMoves.length(); i++) {
            JSONObject prevMove = prevMoves.getJSONObject(i);
            System.out.print(prevMove + " ");
        }

        System.out.print("\n");
        updatePosi(prevMoves);
        for(int i =1; i<oldPos.length;i++){
            int old0 = clamp(oldPos[i][0],grille.length);
            int old1 = clamp(oldPos[i][1],grille[0].length);
            int new0 = clamp(newPos[i][0],grille.length);
            int new1 = clamp(newPos[i][1],grille[0].length); 
            if(old0<0)old0=0;
            if(old1<0)old1=0;
            if(new0<0)new0=0;
            if(new1<0)new1=0;
            
            grille[old0][old1]='W';
            grille[new0][new1]=(char) ('0'+i);
        }
        // Choisis une direction au hasard, mais pas la direction précédente
        //direction = directions[random.nextInt(directions.length)];
        //directionSecondeAI(prevMoves);
        System.out.println("Mouvement choisi : " + direction);
        
        printGrille();
        
        return direction;
    }

    /**
     * Fonction appelée en fin de partie.
     *
     * @param winnerID ID de l'équipe gagnante
     */
    public void end(String winnerID) {
        System.out.println("Équipe gagnante : " + winnerID);
    }

    private void InitialisationOfGame(JSONObject config) throws JSONException {
        final JSONArray players = config.getJSONArray("players");

        final JSONArray s = config.getJSONArray("obstacles");

        final int width = config.getInt("w");
        final int height = config.getInt("h");

        me = Integer.parseInt(config.getString("me"));

        System.out.println("Joueurs : " + players);

        System.out.println("Obstacles : " + s);

        System.out.print("Taille de la grille : ");

        System.out.println(width + " x " + height);

        System.out.println("Votre identifiant : " + me);

        /*Building the grid*/
        grille = new char[height][width];
        for (int rw = 0; rw < grille.length; rw++) {
            Arrays.fill(grille[rw], ' ');
        }

        /*Obstacle*/
        for (int i = 0; i < s.length(); i++) {
            JSONObject obstacle = s.getJSONObject(i);
            int posy = clamp(Integer.parseInt(obstacle.getString("y")), height);
            int posx = clamp(Integer.parseInt(obstacle.getString("x")), width);
            int obHeight = Integer.parseInt(obstacle.getString("h"));
            int obWidth = Integer.parseInt(obstacle.getString("w"));
            for (int j = posy; j < clamp(posy + obHeight, height); j++) {
                for (int k = posx; k < clamp(posx + obWidth, width); k++) {
                    grille[j][k] = 'X';
                }
            }
            System.out.println("i de obstacles:" + i);
        }

        for (int i = 0; i < players.length(); i++) {
            JSONObject player = players.getJSONObject(i);
            int posy = clamp(Integer.parseInt(player.getString("y")), height);
            int posx = clamp(Integer.parseInt(player.getString("x")), width);
            grille[posy][posx] = (char) ((i + 1) + '0');
            oldPos[i + 1][0] = posy;
            oldPos[i + 1][1] = posx;
            System.out.println("pos de" + (i + 1) + ":" + posy + ", " + posx);
        }
        for (int i = 0; i < oldPos[0].length; i++){
            for (int j = 0; j < oldPos[1].length; j++){
                newPos[i][j] = oldPos[i][j];
            }
        }

        printGrille();

    }

    private void printGrille() {
        for (int rangee = 0; rangee < grille.length; rangee++) {
            System.out.println(Arrays.toString(grille[rangee]));
        }
    }

    public static int clamp(int val, int max) {
        return Math.max(0, Math.min(max, val));
    }

    /**
     * Méthode qui met à jour les coordonnées des joueurs
     */
    public void updatePosi(JSONArray prevMoves) throws JSONException {
        for (int i = 0; i < oldPos[0].length; i++){
            for (int j = 0; j < oldPos[1].length; j++){
                oldPos[i][j] = newPos[i][j];
            }
        }
        if (prevMoves.length() == 0) {
            System.out.println("prevMoves.length() ==0");
            return;
        }
        for (int i = 1; i < prevMoves.length(); i++) {
            char direction = prevMoves.getJSONObject(i - 1).getString("direction").charAt(0);
            if (direction == 'u') {
                newPos[i][0] -= 1;
            }
            else if (direction == 'd') {
                newPos[i][0] += 1;
            }
            else if (direction == 'l') {
                newPos[i][1] -= 1;
            }
            else if (direction == 'r') {
                newPos[i][1] += 1;
            }
            else{
                System.out.println("UNKNOWN CHARACTER:"+direction);
            }
        }
        System.out.println("newPos:" + Arrays.deepToString(newPos));
    }
    
    /*public char directionSecondeAI(JSONArray prevMoves) throws JSONException{
        direction = directions[random.nextInt(directions.length)];            
            char direction = prevMoves.getJSONObject(me).getString("direction").charAt(0);
            if (direction == 'u') {
                if(grille[newPos[me][0]-1][newPos[me][1]]==' '){
                    return 'u';
                }
            }
            else if (direction == 'd') {
                if(grille[newPos[me][0]+1][newPos[me][1]]==' '){
                    return 'd';
                } 
            }
            else if (direction == 'l') {
                if(grille[newPos[me][0]][newPos[me][1]-1]==' '){
                    return 'l';
                }
            }
            else if (direction == 'r') {
                if(grille[newPos[me][0]][newPos[me][1]+1]==' '){
                    return 'r';
                }
            }
            else{
                System.out.println("UNKNOWN CHARACTER:"+direction);
            }
        
        
        return direction;
    }*/
    

}
