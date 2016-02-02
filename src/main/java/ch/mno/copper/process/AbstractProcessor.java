package ch.mno.copper.process;


import ch.mno.copper.ValuesStore;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dutoitc on 02.02.2016.
 */
public abstract class AbstractProcessor {

    private Set<String> valuesTrigger;

    public AbstractProcessor(List<String> valuesTrigger) {
        this.valuesTrigger = new HashSet<>(valuesTrigger);
    }

//    public boolean hasTriggerKey(String name) {
//        return valuesTrigger.contains(name);
//    }

    public Collection<String> findKnownKeys(Collection<String> coll) {
        return CollectionUtils.intersection(valuesTrigger, coll);
    }

    public abstract void trig(ValuesStore valueStore, Collection<String> changedValueKeys);

}
