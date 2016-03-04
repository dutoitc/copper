package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class PushoverReporter implements AbstractReporter  {
    private Logger LOG = LoggerFactory.getLogger(getClass());

    private static final String URL = "https://api.pushover.net/1/messages.json";
    public static enum PARAMETERS {APPLICATION_TOKEN, DEST, TITLE, HTML};

    private static long nbMessageInHour=0;
    private static long hour = -1;
    private static int MAX_MESSAGE_PER_HOUR = 5;

//    public void send(String applicationToken, String dest, String title, String message, boolean html) throws ConnectorException {
//
//    }

//    public static void main(String[] args) throws ConnectorException {
//        new PushoverReporter().send("anAppToken", "aUser", "aTitle", "test", false);
//    }

    @Override
    public void report(String message, Map<String, String> values) throws ConnectorException {
        int currHour = new Date().getHours();
        if (currHour==hour) {
            if (nbMessageInHour> MAX_MESSAGE_PER_HOUR) {
                LOG.warn("Too much message for this hour, skipping message: " + message);
                return;
            }
        } else {
            hour = currHour;
            nbMessageInHour=0;
        }
        nbMessageInHour++;


        HttpConnector conn = new HttpConnector("api.pushover.net", 443, "https", "localhost", 3128, "http"); // FIXME: temporary test, create configuration file for this
        Map<String, String> params = new HashMap<>();
        params.put("token", values.get(PARAMETERS.APPLICATION_TOKEN.toString()));
        params.put("user", values.get(PARAMETERS.DEST.toString()));
        params.put("title", values.get(PARAMETERS.TITLE.toString()));
        params.put("message", message);
        if ("true".equals(values.get(PARAMETERS.HTML.toString()))) params.put("html", "1");
        String ret = conn.post(URL, params);
        LOG.info("Pushover returned {}", ret);
//        System.out.println(ret); // {"status":1,"request":"ead7edb7aa67c0e4502etc..."}
    }
    //RCPERSABONNERENTREPRISEIDE,CONTROLEREXISTENCEDEPENDENCES,CONTROLEREXISTENCEDEPENDENCESZEFIX,CREATIONANNONCEIDETYPECREATION,CREATIONANNONCEIMPORTENTITEJURIDIQUE,CREATIONNUMEROCANTONALDEPUISREFINF,DESABONNERENTREPRISEIDE,ENREGISTREMENTINFORMATIONSDESANNONCESOPERATION,ENREGISTREMENTMDMIDE,FINTRAITEMENTANNONCE,FINDMUNICIPALITY,IDENTIFICATIONNUMEROIDEFOSCHR,IDENTIFICATIONTYPEANNULATION,METTREAJOURABONNEMENTIDE,MISEAJOURENTREPRISEDEPUISRCVD,MISEAJOURENTREPRISEDEPUISZEFIX,NORMALISERADRESSESTAGING,RECHERCHEPERSONNESRCPERS,RECUPERERINFOENTREPRISEDEPUISIDE,RECUPERERINFOENTREPRISEDEPUISRCVD,RECUPERERINFOENTREPRISEDEPUISZEFIX,RELANCETRAITEMENTANNONCE,SECCONTROLEDEMANDEANNONCE,SECENREGISTREMENTDEMANDE,SECENREGISTREMENTQUITTANCEIDE,SECENRICHISSEMENTNOTICEREQUEST,SECENVOIQUITTANCE,SECTRANSFERTNOTICEREQUESTSTAGINGVERSMDM,SECTRANSMISSIONDEMANDEDANNONCEIDE,TRAITERDONNEESIDECREATION,TRAITERDONNEESIDEANNULATION,TRAITERDONNEESIDECHANGEMENT,TRANSFERTSTAGINGVERSMDM,UPDATESTATUTANNONCE,UPDATESTATUTPUBLICATION,WRITEJOURNALEVENT,NOTICEREQUESTVALIDATE,NOTICEREQUESTLIST,ORGANISATIONSOFNOTICE,MONITORING,INFRASTRUCTURE,ORGANISATION,LOOKUP,ADMIN,PDP,XACMLREGISTRYATOMRPWSV1_KO



}
