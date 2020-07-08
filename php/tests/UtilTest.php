<?php

namespace AntChain\Util\Tests;

use AntChain\Util\UtilClient;
use PHPUnit\Framework\TestCase;

/**
 * @internal
 * @coversNothing
 */
class UtilTest extends TestCase
{
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
    }
}
