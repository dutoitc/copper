package ch.mno.copper.web;

import ch.mno.copper.helpers.GraphHelper;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;
import ch.mno.copper.stories.DiskHelper;
import ch.mno.copper.web.adapters.JsonInstantAdapter;
import ch.mno.copper.web.helpers.InstantHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.ApiOperation;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping(value = "/ws", produces = MediaType.APPLICATION_JSON, consumes = MediaType.WILDCARD)
public class CopperUserServices {

    private static final Logger LOG = LoggerFactory.getLogger(CopperUserServices.class);
    private final ValuesStore valuesStore;

    @Autowired
    private DiskHelper diskHelper;

    public CopperUserServices(ValuesStore valuesStore) {
        this.valuesStore = valuesStore;
    }

    @GetMapping(value = "/")
    public String root() {
        // See https://stackoverflow.com/questions/32184175/how-to-use-spring-redirect-if-controller-method-returns-responseentity
        return "redirect:swagger.json";
    }


    @GetMapping(value = "infos/headers", produces = MediaType.TEXT_PLAIN)
    public String getInfoHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP HEADERS:\n");
        for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
            String header = it.next();
            sb.append(header).append("=").append(request.getHeader(header)).append('\n');
        }
        return sb.toString();
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ping method answering 'pong'",
            notes = "Use this to monitor that Copper is up")
    public String test() {
        return "pong";
    }

    @GetMapping(value = "/screens", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get embedded screens",
            notes = "A way to get the embedded screens")
    public Map<String, String> getScreens() {
        return diskHelper.findScreens();
    }

    @GetMapping(value = "values", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Convenience way to retrieve all valid values from Copper",
            notes = "Use this to extract many values, example for remote webservice, angular service, ...")
    public String getValues() {
        try {
            Map<String, StoreValue> values = valuesStore.getValues();
            return buildGson().toJson(values);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping(value = "values/alerts", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Find alerts on values volumetry", notes = "Use this to find values with too much store")
    public String getValuesAlerts() {
        return valuesStore.getValuesAlerts();
    }


    @GetMapping(value = "values/query")
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public ResponseEntity getValues(@QueryParam("from") String dateFrom,
                                    @QueryParam("to") String dateTo,
                                    @QueryParam("columns") String columns,
                                    @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues) {
        if (columns == null) {
            return new ResponseEntity("Missing 'columns'", HttpStatus.NOT_ACCEPTABLE);
        }
        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            List<String> cols = Arrays.asList(columns.split(","));
            String ret = buildGson().toJson(valuesStore.queryValues(from, to, cols, maxValues));
            return ResponseEntity.of(Optional.of(ret));

        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(value = "instants/query")
    @ApiOperation(value = "Retrieve values between date",
            notes = "")
    public ResponseEntity getValues(@QueryParam("from") String dateFrom,
                                    @QueryParam("to") String dateTo,
                                    @QueryParam("columns") String columns,
                                    @QueryParam("intervalSeconds") long intervalSeconds,
                                    @QueryParam("maxvalues") Integer maxValues) {

        maxValues = maxValues == null ? 100 : maxValues;

        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);
        try {
            List<String> cols = Arrays.asList(columns.split(","));
            List<InstantValues> values = valuesStore.queryValues(from, to, intervalSeconds, cols, maxValues);
            String ret = buildGson().toJson(values);
            return ResponseEntity.of(Optional.of(ret));
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @GetMapping(value = "value/{valueName}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve a single value",
            notes = "")
    public ResponseEntity<StoreValue> getValue(@PathVariable("valueName") String valueName) {
        StoreValue storeValue = valuesStore.getValues().get(valueName);

        if (storeValue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Value " + valueName + " not found");
        }
        return new ResponseEntity(storeValue.getValue(), HttpStatus.OK);
    }


    @PostMapping(value = "value/{valueName}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<StoreValue> postValue(@PathVariable("valueName") String valueName, @RequestBody String message) {
        valuesStore.put(valueName, message);
        return getValue(valueName);
    }


    @GetMapping(value = "values/query/png", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public HttpEntity getValuesAsPNG(@QueryParam("from") String dateFrom,
                                     @QueryParam("to") String dateTo,
                                     @QueryParam("columns") String columns,
                                     @QueryParam("ytitle") String yTitle,
                                     @QueryParam("maxvalues") Integer maxValues,
                                     @QueryParam("width") Integer width,
                                     @QueryParam("height") Integer height,
                                     HttpServletResponse response) {
        if (columns == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing 'columns'");
        }
        maxValues = maxValues == null ? 100 : maxValues;
        width = width == null ? 600 : width;
        height = height == null ? 400 : height;

        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            // Query
            List<String> cols = Arrays.asList(columns.split(","));
            List<StoreValue> storeValues = valuesStore.queryValues(from, to, cols, maxValues);

            // Graph
            JFreeChart chart = GraphHelper.createChart(storeValues, columns, yTitle);
            byte[] png = GraphHelper.toPNG(chart, width, height);

            response.setContentType(org.springframework.http.MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(png, response.getOutputStream());
            return new ResponseEntity(HttpStatus.OK);
        } catch (RuntimeException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new JsonInstantAdapter());
        return builder.create();
    }

}