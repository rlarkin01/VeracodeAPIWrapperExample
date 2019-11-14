package hello;

import com.veracode.apiwrapper.wrappers.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;

class Veracode {

    static String retrieveResults(String appName, String Sandbox) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        String appId = getApplicationId(appName);
        String buildId = getBuildId(appName);

        ResultsAPIWrapper wrapper = new ResultsAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
        String xml = wrapper.detailedReport(buildId);

        return xml;
    }

    static byte[] retrieveResultsPdf(String appName, String Sandbox) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        String appId = getApplicationId(appName);
        String buildId = getBuildId(appName);

        ResultsAPIWrapper wrapper = new ResultsAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
        byte[] pdf = wrapper.detailedReportPdf(buildId);

        return pdf;
    }

    static String getBuildId(String appName) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        ResultsAPIWrapper wrapper = new ResultsAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
        String xml = wrapper.getAppBuilds();

        return findInXMLString(xml, String.format("/applicationbuilds/application[contains(@app_name,'%s')]/build/@build_id", appName));
    }

    static String uploadAndScanWithVeracode(String file, String appName, String sandbox) throws Exception {
        String appId = getApplicationId(appName);
        String status = uploadApplication(appId, file);
        if (status.equals("Uploaded")) {
            status = beginScan(appId);
        }
        // TODO wait for results?? how to return to Front End...
        // TODO return submission status and security error info
        // TODO add vulns to project(s) or scan something else

        return status;
    }

    private static String beginScan(String appId) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        UploadAPIWrapper wrapper = new UploadAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
//        String xml = wrapper.beginPreScan(appId, null, "auto_scan", "scan_all_nonfatal_top_level_modules");
        String xml = wrapper.beginPreScan(appId, null, "auto_scan");

        return findInXMLString(xml, "/buildinfo/build/analysis_unit/@status");
    }

    private static String uploadApplication(String appId, String file) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        UploadAPIWrapper wrapper = new UploadAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
        String xml = wrapper.uploadFile(appId, file);

        return findInXMLString(xml, "/filelist/file/@file_status");
    }

    private static String getApplicationId(String appName) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        UploadAPIWrapper wrapper = new UploadAPIWrapper();
        wrapper.setUpApiCredentials(Credentials.API_ID, Credentials.API_KEY);
        String xml = wrapper.getAppList();

        String appId = findInXMLString(xml, String.format("/applist/app[contains(@app_name,'%s')]/@app_id", appName));

        return appId;
    }

    private static String findInXMLString(String xmlDoc, String expression) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlDoc)));

        XPathExpression exp = XPathFactory.newInstance().newXPath().compile(expression);

        NodeList nl = (NodeList)exp.evaluate(doc.getFirstChild(), XPathConstants.NODESET);

        return nl.item(0).getTextContent();
    }
}

