package cybercycles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AI {

    /* Configuration */
    public final String ROOM = "password";
    public final String TEAM = "Crosemont";

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
    ArrayList<Integer> deadPlayers = new ArrayList<Integer>();

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
        
        /*Essai 
        Thread findDirection =new Thread(){
            public void run(){
                direction = shyAI();
            }
        };findDirection.start();*/
        
        System.out.print("Mouvements précédents : ");

        for (int i = 0; i < prevMoves.length(); i++) {
            JSONObject prevMove = prevMoves.getJSONObject(i);
            System.out.print(prevMove + " ");
        }

        System.out.print("\n");
        updatePosi(prevMoves);
        for (int i = 1; i < oldPos.length; i++) {
            int old0 = clamp(oldPos[i][0], grille.length-1);
            int old1 = clamp(oldPos[i][1], grille[0].length-1);
            int new0 = clamp(newPos[i][0], grille.length-1);
            int new1 = clamp(newPos[i][1], grille[0].length-1);
            if (old0 < 0) {
                old0 = 0;
            }
            if (old1 < 0) {
                old1 = 0;
            }
            if (new0 < 0) {
                new0 = 0;
            }
            if (new1 < 0) {
                new1 = 0;
            }

            grille[old0][old1] = 'W';
            grille[new0][new1] = (char) ('0' + i);
        }
        
        //direction = shyAI();
        //directionSecondeAI(prevMoves);
        //direction =directions[random.nextInt(directions.length)];
        direction =
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
            int id =Integer.parseInt(player.getString("id"));
            int posy = clamp(Integer.parseInt(player.getString("y")), height);
            int posx = clamp(Integer.parseInt(player.getString("x")), width);
            if (grille[posy][posx] == ' ') {
                grille[posy][posx] = (char) ((id + 1) + '0');
            } else if (grille[posy][posx] != ' ') {
                deadPlayers.add(id);
            }
            oldPos[i + 1][0] = posy;
            oldPos[i + 1][1] = posx;
            System.out.println("pos de" + (i + 1) + ":" + posy + ", " + posx);
        }
        for (int i = 0; i < oldPos.length; i++) {
            newPos[i][0] = oldPos[i][0];
            newPos[i][1] = oldPos[i][1];
        }

        printGrille();

    }

    private void printGrille() {
        for (int rangee = 0; rangee < grille.length; rangee++) {
            System.out.println(Arrays.toString(grille[rangee]));
        }
    }

    /**
     *
     * @param name the value of name
     */
    protected void printArray(final String name, final int[][] array) {
        System.out.println(name + Arrays.deepToString(array));
    }

    public static int clamp(int val, int max) {
        return Math.max(0, Math.min(max, val));
    }

    /**
     * Méthode qui met à jour les coordonnées des joueurs
     */
    public void updatePosi(JSONArray prevMoves) throws JSONException {
        for (int i = 0; i < 5; i++) {
            oldPos[i][0] = newPos[i][0];
            oldPos[i][1] = newPos[i][1];
        }
        printArray("newPos:", newPos);
        printArray("oldPos:", oldPos);

        if (prevMoves.length()== 0) {
            System.out.println("prevMoves.length() ==0");
            return;
        }
        for (int i = 1; i < 5 - deadPlayers.size(); i++) {
            //If player is dead, stop updating it.
            if (deadPlayers.contains(i)) {
                i++;
            }
            try {
                char direction = prevMoves.getJSONObject(i - 1).getString("direction").charAt(0);

                if (direction == 'u') {
                    newPos[i][0] -= 1;
                } else if (direction == 'd') {
                    newPos[i][0] += 1;
                } else if (direction == 'l') {
                    newPos[i][1] -= 1;
                } else if (direction == 'r') {
                    newPos[i][1] += 1;
                } else {
                    System.out.println("UNKNOWN CHARACTER:" + direction);
                }
            } catch (JSONException e) {
                System.err.println(e.toString());
            }

        }
        printArray("newPos:", newPos);
    }
    
    //Tentative d'AI
    public char directionSecondeAI(JSONArray prevMoves){
        try{
            direction = directions[random.nextInt(directions.length)];
                switch (direction) {
                    case 'u':
                        if (grille[newPos[me][0] - 1][newPos[me][1]] == ' ') {
                            newPos[me][0] -= 1;
                            return 'u';
                        }      
                    case 'd':
                        if (grille[newPos[me][0] + 1][newPos[me][1]] == ' ') {
                            newPos[me][0] += 1;
                            return 'd';
                        }  
                    case 'l':
                        if (grille[newPos[me][0]][newPos[me][1] - 1] == ' ') {
                            newPos[me][1] -= 1;
                            return 'l';
                        }  
                    case 'r':
                        if (grille[newPos[me][0]][newPos[me][1] + 1] == ' ') {
                            newPos[me][1] += 1;
                            return 'r';
                        }   
                }
        }catch(Exception e){
            System.err.println(e);
            //directionSecondeAI(prevMoves);
        }

        System.out.println("UNKNOWN CHARACTER:" + direction);
        return 'X';

    }
    
    
    /**
     * Méthode qui retourne le nombre de cases vides au haut du personnage
     * @return nombre de cases vides au haut
     */
    public int upFree(){
        int casesVides=0;
        int countVariationDePosition=1;
        while(grille[clamp(newPos[me][0] - countVariationDePosition,grille.length-1)][newPos[me][1]] == ' '){
            casesVides++;
            countVariationDePosition++;
        }
        return casesVides;
    }
    
    /**
     * Méthode qui retourne le nombre de cases vides au dessous du personnage
     * @return nombre de cases vides au bas
     */
    public int downFree(){
       int casesVides=0;
       int countVariationDePosition=1; 
       while(grille[clamp(newPos[me][0] + countVariationDePosition,grille.length-1)][newPos[me][1]] == ' '){
           casesVides++;
           countVariationDePosition++;
       }
       return casesVides;
    }
    
    /**
     * Méthode qui retourne le nombre de cases vides à gauche du personnage
     * @return nombre de cases vides à gauche
     */
    public int leftFree(){
       int casesVides=0;
       int countVariationDePosition=1; 
       while(grille[newPos[me][0]][clamp(newPos[me][1] - countVariationDePosition,grille.length-1)] == ' '){
           casesVides++;
           countVariationDePosition++;
       }
       return casesVides; 
    }
    
    /**
     * Méthode qui retourne le nombre de cases vides à droite du personnage
     * @return nombre de cases vides à droite
     */
    public int rightFree(){
       int casesVides=0;
       int countVariationDePosition=1; 
       while(grille[newPos[me][0]][clamp(newPos[me][1] + countVariationDePosition,grille.length-1)] == ' '){
           casesVides++;
           countVariationDePosition++;
       }
       return casesVides; 
    }
    
    /**
     * Déduction de la direction que devrait prendre l'AI peureuse (shyAI)
     * @return direction sécuritaire pour l'AI
     */
    public char safestPath(){
        char safeWay='*'; //Valeur si toutes les directions sont autant sécuritaires
        int max=Integer.MIN_VALUE;
        if(upFree()>max){
            max=upFree();
            safeWay = 'u';
        }
        if(downFree()>max){
            max=downFree();
            safeWay = 'd';
        }
        if(leftFree()>max){
            max=leftFree();
            safeWay = 'l';
        }
        if(rightFree()>max){
            safeWay = 'r';
        }
        return safeWay;
    }
    
    /**
     * AI peureuse (expérimentale)
     * @return direction choisie par l'AI
     */
    public char shyAI(){
        final char safestPath = safestPath();
        System.out.println("SafestPath Done:"+safestPath);
        if(safestPath=='*'){
            return directions[random.nextInt(directions.length)]; //Gestion de la décision aléatoire lorsque l'AI peureuse a le choix
        }else{
            return safestPath; //retourne u,d,l ou r
        }
    }
}
