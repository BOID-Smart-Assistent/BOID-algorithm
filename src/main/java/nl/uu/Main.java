package nl.uu;

import nl.uu.components.BOIDClient;
import nl.uu.components.BOIDReasoner;
import nl.uu.components.BOIDRule;
import nl.uu.components.BOIDTheory;
import nl.uu.model.boid.data.BoidOutput;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.tweetyproject.logics.fol.reasoner.FolReasoner;
import org.tweetyproject.logics.fol.reasoner.SimpleFolReasoner;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.rdl.semantics.Extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
//        Main.setup();
//        Main.setupWebSocket();

        Transpiler transpiler = new Transpiler();
        ImmutablePair<ArrayList<BOIDRule>, FolBeliefSet> transpilerResult = transpiler.readTextFile("./rules_extended.txt");

        BOIDTheory theory = new BOIDTheory(transpilerResult.getRight(), transpilerResult.getLeft());
        BOIDReasoner reasoner = new BOIDReasoner();

        FolReasoner.setDefaultReasoner(new SimpleFolReasoner());
//        Collection<Extension> extensions = reasoner.getGoals(theory);
        Collection<Extension> extensions = reasoner.getGoalsExperimental(theory);
//        Collection<Extension> extension = reasoner.getGoals(theory);

        System.out.println(extensions);
//        BoidOutput schedule = transpiler.transpileExtension(transpilerResult.getLeft(), extension, 1);

//        System.out.println(schedule.toString());
    }

    private static void setup() {
        FolReasoner.setDefaultReasoner(new SimpleFolReasoner());
    }

    private static void setupWebSocket() throws URISyntaxException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("identification", "boid-algorithm");

        BOIDClient client = new BOIDClient(URI.create("ws://localhost:8081"), headers);
        client.connect();
    }
}
