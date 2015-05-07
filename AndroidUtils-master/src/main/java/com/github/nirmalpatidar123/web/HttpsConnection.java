package com.github.nirmalpatidar123.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

public class HttpsConnection {

    private static HttpsConnection httpConn = null;
    private static HttpsConnection httpConnSecure = null;

    private int defaultTimeOut = 30000;
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";

    private Context context;
    private String keyStorePassword;
    private int keyStoreRawResourceId = 0;

    private static boolean isAppLogShown = true;

    private HttpsConnection() {
    }

    /**
     * @param context
     * @param keyStoreRawResourceId
     * @param keystorePassword
     */
    private HttpsConnection(Context context, int keyStoreRawResourceId, String keystorePassword) {
        this.context = context;
        this.keyStoreRawResourceId = keyStoreRawResourceId;
        this.keyStorePassword = keystorePassword;
    }

    public static void disableAppLog() {
        isAppLogShown = false;
    }

    public synchronized static HttpsConnection createHttpConnection() {

        if (httpConn == null) {
            httpConn = new HttpsConnection();
        }
        return httpConn;
    }

    /**
     * @param context
     * @param keyStoreRawResourceId
     * @param keystorePassword
     * @return
     */
    public synchronized static HttpsConnection createHttpConnection(Context context, int keyStoreRawResourceId,
                                                                    String keystorePassword) {

        if (httpConnSecure == null) {
            httpConnSecure = new HttpsConnection(context, keyStoreRawResourceId, keystorePassword);
        }
        return httpConnSecure;
    }

