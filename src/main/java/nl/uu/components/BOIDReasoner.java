package nl.uu.components;

import org.tweetyproject.logics.rdl.semantics.Extension;

import java.util.Collection;

public class BOIDReasoner {
    public Collection<Extension> getGoals(BOIDTheory theory) {
        return new BOIDGoalGenerator().getGoals(theory);
    }

    public Extension getGoal(BOIDTheory theory) {
        return new BOIDGoalGenerator().getStrictGoal(theory);
    }

    public Collection<Extension> getModels(BOIDTheory bbase) {
        return new BOIDProcessTree(bbase).getExtensions();
    }
}
