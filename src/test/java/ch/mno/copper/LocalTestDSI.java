package ch.mno.copper;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.report.MailReporter;
import ch.mno.copper.report.MailReporterWrapper;
import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xsicdt on 18/03/16.
 */
public class LocalTestDSI {

    public static void main(String[] args) throws ConnectorException {
       /* Map<String, String> values = new HashMap<>();
        values.put(MailReporter.PARAMETERS.TO.toString(), "cedric.dutoit@vd.ch");
        values.put(MailReporter.PARAMETERS.TITLE.toString(), "RCFACE Copper test");
        values.put(MailReporter.PARAMETERS.BODY.toString(), "Body<br>body2");
        new MailReporter("smtp.vd.ch", null, null, 25, "socref-copper@vd.ch<Copper>", "cedric.dutoit@vd.ch").report("aMessage", values);*/

        String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SearchResponse xmlns=\"http://www.uid.admin.ch/xmlns/uid-wse\">\n" +
                "\t<SearchResult>\n" +
                "\t\t<ratedOrganisation>\n" +
                "\t\t\t<organisation xmlns=\"http://www.bit.admin.ch/xmlns/uid-wse-f/3\">\n" +
                "\t\t\t\t<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">\n" +
                "\t\t\t\t\t<organisationIdentification xmlns=\"http://www.ech.ch/xmlns/eCH-0098-f/3\">\n" +
                "\t\t\t\t\t\t<uid xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">\n" +
                "\t\t\t\t\t\t\t<uidOrganisationIdCategorie>CHE</uidOrganisationIdCategorie>\n" +
                "\t\t\t\t\t\t\t<uidOrganisationId>103010478</uidOrganisationId>\n" +
                "\t\t\t\t\t\t</uid>\n" +
                "\t\t\t\t\t\t<OtherOrganisationId xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">\n" +
                "\t\t\t\t\t\t\t<organisationIdCategory>CH.HR</organisationIdCategory>\n" +
                "\t\t\t\t\t\t\t<organisationId>CH27030049416</organisationId>\n" +
                "\t\t\t\t\t\t</OtherOrganisationId>\n" +
                "\t\t\t\t\t\t<organisationName xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">Sopar AG</organisationName>\n" +
                "\t\t\t\t\t\t<organisationLegalName xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">Sopar AG</organisationLegalName>\n" +
                "\t\t\t\t\t\t<legalForm xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">0106</legalForm>\n" +
                "\t\t\t\t\t</organisationIdentification>\n" +
                "\t\t\t\t\t<nogaCode xmlns=\"http://www.ech.ch/xmlns/eCH-0098-f/3\">683200</nogaCode>\n" +
                "\t\t\t\t\t<contact xmlns=\"http://www.ech.ch/xmlns/eCH-0098-f/3\">\n" +
                "\t\t\t\t\t\t<address xmlns=\"http://www.ech.ch/xmlns/eCH-0046-f/3\">\n" +
                "\t\t\t\t\t\t\t<otherAddressCategory>main</otherAddressCategory>\n" +
                "\t\t\t\t\t\t\t<postalAddress>\n" +
                "\t\t\t\t\t\t\t\t<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<organisationName>Sopar AG</organisationName>\n" +
                "\t\t\t\t\t\t\t\t</organisation>\n" +
                "\t\t\t\t\t\t\t\t<addressInformation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<addressLine1>c/o Brigitte Flach</addressLine1>\n" +
                "\t\t\t\t\t\t\t\t\t<street>Spalenvorstadt</street>\n" +
                "\t\t\t\t\t\t\t\t\t<houseNumber>23</houseNumber>\n" +
                "\t\t\t\t\t\t\t\t\t<town>Basel</town>\n" +
                "\t\t\t\t\t\t\t\t\t<swissZipCode>4051</swissZipCode>\n" +
                "\t\t\t\t\t\t\t\t\t<country>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryIdISO2>CH</countryIdISO2>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryNameShort>CH</countryNameShort>\n" +
                "\t\t\t\t\t\t\t\t\t</country>\n" +
                "\t\t\t\t\t\t\t\t</addressInformation>\n" +
                "\t\t\t\t\t\t\t</postalAddress>\n" +
                "\t\t\t\t\t\t</address>\n" +
                "\t\t\t\t\t\t<address xmlns=\"http://www.ech.ch/xmlns/eCH-0046-f/3\">\n" +
                "\t\t\t\t\t\t\t<otherAddressCategory>additional</otherAddressCategory>\n" +
                "\t\t\t\t\t\t\t<postalAddress>\n" +
                "\t\t\t\t\t\t\t\t<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<organisationName>-</organisationName>\n" +
                "\t\t\t\t\t\t\t\t</organisation>\n" +
                "\t\t\t\t\t\t\t\t<addressInformation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<addressLine1>c/o Brigitte Flach</addressLine1>\n" +
                "\t\t\t\t\t\t\t\t\t<street>Spalenvorstadt</street>\n" +
                "\t\t\t\t\t\t\t\t\t<houseNumber>23</houseNumber>\n" +
                "\t\t\t\t\t\t\t\t\t<town>Basel</town>\n" +
                "\t\t\t\t\t\t\t\t\t<swissZipCode>4051</swissZipCode>\n" +
                "\t\t\t\t\t\t\t\t\t<country>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryIdISO2>CH</countryIdISO2>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryNameShort>CH</countryNameShort>\n" +
                "\t\t\t\t\t\t\t\t\t</country>\n" +
                "\t\t\t\t\t\t\t\t</addressInformation>\n" +
                "\t\t\t\t\t\t\t</postalAddress>\n" +
                "\t\t\t\t\t\t</address>\n" +
                "\t\t\t\t\t\t<address xmlns=\"http://www.ech.ch/xmlns/eCH-0046-f/3\">\n" +
                "\t\t\t\t\t\t\t<otherAddressCategory>postbox</otherAddressCategory>\n" +
                "\t\t\t\t\t\t\t<postalAddress>\n" +
                "\t\t\t\t\t\t\t\t<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<organisationName>-</organisationName>\n" +
                "\t\t\t\t\t\t\t\t</organisation>\n" +
                "\t\t\t\t\t\t\t\t<addressInformation xmlns=\"http://www.ech.ch/xmlns/eCH-0010-f/6\">\n" +
                "\t\t\t\t\t\t\t\t\t<street/>\n" +
                "\t\t\t\t\t\t\t\t\t<postOfficeBoxText/>\n" +
                "\t\t\t\t\t\t\t\t\t<town/>\n" +
                "\t\t\t\t\t\t\t\t\t<swissZipCode>4003</swissZipCode>\n" +
                "\t\t\t\t\t\t\t\t\t<country>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryIdISO2>CH</countryIdISO2>\n" +
                "\t\t\t\t\t\t\t\t\t\t<countryNameShort>CH</countryNameShort>\n" +
                "\t\t\t\t\t\t\t\t\t</country>\n" +
                "\t\t\t\t\t\t\t\t</addressInformation>\n" +
                "\t\t\t\t\t\t\t</postalAddress>\n" +
                "\t\t\t\t\t\t</address>\n" +
                "\t\t\t\t\t</contact>\n" +
                "\t\t\t\t\t<languageOfCorrespondance xmlns=\"http://www.ech.ch/xmlns/eCH-0098-f/3\">DE</languageOfCorrespondance>\n" +
                "\t\t\t\t</organisation>\n" +
                "\t\t\t\t<uidregInformation xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">\n" +
                "\t\t\t\t\t<uidregStatusEnterpriseDetail>3</uidregStatusEnterpriseDetail>\n" +
                "\t\t\t\t\t<uidregPublicStatus>1</uidregPublicStatus>\n" +
                "\t\t\t\t\t<uidregOrganisationType>1</uidregOrganisationType>\n" +
                "\t\t\t\t\t<uidregSource>\n" +
                "\t\t\t\t\t\t<uidOrganisationIdCategorie xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">CHE</uidOrganisationIdCategorie>\n" +
                "\t\t\t\t\t\t<uidOrganisationId xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">164633562</uidOrganisationId>\n" +
                "\t\t\t\t\t</uidregSource>\n" +
                "\t\t\t\t\t<uidregSource>\n" +
                "\t\t\t\t\t\t<uidOrganisationIdCategorie xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">CHE</uidOrganisationIdCategorie>\n" +
                "\t\t\t\t\t\t<uidOrganisationId xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">115117479</uidOrganisationId>\n" +
                "\t\t\t\t\t</uidregSource>\n" +
                "\t\t\t\t\t<uidregSource>\n" +
                "\t\t\t\t\t\t<uidOrganisationIdCategorie xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">CHE</uidOrganisationIdCategorie>\n" +
                "\t\t\t\t\t\t<uidOrganisationId xmlns=\"http://www.ech.ch/xmlns/eCH-0097-f/2\">115136181</uidOrganisationId>\n" +
                "\t\t\t\t\t</uidregSource>\n" +
                "\t\t\t\t</uidregInformation>\n" +
                "\t\t\t\t<commercialRegisterInformation xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">\n" +
                "\t\t\t\t\t<commercialRegisterStatus>2</commercialRegisterStatus>\n" +
                "\t\t\t\t\t<commercialRegisterEntryStatus>1</commercialRegisterEntryStatus>\n" +
                "\t\t\t\t\t<commercialRegisterEntryDate>1949-11-07</commercialRegisterEntryDate>\n" +
                "\t\t\t\t\t<commercialRegisterEnterpriseType>1</commercialRegisterEnterpriseType>\n" +
                "\t\t\t\t</commercialRegisterInformation>\n" +
                "\t\t\t\t<organisationMunicipality xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">\n" +
                "\t\t\t\t\t<municipalityId xmlns=\"http://www.ech.ch/xmlns/eCH-0007-f/6\">2701</municipalityId>\n" +
                "\t\t\t\t\t<municipalityName xmlns=\"http://www.ech.ch/xmlns/eCH-0007-f/6\">Basel</municipalityName>\n" +
                "\t\t\t\t</organisationMunicipality>\n" +
                "\t\t\t\t<cantonAbbreviationMainAddress xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">BS</cantonAbbreviationMainAddress>\n" +
                "\t\t\t\t<cantonAbbreviationAdditionalAddress xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">BS</cantonAbbreviationAdditionalAddress>\n" +
                "\t\t\t</organisation>\n" +
                "\t\t\t<toto:rating xmlns=\"http://www.bit.admin.ch/xmlns/uid-wse-f/3\">100</rating>\n" +
                "\t\t</ratedOrganisation>\n" +
                "\t</SearchResult>\n" +
                "</SearchResponse>\n";


        String xml2 = extractIDEWSV3_Valid(xml);
        System.out.println(xml2);
    }

