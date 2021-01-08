package service

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/alibabacloud-go/tea/tea"
	"github.com/alibabacloud-go/tea/utils"
)

func Test_GetTimestamp(t *testing.T) {
	utils.AssertEqual(t, len(tea.StringValue(GetTimestamp())), 20)
}

func Test_GetSignature(t *testing.T) {
	signedParams := map[string]*string{
		"test": tea.String("ok"),
	}

	sign := GetSignature(signedParams, tea.String("secret"))
	utils.AssertEqual(t, tea.StringValue(sign), "qlB4B1lFcehlWRelL7Fo4uNHPCs=")
}

func Test_HasError(t *testing.T) {
	tmp := `testInvalidJson`
	res := HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), true)

	tmp = `{"noResponse":"true"}`
	res = HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), true)

	tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"false"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}`
	res = HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), true)

	tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"}}`
	res = HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), true)

	tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}`
	res = HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), false)

	tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLqtFnsjF1Zy6B6OWbCg="}`
	res = HasError(tea.String(tmp), tea.String("secret"))
	utils.AssertEqual(t, tea.BoolValue(res), true)
}

func Test_GetNonce(t *testing.T) {
	res := GetNonce()
	utils.AssertEqual(t, len(tea.StringValue(res)), 32)
}

func Test_ParseUploadHeaders(t *testing.T) {
	res := ParseUploadHeaders(make(chan int))
	utils.AssertEqual(t, len(res), 0)

	res = ParseUploadHeaders(`{"test":"ok"}`)
	utils.AssertEqual(t, len(res), 0)

	in := make([]map[string]string, 1)
	in[0] = map[string]string{
		"name":  "content-type",
		"value": "text",
	}
	res = ParseUploadHeaders(in)
	utils.AssertEqual(t, tea.StringValue(res["content-type"]), "text")
}

func mockServer(status int, json string) (server *httptest.Server) {
	// Start a test server locally.
	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(status)
		w.Write([]byte(json))
	}))
	return ts
}

func Test_PutObject(t *testing.T) {
	err := PutObject(nil, nil, tea.String("###@@@@@"))
	utils.AssertNotNil(t, err)
	utils.AssertEqual(t, err.Error(), "Upload file failed.")

	ts := mockServer(400, `<?xml version="1.0" encoding="UTF-8"?>
<Error>
  <Code>CallbackFailed</Code>
</Error>`)
	defer ts.Close()

	err = PutObject(nil, map[string]*string{"test": tea.String("ok")}, tea.String(ts.URL))
	utils.AssertNil(t, err)

	ts1 := mockServer(400, `<?xml version="1.0" encoding="UTF-8"?>
<Error>
  <Code>NoSuchKey</Code>
</Error>`)
	defer ts1.Close()

	err = PutObject(nil, map[string]*string{"test": tea.String("ok")}, tea.String(ts1.URL))
	realErr := err.(*tea.SDKError)
	utils.AssertEqual(t, tea.StringValue(realErr.Code), "NoSuchKey")

	ts2 := mockServer(200, ``)
	defer ts2.Close()

	err = PutObject(nil, map[string]*string{"test": tea.String("ok")}, tea.String(ts2.URL))
	utils.AssertNil(t, err)
}
