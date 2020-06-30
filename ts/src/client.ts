// This file is auto-generated, don't edit it
/**
 * This is a utility module
 */
import * as $tea from '@alicloud/tea-typescript';
import * as kitx from "kitx";

function encode(str: string) {
  var result = encodeURIComponent(str);

  return result.replace(/!/g, '%21')
    .replace(/'/g, '%27')
    .replace(/\(/g, '%28')
    .replace(/\)/g, '%29')
    .replace(/\*/g, '%2A');
}

function replaceRepeatList(target: { [key: string]: any }, key: string, repeat: any[]) {
  for (var i = 0; i < repeat.length; i++) {
    var item = repeat[i];

    if (item && typeof item === 'object') {
      const keys = Object.keys(item);
      for (var j = 0; j < keys.length; j++) {
        target[`${key}.${i + 1}.${keys[j]}`] = item[keys[j]];
      }
    } else {
      target[`${key}.${i + 1}`] = item;
    }
  }
}

function flatParams(params: { [key: string]: any }) {
  var target: { [key: string]: any } = {};
  var keys = Object.keys(params);
  for (let i = 0; i < keys.length; i++) {
    var key = keys[i];
    var value = params[key];
    if (Array.isArray(value)) {
      replaceRepeatList(target, key, value);
    } else {
      target[key] = value;
    }
  }
  return target;
}

function normalize(params: { [key: string]: any }) {
  var list = [];
  var flated = flatParams(params);
  var keys = Object.keys(flated).sort();
  for (let i = 0; i < keys.length; i++) {
    var key = keys[i];
    var value = flated[key];
    list.push([encode(key), encode(value)]);
  }
  return list;
}

function canonicalize(normalized: any[]) {
  var fields = [];
  for (var i = 0; i < normalized.length; i++) {
    var [key, value] = normalized[i];
    fields.push(key + '=' + value);
  }
  return fields.join('&');
}


export default class Client {

  /**
   * Get timestamp
   * @return the string
   */
  static getTimestamp(): string {
    let date = new Date();
    let YYYY = date.getUTCFullYear();
    let MM = kitx.pad2(date.getUTCMonth() + 1);
    let DD = kitx.pad2(date.getUTCDate());
    let HH = kitx.pad2(date.getUTCHours());
    let mm = kitx.pad2(date.getUTCMinutes());
    let ss = kitx.pad2(date.getUTCSeconds());
    return `${YYYY}-${MM}-${DD}T${HH}:${mm}:${ss}Z`;
  }

  /**
   * Judge if the api called success or not
   * @param res the response
   * @return the boolean
   */
  static hasError(res: { [key: string]: any }): boolean {
    if (!res || !res.response) {
      return false;
    }
    if (res.response.result_code && res.response.result_code.toString().toLowerCase() !== 'ok') {
      return true
    }
    return false;
  }

  /**
   * Calculate signature according to signedParams and secret
   * @param signedParams the signed string
   * @param secret the accesskey secret
   * @return the signature string
   */
  static getSignature(signedParams: { [key: string]: string }, secret: string): string {
    var normalized = normalize(signedParams);
    var stringToSign = canonicalize(normalized);
    return <string>kitx.sha1(stringToSign, secret, 'base64');
  }

}
