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
    public final String TEAM = "CRosemont23";

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

        //updatePosi(prevMoves);
        for (int i = 1; i < oldPos.length; i++) {
            grille[oldPos[i][0]][oldPos[i][1]] = 'W';
            grille[newPos[i][0]][newPos[i][1]] = (char) ('0' + i);
        }
        // Choisis une direction au hasard, mais pas la direction précédente
        direction = directions[random.nextInt(directions.length)];

        System.out.println("Mouvement choisi : " + direction);

        printGrille();

        return directionSecondeAI(prevMoves);
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
        oldPos = newPos;
        if (prevMoves.length() == 0) {
            System.out.println("prevMoves.length() ==0");
            return;
        }
        for (int count = 1; count < prevMoves.length() + 1; count++) {
            char direction = prevMoves.getJSONObject(count - 1).getString("direction").charAt(0);
            if (direction == 'u') {
                newPos[count][0] -= 1;
            } else if (direction == 'd') {
                newPos[count][0] += 1;
            } else if (direction == 'l') {
                newPos[count][1] -= 1;
            } else if (direction == 'r') {
                newPos[count][1] += 1;
            } else {
                System.out.println("UNKNOWN CHARACTER:" + direction);
            }
        }
        System.out.println("newPos:" + Arrays.deepToString(newPos));
    }

    public char directionSecondeAI(JSONArray prevMoves) throws JSONException {
        for (int count = 1; count < prevMoves.length() + 1; count++) {
           if (direction == 'u') {
            if (grille[newPos[count][0] - 1][newPos[count][1]] == ' ') {
                newPos[count][0] -= 1;
                return 'u';
            }
            } else if (direction == 'd') {
                if (grille[newPos[count][0] + 1][newPos[count][1]] == ' ') {
                    newPos[count][0] += 1;
                    return 'd';
                }
            } else if (direction == 'l') {
                if (grille[newPos[count][0]][newPos[count][1] - 1] == ' ') {
                    newPos[count][1] -= 1;
                    return 'l';
                }
            } else if (direction == 'r') {
                if (grille[newPos[count][0]][newPos[count][1] + 1] == ' ') {
                    newPos[count][1] += 1;
                    return 'r';
                }
            }
        }

        System.out.println("UNKNOWN CHARACTER:" + direction);
        return 'X';

    }

}
