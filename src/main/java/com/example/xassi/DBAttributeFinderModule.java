/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.xassi;

import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sample attribute finder module. Actually this must be the point that calls to K-Market user store
 * and retrieve the customer attributes. But here we are not talking any user store and values has
 * been hard corded in the source.
 */
public class DBAttributeFinderModule extends AttributeFinderModule {

    private URI defaultSubjectId;
    public static final List<Long> responseTimes = new ArrayList<Long>();
    public DBAttributeFinderModule() {

        try {
            defaultSubjectId = new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        } catch (URISyntaxException e) {
            // ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }

    @Override
    public Set getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("urn:med:vaccine:credential-type");
        ids.add("unipi:degreeType");
        ids.add("org:developer:developer-type");
        ids.add("org:attribute");
        for (int i = 1; i <= 63; i++) {
            ids.add("org:attribute:" + i);
        }
        return ids;
    }

    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
            URI category, EvaluationCtx context) {

        // retrieve the subject id from the request
        String subjectId = "";
        EvaluationResult result =
                context.getAttribute(attributeType, defaultSubjectId, issuer, category);
        if (result != null && result.getAttributeValue() != null
                && result.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) result.getAttributeValue();
            if (bagAttribute.size() > 0) {
                subjectId = ((AttributeValue) bagAttribute.iterator().next()).encode();
            }
        }

        List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
        try {
            String endpoint1 = "subjectAttribute";
            String endpoint2 = "distributed";
            String apiUrl = "https://d1-tutorial.giulio-piva-it.workers.dev/" + endpoint2
                    + "?subjectId=" + subjectId + "&AttributeIdentifier=" + attributeId.toString()
                    + "&AttributeCategory=" + category.toString();
            URL url = new URL(apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method (GET, POST, etc.)
            connection.setRequestMethod("GET");

            // Get the response code
            long startTime = System.currentTimeMillis();
            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            responseTimes.add(duration);
            // System.out.println("Response time: " + duration + "ms");

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());

                // Now you can work with the JSON data
                // System.out.println("Response: " + jsonResponse.toString(2)); // Pretty print JSON
                JSONArray resultList = jsonResponse.getJSONArray("results");
                for (int i = 0; i < resultList.length(); i++) {
                    JSONObject resultObject = resultList.getJSONObject(i);
                    attributeValues
                            .add(new StringAttribute(resultObject.getString("AttributeValue")));
                }
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
    }

    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

}