    public static String extractIDEWSV3_Valid(String xml) {
        // Capture "organisation" outer balise, do not dto prefixed namespace.
        Matcher match = Pattern.compile("(?<xml2><organisation.*)<(.*?:)?rating", Pattern.DOTALL).matcher(xml);
        if (match.find()) {
            String xml2 = match.group("xml2");
            xml2 = xml2.replace("<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">", "<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0098-f/3\">");
            xml2 = xml2.replace("<organisation xmlns=\"http://www.bit.admin.ch/xmlns/uid-wse-f/3\">", "<organisation xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">");
            //xml2 = "<eCH_0108_3:organisationRoot xmlns:eCH_0108_3=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">" + xml2 + "</eCH_0108_3:organisationRoot>";
            xml2 = "<organisationRoot xmlns=\"http://www.ech.ch/xmlns/eCH-0108-f/3\">" + xml2 + "</organisationRoot>";

            String xml3 = convertDefaultNamespaceToPrefixed(xml2);


            // Add prolog
            if (xml.startsWith("<?xml")) {
                xml3 = xml.split("\n")[0] + "\r\n" + xml3;
            }


            return xml3;
        }

        return xml;
    }

    private static String convertDefaultNamespaceToPrefixed(String xml2) {
        // Convert default namespace to prefixed
        Map<String, String> prefixes = new HashMap<>();
        Map<Integer, String> prefixByLevels = new HashMap<>();
        int noLevel=0;
        boolean inTag=false;
        StringBuilder sb = new StringBuilder();
        String tag="";
        String prefix="dummy";
        for (int i=0; i<xml2.length(); i++) {
            char c = xml2.charAt(i);

            if (c=='<') {
                // Tag start
                inTag=true;
                tag="";
            } else if (c=='>') {
                // Tag end: write tag or closing tag
                inTag = false;
                if (tag.charAt(0)=='/') {
                    // Closing tag
                    prefix = prefixByLevels.get(noLevel);
                    sb.append("</"+prefix+":" + tag.substring(1) + ">");
                    noLevel--;
                } else {
                    noLevel++;

                    // Opening tag
                    int p1 = tag.indexOf("xmlns");
                    if (p1>0) {
                        int p2 = tag.lastIndexOf("\"");
                        String uri = tag.substring(tag.indexOf('"')+1, p2);
                        tag = tag.substring(0,p1);
                        prefix = prefixes.get(uri);
                        if (prefix==null) {
                            prefix="ns"+prefixes.size();
                            prefixes.put(uri, prefix);
                            tag = tag+" xmlns:"+prefix+"=\""+uri+"\"";
                        }
                    }
                    prefixByLevels.put(noLevel, prefix); // (also copy default if not found)
                    sb.append("<"+prefix +":" + tag + ">");
                    if (tag.endsWith("/")) noLevel--;
                }
            } else if (inTag) {
                tag+=c;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}