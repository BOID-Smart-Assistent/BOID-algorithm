package nl.uu.components;

import org.tweetyproject.logics.fol.reasoner.FolReasoner;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import org.tweetyproject.logics.fol.syntax.Negation;
import org.tweetyproject.logics.rdl.syntax.DefaultRule;

import java.util.*;

public class BOIDSequence {
    /**
     * the sequence of defaults
     */
    private List<DefaultRule> defaults = new ArrayList<>();

    /**
     * the out set
     */
    private Set<FolFormula> out = new HashSet<>();

    /**
     * the in set
     */
    private FolBeliefSet in = new FolBeliefSet();

    /**
     * true if the sequence is a process
     */
    boolean process = true;

    /**
     * constructs an empty sequence of defaults of the default theory dt
     * @param dt a default theory, from which defaults will be added to the sequence
     */
    public BOIDSequence(BOIDTheory dt) {
        in.addAll(dt.getFacts());
    }

    /**
     * constructs a sequence by appending d to ds
     * @param ds a default sequence
     * @param d a default rule
     */
    public BOIDSequence(BOIDSequence ds, BOIDRule d) {
        defaults.addAll(ds.defaults);
        in.addAll(ds.in);
        process = ds.isApplicable(d);
        for(DefaultRule r: defaults)
            if(d.equals(r))
                process = false;
        in.add(d.getConclusion());
        defaults.add(d);
        out.addAll(ds.out);
        for(FolFormula f: d.getJustification())
            out.add(new Negation(f));
    }

    /**
     * Constructs a new DefaultSequence
     * @param d a default rule
     * @return new Sequence adding d to this
     */
    public BOIDSequence app(BOIDRule d){
        return new BOIDSequence(this,d);
    }

    /**
     * applicable ^= pre in In and (not jus_i) not in In forall i
     * @param d a default rule
     * @return true iff d is applicable to In
     */
    public boolean isApplicable(DefaultRule d){
        FolReasoner prover = FolReasoner.getDefaultReasoner();
        for(FolFormula f: d.getJustification())
            if(prover.query(in, new Negation(f)))
                return false;
        return prover.query(in, d.getPrerequisite());

    }

    /**
     * 	@return the sequence's in set
     */
    public Collection<FolFormula> getIn() {
        return in;
    }


    /**
     * @return the sequence's out set
     */
    public Collection<FolFormula> getOut() {
        return out;
    }

    /**
     * process &lt;=&gt; all defaults are unique and applicable in sequence
     * @return true iff is process
     */
    public boolean isProcess() {
        return process;
    }

    /**
     * successfull &lt;=&gt; there is no x: x in In and x in Out
     * @return true iff successfull
     */
    public boolean isSuccessful() {
        FolReasoner prover = FolReasoner.getDefaultReasoner();
        for(FolFormula g: out)
            if(prover.query(in,g))
                return false;
        return true;
    }

    /**
     * Tests wether all applicble defaukts from t have been applied
     * @param t a default theory
     * @return true iff every possible default is applied
     */
    public boolean isClosed(BOIDTheory t) {
        for(BOIDRule d: t.getDefaults())
            if(this.app(d).isProcess())
                return false;
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DefaultSequence"
                + (isProcess() ? " is process":"")
                + (isSuccessful()?" is successfull":"")
                +" [\n\tdefaults = " + defaults + ", \n\tout = " + out + ", \n\tin = " + in + "\n]";
    }

    public boolean hasBeenApplied(BOIDRule rule) {
        return this.defaults.contains(rule);
    }
}
