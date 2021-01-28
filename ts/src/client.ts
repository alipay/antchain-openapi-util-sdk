// This file is auto-generated, don't edit it
/**
 * This is a utility module
 */
import OssUtil from '@alicloud/oss-util';
import * as $tea from '@alicloud/tea-typescript';
import Util from '@alicloud/tea-util';
import * as httpx from 'httpx';
import * as kitx from "kitx";
import { Readable } from 'stream';

export class ErrRes extends $tea.Model {
  response?: SubResponse;
  sign: string;
  static names(): { [key: string]: string } {
    return {
      response: 'response',
      sign: 'sign',
    };
  }

  static types(): { [key: string]: any } {
    return {
      response:  SubResponse,
      sign: 'string'
    };
  }

  constructor(map?: { [key: string]: any }) {
    super(map);
  }
}

export class SubResponse extends $tea.Model {
  resultCode?: string;
  static names(): { [key: string]: string } {
    return {
      resultCode: 'result_code',
    };
  }

  static types(): { [key: string]: any } {
    return {
      resultCode: 'string',
    };
  }

  constructor(map?: { [key: string]: any }) {
    super(map);
  }
}

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
  static hasError(raw: string, secret: string): boolean {
    var tmp;
    try{
      tmp = $tea.cast<ErrRes>(JSON.parse(raw), new ErrRes());
    } catch {
      return true;
    }
    if(!tmp.response) {
      return true;
    }

    if (tmp.response.resultCode && tmp.response.resultCode.toLowerCase() != "ok") {
        return false;
    }

    if (!tmp.sign) {
        return true;
    }

    let s = raw.indexOf("response");
    let end = raw.indexOf("sign");
    let res = raw.substring(s, end);
    s = res.indexOf("{");
    end = res.lastIndexOf("}");
    let stringToSign = res.substring(s, end + 1);
    const sign = <string>kitx.sha1(stringToSign, secret, 'base64');
    const signServer = tmp.sign;
    if(sign === signServer) {
      return false;
    }

    return true;
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

  /**
  * Upload item with urlPath
  * @param item the file
  * @param urlPath the upload url
  */
  static async putObject(item: Readable, headers: { [key: string]: string }, urlPath: string): Promise<void> {
    let options: httpx.Options = {
      method: 'PUT',
      headers: headers
    };
    options.data = item;

    let response = await httpx.request(urlPath, options);

    if (response.statusCode >= 400 && response.statusCode < 600) {
      const errStr = await Util.readAsString(response);
      const respMap = OssUtil.getErrMessage(errStr);

      if (respMap["Code"] != null && respMap["Code"] === 'CallbackFailed') {
        return null;
      }

      throw $tea.newError({
        "code": respMap["Code"],
        "message": respMap["Message"]
      });
    }
  }

  /**
 * Parse  headers into map[string]string 
 * @param headers the target headers
 * @return the map[string]string
 */
  static parseUploadHeaders(headers: any): { [key: string]: string } {
    const byt = JSON.stringify(headers);
    const tmp = JSON.parse(byt);

    if(Array.isArray(tmp)) {
      let result = {};
      for(let i = 0; i < tmp.length; i++) {
        const item = tmp[i];
        result[item["name"]] = item["value"];
      }

      return result;
    } else {
      return null;
    }
  }

  /**
 * Generate a nonce string
 * @return the nonce string
 */
  static getNonce(): string {
    return kitx.makeNonce().replace('-', '');
  }



}
