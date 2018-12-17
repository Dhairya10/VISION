package com.datadit.vision;

import com.amazonaws.regions.Regions;
// Class for storing all the AWS Credentials
public class AWSCredentials {
    // IAM Authorisation
    private static final String COGNITO_POOL_ID =   "us-east-1:b82cb567-a0ae-4c4c-9ce1-eb4d87aaab02"; // Identity pool ID
    private static final Regions COGNITO_REGION = Regions.US_EAST_1;




    public static String getCognitoPoolId() {
        return COGNITO_POOL_ID;
    }

    public static Regions getCognitoRegion() {
        return COGNITO_REGION;
    }

}
