package nl.uu.components;

import org.tweetyproject.logics.rdl.semantics.Extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class BOIDProcessTree {
    Collection<BOIDSequence> processes = new ArrayList<>();

    Collection<Extension> extensions = new HashSet<>();

    public BOIDProcessTree(BOIDTheory theory) {
        theory.ground();
        BOIDSequence state = new BOIDSequence(theory);

        while (!state.isClosed(theory)) {
            for (BOIDRule rule : theory.getDefaults()) {
                if (state.isApplicable(rule) && state.hasBeenApplied(rule)) {
                    BOIDSequence sequence = state.app(rule);
                    if (sequence.isProcess() && state.isSuccessful()) {
                        state = sequence;
                        break;
                    }
                }
            }
        }

        System.out.println("Finished building tree?");
//        theory = theory.ground();
//        List<BOIDSequence> seqs_old = new ArrayList<>();
//        List<BOIDSequence> seqs_new = new ArrayList<>();
//
//        seqs_old.add(new BOIDSequence(theory));
//
//        while(!seqs_old.isEmpty()){
//            for(BOIDSequence seq_old: seqs_old) {
//                Collection<BOIDRule> defaults = (Collection<BOIDRule>) theory.getDefaults();
//
//                // Should probably add ordering here
//                for(BOIDRule d: theory.getDefaults()){
//                    BOIDSequence seq_new = seq_old.app(d);
//                    if(seq_new.isProcess())
//                        if(seq_new.isSuccessful())
//                            if(seq_new.isClosed(theory)) {
//                                processes.add(seq_new);
//                            }
//                            else
//                                seqs_new.add(seq_new);
//                }
//            }
//            seqs_old = seqs_new;
//            seqs_new  = new ArrayList<>();
//        }
//        for(BOIDSequence seq: processes)
//            extensions.add(new Extension(seq.getIn()));
    }

    public Collection<Extension> getExtensions() {
        return extensions;
    }
}
