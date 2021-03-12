import unittest
from antchain_alipay_util.antchain_utils import AntchainUtils
from Tea.exceptions import TeaException

import threading
from http.server import HTTPServer, BaseHTTPRequestHandler


class Request(BaseHTTPRequestHandler):
    def do_PUT(self):
        body = self.rfile.read(int(self.headers['content-length']))
        assert body == b'test python'

        expected = self.headers['expected']
        if expected == 'success':
            self.send_response(200)
            self.send_header('Content-type', 'application/xml')
            self.end_headers()
            self.wfile.write(b'''<?xml version="1.0" encoding="UTF-8"?>
        <Error>
          <Code>OK</Code>
        </Error>''')
        else:
            self.send_response(400)
            self.send_header('Content-type', 'application/xml')
            self.end_headers()
            self.wfile.write(b'''<?xml version="1.0" encoding="UTF-8"?>
        <Error>
          <Code>NoSuchKey</Code>
        </Error>''')


def run_server():
    server = HTTPServer(('localhost', 8888), Request)
    server.serve_forever()


class TestClient(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        server = threading.Thread(target=run_server)
        server.setDaemon(True)
        server.start()

    def test_get_timestamp(self):
        timestamp = AntchainUtils.get_timestamp()
        self.assertEqual(20, len(timestamp))

    def test_has_error(self):
        tmp = 'testInvalidJson'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertTrue(resp)

        tmp = '{"noResponse":"true"}'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertTrue(resp)

        tmp = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":' \
              '"79e093b3ae0f3f2c1","result_code":"false"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertFalse(resp)

        tmp = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":' \
              '"79e093b3ae0f3f2c1","result_code":"OK"}}'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertTrue(resp)

        tmp = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":' \
              '"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertFalse(resp)

        tmp = '{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":' \
              '"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLqtFnsjF1Zy6B6OWbCg="}'
        resp = AntchainUtils.has_error(tmp, 'secret')
        self.assertTrue(resp)

    def test_get_signature(self):
        params = {
            'test': 'ok'
        }
        signature = AntchainUtils.get_signature(params, 'secret')
        self.assertEqual('qlB4B1lFcehlWRelL7Fo4uNHPCs=', signature)

    def test_get_nonce(self):
        self.assertEqual(32, len(AntchainUtils.get_nonce()))

    def test_parse_upload_headers(self):
        res = AntchainUtils.parse_upload_headers(12)
        self.assertEqual({}, res)

        res = AntchainUtils.parse_upload_headers('{"test":"ok"}')
        self.assertEqual({}, res)

        res = AntchainUtils.parse_upload_headers([
            {
                "name": "content-type",
                "value": "text",
            },
            {
                "name": "content-md5",
                "value": "md5value",
            },
        ])
        self.assertEqual('text', res['content-type'])
        self.assertEqual('md5value', res['content-md5'])

    def test_put_object(self):
        url = 'http://127.0.0.1:8888'
        with open('test.txt', 'rb') as f:
            AntchainUtils.put_object(f, {'expected': 'success'}, url)

        with open('test.txt', 'rb') as f:
            try:
                AntchainUtils.put_object(f, {'expected': 'fail'}, url)
                assert False
            except TeaException as e:
                self.assertEqual('NoSuchKey', e.code)
                self.assertEqual(400, e.data['httpCode'])

    def test_is_success(self):
        s1 = 'OK'
        s2 = 'fail'
        self.assertTrue(AntchainUtils.is_success(s1, s2))

        s1 = 'fail'
        s2 = 'OK'
        self.assertFalse(AntchainUtils.is_success(s1, s2))

        s1 = 'fail'
        s2 = 'fail'
        self.assertTrue(AntchainUtils.is_success(s1, s2))
