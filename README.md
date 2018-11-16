# VISION

OBJECTIVE - 

This app allows the user to translate a piece of foreign language text written within an image.

USEFULNESS - 

1. This app can translate a safety sign,food menu or any other foreign language text written within an image and can also convert the translated text into speech.

2. It can also be used for extracting url for websites or email ids from an image.

TECHNOLOGIES USED - 

Amazon Web Services(AWS)  - It provides the APIs which are integrated with the Android application for providing state of the art machine learning capabilities.This project uses three AWS Ml APIs.

1. AWS Rekognition API - It is used for extracting the text from the image.

2. AWS Translate API - It is used to convert the foreign language text into english.

3. AWS Polly API - It is used for providing Text to Speech capabilities.

Android - All the AWS Machine Learning APIs are integrated into an Android application which makes it portable,easy to use and user friendly.
                       
TECHNICAL APPROACH

This app allows the user to click an image or choose an already existing image from the file picker.
After choosing the image, a network request is made to the AWS server. This request is made on the background thread (as network request can’t be made on the Main thread) using an AsyncTask.
The results of these requests are gathered and what the user sees and hears as an end result is the translated text.

IMPORTANT NOTE - 

The Amazon Translate API can translate the text for only 6 languages currently — Arabic (ar) , Chinese (zh) ,French (fr) , German (de) , Portuguese (pt) and Spanish (es) .

VIDEO LINK - 

https://www.youtube.com/watch?v=0qzGkjEl7FY&frags=pl%2Cwn

