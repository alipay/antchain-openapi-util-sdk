<?php

namespace AntChain\Util;

use AlibabaCloud\Tea\Exception\TeaError;
use AlibabaCloud\Tea\OSSUtils\OSSUtils;
use AlibabaCloud\Tea\Tea;
use AlibabaCloud\Tea\Utils\Utils;
use DateTime;
use Exception;
use GuzzleHttp\Psr7\Stream;

/**
 * This is a utility module.
 */
class UtilClient
{
    /**
     * Get timestamp.
     *
     * @return string the string
     *
     * @example 2006-01-02T15:04:05Z
     * @error   no error throws
     */
    public static function getTimestamp()
    {
        return gmdate('Y-m-d\\TH:i:s\\Z');
    }

    /**
     * Judge if the api called success or not.
     *
     * @param string $res    the response string
     * @param string $secret the accesskey secret string
     *
     * @return bool the boolean
     *
     * @example true
     * @error   no error throws
     */
    public static function hasError($res, $secret)
    {
        $data = @json_decode($res, true);
        if (!$data) {
            return true;
        }
        if (!isset($data['response'])) {
            return true;
        }
        $response = $data['response'];
        if (isset($response['result_code']) && 'ok' !== strtolower($response['result_code'])) {
            return false;
        }
        if (!isset($data['sign'])) {
            return true;
        }
        $begin        = strpos($res, 'response":');
        $end          = strpos($res, '"sign"');
        $signToString = substr($res, $begin + 10, $end - $begin - 10 - 1);
        $sign         = base64_encode(hash_hmac('sha1', $signToString, $secret, true));
        $signServer   = $data['sign'];
        $a            = $sign === $signServer;
        if ($sign === $signServer) {
            return false;
        }

        return true;
    }

    /**
     * Calculate signature according to signedParams and secret.
     *
     * @param string[] $signedParams the signed string
     * @param string   $secret       the accesskey secret
     *
     * @return string the signature string
     *
     * @example qlB4B1lFcehlWRelL7Fo4uNHPCs=
     * @error   no error throws
     */
    public static function getSignature($signedParams, $secret)
    {
        return base64_encode(hash_hmac('sha1', self::getSignatureString($signedParams), $secret, true));
    }

    /**
     * Upload item with urlPath.
     *
     * @param Stream   $item    the file
     * @param string[] $headers
     * @param string   $urlPath the upload url
     *
     * @throws \GuzzleHttp\Exception\GuzzleException
     *
     * @return TeaError|null
     */
    public static function putObject($item, $headers, $urlPath)
    {
        $options = [
            'headers' => $headers,
            'body'    => $item,
        ];

        try {
            $response = Tea::request('PUT', $urlPath, $options);
            $code     = $response->getStatusCode();
            if ($code >= 400 && $code < 600) {
                $body    = Utils::readAsString($response->getBody());
                $respMap = OSSUtils::getErrMessage($body);

                return new TeaError([
                    'code'    => @$respMap['Code'],
                    'message' => @$respMap['Message'],
                    'data'    => [
                        'httpCode'  => $response->getStatusCode(),
                        'requestId' => @$respMap['RequestId'],
                        'hostId'    => @$respMap['HostId'],
                    ],
                ]);
            }
        } catch (Exception $e) {
            throw new TeaError([], 'Upload file failed.', $e->getCode(), $e);
        }

        return null;
    }

    /**
     * Parse  headers into map[string]string.
     *
     * @param mixed $headers the target headers
     *
     * @return array the map[string]string
     */
    public static function parseUploadHeaders($headers)
    {
        $headers = @json_decode(@json_encode($headers), true); // object to array
        if (!\is_array($headers)) {
            return [];
        }
        $res = [];
        foreach ($headers as $k => $v) {
            if (isset($v['name'])) {
                $res[$v['name']] = @$v['value'];
            }
        }

        return $res;
    }

    /**
     * Generate a nonce string.
     *
     * @return string the nonce string
     */
    public static function getNonce()
    {
        return md5(uniqid() . uniqid(md5(microtime(true)), true));
    }

    public static function getSignatureString($signedParams)
    {
        ksort($signedParams);
        $params = [];
        foreach ($signedParams as $k => $v) {
            if ($v instanceof Stream) {
                continue;
            }
            if (null !== $v) {
                $params[] = $k . '=' . rawurlencode($v);
            }
        }

        return implode('&', $params);
    }

    /**
     * Judge upload if ok or not.
     * *
     * @example resultCode == successCode or resultCode == ok is true
     * @error no error throws
     *
     * @param string $resultCode
     * @param string $successCode
     *
     * @return bool the boolean
     */
    public static function isSuccess($resultCode, $successCode)
    {
        $resultCode  = strtolower($resultCode);
        $successCode = strtolower($successCode);

        return 'ok' === $resultCode || $resultCode === $successCode;
    }

    /**
     * @param string $dateStr
     *
     * @return Datetime
     */
    public static function parseDate($dateStr)
    {
        return new DateTime($dateStr);
    }

    /**
     * @param Datetime $datetime
     *
     * @return string
     */
    public static function formatDate($datetime, $format='Y-m-d H:i:s')
    {
        return $datetime->format($format);
    }
}
