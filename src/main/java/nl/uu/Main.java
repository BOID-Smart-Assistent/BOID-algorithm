package nl.uu;

import nl.uu.components.BOIDReasoner;
import nl.uu.components.BOIDRule;
import nl.uu.components.BOIDTheory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.rdl.semantics.Extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Transpiler transpiler = new Transpiler();
        ImmutablePair<ArrayList<BOIDRule>, FolBeliefSet> transpilerResult = transpiler.readTextFile("./rules.txt");

        BOIDTheory theory = new BOIDTheory(transpilerResult.getRight(), transpilerResult.getLeft());
        BOIDReasoner reasoner = new BOIDReasoner();
//        Collection<Extension> extensions = reasoner.getGoals(theory);
        Extension extension = reasoner.getGoal(theory);

        Map<String, String> schedule = transpiler.transpileExtension(transpilerResult.getLeft(), extension);

        System.out.println(schedule.toString());
    }
}
