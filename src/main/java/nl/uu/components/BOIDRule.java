package nl.uu.components;

import org.tweetyproject.logics.fol.syntax.FolFormula;
import org.tweetyproject.logics.rdl.syntax.DefaultRule;

public class BOIDRule  extends DefaultRule {
    public BOIDTypes boidType = null;
    public int priority = 0;

    public BOIDRule(BOIDTypes boidType, int priority) {
        this.boidType = boidType;
        this.priority = priority;
    }

    public BOIDRule(FolFormula pre, FolFormula jus) throws IllegalArgumentException {
        super(pre, jus, jus);

    }

    public BOIDRule(FolFormula pre, FolFormula jus, BOIDTypes boidType, int priority) throws IllegalArgumentException {
        super(pre, jus, jus);
        this.boidType = boidType;
        this.priority = priority;
    }

    public BOIDRule(FolFormula pre, FolFormula jus, FolFormula conc, BOIDTypes boidType, int priority) throws IllegalArgumentException {
        super(pre, jus, conc);
        this.boidType = boidType;
        this.priority = priority;
    }

    public BOIDTypes getBoidType() {
        return boidType;
    }

    public int getPriority() {
        return priority;
    }
}
