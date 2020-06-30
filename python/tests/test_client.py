import unittest
from antchain_alipay_util.client import Client


class TestClient(unittest.TestCase):
    def test_get_timestamp(self):
        timestamp = Client.get_timestamp()
        self.assertEqual(20, len(timestamp))

    def test_has_error(self):
        res = {'test': 'ok'}
        resp = Client.has_error(res)
        self.assertFalse(resp)

        res = {"result_code": "OK"}
        resp = Client.has_error(res)
        self.assertFalse(resp)

        res = {
            'response': {
                "result_code": "success"
            }
        }
        resp = Client.has_error(res)
        self.assertTrue(resp)

    def test_get_signature(self):
        params = {
            'test': 'ok'
        }
        signature = Client.get_signature(params, 'secret')
        self.assertEqual('qlB4B1lFcehlWRelL7Fo4uNHPCs=', signature)
