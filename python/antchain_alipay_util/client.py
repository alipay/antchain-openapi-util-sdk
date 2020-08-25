import datetime
import hashlib
import hmac
import base64

from urllib.parse import quote


class Client:
    @staticmethod
    def get_timestamp():
        """
        Get timestamp

        @return the string
        """
        return datetime.datetime.strftime(datetime.datetime.utcnow(), '%Y-%m-%dT%H:%M:%SZ')

    @staticmethod
    def has_error(res):
        """
        Judge if the api called success or not

        @param res: the response
        @return the boolean
        """
        if not res or res.get('response') is None:
            return False
        result_code = res['response'].get('result_code')
        if result_code is not None and str(result_code).upper() != 'OK':
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
                    quote(k, safe=''),
                    quote(signed_params[k], safe='')
                )

        hash_val = hmac.new(secret.encode('utf-8'), str_to_sign[1:].encode('utf-8'), hashlib.sha1).digest()
        signature = base64.b64encode(hash_val).decode('utf-8')
        return signature
