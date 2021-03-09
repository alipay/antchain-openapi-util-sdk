import org.junit.Assert;
import org.junit.Test;
import com.antgroup.antchain.openapi.antchain.util.*;

public class UtilTest {
    @Test
    public void testHasError() throws Exception{
        String content = "{\"response\":{\"result_msg\":\"OK\",\"req_msg_id\":\"56c58a4bbffe493e4b89fc5a4a0611f0\","+
                "\"file_id\":\"kjrx355r85e0dc9c\",\"expired_time\":\"2021-01-11T10:58:27.039+08:00\","+
                "\"upload_headers\":[{\"name\":\"Content-Disposition\",\"value\":\"application/octet-stream\"},"+
                "{\"name\":\"x-oss-forbid-overwrite\",\"value\":\"true\"},{\"name\":\"Content-Type\","+
                "\"value\":\"application/octet-stream\"}],\"upload_url\":\"http://acpmpcore-dev.oss-cn-hangzhou."+
                "aliyuncs.com/gatewayx/kjrx355r85e0dc9c/kjrx355r85e0dc9c?Expires=1610333907&OSSAccessKeyId="+
                "xxxxxx&Signature=xxxxx%3D&response-content-disposition="+
                "application%2Foctet-stream\",\"result_code\":\"OK\"},\"sign\":\"dYj+3O1oOl8bwh3vjfDMody6caY=\"}";
        boolean error = AntchainUtils.hasError(content, "xxxxx");
        Assert.assertEquals(false, error);
    }

    @Test
    public void testIsSuccess() {
        String strA = "Java";
        String strB = "java";
        boolean eq = AntchainUtils.isSuccess(strA, strB);
        Assert.assertEquals(true, eq);
    }
}
