// This file is auto-generated, don't edit it. Thanks.
package com.antgroup.antchain.openapi.antchain.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
        Map<String, String> signedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            signedParams.put(entry.getKey(), entry.getValue().toString());
        }
        String sign = getSignature(signedParams, secret);
        if (sign != response.get("sign")) {
            return false;
        }
        if (map != null && !"ok".equalsIgnoreCase(String.valueOf(map.get("result_code")))) {
            return true;
        }
        return false;
    }

    /**
     * Calculate signature according to signedParams and secret
     *
     * @param signedParams the signed string
     * @param secret       the accesskey secret
     * @return the signature string
     * @example qlB4B1lFcehlWRelL7Fo4uNHPCs=
     * @error no error throws
     */
    public static String getSignature(Map<String, String> signedParams, String secret) throws Exception {
        String charset = "UTF-8";
        String algorithm = "HmacSHA1";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret.getBytes(charset), algorithm));
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, String> item : signedParams.entrySet()) {
            if (item.getValue() == null || !item.getValue().startsWith("sign_type")) {
                keys.add(item.getKey());
            }
        }
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            if (i != 0) {
                mac.update("&".getBytes(charset));
            }

            mac.update(URLEncoder.encode(key, charset).getBytes(charset));
            mac.update("=".getBytes(charset));
            mac.update(URLEncoder.encode(signedParams.get(key), charset).getBytes(charset));
        }
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