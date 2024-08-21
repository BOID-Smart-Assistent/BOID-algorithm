package nl.uu.components;

import org.tweetyproject.logics.rdl.semantics.Extension;

import java.util.*;

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

                for (BOIDRule rule : theory.getDefaults()) {
                    List<BOIDRule> rules = theory.getDefaults().stream()
                            .filter(x -> x.priority == rule.priority)
                            .toList();

                    boolean shouldBreak = false;

                    for (BOIDRule df : rules) {
                        if (state.isApplicable(df) && !state.hasBeenApplied(df)) {
                            BOIDSequence nextState = state.app(df);
                            if (nextState.isProcess() && nextState.isSuccessful()) {
                                newStates.add(nextState);
                                shouldBreak = true;
                            }
                        }
                    }

                    if (shouldBreak) {
                        iterator.remove();
                        break;
                    }
                }
            }

            states.addAll(newStates);
        }

        for (BOIDSequence state : states) {
            extensions.add(new Extension(state.getIn()));
        }

        return extensions;
    }
}