    private synchronized DefaultHttpClient createHttpsClient() {

        HttpParams httpParameters = getHttpParams();

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        DefaultHttpClient client = new DefaultHttpClient(httpParameters);

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory
                .setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 443));

        // added by sanjay on 23 Jan 2013

        registry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));

        SingleClientConnManager mgr = new SingleClientConnManager(
                client.getParams(), registry);
        DefaultHttpClient httpClient = new DefaultHttpClient(mgr,
                client.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        return httpClient;
    }

    private synchronized BasicHttpParams getHttpParams() {
        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                defaultTimeOut);
        HttpConnectionParams.setSoTimeout(httpParameters, defaultTimeOut * 2);
        return httpParameters;
    }

    public synchronized String getResponseFromHttpGetRequestUTF(String url) {

        try {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            params.setBooleanParameter("http.protocol.expect-continue", false);
            HttpClient httpClient = getHttpClient();
            HttpGet getRequest = new HttpGet(url);

            HttpResponse response = httpClient.execute(getRequest);

            HttpEntity entity = response.getEntity();

            return EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return
     */
    public HttpClient getHttpClient() {
        HttpClient httpClient = null;
        if (keyStoreRawResourceId == 0) {
            httpClient = createHttpsClient();
        } else {
            HttpParams httpParams = getHttpParams();
            httpClient = new DefaultHttpClient(doSSLValidate(httpParams), httpParams);
        }
        return httpClient;
    }

    public synchronized String getResponseFromHttpPostRequest(String URL,
                                                              String jsonRequest) {
        if (isAppLogShown) {
            Log.e("URL:", URL + "");
            Log.e("jsonRequest:", jsonRequest + "");
        }
        String result = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            HttpClient httpClient = getHttpClient();

            // use POST method for data sending
            httpPost = new HttpPost(URL);

            StringEntity stringEntity = new StringEntity(jsonRequest);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));

            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCode UploadLog ", "" + statusCode);

            InputStream inStream = response.getEntity().getContent();
            result = getResultFromStream(inStream);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                return result;
            } else
                Log.e("ResultNotFound From Res",""+statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param formBodyPartList
     * @return String
     */
    public String getResponseFromHttpPostUrlFormBodyPartRequest(
            final String URL, final List<FormBodyPart> formBodyPartList) {

        if (isAppLogShown) {
            Log.e("URL:", URL + "");
            Log.e("List<FormBodyPart>:", formBodyPartList + "");
        }

        String result = null;
        HttpClient httpClient = null;
        MultipartEntity entity = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        InputStream in = null;
        HttpResponse response = null;
        try {
            httpClient = getHttpClient();
            // use POST method for data sending
            httpPost = new HttpPost(URL);
            // send data in Multipart
            entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            Iterator<FormBodyPart> fbpIt = formBodyPartList.iterator();
            while (fbpIt.hasNext()) {
                FormBodyPart fbp = fbpIt.next();
                entity.addPart(fbp);
            }

            // // Add sthe json string as a entry body
            // entity.addPart("requestJSON", new StringBody(json));
            // // key of the file is uploadfile and value is file
            // File f = new File(srcFile);
            // if (f.exists()) {
            // Utility.printLog("Uploaded Log File Size is",
            // (((f.length() / 1024)) / 1024) + " MB");
            // entity.addPart("deviceData", new FileBody(f));
            // } else
            // entity.addPart("deviceData", new StringBody(""));
            httpPost.setEntity(entity);
            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCode UplOad Log ", "" + statusCode);
            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            }
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (response != null)
                    response = null;
                if (localContext != null)
                    localContext = null;
                if (entity != null)
                    entity = null;
                if (httpPost != null)
                    httpPost = null;
                if (httpClient != null)
                    httpClient = null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    /**
     * @param URL
     * @return
     */
    public synchronized String getResponseFromHttpPatchRequest(String URL) {

        String result = null;
        try {
            Log.v("getResHttpPatchReq", "URL: " + URL);
            DefaultHttpClient client = createHttpsClient();
            HttpPatch getRequest = new HttpPatch(URL);

            getRequest.setHeader("X-HTTP-Method-Override", "PATCH");

            getRequest.setHeader("Accept", "application/json");
            getRequest.setHeader("Content-type", "application/json");

            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCode UplOadLog ", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", ""+ statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param nameValuePairs
     * @return String Response
     */
    public String getResponseFromHttpPostUrlEncodedRequest(final String URL,
                                                           final List<NameValuePair> nameValuePairs) {

        if (isAppLogShown) {
            Log.e("URL:", URL + "");
            // //Log.e("nameValuePairs:", nameValuePairs + "");
        }
        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            httpClient = getHttpClient();
            httpPost = new HttpPost(URL);

            UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(
                    nameValuePairs, HTTP.UTF_8);
            httpPost.setEntity(p_entity);
            localContext = new BasicHttpContext();
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            InputStream inStream = response.getEntity().getContent();
            result = getResultFromStream(inStream);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                return result;
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }


    /**
     * @param URL
     * @param key
     * @param value
     * @return
     */
    public synchronized String getResponseFromHttpHeaderGetRequest(String URL,
                                                                   String key, String value) {

        String result = null;
        try {

            if (isAppLogShown) {
                Log.e("URL:", URL + "");
                // //Log.e("nameValuePairs:", nameValuePairs + "");
            }

            HttpClient client = getHttpClient();
            HttpGet getRequest = new HttpGet(URL);

            getRequest.setHeader("Accept", "application/json");
            getRequest.setHeader("Content-type", "application/json");
            getRequest.setHeader(key, value);

            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UploadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }


    /**
     * @param stream
     * @return
     * @throws Exception
     */
    private synchronized String getResultFromStream(InputStream stream)
            throws Exception {

        StringBuffer buffer = new StringBuffer();
        int ch = 0;
        while ((ch = stream.read()) != -1)
            buffer.append((char) ch);
        String result = buffer.toString().trim();
        if (isAppLogShown) {
            Log.e("getResultFromStream", result + "");
        }
        stream.close();
        return result;
    }

    public static synchronized String getResultFromInputStream(
            InputStream stream) throws Exception {

        StringBuffer buffer = new StringBuffer();
        int ch = 0;
        while ((ch = stream.read()) != -1)
            buffer.append((char) ch);
        String result = buffer.toString().trim();
        if (isAppLogShown) {
            Log.e("Upload Log Response", result);
        }
        stream.close();
        return result;
    }

    public synchronized String getResponseFromHttpGetRequest(String URL) {

        String result = null;
        try {
            Log.v("getRespFrmHttpGetReq", "URL: " + URL);
            HttpClient client = getHttpClient();
            HttpGet getRequest = new HttpGet(URL);
            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param headerMap
     * @param requsetParams
     * @return String
     */
    public synchronized String getResponseFromHttpHeaderPostRequest(String URL,
                                                                    Map<String, String> headerMap,
                                                                    final String contentType,
                                                                    final String requsetParams) {

        if (isAppLogShown) {
            Log.e("URL", URL + "");
            Log.e("headerMap", headerMap + "");
            Log.e("requsetParams", requsetParams + "");
        }

        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPost = new HttpPost(URL);

            httpPost.setHeader("Accept", contentType);
            httpPost.setHeader("Content-type", contentType);

            Set<String> keys = headerMap.keySet();
            for (String key : keys) {
                String value = headerMap.get(key);
                httpPost.setHeader(key, value);
            }

            StringEntity stringEntity = new StringEntity(requsetParams);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    contentType));
            httpPost.setEntity(stringEntity);

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UploadLog", "" + statusCode);

            InputStream inStream = response.getEntity().getContent();
            result = getResultFromStream(inStream);

            // check the response code is HTTP_OK - 200
            // if (statusCode == 200) {
            // InputStream inStream = response.getEntity().getContent();
            // result = getResultFromStream(inStream);
            // } else {
            // Log.e("Result Not Found From Response:", "StatusCode: "
            // + statusCode);
            // }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param key
     * @param value
     * @return String
     */
    public synchronized String getResponseFromHttpHeaderPostRequest(String URL,
                                                                    String key, String value) {

        if (isAppLogShown) {
            Log.e("URL", URL + "");
            Log.e("key", key + "");
            Log.e("value", value + "");
        }

        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPost = new HttpPost(URL);

            // StringEntity stringEntity = new StringEntity(jsonRequest);
            // stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
            // "application/json"));
            //
            // httpPost.setEntity(stringEntity);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            httpPost.setHeader(key, value);

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * *
     *
     * @param URL
     * @param headerMap
     * @return
     */
    public synchronized String getResponseFromHttpHeaderPostRequest(String URL,
                                                                    Map<String, String> headerMap) {

        if (isAppLogShown) {
            Log.e("URL", URL + "");
        }

        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPost = new HttpPost(URL);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Set<String> keys = headerMap.keySet();
            for (String key : keys) {
                String value = headerMap.get(key);
                httpPost.setHeader(key, value);
            }

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }


    /**
     * @param URL
     * @param headerMap
     * @param requsetParams
     * @return String
     */
    public synchronized HttpResponse getHttpResponseFromHttpHeaderPostRequest(
            String URL, Map<String, String> headerMap,
            final String contentType, final String requsetParams) {

        if (isAppLogShown) {
            Log.e("URL", URL + "");
            Log.e("headerMap", headerMap + "");
            Log.e("requsetParams", requsetParams + "");
        }

        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPost = new HttpPost(URL);

            httpPost.setHeader("Accept", contentType);
            httpPost.setHeader("Content-type", contentType);

            Set<String> keys = headerMap.keySet();
            for (String key : keys) {
                String value = headerMap.get(key);
                httpPost.setHeader(key, value);
            }

            StringEntity stringEntity = new StringEntity(requsetParams);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    contentType));
            httpPost.setEntity(stringEntity);

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.e("StatusCod for UplOadLog", "" + statusCode);

            if (statusCode == 201)
                return response;
            else {
                InputStream inStream = response.getEntity().getContent();
                Log.e("StatusCodeAndResponse", statusCode + " : \n"
                        + getResultFromStream(inStream));
            }

            // Header[] headers = response.getAllHeaders();
            // for (int i = 0; i < headers.length; i++) {
            // String n = headers[i].getName();
            // String val = headers[i].getValue();
            // Log.e(n + "", val + "");
            // }
        } catch (Exception e) {
            e.printStackTrace();
            response = null;
        }
        return response;
    }

    /**
     * @param URL
     * @param headerMap
     * @param requsetParams
     * @return String
     */
    public synchronized String getHttpResponseFromHttpHeaderPutRequest(
            String URL, Map<String, String> headerMap,
            final String contentType, final String requsetParams) {

        if (isAppLogShown) {
            Log.e("URL", URL + "");
            Log.e("headerMap", headerMap + "");
            Log.e("requsetParams", requsetParams + "");
        }

        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPut httpPut = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPut = new HttpPut(URL);

            httpPut.setHeader("Accept", contentType);
            httpPut.setHeader("Content-type", contentType);

            Set<String> keys = headerMap.keySet();
            for (String key : keys) {
                String value = headerMap.get(key);
                httpPut.setHeader(key, value);
            }

            StringEntity stringEntity = new StringEntity(requsetParams);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    contentType));
            httpPut.setEntity(stringEntity);

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPut, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.e("StatusCod for UplOadLog", "" + statusCode);

            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                return getResultFromStream(inStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = null;
        }
        return null;
    }

    /**
     * @param URL
     * @param headerMap
     * @return String
     */
    public synchronized String getResponseFromHttpHeaderGetRequest(String URL,
                                                                   Map<String, String> headerMap) {

        String result = null;
        try {

            if (isAppLogShown) {
                Log.e("URL:", URL + "");
                // //Log.e("nameValuePairs:", nameValuePairs + "");
            }

            HttpClient client = getHttpClient();
            HttpGet getRequest = new HttpGet(URL);

            getRequest.setHeader("Accept", "application/json");
            getRequest.setHeader("Content-type", "application/json");

            Set<String> keys = headerMap.keySet();
            for (String key : keys) {
                String value = headerMap.get(key);
                getRequest.setHeader(key, value);
            }

            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param jsonRequest
     * @return String
     */
    public synchronized String getResponseFromHttpPostXMLRequest(String URL,
                                                                 String jsonRequest) {

        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            httpPost = new HttpPost(URL);

            StringEntity stringEntity = new StringEntity(jsonRequest);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/xml"));

            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/xml");

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(httpPost, localContext);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * @param URL
     * @param jsonString
     * @return String result
     */
    public synchronized String getResponseFromHttpEncodedGetRequest(
            final String URL, final String jsonString) {

        String result = null;
        try {
            String encodedJsonString = URLEncoder.encode(jsonString, "UTF-8");

            HttpClient client = getHttpClient();
            HttpGet getRequest = new HttpGet(URL + encodedJsonString);
            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200) {
                InputStream inStream = response.getEntity().getContent();
                result = getResultFromStream(inStream);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public synchronized InputStream getResponseStreamFromHttpGetRequest(
            String URL) {
        InputStream inStream = null;
        try {
            HttpClient client = getHttpClient();
            HttpGet getRequest = new HttpGet(URL);
            HttpResponse response = client.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            // check the response code is HTTP_OK - 200
            if (statusCode == 200)
                inStream = response.getEntity().getContent();
            else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            inStream = null;
        }
        return inStream;
    }

    /**
     * @param multipartEntity
     * @param url
     * @param contentType     can be null also
     * @return String (response)
     */
    public synchronized String openHttpConnection_post_All_Multipart(
            MultipartEntity multipartEntity, String url, String contentType) {
        try {
            if (isAppLogShown) {
                Log.e("URL", url + "");
            }

            String result = null;
            InputStream responseStream = null;
            // Create a new HttpClient and Post Header
            HttpClient httpClient = getHttpClient();

            HttpPost httppost = new HttpPost(url);

            // HttpPost httppost = new
            // HttpPost("http://10.10.1.147/HTML5/index.php");

            // httppost.setHeader("Content-Type", header);
            // httppost.setHeader("Content-Type", header);
            if (contentType != null)
                httppost.setHeader("Content-type", contentType);
            httppost.setEntity(multipartEntity);

            // Execute HTTP Post Request
            System.out.println("Connecting...");
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient
                    .execute(httppost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.i("StatusCod for UplOadLog", "" + statusCode);

            if (statusCode == 200) {
                responseStream = httpResponse.getEntity().getContent();
                result = getResultFromStream(responseStream);
                System.out.println("RESULT" + result);
            } else
                Log.e("ResultNotFound From Res", "StatusCode: "
                        + statusCode);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param URL
     * @param jsonRequest
     * @return ApiResponse
     */
    public synchronized ApiResponse getResponseFromHttpPatchRequest(String URL,
                                                                    String jsonRequest) {
        Log.e("URL:", URL + "");
        Log.e("jsonRequest:", jsonRequest + "");
        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPatch mHttpPatch = null;
        HttpResponse response = null;

        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            mHttpPatch = new HttpPatch(URL);

            StringEntity stringEntity = new StringEntity(jsonRequest);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));

            mHttpPatch.setEntity(stringEntity);
            mHttpPatch.setHeader("Accept", "application/json");
            mHttpPatch.setHeader("Content-type", "application/json");

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(mHttpPatch, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            Log.i("StatusCod for UplOadLog", "" + statusCode);

            InputStream inStream = response.getEntity().getContent();
            result = getResultFromStream(inStream);

            // check the response code is HTTP_OK - 200
            return new ApiResponse(result, statusCode);

            // if (statusCode == 200) {
            // return result;
            // } else
            // Log.e("Result Not Found From Response:", "StatusCode: "
            // + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * authHeader (cab be null if not then set as)
     * mHttpPatch.setHeader("Authorization", authHeader);
     *
     * @param URL
     * @param authHeader
     * @param primaryKey
     * @param jsonRequest
     * @return ApiResponse
     */
    public synchronized ApiResponse getResponseFromHttpPatchRequest(String URL,
                                                                    String authHeader, int primaryKey, String jsonRequest) {
        URL = URL + primaryKey + "/";
        Log.e("URL:", URL + "");
        Log.e("jsonRequest:", jsonRequest + "");
        String result = null;
        HttpClient httpClient = null;
        HttpContext localContext = null;
        HttpPatch mHttpPatch = null;
        HttpResponse response = null;

        try {
            // client which actually send request
            httpClient = getHttpClient();
            // use POST method for data sending

            mHttpPatch = new HttpPatch(URL);

            StringEntity stringEntity = new StringEntity(jsonRequest);
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));

            mHttpPatch.setEntity(stringEntity);
            mHttpPatch.setHeader("Accept", "application/json");
            mHttpPatch.setHeader("Content-type", "application/json");

            if (!TextUtils.isEmpty(authHeader)) {
                mHttpPatch.setHeader("Authorization", authHeader);
            }

            localContext = new BasicHttpContext();
            // sending data to erver
            response = httpClient.execute(mHttpPatch, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            Log.i("StatusCod for UplOadLog", "" + statusCode);

            InputStream inStream = response.getEntity().getContent();
            result = getResultFromStream(inStream);

            // check the response code is HTTP_OK - 200
            return new ApiResponse(result, statusCode);

            // if (statusCode == 200) {
            // return result;
            // } else
            // Log.e("Result Not Found From Response:", "StatusCode: "
            // + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*************************************************************/

    /**
     * @return SSLSocketFactory object passed into paySmard core class to call
     * web apis with secure ssl.
     */
    private SSLSocketFactory newSslSocketFactory() {

        try {
            /**
             * Get an instance of the Bouncy Castle KeyStore format
             */
            KeyStore trusted = KeyStore.getInstance("BKS");
            /**
             * Get the raw resource, which contains the keystore with your
             * trusted certificates (root and any intermediate certs)
             */

            InputStream in = context.getResources().openRawResource(
                    keyStoreRawResourceId);
            try {
                /**
                 * Initialize the keystore with the provided trusted
                 * certificates Also provide the password of the keystore
                 */
                // trusted.load(in, "my_password".toCharArray());

                if (!TextUtils.isEmpty(keyStorePassword)) {
                    trusted.load(in, keyStorePassword.toCharArray());
                } else {
                    trusted.load(in, null);
                }
            } finally {
                in.close();
            }
            /**
             * Pass the keystore to the SSLSocketFactory. The factory is
             * responsible for the verification of the server certificate
             */
            MySSLSocketFactory sf = new MySSLSocketFactory(trusted);
            /**
             * Hostname verification from certificate
             * http://hc.apache.org/httpcomponents
             * -client-ga/tutorial/html/connmgmt.html#d4e506
             */
            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            return sf;
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register for port 443 our SSLSocketFactory with our keystore to the
     * ConnectionManager
     */
    protected SingleClientConnManager doSSLValidate(HttpParams httpParams) {

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));

        registry.register(new Scheme("https", newSslSocketFactory(), 443));
        httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
        httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                new ConnPerRouteBean(1));
        httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, "utf8");
        return new SingleClientConnManager(httpParams,
                registry);
    }

    /*
     * Without certificate
     *
     * @param httpParams
     */
    private void validate(HttpParams httpParams) {

        httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
        httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                new ConnPerRouteBean(1));
        httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

    }
    /**************************************************************/
}