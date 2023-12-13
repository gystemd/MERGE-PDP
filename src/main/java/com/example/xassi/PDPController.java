package com.example.xassi;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wso2.balana.Balana;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import jakarta.annotation.PostConstruct;

@RestController
@CrossOrigin(origins = "*")
public class PDPController {
    private static Balana balana;
    private static PDP pdp;
    private static PDPConfig pdpConfig;

    @PostConstruct
    private void init() {
        initBalana();
        pdp = getPDPNewInstance();
    }

    @PostMapping("/evaluate")
    public boolean evaluate(@RequestBody String request) {
        String processedRequest= request.substring(1, request.length() - 1).replace("\\", "");
        long start = System.currentTimeMillis();
        String response = pdp.evaluate(processedRequest);
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        Long pipTime = DBAttributeFinderModule.responseTimes.stream().mapToLong(Long::longValue).sum();
        Long evaluationTime = elapsedTime - pipTime;
        appendToFile("measurements/pip.txt", String.valueOf(pipTime) + "\n" );
        appendToFile("measurements/pdp.txt", String.valueOf(evaluationTime) + "\n");

        DBAttributeFinderModule.responseTimes.clear();
        try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(response));
            AbstractResult result = responseCtx.getResults().iterator().next();
            System.out.println("Result : " + result.getDecision());
            if (AbstractResult.DECISION_PERMIT == result.getDecision()) {
                return true;
            } else {
                return false;
            }
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        return false;
    }



    private static void initBalana() {

        try {
            // using file based policy repository. so set the policy location as system property
            String policyLocation =
                    (new File(".")).getCanonicalPath() + File.separator + "policies";
            System.out.println("policy location: " + policyLocation);
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        balana = Balana.getInstance();
    }

    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @return a PDP instance
     */
    private static PDP getPDPNewInstance() {

        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();
        finderModules.add(new DBAttributeFinderModule());
        attributeFinder.setModules(finderModules);

        return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), null, true));
    }

    /**
     * Creates DOM representation of the XACML request
     *
     * @param response XACML request as a String object
     * @return XACML request as a DOM element
     */
    public static Element getXacmlResponse(String response) {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory dbf;
        Document doc;

        inputStream = new ByteArrayInputStream(response.getBytes());
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            doc = dbf.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            System.err.println("DOM of request element can not be created from String");
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("Error in closing input stream of XACML response");
            }
        }
        return doc.getDocumentElement();
    }

    private static void appendToFile(String filename, String data) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.append(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
