<?php

namespace AntChain\Util\Tests;

use AntChain\Util\UtilClient;
use GuzzleHttp\Psr7\Stream;
use PHPUnit\Framework\TestCase;

/**
 * @internal
 * @coversNothing
 */
class UtilClientTest extends TestCase
{
    public function testGetTimestamp()
    {
        $this->assertEquals(20, \strlen(UtilClient::getTimestamp()));
    }

    public function testHasError()
    {
        $this->assertTrue(UtilClient::hasError('testInvalidJson', 'secret'));

        $this->assertTrue(UtilClient::hasError('{"noResponse":"true"}', 'secret'));

        // result_code is not ok
        $res = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"false"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}';
        $this->assertTrue(UtilClient::hasError($res, 'secret'));

        // not have sign
        $res = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"}}';
        $this->assertTrue(UtilClient::hasError($res, 'secret'));

        // wrong sign
        $res = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLqtFnsjF1Zy6B6OWbCg=';
        $this->assertTrue(UtilClient::hasError($res, 'secret'));

        // not equal to success_code
        $res = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"success"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}';
        $this->assertTrue(UtilClient::hasError($res, 'secret', 'failed'));

        $res = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}';
        $this->assertFalse(UtilClient::hasError($res, 'secret', 'ok'));
    }

    public function testSignature()
    {
        $params = [
            'charset'       => 'UTF-8',
            'biz_content'   => '{"order_id":"197002BB1bcb984cc0ab4c5ebed9c592df6acX80","user_id":"2088102285927804"}',
            'utc_timestamp' => '1518170340170',
            'sign'          => 'ABC',
            'ak'            => '1970121701099429',
            'version'       => '1.0',
            'sign_type'     => 'HmacSHA1',
            'msg_id'        => '2798c9cf5f88f24bb7ab6a94733cab1m3l',
            'msg_key'       => 'alipay.open.auth.appauth.cancelled',
        ];
        $this->assertEquals(
            'ak=1970121701099429&biz_content=%7B%22order_id%22%3A%22197002BB1bcb984cc0ab4c5ebed9c592df6acX80%22%2C%22user_id%22%3A%222088102285927804%22%7D&charset=UTF-8&msg_id=2798c9cf5f88f24bb7ab6a94733cab1m3l&msg_key=alipay.open.auth.appauth.cancelled&sign=ABC&sign_type=HmacSHA1&utc_timestamp=1518170340170&version=1.0',
            UtilClient::getSignatureString($params)
        );

        $this->assertEquals('rcDNNSTdufjwCpRqzFpoV5bC0IU=', UtilClient::getSignature($params, 'secret'));

        $signedParam = [
            "req_msg_id"       => "462b26b053d611eb82176c96cfdde571",
            "method"           => "demo.gateway.check.echo",
            "version"          => "2.0.0",
            "input_string"     => "OK",
            "sign_type"        => "HmacSHA1",
            "req_time"         => "2021-01-11T06:28:53Z",
            "access_key"       => "ACn9BRjVEJfaPeeI",
            "base_sdk_version" => "Tea-SDK",
            "sdk_version"      => "Tea-SDK-20201203",
            "file_id"          => "kjs6qx6xad54d8c0",
            'fileObject'       => new Stream(fopen('data://text/plain;base64,' . base64_encode('This is test file content.'), 'r'))
        ];
        $str         = "access_key=ACn9BRjVEJfaPeeI&base_sdk_version=Tea-SDK&file_id=kjs6qx6xad54d8c0&input_string=OK&method=demo.gateway.check.echo&req_msg_id=462b26b053d611eb82176c96cfdde571&req_time=2021-01-11T06%3A28%3A53Z&sdk_version=Tea-SDK-20201203&sign_type=HmacSHA1&version=2.0.0";
        $this->assertEquals($str, UtilClient::getSignatureString($signedParam));
    }

    public function testParseUploadHeaders()
    {
        $res = UtilClient::parseUploadHeaders([]);
        $this->assertCount(0, $res);

        $res = UtilClient::parseUploadHeaders('{"test":"ok"}');
        $this->assertCount(0, $res);

        $res = UtilClient::parseUploadHeaders([
            ['name' => 'content-type', 'value' => 'text'],
        ]);
        $this->assertEquals('text', $res['content-type']);
    }

    public function testGetNonce()
    {
        $this->assertEquals(32, \strlen(UtilClient::getNonce()));
    }

    public function testGetSignature()
    {
        $signedParams = [
            'test' => 'ok',
        ];
        $sign         = UtilClient::getSignature($signedParams, 'secret');
        $this->assertEquals('qlB4B1lFcehlWRelL7Fo4uNHPCs=', $sign);
    }
}
