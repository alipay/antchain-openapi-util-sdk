'use strict';

import assert from 'assert';
import 'mocha';
import rewire from 'rewire';
import BaseClient from "../src/client";


describe('base client', function () {

  it('getSignature should ok', async function () {
    const queryParams = {
      test: 'ok',
    };
    let sign = BaseClient.getSignature(queryParams, 'secret');
    assert.strictEqual(sign, 'qlB4B1lFcehlWRelL7Fo4uNHPCs=');
  });

  it('hasError should ok', async function () {
    let tmp = 'testInvalidJson';
    //let res = BaseClient.hasError(tmp, "secret");
    //assert.strictEqual(res, true);

    tmp = `{"noResponse":"true"}`;
    let res = BaseClient.hasError(tmp, "secret");
    assert.strictEqual(res, true);

    tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"false"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}`;
    res = BaseClient.hasError(tmp, "secret");
    assert.strictEqual(res, true);

    tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"}}`;
    res = BaseClient.hasError(tmp, "secret");
    assert.strictEqual(res, true);

    tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLq7utFnsjF1Zy6B6OWbCg="}`;
    res = BaseClient.hasError(tmp, "secret");
    assert.strictEqual(res, false);

    tmp = `{"response":{"expired_time":"2021-01-04T17:04:42.072+08:00","file_id":"kjiac1a298f8d","req_msg_id":"79e093b3ae0f3f2c1","result_code":"OK"},"sign":"IUl/4uLqtFnsjF1Zy6B6OWbCg="}`;
    res = BaseClient.hasError(tmp, "secret");
    assert.strictEqual(res, true);

  });

  it('getTimestamp should ok', async function () {
    assert.ok(BaseClient.getTimestamp())
  });

  
  it('getNonce should ok', function() {
    assert.strictEqual(32, BaseClient.getNonce().length);
  })

});

describe('private methods', function () {
  const client = rewire('../src/client');

  it('replaceRepeatList should ok', function () {
    const replaceRepeatList = client.__get__('replaceRepeatList');
    function helper(target: any, key: any, repeat: any) {
      replaceRepeatList(target, key, repeat);
      return target;
    }
    assert.deepEqual(helper({}, 'key', []), {})
    assert.deepEqual(helper({}, 'key', ['value']), {
      'key.1': 'value'
    })
    assert.deepEqual(helper({}, 'key', [{
      Domain: '1.com'
    }]), {
      'key.1.Domain': '1.com'
    })
  });

  it('flatParams should ok', function () {
    const flatParams = client.__get__('flatParams');
    assert.deepEqual(flatParams({}), {})
    assert.deepEqual(flatParams({ key: ['value'] }), { 'key.1': 'value' })
    assert.deepEqual(flatParams({ 'key': 'value' }), { 'key': 'value' })
    assert.deepEqual(flatParams({
      key: [
        {
          Domain: '1.com'
        }
      ]
    }), { 'key.1.Domain': '1.com' })
  });

  it('canonicalize should ok', function () {
    const canonicalize = client.__get__('canonicalize');
    assert.strictEqual(canonicalize([]), '');
    assert.strictEqual(canonicalize([
      ['key.1', 'value']
    ]), 'key.1=value');
    assert.strictEqual(canonicalize([
      ['key', 'value']
    ]), 'key=value')
    assert.strictEqual(canonicalize([
      ['key.1.Domain', '1.com']
    ]), 'key.1.Domain=1.com');
    assert.strictEqual(canonicalize([
      ['a', 'value'],
      ['b', 'value'],
      ['c', 'value']
    ]), 'a=value&b=value&c=value')
  });
});

