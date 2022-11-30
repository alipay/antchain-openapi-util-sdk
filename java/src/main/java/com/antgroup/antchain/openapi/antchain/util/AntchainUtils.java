// This file is auto-generated, don't edit it. Thanks.
package com.antgroup.antchain.openapi.antchain.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.*;
import com.aliyun.tea.*;
import com.aliyun.tea.TeaModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.format.*;
import org.joda.time.*;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

public class AntchainUtils {
    private static final Pattern ENCODED_CHARACTERS_PATTERN;
    private static final String DEFAULT_ENCODING = "UTF-8";


    static {
        StringBuilder pattern = new StringBuilder();

        pattern.append(Pattern.quote("+")).append("|").append(Pattern.quote("*")).append("|")
                .append(Pattern.quote("%7E")).append("|").append(Pattern.quote("%2F"));

        ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
    }

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
            return true;
        }
        Map<String, Object> map = (Map<String, Object>) response.get("response");
        if (map == null) {
            return true;
        }
        if (!"ok".equalsIgnoreCase(String.valueOf(map.get("result_code")))) {
            return false;
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
            content += urlEncode(key) + "=" + urlEncode(signedParams.get(key));

        }
        return sign(content, secret);
    }

    /**
     * URL encode
     *
     * @param value the response string
     * @return the string
     */
    public static String urlEncode(final String value) {
        if (value == null) {
            return "";
        }

        try {
            String encoded = URLEncoder.encode(value, DEFAULT_ENCODING);

            Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);
            StringBuffer buffer = new StringBuffer(encoded.length());

            while (matcher.find()) {
                String replacement = matcher.group(0);

                if ("+".equals(replacement)) {
                    replacement = "%20";
                } else if ("*".equals(replacement)) {
                    replacement = "%2A";
                } else if ("%7E".equals(replacement)) {
                    replacement = "~";
                }

                matcher.appendReplacement(buffer, replacement);
            }

            matcher.appendTail(buffer);
            return buffer.toString();

        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calculate signature according to content and secret
     *
     * @param content the string to sign
     * @param secret  the accesskey secret
     * @return the signature string
     */
    private static String sign(String content, String secret) throws Exception {
        return sign(content, "HmacSHA1", secret, "UTF-8");
    }

    /**
     * Calculate signature according to content and secret
     *
     * @param content the string to sign
     * @param secret  the accesskey secret
     * @return the signature string
     */
    private static String sign(String content, String algorithm, String secret, String charset) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret.getBytes(charset), algorithm));
        mac.update(content.getBytes(charset));
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
            conn.setRequestMethod("PUT");
            conn.setChunkedStreamingMode(0);
            conn.connect();
            OutputStream out = conn.getOutputStream();
            IOUtils.copy(item, out);
            out.flush();
            out.close();
            int statusCode = conn.getResponseCode();
            if (statusCode >= 400 && statusCode <=600) {
                String bodyStr = "";
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyStr += line + "\n";
                }
                reader.close();
                Map<String, Object> respMap = com.aliyun.ossutil.Client.getErrMessage(bodyStr);
                if (respMap.get("Code") != null && String.valueOf(respMap.get("Code")) != "CallbackFailed") {
                    throw new TeaException(TeaConverter.buildMap(
                        new TeaPair("code", respMap.get("Code")),
                        new TeaPair("message", respMap.get("Message")),
                        new TeaPair("data", TeaConverter.buildMap(
                            new TeaPair("httpCode", statusCode),
                            new TeaPair("requestId", respMap.get("RequestId")),
                            new TeaPair("hostId", respMap.get("HostId"))
                        ))
                    ));
                }
            }
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
        if (headers == null) {
            return result;
        }
        if (List.class.isAssignableFrom(headers.getClass())) {
            ((List<?>) headers).forEach(item -> {
                if (TeaModel.class.isAssignableFrom(item.getClass())) {
                    Map<String, Object> tmp = ((TeaModel) item).toMap();
                    result.put(tmp.get("name").toString(), tmp.get("value").toString());
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

    /**
     * Get nonce
     *
     * @return the string
     */
    public static String getNonce() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Judge upload is ok
     *
     * @return the boolean
     * @example resultMsg == ok is true
     * @error no error throws
     */
    public static boolean isSuccess(String resultCode, String successCode) {
        return resultCode.equalsIgnoreCase("ok") || resultCode.equalsIgnoreCase(successCode);
    }

    /**
     * string to date
     *
     * @param date
     * @return the date
     */
    public static Date parseDate(String date) {
        return StringUtils.isEmpty(date) ? null : ISODateTimeFormat.dateTimeParser().parseDateTime(date).toDate();
    }

    /**
     * date to string
     *
     * @param date
     * @return the string
     */
    public static String formatDate(Date date) {
        return date == null ? null : new DateTime(date).toString();
    }

    /**
     * add two integer
     *
     * @return result num add num1 
     */
    public static Integer addInteger(Integer num, Integer num1) {
        return num + num1;
    }

    /**
     * put trace to headers
     * 
     * @param headers
     * @return headers
     */
    public static Map<String, String> putTrace(Map<String, String> headers){
        try {
            if (StringUtils.isEmpty(headers.get("X-B3-TraceId")) && StringUtils.isEmpty(headers.get("SOFA-TraceId"))){
                String traceId = "";
                String spanId = "";
                SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
                if (currentSpan != null && currentSpan.getSofaTracerSpanContext() != null){
                    traceId = currentSpan.getSofaTracerSpanContext().getTraceId();
                    spanId = currentSpan.getSofaTracerSpanContext().nextChildContextId();
                }

                if(StringUtils.isEmpty(traceId)){
                    SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
                    traceId = spanContext.getTraceId();
                    spanId = "0.1";
                }
                headers.put("X-B3-TraceId", traceId);
                headers.put("X-B3-SpanId", spanId);
                headers.put("SOFA-TraceId", traceId);
                headers.put("SOFA-RpcId", spanId);
                headers.put("SOFA-SpanId", spanId);
            }else if(StringUtils.isEmpty(headers.get("X-B3-SpanId")) && StringUtils.isEmpty(headers.get("SOFA-SpanId")) && StringUtils.isEmpty(headers.get("SOFA-RpcId"))){
                headers.put("X-B3-SpanId", "0.1");
                headers.put("SOFA-RpcId", "0.1");
                headers.put("SOFA-SpanId", "0.1");
            }
        } catch (Exception e) {
            System.out.println(e != null ? e.getMessage() : "unknow error");
        }

        return headers;
    }
    
}