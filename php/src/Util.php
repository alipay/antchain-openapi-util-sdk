<?php

namespace Alipaycloud\Alipay\Util;

/**
 * This is a utility module.
 */
class Util
{
    /**
     * Get timestamp.
     *
     * @example 2006-01-02T15:04:05Z
     * @error no error throws
     *
     * @return string string
     *
     * @throws \Exception
     */
    public static function getTimestamp()
    {
        return gmdate('Y-m-d\\TH:i:s\\Z');
    }

    /**
     * Judge if the api called success or not.
     *
     * @example true
     * @error no error throws
     *
     * @param array $res the response
     *
     * @return bool boolean
     *
     * @throws \Exception
     */
    public static function hasError($res)
    {
        if (null === $res || !isset($res['response'])) {
            return false;
        }
        $response = $res['response'];
        if (isset($response['result_code']) && 'ok' !== $response['result_code']) {
            return true;
        }
        return false;
    }

    /**
     * Calculate signature according to signedParams and secret.
     *
     * @example qlB4B1lFcehlWRelL7Fo4uNHPCs=
     * @error no error throws
     *
     * @param array  $signedParams the signed string
     * @param string $secret       the accesskey secret
     *
     * @return string signature string
     *
     * @throws \Exception
     */
    public static function getSignature($signedParams, $secret)
    {
        return base64_encode(hash_hmac('sha1', self::getSignatureString($signedParams), $secret, true));
    }

    public static function getSignatureString($signedParams)
    {
        ksort($signedParams);
        $params = [];
        foreach ($signedParams as $k => $v) {
            if (null !== $v) {
                array_push($params, $k . '=' . rawurlencode($v));
            }
        }

        return implode('&', $params);
    }
}
