package ch.mno.copper.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Store for values. Timestamp is set as data insertion.
 * Put will erase data. get will return null or -1 if not found.
 * Created by dutoitc on 29.01.2016.
 */
public class MemoryValuesStore implements ValuesStore {

    // TODO: dependency graph, with temporized triggers and quiet time

    private static final Logger LOG = LoggerFactory.getLogger(MemoryValuesStore.class);
    private static final MemoryValuesStore instance = new MemoryValuesStore();
    public static final String DATA_FILENAME = "valuesStore.tmp";
    private Map<String, StoreValue> map = new HashMap<>();
//    private Set<String> changedValues = new HashSet<>();

    static {
        try {
            instance.load();
        } catch (IOException e) {
            System.err.println("Cannot load valuesStore.tmp, ignoring");
        }
    }

    public static MemoryValuesStore getInstance() {
        return instance;
    }

    @Override
    public void put(String key, String value) {
        if (map.containsKey(key)) {
            if (value==null && map.get(key)==null) return;
            // commented: always store, as store date = last check date. TODO: implement from... to dates
            //if (map.get(key)!=null && map.get(key).getValue().equals(value)) return;
            if (map.get(key)!=null && !map.get(key).getValue().equals(value)) {
                map.put(key, new StoreValue(-1, key, value, Instant.now(), null));
            }
            return; // Otehrwise same value
        }
        map.put(key, new StoreValue(-1, key, value, Instant.now(), null));
//        changedValues.add(key);
    }

    @Override
    public Map<String, StoreValue> getValues() {
        return map;
    }



    public void save(OutputStream os) throws IOException {
        PrintWriter pw = new PrintWriter(os);
        pw.write("1\n");
        pw.write(map.size()+"\n");
        map.forEach((k,v)->{
            StringBuilder sb = new StringBuilder();
            sb.append(k);
            // TODO: read/write à revoir (stocker toutes les valeurs)
            sb.append('|');
            sb.append(v==null||v.getValue()==null?null:v.getValue().replace("|", "£").replace("\n","¢"));
            sb.append('|');
            sb.append(v==null?null:v.getTimestampFrom().getEpochSecond());
            sb.append('|');
            sb.append(v==null?null:v.getTimestampTo().getEpochSecond());
            sb.append('\n');
            pw.write(sb.toString());
        });
//        pw.write(""+changedValues.size()+'\n');
//        changedValues.forEach(k->pw.write(k+'\n'));
        pw.flush();
        pw.close();
        os.flush();
    }

    @Override
    public void save() throws IOException {
        save(new FileOutputStream(DATA_FILENAME));
    }


    @Override
    public void load() throws IOException {
        load(new FileInputStream(DATA_FILENAME));
    }


    public void load(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        // Version
        int version = Integer.parseInt(reader.readLine());
        if (version!=1) throw new RuntimeException("Unsupported version: " + version);

        // Size
        int mapSize = Integer.parseInt(reader.readLine());
        map = new HashMap<>(mapSize*4/3);

        // Map
        for (int noLine=0; noLine<mapSize; noLine++) {
            try {
                String line = reader.readLine();
                int p = line.indexOf('|');
                String key = line.substring(0,p);
                line = line.substring(p+1);

                int p2 = line.lastIndexOf('|');
                String timestampTo = line.substring(p2+1);
                line = line.substring(0, p2);
                p2 = line.lastIndexOf('|');
                String timestampFrom = line.substring(p2+1);
                line = line.substring(0, p2);

                String content = line.replaceAll("£", "|").replace("¢", "\n");
                StoreValue sv = new StoreValue(-1, key, content, Instant.ofEpochSecond(Long.parseLong(timestampFrom)), Instant.ofEpochSecond(Long.parseLong(timestampTo)));

                map.put(key, sv);
            } catch (Exception e) {
                throw new RuntimeException("Error at line #" + noLine + ": " + e.getMessage(), e);
            }
        }

        // List
        //String s = reader.readLine();
        //int listSize = Integer.parseInt(s);
//        changedValues = new HashSet<String>(listSize*4/3);
//        for (int i=0; i<listSize; i++) {
//            changedValues.add(reader.readLine());
//        }
        System.out.println("ValuesStore loaded from tmp file");
    }

//    @Override
//    public Collection<String> getChangedValues() {
//        // TODO: Improve this with a quiet time for stable values ?
//        List<String> values = new ArrayList<>(changedValues);
//        changedValues.clear();
//        return values;
//    }

    @Override
    public String getValue(String key) {
        if (key.startsWith("NOW_")) {
            // NOW_DD.MM.YY_HH:MM
            String pattern = key.substring(4).replace("_", " ");
            try {
                return new SimpleDateFormat(pattern).format(new Date());
            } catch (IllegalArgumentException e) {
                LOG.error("Unparseable pattern: " + pattern + "; " + e.getMessage());
            }
        }
        if (map.containsKey(key)) {
            return map.get(key).getValue();
        }

        return null;
    }

    public Instant getTimestampFrom(String key) {
        if (map.containsKey(key)) {
            return map.get(key).getTimestampFrom();
        }
        return null;
    }

    public Instant getTimestampTo(String key) {
        if (map.containsKey(key)) {
            return map.get(key).getTimestampTo();
        }
        return null;
    }

    /**
     *
     * @param desc key1,key2...
     * @param values values for keys, ordered
     */
    public void putAll(String desc, List<String> values) {
        String[] keys = desc.split(",");
        if (keys.length!=values.size()) throw new RuntimeException("Wrong number of parameters");
        for (int i=0; i<keys.length; i++) {
            put(keys[i],values.get(i));
        }
    }

    @Override
    public Map<String, String> getValuesMapString() {
        // FIXME make it Java8
        Map<String, String> values = new HashMap<>();
        map.forEach((k,v)->values.put(k,v.getValue()));
        return values;
    }

    public void clear() {
        map.clear();
//        changedValues.clear();
    }

    @Override
    public Collection<String> queryValues(Instant from, Instant to) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, StoreValue> entry: map.entrySet()) {
            if ((from==null || !entry.getValue().getTimestampFrom().isBefore(from)) &&
                    (to==null || !entry.getValue().getTimestampTo().isAfter(to)))
            {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public List<List<String>> queryValues(Instant from, Instant to, String columns) {
        Collection<String> keys = queryValues(from, to);
        List<List<String>> values = new ArrayList<>();
        for (String key: keys) {
            List<String> line = new ArrayList<>();
            StoreValue sv = map.get(key);
            line.add(sv.getKey());
            line.add(sv.getValue());
        }

        return values;
    }

    public static void main(String[] args) throws IOException {
        new MemoryValuesStore().load(new FileInputStream("/tmp/valuesStore.tmp"));
    }


}
