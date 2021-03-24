/**
 * This is a utility module
 */
// This file is auto-generated, don't edit it. Thanks.

using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using AntChain.AlipayUtil.Models;
using Newtonsoft.Json;
using Tea;
using Tea.Utils;

namespace AntChain.AlipayUtil
{
    public class AntchainUtils
    {
        /**
         * Get timestamp
         * @return the string
         */
        public static string GetTimestamp()
        {
            return DateTime.UtcNow.ToUniversalTime().ToString("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }

        /**
         * Calculate signature according to signedParams and secret
         * @param signedParams the signed string
         * @param secret the accesskey secret
         * @return the signature string
         */
        public static string GetSignature(Dictionary<string, string> signedParams, string secret)
        {
            string stringToSign = BuildStringToSign(signedParams);
            System.Diagnostics.Debug.WriteLine("Alibabacloud.AlipayUtil.GetSignature:stringToSign is " +
                                               stringToSign.ToString());
            string signedStr = Sign(stringToSign, secret);
            return signedStr;
        }

        public static bool HasError(string raw, string secret)
        {
            ErrRes err = new ErrRes();
            try
            {
                err = JsonConvert.DeserializeObject<ErrRes>(raw);
            }
            catch
            {
                return true;
            }

            if (err.Response == null)
            {
                return true;
            }

            if (err.Response.ResultCode != null && err.Response.ResultCode.ToLower() != "ok")
            {
                return false;
            }

            if (string.IsNullOrEmpty(err.Sign))
            {
                return true;
            }

            int s = raw.IndexOf("\"response\"");
            int end = raw.IndexOf("\"sign\"");
            string res = raw.Substring(s, end - s - 1);
            s = res.IndexOf('{');
            end = res.LastIndexOf('}');
            string signToString = res.Substring(s, end - s + 1);
            string sign = Sign(signToString, secret);
            if (err.Sign == sign)
            {
                return false;
            }

            return true;
        }

        internal static string Sign(string signToString, string secret)
        {
            byte[] signData;
            using (KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA1") as KeyedHashAlgorithm)
            {
                algorithm.Key = Encoding.UTF8.GetBytes(secret);
                signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(signToString.ToCharArray()));
            }

            string signedStr = Convert.ToBase64String(signData);

            return signedStr;
        }

        /// <summary>
        /// Upload item with urlPath
        /// </summary>
        /// <param name="item">the file</param>
        /// <param name="headers"></param>
        /// <param name="urlPath">the upload url</param>
        public static void PutObject(Stream item, Dictionary<string, string> headers, string urlPath)
        {
            var uri = new Uri(urlPath);
            var httpWebRequest = (HttpWebRequest) WebRequest.Create(uri);

            foreach (var keypair in headers)
            {
                httpWebRequest.Headers.Add(keypair.Key, keypair.Value);
            }

            httpWebRequest.Method = "PUT";

            using (var stream = httpWebRequest.GetRequestStream())
            {
                item.CopyTo(stream);
            }

            try
            {
                HttpWebResponse httpWebResponse = (HttpWebResponse) httpWebRequest.GetResponse();
                httpWebResponse.Dispose();
            }
            catch (WebException we)
            {
                var httpWebResponse = we.Response as HttpWebResponse;
                if ((int) httpWebResponse.StatusCode >= 400 && (int) httpWebResponse.StatusCode < 600)
                {
                    byte[] bytes;
                    using (var ms = new MemoryStream())
                    using (var stream = httpWebResponse.GetResponseStream())
                    {
                        {
                            var buffer = new byte[1024];
                            while (true)
                            {
                                var length = stream.Read(buffer, 0, 1024);
                                if (length == 0)
                                {
                                    break;
                                }

                                ms.Write(buffer, 0, length);
                            }

                            ms.Seek(0, SeekOrigin.Begin);
                            bytes = new byte[ms.Length];
                            ms.Read(bytes, 0, bytes.Length);
                        }
                    }

                    string body = Encoding.UTF8.GetString(bytes);
                    var respMap = AlibabaCloud.OSSUtil.Common.GetErrMessage(body);
                    if (respMap["Code"] != null && respMap["Code"].ToSafeString() == "CallbackFailed")
                    {
                        return;
                    }

                    var errMap = new Dictionary<string, object>();
                    errMap["code"] = respMap["Code"];
                    errMap["message"] = respMap["Message"];
                    errMap["data"] = new Dictionary<string, object>
                    {
                        {"httpCode", (int) httpWebResponse.StatusCode},
                        {"requestId", respMap["RequestId"]},
                        {"hostId", respMap["HostId"]}
                    };
                    throw new TeaException(errMap);
                }

                httpWebResponse.Dispose();
            }
        }

        /// <summary>
        /// Upload item with urlPath
        /// </summary>
        /// <param name="item">the file</param>
        /// <param name="headers"></param>
        /// <param name="urlPath">the upload url</param>
        public async static Task PutObjectAsync(Stream item, Dictionary<string, string> headers, string urlPath)
        {
            var uri = new Uri(urlPath);
            var httpWebRequest = (HttpWebRequest) WebRequest.Create(uri);

            httpWebRequest.Method = "PUT";

            foreach (var keypair in headers)
            {
                httpWebRequest.Headers.Add(keypair.Key, keypair.Value);
            }

            using (var stream = await httpWebRequest.GetRequestStreamAsync())
            {
                await item.CopyToAsync(stream);
            }

            try
            {
                HttpWebResponse httpWebResponse = (HttpWebResponse) await httpWebRequest.GetResponseAsync();
                httpWebResponse.Dispose();
            }
            catch (WebException we)
            {
                var httpWebResponse = we.Response as HttpWebResponse;
                if ((int) httpWebResponse.StatusCode >= 400 && (int) httpWebResponse.StatusCode < 600)
                {
                    byte[] bytes;
                    using (var ms = new MemoryStream())
                    using (var stream = httpWebResponse.GetResponseStream())
                    {
                        {
                            var buffer = new byte[1024];
                            while (true)
                            {
                                var length = stream.Read(buffer, 0, 1024);
                                if (length == 0)
                                {
                                    break;
                                }

                                ms.Write(buffer, 0, length);
                            }

                            ms.Seek(0, SeekOrigin.Begin);
                            bytes = new byte[ms.Length];
                            ms.Read(bytes, 0, bytes.Length);
                        }
                    }

                    string body = Encoding.UTF8.GetString(bytes);
                    var respMap = AlibabaCloud.OSSUtil.Common.GetErrMessage(body);
                    if (respMap["Code"] != null && respMap["Code"].ToSafeString() == "CallbackFailed")
                    {
                        return;
                    }

                    var errMap = new Dictionary<string, object>();
                    errMap["code"] = respMap["Code"];
                    errMap["message"] = respMap["Message"];
                    errMap["data"] = new Dictionary<string, object>
                    {
                        {"httpCode", (int) httpWebResponse.StatusCode},
                        {"requestId", respMap["RequestId"]},
                        {"hostId", respMap["HostId"]}
                    };
                    throw new TeaException(errMap);
                }

                httpWebResponse.Dispose();
            }
        }

        /// <summary>
        /// Parse  headers into map[string]string
        /// </summary>
        /// <param name="headers">the target headers</param>
        /// <returns>map[string]string</returns>
        public static Dictionary<string, string> ParseUploadHeaders(object headers)
        {
            if (headers == null)
            {
                return null;
            }

            var result = new Dictionary<string, string>();

            if (typeof(IList).IsAssignableFrom(headers.GetType()))
            {
                IList list = (IList) headers;
                Type listType = headers.GetType().GetGenericArguments()[0];
                for (int i = 0; i < list.Count; i++)
                {
                    Dictionary<string, object> itemMap = new Dictionary<string, object>();
                    if (typeof(TeaModel).IsAssignableFrom(listType))
                    {
                        itemMap = ((TeaModel) list[i]).ToMap();
                    }
                    else if (typeof(IDictionary).IsAssignableFrom(listType))
                    {
                        IDictionary dic = (IDictionary) list[i];
                        itemMap = dic.Keys.Cast<string>().ToDictionary(key => key, key => dic[key]);
                    }

                    result[itemMap["name"].ToString()] = itemMap["value"].ToSafeString();
                }
            }
            else
            {
                return null;
            }

            return result;
        }

        /// <summary>
        /// Generate a nonce string
        /// </summary>
        /// <returns>the nonce string</returns>
        public static string GetNonce()
        {
            return Guid.NewGuid().ToString("N");
        }

        /**
        * Judge upload if ok or not
        *
        * @return the boolean
        * @example resultCode == successCode or resultCode == ok is true
        * @error no error throws
        */
        public static bool? IsSuccess(string resultCode, string successCode)
        {
            resultCode = resultCode.ToLower();
            successCode = successCode.ToLower();
            return "ok".Equals(resultCode) || resultCode == successCode;
        }

        public static DateTime parseDate(String date)
        {
            return DateTime.Parse(date);
        }

        public static String formatDate(DateTime date)
        {
            return date.ToString();
        }

        internal static string BuildStringToSign(Dictionary<string, string> signedParam)
        {
            string stringToSign = string.Empty;
            var signParams = new Dictionary<string, string>();
            foreach (var keypair in signedParam)
            {
                if (!string.IsNullOrEmpty(keypair.Value))
                {
                    signParams.Add(keypair.Key, keypair.Value);
                }
            }

            return GetUrlFormedMap(signParams);
        }

        internal static string GetUrlFormedMap(Dictionary<string, string> source)
        {
            List<string> sortedKeys = source.Keys.ToList();
            sortedKeys.Sort();
            StringBuilder canonicalizedQueryString = new StringBuilder();

            for (int i = 0; i < sortedKeys.Count; i++)
            {
                string key = sortedKeys[i];
                if (i > 0)
                {
                    canonicalizedQueryString.Append("&");
                }

                canonicalizedQueryString.Append(PercentEncode(key)).Append("=")
                    .Append(PercentEncode(source[key]));
            }

            return canonicalizedQueryString.ToString();
        }

        internal static string PercentEncode(string value)
        {
            if (value == null)
            {
                return null;
            }

            var stringBuilder = new StringBuilder();
            var text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";
            var bytes = Encoding.UTF8.GetBytes(value);
            foreach (char c in bytes)
            {
                if (text.IndexOf(c) >= 0)
                {
                    stringBuilder.Append(c);
                }
                else
                {
                    stringBuilder.Append("%").Append(string.Format(CultureInfo.InvariantCulture, "{0:X2}", (int) c));
                }
            }

            return stringBuilder.ToString().Replace("+", "%20")
                .Replace("*", "%2A").Replace("%7E", "~");
        }
    }
}