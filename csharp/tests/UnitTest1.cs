using System;
using System.Collections.Generic;
using Alipaycloud.AlipayUtil;
using Xunit;

namespace tests
{
    public class UnitTest1
    {
        [Fact]
        public void Test_GetTimestamp()
        {
            Assert.Contains("GMT",Client.GetTimestamp());
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
            var resp = new Dictionary<string, string>
            {
                { "test","ok"}
            };

            Assert.False(Client.HasError(resp));

            var resObj = new Dictionary<string, object>
            {
                { "response", new Dictionary<string,object>
                {
                    {"result_code", "OK" }
                }
                }
            };

            Assert.False(Client.HasError(resObj));

            resObj = new Dictionary<string, object>
            {
                { "response", new Dictionary<string,object>
                {
                    {"result_code", "Success" }
                }
                }
            };
            Assert.True(Client.HasError(resObj));
        }


    }
}
