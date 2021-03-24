// This file is auto-generated, don't edit it. Thanks.
/**
 * This is a utility module
 */
package service

import (
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"

	ossutil "github.com/alibabacloud-go/tea-oss-utils/service"
	util "github.com/alibabacloud-go/tea-utils/service"
	"github.com/alibabacloud-go/tea/tea"
	uuid "github.com/satori/go.uuid"
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
func HasError(raw *string, secret *string) *bool {
	res := tea.StringValue(raw)
	tmp := make(map[string]interface{})
	err := json.Unmarshal([]byte(res), &tmp)
	if err != nil {
		return tea.Bool(true)
	}
	if tmp["response"] == nil {
		return tea.Bool(true)
	}

	real, ok := tmp["response"].(map[string]interface{})
	if ok && real["result_code"] != nil && strings.ToLower(real["result_code"].(string)) != "ok" {
		return tea.Bool(false)
	}

	if tmp["sign"] == nil {
		return tea.Bool(true)
	}
	s := strings.Index(res, "\"response\"")
	end := strings.Index(res, "\"sign\"")
	res = res[s:end]
	s = strings.Index(res, "{")
	end = strings.LastIndex(res, "}")
	signToString := res[s : end+1]
	sign := sign(signToString, tea.StringValue(secret))
	signServer := tmp["sign"].(string)
	if signServer == sign {
		return tea.Bool(false)
	}

	return tea.Bool(true)
}

/**
 * Upload item with urlPath
 * @param item the file
 * @param urlPath the upload url
 */
func PutObject(item io.Reader, headers map[string]*string, urlPath *string) error {
	req, err := http.NewRequest("PUT", tea.StringValue(urlPath), item)
	if err != nil {
		return err
	}
	for k, v := range headers {
		req.Header.Add(k, tea.StringValue(v))
	}
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return errors.New("Upload file failed.")
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 && resp.StatusCode < 600 {
		bodyStr, err := util.ReadAsString(resp.Body)
		if err != nil {
			return err
		}

		respMap := ossutil.GetErrMessage(bodyStr)
		if respMap["Code"] != nil && respMap["Code"].(string) == "CallbackFailed" {
			return nil
		}
		return tea.NewSDKError(map[string]interface{}{
			"code":    respMap["Code"],
			"message": respMap["Message"],
			"data": map[string]interface{}{
				"httpCode":  resp.StatusCode,
				"requestId": respMap["RequestId"],
				"hostId":    respMap["HostId"],
			},
		})
	}

	return nil
}

/**
 * Parse  headers into map[string]string
 * @param headers the target headers
 * @return the map[string]string
 */
func ParseUploadHeaders(headers interface{}) map[string]*string {
	byt, err := json.Marshal(headers)
	if err != nil {
		return nil
	}

	tmp := make([]map[string]string, 0)
	err = json.Unmarshal(byt, &tmp)
	if err != nil {
		return nil
	}
	res := make(map[string]*string)
	for _, m := range tmp {
		res[m["name"]] = tea.String(m["value"])
	}
	return res
}

/**
 * Generate a nonce string
 * @return the nonce string
 */
func GetNonce() *string {
	uuid := uuid.NewV1().String()
	return tea.String(strings.ReplaceAll(uuid, "-", ""))
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

func IsSuccess(resultCode, successCode string) bool {
	return strings.EqualFold(resultCode, "ok") || strings.EqualFold(resultCode, successCode)
}