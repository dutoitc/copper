package ch.mno.copper.process;


import ch.mno.copper.data.ValuesStore;
import org.apache.commons.collections4.CollectionUtils;

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
        if (valuesTrigger.size()==1 && valuesTrigger.iterator().next().equals("*")) return coll;
        return CollectionUtils.intersection(valuesTrigger, coll);
    }

    public abstract void trig(ValuesStore valueStore, Collection<String> changedValueKeys);

}
