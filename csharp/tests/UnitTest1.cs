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
            Assert.Equal(20, AntchainUtils.GetTimestamp().Length);
        }

        [Fact]
        public void Test_GetSignature()
        {
            var signedParams = new Dictionary<string, string>
            {
                {"test", "ok"}
            };

            string sign = AntchainUtils.GetSignature(signedParams, "secret");
            Assert.Equal("qlB4B1lFcehlWRelL7Fo4uNHPCs=", sign);
        }

        [Fact]
        public void Test_HasError()
        {
            string tmp = "testInvalidJson";
            var res = AntchainUtils.HasError(tmp, "secret");
            Assert.True(res);

            tmp = "{\"noResponse\":\"true\"}";
            res = AntchainUtils.HasError(tmp, "secret");
            Assert.True(res);

            tmp =
                "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"false\"},\"sign\":\"IUl/4uLq7utFnsjF1Zy6B6OWbCg=\"}";
            res = AntchainUtils.HasError(tmp, "secret");
            Assert.False(res);

            tmp =
                "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"}}";
            res = AntchainUtils.HasError(tmp, "secret");
            Assert.True(res);

            tmp =
                "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"},\"sign\":\"IUl/4uLq7utFnsjF1Zy6B6OWbCg=\"}";
            res = AntchainUtils.HasError(tmp, "secret");
            Assert.False(res);

            tmp =
                "{\"response\":{\"expired_time\":\"2021-01-04T17:04:42.072+08:00\",\"file_id\":\"kjiac1a298f8d\",\"req_msg_id\":\"79e093b3ae0f3f2c1\",\"result_code\":\"OK\"},\"sign\":\"IUl/4uLqtFnsjF1Zy6B6OWbCg=\"}";
            res = AntchainUtils.HasError(tmp, "secret");
            Assert.True(res);
        }

        [Fact]
        public void Test_IsSuccess()
        {
            Assert.True(AntchainUtils.IsSuccess("OK", "success"));
            Assert.True(AntchainUtils.IsSuccess("success", "success"));
            Assert.False(AntchainUtils.IsSuccess("failed", "success"));
        }
    }
}