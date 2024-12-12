package nl.uu.components;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.uu.Transpiler;
import nl.uu.model.boid.LLMRulesOuput;
import nl.uu.model.boid.data.BoidOutput;
import nl.uu.model.boid.data.Schedule;
import nl.uu.model.boid.data.LlmOutput;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.rdl.semantics.Extension;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class BOIDClient extends WebSocketClient {
    public BOIDClient(URI serverUri) {
        super(serverUri);
    }

    public BOIDClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to boid-api!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // TODO: Shoulld add a way to deal with what the type of the message is
        try {
            LlmOutput message = LlmOutput.parseFrom(bytes);

            Transpiler transpiler = new Transpiler();
            ImmutablePair<ArrayList<BOIDRule>, FolBeliefSet> transpilerResult = transpiler.readProtobuf(message);

            BOIDTheory theory = new BOIDTheory(transpilerResult.getRight(), transpilerResult.getLeft());
            BOIDReasoner reasoner = new BOIDReasoner();
            Extension extension = reasoner.getGoal(theory);
            BoidOutput boidOutput = transpiler.transpileExtension(transpilerResult.getLeft(), extension, message.getUserId());

            JSONObject response = new JSONObject();
            response.put("event", "schedule-result");
            response.put("data", boidOutput.toByteArray());

            this.send(String.valueOf(response));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println(
                "Connection closed by " + (b ? "remote peer" : "us") + " Code: " + i + " Reason: "
                        + s);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}
