// This file is auto-generated, don't edit it. Thanks.
package com.antgroup.antchain.openapi.antchain.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.AccountException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.*;
import org.apache.commons.io.IOUtils;

public class Client {

    /**
     * Get timestamp
     *
     * @return the string
     * @example 2006-01-02T15:04:05Z
     * @error no error throws
     */
    public static String getTimestamp() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(new Date());
    }

    /**
     * Judge if the api called success or not
     *
     * @param res the response
     * @return the boolean
     * @example true
     * @error no error throws
     */
    public static Boolean hasError(String res, String secret) throws Exception {
        JSONObject response = JSONObject.parseObject(res);
        if (response == null && response.get("response") == null) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) response.get("response");
        if (map != null && !"ok".equalsIgnoreCase(String.valueOf(map.get("result_code")))) {
            return true;
        }
        String content = extractStringToSign(res);
        String sign = sign(content, secret);
        if (!response.get("sign").equals(sign)) {
            return true;
        }
        return false;
    }

    /**
     * extract string form response to sign
     *
     * @param responseString the response string
     * @return the string
     */
    private static String extractStringToSign(String responseString) {
        String responseNodeKey = "\"response\"";
        String signNodeKey = "\"sign\"";
        int indexOfResponseNode = responseString.indexOf(responseNodeKey);
        int indexOfSignNode = responseString.lastIndexOf(signNodeKey);
        if (indexOfResponseNode < 0) {
            return null;
        }
        if (indexOfSignNode < 0 || indexOfSignNode < indexOfResponseNode) {
            indexOfSignNode = responseString.lastIndexOf('}') - 1;
        }
        int startIndex = responseString.indexOf('{',
                indexOfResponseNode + responseNodeKey.length());
        int endIndex = responseString.lastIndexOf("}", indexOfSignNode);

        try {
            return responseString.substring(startIndex, endIndex + 1);
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
    }

    /**
     * Calculate signature according to signedParams and secret
     *
     * @param signedParams the signed string
     * @param secret       the accesskey secret
     * @return the signature string
     * @example qlB4B1lFcehlWRelL7Fo4uNHPCs=
     */
    public static String getSignature(Map<String, String> signedParams, String secret) throws Exception {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, String> item : signedParams.entrySet()) {
            if (item.getValue() == null || !item.getValue().startsWith("sign_type")) {
                keys.add(item.getKey());
            }
        }
        Collections.sort(keys);
        String content = "";
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            if (i != 0) {
                content += "&";
            }
            content += key + "=" + signedParams.get(key);

        }
        return sign(content, secret);
    }

    /**
     * Calculate signature according to content and secret
     *
     * @param content the string to sign
     * @param secret  the accesskey secret
     * @return the signature string
     */
    private static String sign(String content, String secret) throws Exception {
        String charset = "UTF-8";
        String algorithm = "HmacSHA1";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret.getBytes(charset), algorithm));
        mac.update(URLEncoder.encode(content, charset).getBytes(charset));
        byte[] signData = mac.doFinal();
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * Upload item with urlPath
     *
     * @param item    the file
     * @param urlPath the upload url
     */
    public static void putObject(java.io.InputStream item, Map<String, String> headers, String urlPath) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setRequestMethod("POST");
            conn.setChunkedStreamingMode(0);
            conn.connect();
            OutputStream out = conn.getOutputStream();
            IOUtils.copy(item, out);
            out.flush();
            out.close();
            conn.disconnect();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Parse  headers into map[string]string
     *
     * @param headers the target headers
     * @return the map[string]string
     */
    public static Map<String, String> parseUploadHeaders(Object headers) throws Exception {
        Map<String, String> result = new HashMap<>();
        if (List.class.isAssignableFrom(headers.getClass())) {
            ((List<?>) headers).forEach(item -> {
                if (Map.class.isAssignableFrom(item.getClass())) {
                    result.putAll((Map<? extends String, ? extends String>) item);
                }
                if (List.class.isAssignableFrom(item.getClass())) {
                    try {
                        result.putAll(parseUploadHeaders(item));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return result;
    }

}