// This file is auto-generated, don't edit it. Thanks.
/**
 * This is a utility module
 */
package service

import (
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"net/url"
	"strings"
	"time"

	"github.com/alibabacloud-go/tea/tea"
)

/**
 * Get timestamp
 * @return the string
 */
func GetTimestamp() *string {
	gmt := time.FixedZone("GMT", 0)
	return tea.String(time.Now().In(gmt).Format("2006-01-02T15:04:05Z"))
}

/**
 * Calculate signature according to signedParams and secret
 * @param signedParams the signed string
 * @param secret the accesskey secret
 * @return the signature string
 */
func GetSignature(signedParams map[string]*string, secret *string) (_result *string) {
	stringToSign := buildStringToSign(signedParams)
	signature := sign(stringToSign, tea.StringValue(secret))
	return tea.String(signature)
}

/**
 * Judge if the api called success or not
 * @param res the response
 * @return the boolean
 */
func HasError(res map[string]interface{}) *bool {
	if res == nil || res["response"] == nil {
		return tea.Bool(false)
	}

	real, ok := res["response"].(map[string]interface{})
	if ok && real["result_code"] != nil && strings.ToLower(real["result_code"].(string)) != "ok" {
		return tea.Bool(true)
	}

	return tea.Bool(false)
}

func sign(stringToSign, accessKeySecret string) string {
	signedBytes := shaHmac1(stringToSign, accessKeySecret)
	signedString := base64.StdEncoding.EncodeToString(signedBytes)
	return signedString
}

func shaHmac1(source, secret string) []byte {
	key := []byte(secret)
	h := hmac.New(sha1.New, key)
	h.Write([]byte(source))
	return h.Sum(nil)
}

func buildStringToSign(signedParam map[string]*string) (stringToSign string) {
	signParams := make(map[string]string)
	for key, value := range signedParam {
		signParams[key] = tea.StringValue(value)
	}

	stringToSign = getUrlFormedMap(signParams)
	stringToSign = strings.Replace(stringToSign, "+", "%20", -1)
	stringToSign = strings.Replace(stringToSign, "*", "%2A", -1)
	stringToSign = strings.Replace(stringToSign, "%7E", "~", -1)
	return
}

func getUrlFormedMap(source map[string]string) (urlEncoded string) {
	urlEncoder := url.Values{}
	for key, value := range source {
		urlEncoder.Add(key, value)
	}
	urlEncoded = urlEncoder.Encode()
	return
}
