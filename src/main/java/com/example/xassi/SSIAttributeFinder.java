package com.example.xassi;

import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SSIAttributeFinder extends AttributeFinderModule{

    private URI defaultSubjectId;

    public SSIAttributeFinder() {

        try {
            defaultSubjectId = new URI("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }

    @Override
    public Set getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        ids.add("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        return ids;
    }

    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                                            URI category, EvaluationCtx context) {
        System.out.println("SSIAttributeFinder.findAttribute");
        String roleName = null;
        List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

        EvaluationResult result = context.getAttribute(attributeType, defaultSubjectId, issuer, category);
        System.out.println(result.getAttributeValue().toString());

        if(result != null && result.getAttributeValue() != null && result.getAttributeValue().isBag()){
            BagAttribute bagAttribute = (BagAttribute) result.getAttributeValue();
            if(bagAttribute.size() > 0){
                String userName = ((AttributeValue) bagAttribute.iterator().next()).encode();
                roleName = findRole(userName);
            }
        }

        if (roleName != null) {
            attributeValues.add(new StringAttribute(roleName));
        }

        return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
    }

    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

    private String findRole(String userName){

        if(userName.equals("bob")){
            return "blue";
        } else if(userName.equals("alice")){
            return "silver";
        } else if(userName.equals("peter")){
            return "gold";
        }

        return null;
    }
}
