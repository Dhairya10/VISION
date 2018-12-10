# VISION

SHORT DESCRIPTION - 

This app enables the user to comprehend and translate (if written in foreign language) the text written within an image. 

USEFULNESS - 

1. This app can translate a safety sign,food menu or any other foreign language text written within an image and can also convert the translated text into speech.

Example - If there is a safety sign in China (english speaking population is pretty low) which says "High Current", then a user who doesn't understand chinese won't be able to comprehend it and it is not easy to type chinese either so what the user can do is that he/she can just take a picture of that safety sign and the app will convert the chinese text into english and will convert that english text into speech as well. So the user will hear the words "High Current". 

2. It can also be used for extracting url for websites or email ids from an image.

Example - If we see an advertisement in newspaper then we just need to take an image of that ad using this app and we can easily navigate to their website without typing the whole web address.

TECHNOLOGIES USED - 

Amazon Web Services(AWS)  - It provides the APIs which are integrated with the Android application for providing state of the art machine learning capabilities. This project uses three AWS ML APIs. All these three APIs use Deep Learning framework.

1. AWS Rekognition API - It is used for extracting the text from the image.

2. AWS Translate API - It is used to convert the foreign language text into english.

3. AWS Polly API - It is used for providing Text to Speech capabilities.

Android - All the AWS Machine Learning APIs are integrated into an Android application which makes it portable,easy to use and user friendly.
                       
TECHNICAL APPROACH

This app allows the user to click an image or choose an already existing image from the file picker.
After choosing the image, a network request is made to the AWS server. This request is made on the background thread (as network request can’t be made on the main thread) using an AsyncTask.
The results of these requests are gathered and what the user sees and hears as an end result is the translated text.

IMPORTANT NOTES - 

* The Amazon Translate API can translate the text for only 6 languages currently — Arabic (ar) , Chinese (zh) ,French (fr) ,     German (de) , Portuguese (pt) and Spanish (es) .
* AWS Rekognition can detect upto 50 words at a time from an image, and currently it can't detect handwritten text.


VIDEO LINK - 

https://www.youtube.com/watch?v=0qzGkjEl7FY&frags=pl%2Cwn


| https://github.com/Dhairya10/VISION/blob/master/vision.apk | Alpha | This app enables the user to comprehend and translate (if written in foreign language) the text written within an image | Apache License 2.0 | India | https://github.com/Dhairya10 |
