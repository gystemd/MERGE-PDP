package com.example.xassi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RestController
@CrossOrigin(origins = "*")
public class PDPController {
    private static Balana balana;
    private static PDP pdp;
    private static PDPConfig pdpConfig;
    private Connection conn;

    @PostConstruct
    private void init() {
        initBalana();
        pdp = getPDPNewInstance();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/resource_policy?useSSL=false";
            String user = "user1";
            String password = "password";
            conn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    // @GetMapping("/resources")

    @PostMapping("/upload")
    public String upload(@RequestBody String policy, @RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get("resources/" + file.getOriginalFilename());
            Files.write(path, bytes);

            message = "Successfully uploaded " + file.getOriginalFilename() + "!";
        } catch (IOException e) {
            message = "Failed to upload " + file.getOriginalFilename() + "!";
        }

        return message;
    }

    @PostMapping("/evaluate")
    public boolean evaluate(@RequestBody String request) {
        String processedRequest = request.substring(1, request.length() - 1).replace("\\", "");

        System.out.println(processedRequest);
        System.out.println();
        String response = pdp.evaluate(processedRequest);

        System.out.println("Response:" + response);
        try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(response));
            AbstractResult result = responseCtx.getResults().iterator().next();
            System.out.println("Result : " + result.getDecision());
            if (AbstractResult.DECISION_PERMIT == result.getDecision()) {
                return true;
            } else {
                return false;
                // List<Advice> advices = result.getAdvices();
                // for (Advice advice : advices) {
                // List<AttributeAssignment> assignments = advice.getAssignments();
                // for (AttributeAssignment assignment : assignments) {
                // System.out.println("Advice : " + assignment.getContent() + "\n\n");
                // }
                // }
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
        finderModules.add(new SSIAttributeFinder());
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
}
