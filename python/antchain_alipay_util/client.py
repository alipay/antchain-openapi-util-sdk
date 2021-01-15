import datetime
import json
import hashlib
import hmac
import base64
import socket
import uuid
import requests
from xml.etree import ElementTree
from urllib.parse import quote

from Tea.exceptions import TeaException
from Tea.model import TeaModel


def index(s, sub_str):
    try:
        return s.index(sub_str)
    except ValueError:
        return -1


def get_err_message(msg):
    err_fields = ('Code', 'Message', 'RequestId', 'HostId')
    err_resp = {}
    et = ElementTree.fromstring(msg)
    if et.tag == 'Error':
        for lf in et:
            if lf.tag in err_fields:
                err_resp[lf.tag] = lf.text
    return err_resp


def sign(str_to_sign, secret):
    hash_val = hmac.new(secret.encode('utf-8'), str_to_sign.encode('utf-8'), hashlib.sha1).digest()
    signature = base64.b64encode(hash_val).decode('utf-8')
    return signature


class Client:
    @staticmethod
    def get_timestamp():
        """
        Get timestamp

        @return the string
        """
        return datetime.datetime.strftime(datetime.datetime.utcnow(), '%Y-%m-%dT%H:%M:%SZ')

    @staticmethod
    def has_error(res, secret):
        """
        Judge if the api called success or not

        @param res: the response
        @return the boolean
        """
        try:
            tmp = json.loads(res)
        except ValueError:
            return True

        if tmp.get('response') is None:
            return True

        result_code = tmp['response'].get('result_code')
        if result_code is not None and str(result_code).upper() != 'OK':
            return False

        if tmp.get('sign') is None:
            return True

        s = index(res, "response")
        end = index(res, "sign")
        res = res[s:end]
        s = index(res, "{")
        end = index(res, "}")
        str_to_sign = res[s: end + 1]

        signature = sign(str_to_sign, secret)
        if signature != tmp['sign']:
            return True
        return False

    @staticmethod
    def get_signature(signed_params, secret):
        """
        Calculate signature according to signedParams and secret

        @param signed_params: the signed string
        @param secret: the accesskey secret
        @return the signature string
        """
        keys = sorted(list(signed_params.keys()))
        str_to_sign = ""

        for k in keys:
            if signed_params[k] is not None:
                str_to_sign += "&%s=%s" % (
                    quote(k, safe='~'),
                    quote(signed_params[k], safe='~')
                )
        return sign(str_to_sign[1:], secret)

    @staticmethod
    def put_object(item, headers, url_path):
        """
        Upload item with urlPath
        @param item the file
        @param url_path the upload url
        """
        response = requests.put(url_path, headers=headers, data=item)
        if 400 <= response.status_code < 600:
            err = get_err_message(response.text)
            if err.get('Code') and err['Code'] == 'CallbackFailed':
                return
            raise TeaException({
                'code': err.get('Code'),
                'message': err.get('Message'),
                'data': {
                    'httpCode': response.status_code,
                    'requestId': err.get('requestId'),
                    'hostId': err.get('hostId')
                }
            })

    @staticmethod
    def parse_upload_headers(headers):
        """
        Parse headers into map[string]string
        @param headers the target headers
        @return the map[string]string
        """
        tmp = dict()
        if isinstance(headers, list):
            for d in headers:
                if isinstance(d, TeaModel):
                    tmp[d.name] = d.value
                else:
                    tmp[d.get('name')] = d.get('value')
        return tmp

    @staticmethod
    def get_nonce():
        """
        Generate a nonce string
        @return the nonce string
        """
        name = socket.gethostname() + str(uuid.uuid1())
        namespace = uuid.NAMESPACE_URL
        return str(uuid.uuid5(namespace, name)).replace('-', '')
