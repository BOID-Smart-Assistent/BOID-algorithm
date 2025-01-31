package nl.uu;

import nl.uu.components.BOIDRule;
import nl.uu.components.BOIDTypes;
import nl.uu.model.boid.Rule;
import nl.uu.model.boid.data.*;
import org.apache.commons.lang3.RandomStringUtils;
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
                String priority = predicates[3];

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

//                int priority = i;

//                if (bInput.equals("transformers") || bInput.equals("compilers")) {
//                    priority = 3;
//                } else if (bInput.contains("timeslot")) {
//                    priority = 6;
//                } else if (bInput.equals("machine_learning")) {
//                    priority = 5;
//                } else if (bInput.equals("large_language_models")) {
//                    priority = 4;
//                } else if (bInput.equals("knowledge_representation")) {
//                    priority = 2;
//                } else if (bInput.equals("web_technology")) {
//                    priority = 1;
//                }

//                if (bInput.contains("timeslot")) {
//                    priority = 9;
//                } else {
//                    priority = i - 9;
//                }
//
                rules.add(new BOIDRule(a, b, boidType, Integer.parseInt(priority)));

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
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }

        return new ImmutablePair<>(rules, beliefSet);
    }

    public ImmutablePair<ArrayList<BOIDRule>, FolBeliefSet> readProtobuf(LlmOutput message) {
        FolBeliefSet beliefSet = new FolBeliefSet();

        BufferedReader bufferedReader;
        ArrayList<BOIDRule> rules = new ArrayList<>();

        Map<String, ArrayList<FolAtom>> periods = new HashMap<>();

        Iterator<Rule> llmRules = message.getRulesList().iterator();

        int i = 1;
        while (llmRules.hasNext()) {
            Rule rule = llmRules.next();
            String head = rule.getHead().trim();
            String complement = rule.getComplement().trim();
            BOIDTypes boidType = BOIDTypes.BOID_TYPES_BELIEF;
            FolFormula a;
            // Check if the prerequisite is a tautology
            if (Objects.equals(head, "true")) {
                a = new Tautology();
            } else {
                a = new FolAtom(new Predicate(head));
            }

            FolAtom b = new FolAtom(new Predicate(complement));
            // If the conclusion is a timeslot then add it to the map, then generate the material exclusivity later?
            if (rule.getComplement().contains("timeslot")) {
                String[] timeslot = complement.split("_");
                String timeslotPeriod = timeslot[1];
                // Create all the mutual exclusiveness
                if (periods.containsKey(timeslotPeriod)) {
                    periods.get(timeslotPeriod).add(b);
                } else {
                    periods.put(timeslotPeriod, new ArrayList<>(Arrays.asList(b)));
                }
            }

            switch (rule.getRuleType()) {
                case BELIEF -> {
                    boidType = BOIDTypes.BOID_TYPES_BELIEF;
                }
                case OBLIGATION -> {
                    boidType = BOIDTypes.BOID_TYPES_OBLIGATION;
                }
                case INTENTION -> {
                    boidType = BOIDTypes.BOID_TYPES_INTENTION;
                }
                case DESIRE -> {
                    boidType = BOIDTypes.BOID_TYPES_DESIRE;
                }
            }

            rules.add(new BOIDRule(a, b, boidType, 1));

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

        return new ImmutablePair<>(rules, beliefSet);
    }

    public BoidOutput transpileExtension(ArrayList<BOIDRule> rules, Extension extension, int userId) {
        BoidOutput.Builder builder = BoidOutput.newBuilder();
        var iterator = extension.stream().iterator();
        builder.setUserId(userId);

        while (iterator.hasNext()) {
            var goal = iterator.next();

            if (goal instanceof FolAtom) {
                String name = ((FolAtom) goal).getName();

                if (name.contains("timeslot")) {
                    // Get the ID of the presentation
                    builder.addPresentations(Integer.parseInt(name.split("_")[2]));
                }
            }
        }

        return builder.build();
    }
}
