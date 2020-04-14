package com.datadit.vision;

import com.amazonaws.regions.Regions;
// Class for storing all the AWS Credentials
public class AWSCredentials {
    // IAM Authorisation
    private static final String COGNITO_POOL_ID =   "Enter your Your Cognito Pool ID here"; // Identity pool ID
    private static final Regions COGNITO_REGION = Regions.US_EAST_1;




    public static String getCognitoPoolId() {
        return COGNITO_POOL_ID;
    }

    public static Regions getCognitoRegion() {
        return COGNITO_REGION;
    }

}
