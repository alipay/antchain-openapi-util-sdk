package service

import (
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
	resp := map[string]interface{}{
		"test": "ok",
	}

	res := HasError(resp)
	utils.AssertEqual(t, tea.BoolValue(res), false)

	resp = map[string]interface{}{
		"response": map[string]interface{}{
			"result_code": "OK",
		},
	}

	res = HasError(resp)
	utils.AssertEqual(t, tea.BoolValue(res), false)

	resp = map[string]interface{}{
		"response": map[string]interface{}{
			"result_code": "success",
		},
	}

	res = HasError(resp)
	utils.AssertEqual(t, tea.BoolValue(res), true)
}
