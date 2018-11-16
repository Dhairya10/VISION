package com.datadit.vision;

import com.amazonaws.regions.Regions;
// Class for storing all the AWS Credentials
public class AWSCredentials {
    // IAM Authorisation
    private static final String COGNITO_POOL_ID = "us-east-1:ef731981-3a33-4370-a8df-271366ca4361";
    private static final Regions COGNITO_REGION = Regions.US_EAST_1;

    public static String getCognitoPoolId() {
        return COGNITO_POOL_ID;
    }

    public static Regions getCognitoRegion() {
        return COGNITO_REGION;
    }

}
