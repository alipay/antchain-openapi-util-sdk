'use strict';

import BaseClient from "../src/client";
import rewire from 'rewire';
import 'mocha';
import assert from 'assert';


describe('base client', function () {

  it('getSignature should ok', async function () {
    const queryParams = {
      test: 'ok',
    };
    let sign = BaseClient.getSignature(queryParams, 'secret');
    assert.strictEqual(sign, 'qlB4B1lFcehlWRelL7Fo4uNHPCs=');
  });

  it('hasError should ok', async function () {
    assert.strictEqual(BaseClient.hasError({ test: 'ok' }), false)
    assert.strictEqual(BaseClient.hasError({
      response: {
        result_code: 'ok'
      }
    }), false)
    assert.strictEqual(BaseClient.hasError(undefined), false)
    assert.strictEqual(BaseClient.hasError({
      response: {
        result_code: 'success'
      }
    }), true)
  });

  it('getTimestamp should ok', async function () {
    assert.ok(BaseClient.getTimestamp())
  });

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

