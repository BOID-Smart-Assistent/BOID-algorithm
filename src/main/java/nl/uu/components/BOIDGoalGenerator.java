package nl.uu.components;

import org.tweetyproject.logics.rdl.semantics.Extension;

import java.util.*;
import java.util.stream.Collectors;

public class BOIDGoalGenerator {
    public Extension getStrictGoal(BOIDTheory theory) {
        theory.ground();
        BOIDSequence state = new BOIDSequence(theory);

        while (!state.isClosed(theory)) {
            for (BOIDRule rule : theory.getDefaults()) {
                if (state.isApplicable(rule) && !state.hasBeenApplied(rule)) {
                    BOIDSequence sequence = state.app(rule);
                    if (sequence.isProcess() && state.isSuccessful()) {
                        state = sequence;
                        break;
                    }
                }
            }
        }

        return new Extension(state.getIn());
    }

    public Collection<Extension> getGoals(BOIDTheory theory) {
        theory.ground();
        Set<BOIDSequence> states = new HashSet<>();
        states.add(new BOIDSequence(theory));
        Collection<Extension> extensions = new HashSet<>();

        while (true) {
            boolean allClosed = true;
            for (BOIDSequence state : states) {
                if (!state.isClosed(theory)) {
                    allClosed = false;
                    break;
                }
            }

            if (allClosed) {
                break;
            }

            List<BOIDSequence> newStates = new ArrayList<>();
            Iterator<BOIDSequence> iterator = states.iterator();

            while (iterator.hasNext()) {
                BOIDSequence state = iterator.next();

                List<BOIDRule> applicableRules = theory.getDefaults().stream()
                        .filter(rule -> state.isApplicable(rule) && !state.hasBeenApplied(rule))
                        .toList();

                // Step 1: Get the highest priority value
                OptionalInt maxPriority = applicableRules.stream()
                        .mapToInt(rule -> rule.priority)
                        .max();

                if (maxPriority.isPresent()) {
                    // TODO: Problem is that the timeslots now all get their own states
                    // Step 2: Filter the list to only keep rules with the highest priority
                    List<BOIDRule> highestPriorityRules = applicableRules.stream()
                            .filter(rule -> rule.priority == maxPriority.getAsInt())
                            .toList();

                    for (BOIDRule rule : highestPriorityRules) {
                        if (rule.getConclusion().toString().contains("timeslot")) {
                            newStates.add(state.app(rule));
                            break;
                        }
                        newStates.add(state.app(rule));
                        System.out.println("Applied rule: " + rule);
                    }
                }

                iterator.remove();
                break;


//                for (BOIDRule rule : theory.getDefaults()) {
//                    if (!state.isApplicable(rule) || state.hasBeenApplied(rule)) {
//                        continue;
//                    }
//
//                    List<BOIDRule> rules = theory.getDefaults().stream()
//                            .filter(x -> x.priority > rule.priority && state.isApplicable(x) && !state.hasBeenApplied(x))
//                            .toList();
//
//                    if (rules.isEmpty()) {
//                        List<BOIDRule> eqRules = theory.getDefaults().stream()
//                                .filter(x -> x.priority == rule.priority && state.isApplicable(x) && !state.hasBeenApplied(x) && x != rule)
//                                .toList();
//
//                        if (eqRules.isEmpty()) {
//                            newStates.add(state.app(rule));
//                        } else {
//                            for (BOIDRule eqRule : eqRules) {
//                                newStates.add(state.app(eqRule));
//                            }
//                            newStates.add(state.app(rule));
//                        }
//
//                    } else {
//                       var highestPriority = rules.stream().max(Comparator.comparingInt(x -> x.priority)).get();
//                       newStates.add(state.app(highestPriority));
//                    }
//
//                    iterator.remove();
//                    break;


//                    boolean shouldBreak = false;
//                    // TODO: Add a message that checks which path the user wants to go to.
//                    for (BOIDRule df : rules) {
//                        if (state.isApplicable(df) && !state.hasBeenApplied(df)) {
//
//                            if (nextState.isProcess() && nextState.isSuccessful()) {
//                                newStates.add(nextState);
//                                System.out.println(nextState.getOut().toString());
//                                shouldBreak = true;
//                            }
//                        }
//                    }
//
//                    if (shouldBreak) {
//
//                    }
//                }
            }

            states.addAll(newStates);
        }

        for (BOIDSequence state : states) {
            extensions.add(new Extension(state.getIn()));
        }

        return extensions;
    }
    public Collection<Extension> getGoalsExperimental(BOIDTheory theory) {
        theory.ground();
        Set<BOIDSequence> trees = new HashSet<>();
        trees.add(new BOIDSequence(theory));
        Collection<Extension> extensions = new HashSet<>();

        while (!trees.isEmpty()) {
            BOIDSequence tree = trees.iterator().next();

            if (tree.isClosed(theory)) {
                if (tree.isSuccessful()) {
                    extensions.add(new Extension(tree.getIn()));
                }
                trees.remove(tree);

                continue;
            }

            Collection<BOIDRule> defaults = theory.getDefaults();
            List<BOIDRule> applicableRules = new ArrayList<>(defaults.size()); // Pre-size for efficiency

            // Precompute applied rules if possible (adjust if tree provides a bulk method)
            Set<BOIDRule> appliedRules = new HashSet<>();
            for (BOIDRule rule : defaults) {
                if (tree.hasBeenApplied(rule)) {
                    appliedRules.add(rule);
                }
            }

            // Single pass with precomputed appliedRules
            for (BOIDRule rule : defaults) {
                if (!appliedRules.contains(rule) && tree.isApplicable(rule)) {
                    applicableRules.add(rule);
                }
            }

//            List<BOIDRule> applicableRules = theory.getDefaults().stream().filter(rule -> tree.isApplicable(rule) && !tree.hasBeenApplied(rule)).toList();

            OptionalInt maxPriority = applicableRules.stream()
                    .mapToInt(rule -> rule.priority)
                    .max();

            if (maxPriority.isPresent()) {
                List<BOIDRule> highestPriorityRules = applicableRules.stream()
                        .filter(rule -> rule.priority == maxPriority.getAsInt())
                        .toList();

                for (BOIDRule rule : highestPriorityRules) {
                    trees.add(tree.app(rule));
                    System.out.println("Applied rule: " + rule);
                }
            }

            trees.remove(tree);
        }

        return extensions;
    }
}
