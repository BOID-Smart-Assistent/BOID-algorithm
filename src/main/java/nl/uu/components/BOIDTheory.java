package nl.uu.components;

import org.tweetyproject.commons.BeliefBase;
import org.tweetyproject.commons.Signature;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import org.tweetyproject.logics.fol.syntax.FolSignature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BOIDTheory implements BeliefBase {

    /** The set of facts (first-order formulas). */
    private FolBeliefSet facts;
    /** The set of default rules */
    private Collection<BOIDRule> defaults;


    /**
     * constructs empty default theory
     */
    public BOIDTheory() {
        super();
        this.facts = new FolBeliefSet();
        this.defaults = new ArrayList<BOIDRule>();
    }

    /**
     * constructs a default theory from a knowledge base and a set of defaults
     * @param facts the knowledge base
     * @param defaults the defaults
     */
    public BOIDTheory(FolBeliefSet facts, Collection<BOIDRule> defaults) {
        super();
        this.facts = facts;
        this.defaults = new ArrayList<BOIDRule>();
        this.defaults.addAll(defaults);
    }

    /**
     * add facts to knowledge base
     * @param fact some fol formula
     */
    void addFact(FolFormula fact){
        facts.add(fact);
    }

    /**
     * removes fact from knowledge base
     * @param fact some fol formula
     */
    void removeFact(FolFormula fact){
        facts.remove(fact);
    }

    /**
     * adds default rule
     * @param d a default rule
     */
    void addDefault(BOIDRule d){
        defaults.add(d);
    }

    /**
     * removes default rule
     * @param d a default rule
     */
    void removeDefault(BOIDRule d){
        defaults.remove(d);
    }

    /**
     * @return all the default rules
     */
    public Collection<BOIDRule> getDefaults() {
        return defaults;
    }

    /**
     * Removes Variables by expanding formulas
     * @return grounded version of the default theory
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BOIDTheory ground(){
        Set<BOIDRule> ds = new HashSet<>();
        for(BOIDRule d: defaults){
            ds.addAll((Set)(d.allGroundInstances(((FolSignature)getMinimalSignature()).getConstants())));
        }
        return new BOIDTheory(facts, ds);
    }


    /* (non-Javadoc)
     * @see org.tweetyproject.commons.BeliefBase#getSignature()
     */
    @Override
    public Signature getMinimalSignature() {
        Signature result = facts.getMinimalSignature();
        for(BOIDRule d: defaults)
            result.addSignature(d.getSignature());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = facts +"\n\n";
        for(BOIDRule d : defaults)
            result += d + "\n";
        return result;
    }

    /**
     *
     * @return FoL formulas in default theories
     */
    public FolBeliefSet getFacts(){
        return facts;
    }
}
