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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
public class ValuesStoreImpl implements ValuesStore {

    // TODO: dependency graph, with temporized triggers and quiet time

    private static final Logger LOG = LoggerFactory.getLogger(ValuesStoreImpl.class);
    private static final ValuesStoreImpl instance = new ValuesStoreImpl();
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

    public static ValuesStoreImpl getInstance() {
        return instance;
    }

    @Override
    public void put(String key, String value) {
        if (map.containsKey(key)) {
            if (value==null && map.get(key)==null) return;
            // commented: always store, as store date = last check date. TODO: implement from... to dates
            //if (map.get(key)!=null && map.get(key).getValue().equals(value)) return;
            if (map.get(key)!=null && !map.get(key).getValue().equals(value)) {
                map.put(key, new StoreValue(value));
            }
            return; // Otehrwise same value
        }
        map.put(key, new StoreValue(value));
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
            sb.append('|');
            sb.append(v==null||v.getValue()==null?null:v.getValue().replace("|", "£").replace("\n","¢"));
            sb.append('|');
            sb.append(v==null?null:v.getTimestamp());
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
                int p2 = line.lastIndexOf('|');
                String key = line.substring(0,p);
                String content = line.substring(p+1, p2);
                String timestamp = line.substring(p2+1);
                map.put(key, new StoreValue(content.replaceAll("£", "|").replace("¢", "\n"), Long.parseLong(timestamp)));
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

    public long getTimestamp(String key) {
        if (map.containsKey(key)) {
            return map.get(key).getTimestamp();
        }
        return -1;
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
    public Collection<String> queryValues(LocalDateTime from, LocalDateTime to) {
        List<String> keys = new ArrayList<>();
        long tsFrom = Timestamp.valueOf(from).getTime();
        long tsTo = Timestamp.valueOf(to).getTime();
        for (Map.Entry<String, StoreValue> entry: map.entrySet()) {
            long entryTS = entry.getValue().getTimestamp();
            if ((entryTS >= tsFrom) && (entryTS <= tsTo || to.equals(LocalDateTime.MAX))) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public List<List<String>> queryValues(LocalDateTime from, LocalDateTime to, String columns) {
        // Note: this code is temporary, waiting for an internal DB to be queried. Yet, it match my project only,
        // with a csv file of format (DATETIME;KEY1;KEY2;...;KEYN\ndd.MM.yyyy HH:mm:ss;value1;value2;...;valueN\n...
        /*List<List<String>> data = new ArrayList<List<String>>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("RCFACE-data.csv"))) {
            String header = br.readLine();

            // Find wanted columns
            List<String> userColumns = Arrays.asList(columns.split(","));
            List<Integer> wantedColumns = new ArrayList<>();
            int noCol=0;
            for (String column: header.split(";")) {
                if (userColumns.contains(column)) {
                    wantedColumns.add(noCol);
                }
                noCol++;
            }

            // Check if all columns have been found
            if (userColumns.size()!=wantedColumns.size()) {
                throw new RuntimeException("Not found all columns: found [" + StringUtils.join(wantedColumns, ",") + "] but wanted [" + StringUtils.join(userColumns, ",")+"]");
            }

            // Extract data
            String line;
            while ((line=br.readLine())!=null) {
                String[] csvColumns = line.split(";");
                LocalDateTime csvDT = LocalDateTime.parse(csvColumns[0], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                if (from!=null && from.isAfter(csvDT)) continue;
                if (to!=null && to.isBefore(csvDT)) continue;

                List<String> row = new ArrayList<>();
                row.add(csvColumns[0]);
                for (int idx: wantedColumns) {
                    row.add(csvColumns[idx]);
                }
                data.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;*/
        throw new RuntimeException("Not implemented for file valuesStore");
    }

    public static void main(String[] args) throws IOException {
        new ValuesStoreImpl().load(new FileInputStream("/tmp/valuesStore.tmp"));
    }


}
