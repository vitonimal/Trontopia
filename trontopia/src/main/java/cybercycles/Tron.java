package cybercycles;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tron {

    public static void main(String[] args) throws URISyntaxException {
        final String server = "http://" + "kekstarter.org" + ":" + "1337";

        final Socket socket = IO.socket(server);
        final AI ai = new AI();

        final String room = System.getProperty("room", ai.ROOM);
        final String team = System.getProperty("team", ai.TEAM);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            public void call(Object... args) {
                if (!room.isEmpty()) {
                    socket.emit("join", room, team);
                    System.out.println("Lien vers le match : " + server + "/" + room);
                } else {
                    System.out.println("Veuillez choisir un nom pour votre chambre");
                    System.exit(0);
                }
            }
        }).on("start", new Emitter.Listener() {
            public void call(Object... args) {
                try {
                    ai.start((JSONObject) args[0]);
                } catch (JSONException ex) {
                    Logger.getLogger("start").log(Level.SEVERE, null, ex);
                }
            }
        }).on("next", new Emitter.Listener() {
            public void call(Object... args) {
                try {
                    socket.emit("move", ai.next((JSONArray) args[0]));
                } catch (JSONException ex) {
                    Logger.getLogger("next").log(Level.SEVERE, null, ex);
                }
            }
        }).on("end", new Emitter.Listener() {
            public void call(Object... args) {
                ai.end((String) args[0]);
                System.exit(0);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            public void call(Object... args) {
                System.out.println("Vous avez été déconnecté");
                System.exit(0);
            }
        });

        socket.connect();
    }
}
