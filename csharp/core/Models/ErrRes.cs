using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

namespace AntChain.AlipayUtil.Models
{
    public class ErrRes
    {
        [JsonProperty(PropertyName = "response")]
        public SubResponse Response { get; set; }

        [JsonProperty(PropertyName = "sign")]
        public string Sign { get; set; }

        public class SubResponse
        {
            [JsonProperty(PropertyName = "result_code")]
            public string ResultCode { get; set; }
        }
    }
}
