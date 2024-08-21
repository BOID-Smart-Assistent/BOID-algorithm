package nl.uu;

import nl.uu.components.BOIDReasoner;
import nl.uu.components.BOIDRule;
import nl.uu.components.BOIDTheory;
import nl.uu.components.BOIDTypes;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.fol.syntax.*;
import org.tweetyproject.logics.rdl.semantics.Extension;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Transpiler {
    public ImmutablePair<ArrayList<BOIDRule>, FolBeliefSet> readTextFile(String path) {
        FolBeliefSet beliefSet = new FolBeliefSet();

        BufferedReader bufferedReader;
        ArrayList<BOIDRule> rules = new ArrayList<>();

        Map<String, ArrayList<FolAtom>> periods = new HashMap<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(path));

            String line = bufferedReader.readLine();
            int i = 1;
            while (line != null) {
                String[] predicates = line.split(" ");

                String aInput = predicates[0];
                String ruleInput = predicates[1];
                String bInput = predicates[2];

                BOIDTypes boidType;
                FolFormula a;
                // Check if the prerequisite is a tautology
                if (Objects.equals(predicates[0], "true")) {
                   a = new Tautology();
                } else {
                   a = new FolAtom(new Predicate(predicates[0]));
                }

                FolAtom b = new FolAtom(new Predicate(bInput));
                // If the conclusion is a timeslot then add it to the map, then generate the material exclusivity later?
                if (bInput.contains("timeslot")) {
                    String[] timeslot = bInput.split("_");
                    String timeslotPeriod = timeslot[1];
                    // Create all the mutual exclusiveness
                    if (periods.containsKey(timeslotPeriod)) {
                        periods.get(timeslotPeriod).add(b);
                    } else {
                        periods.put(timeslotPeriod, new ArrayList<>(Arrays.asList(b)));
                    }
                }

                if (ruleInput.contains("B")) {
                    boidType = BOIDTypes.BOID_TYPES_BELIEF;
                } else if (ruleInput.contains("O")) {
                    boidType = BOIDTypes.BOID_TYPES_OBLIGATION;
                } else if (ruleInput.contains("I")) {
                    boidType = BOIDTypes.BOID_TYPES_INTENTION;
                } else if (ruleInput.contains("D")) {
                    boidType = BOIDTypes.BOID_TYPES_DESIRE;
                } else {
                    throw new ParseException("Could not find BOID rule type at line", i);
                }

                rules.add(new BOIDRule(a, b, boidType, i));

                line = bufferedReader.readLine();
                i++;
            }

            for (String key : periods.keySet()) {
                ArrayList<FolAtom> slots = periods.get(key);
                for (int k = 0; k < slots.size(); k++) {
                    for (int j = k + 1; j < slots.size(); j++) {
                        FolAtom first = slots.get(k);
                        FolAtom second = slots.get(j);

                        beliefSet.add(new Negation(new Conjunction(first, second)));
                    }
                }
            }
        }  catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }

        return new ImmutablePair<>(rules, beliefSet);
    }

    public Map<String, String> transpileExtension(ArrayList<BOIDRule> rules, Extension extension) {
       Map<String, String> result = new HashMap<>();

        for (var atom : extension) {
           if (atom.toString().contains("timeslot")) {
               for (BOIDRule rule : rules) {
                   if (Objects.equals(rule.getConclusion().toString(), atom.toString())) {
                       result.put(atom.toString(), rule.getPrerequisite().toString());
                   }
               }
           }
       }

        return result;
    }
}
