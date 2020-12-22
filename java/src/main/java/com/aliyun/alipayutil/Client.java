// This file is auto-generated, don't edit it. Thanks.
package com.antgroup.antchain.openapi.antchain.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


public class Client {

    /**
     * Get timestamp
     *
     * @return the string
     */
    public static String getTimestamp() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(new Date());
    }

    /**
     * Calculate signature according to signedParams and secret
     *
     * @param signedParams the signed string
     * @param secret       the accesskey secret
     * @return the signature string
     */
    public static String getSignature(java.util.Map<String, String> signedParams, String secret) throws Exception {
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
     * Judge if the api called success or not
     *
     * @param response the response
     * @return the boolean
     */
    public static boolean hasError(Map<String, ?> response) {
        if (response == null && response.get("response") == null) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) response.get("response");
        if (map != null && !"ok".equalsIgnoreCase(String.valueOf(map.get("result_code")))) {
            return true;
        }
        return false;
    }
}
