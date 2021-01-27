using System;
using System.Collections.Generic;
using AntChain.AlipayUtil;
using Xunit;

namespace tests
{
    public class UnitTest1
    {
        [Fact]
        public void Test_GetTimestamp()
        {
            Assert.Equal(20, Client.GetTimestamp().Length);
        }

        [Fact]
        public void Test_GetSignature()
        {
            var signedParams = new Dictionary<string, string>
            {
                { "test","ok"}
            };

            string sign = Client.GetSignature(signedParams, "secret");
            Assert.Equal("qlB4B1lFcehlWRelL7Fo4uNHPCs=", sign);
        }

        [Fact]
        public void Test_HasError()
        {
            string tmp = "testInvalidJson";
            var res = Client.HasError(tmp, "secret", "ok");
            Assert.True(res);

            tmp = "{\"noResponse\":\"true\"}";
            res = Client.HasError(tmp, "secret", "ok");
            Assert.True(res);

            tmp = "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"false\"},\"sign\":\"IUl/4uLq7utFnsjF1Zy6B6OWbCg=\"}";
            res = Client.HasError(tmp, "secret", "ok");
            Assert.True(res);

            tmp = "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"success\"},\"sign\":\"yzkJeThFNNecgG/fDxFgSWj9tDY=\"}";
            res = Client.HasError(tmp, "secret", "success");
            Assert.False(res);

            tmp = "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"}}";
            res = Client.HasError(tmp, "secret", "ok");
            Assert.True(res);

            tmp = "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"},\"sign\":\"IUl/4uLq7utFnsjF1Zy6B6OWbCg=\"}";
            res = Client.HasError(tmp, "secret", "ok");
            Assert.False(res);

            tmp = "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"},\"sign\":\"IUl/4uLqtFnsjF1Zy6B6OWbCg=\"}";
            res = Client.HasError(tmp, "secret", "ok");
            Assert.True(res);
        }


    }
}
