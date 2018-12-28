package com.lingdouhealth.kyowa;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dycdpapi.model.v20180610.CreateCdpOrderRequest;
import com.aliyuncs.dycdpapi.model.v20180610.CreateCdpOrderResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by billy on 2018/12/28.
 */
@Controller
@RequestMapping("flow")
public class ChongLiuliang {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private IAcsClient client;

    private IAcsClient getClient (String aliKey, String aliSecret) throws ClientException {
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //产品名称
        final String product = "Dycdpapi";
        //域
        final String domain = "dycdpapi.aliyuncs.com";
        //初始化客户端
        IClientProfile profile = DefaultProfile.getProfile("cn‐hangzhou", aliKey, aliSecret);
        DefaultProfile.addEndpoint("cn‐hangzhou", "cn‐hangzhou", product, domain);
        client = new DefaultAcsClient(profile);
        return client;
    }

    /**
     * 发送流量
     * @param phone 手机号
     * @param offerId 订单Id
     * @throws ClientException
     */
    @RequestMapping(produces = {"application/json; charset=utf-8"})
    @ResponseBody
    private void sendFlow(String phone, Long offerId, String outOrderId, String aliKey, String aliSecret) throws ClientException {
        client = getClient(aliKey, aliSecret);
        logger.info("充值请求 手机号 {} offerId {}", phone, offerId);
        // TODO out订单号需要外部传参数
        CreateCdpOrderRequest ltReq = new CreateCdpOrderRequest();
        ltReq.setPhoneNumber(phone);
        ltReq.setOutOrderId(outOrderId);
        ltReq.setOfferId(offerId);
        ltReq.setReason("测试流量赠送");
        System.out.println("电信OutOrderId " + outOrderId);
        CreateCdpOrderResponse ltResponse = client.getAcsResponse(ltReq);
        if ("OK".equals(ltResponse.getCode())) {
            //业务处理
            CreateCdpOrderResponse.Data data = ltResponse.getData();
            System.out.println("ResultCode:" + data.getResultCode());
            System.out.println("ResultMsg:" + data.getResultMsg());
            System.out.println("OrderId:" + data.getOrderId());
            System.out.println("ExtendParam:" + data.getExtendParam());
        } else {
            // 说明请求超时，没有拿到结果，需要用该outOrderId重试，获取幂等结果
            //如果重试获取的CreateCdpOrderResponse.Code为10004或00000则可标识下单成功
            //10004表示重复的outOrderId，说明可能是上次超时的请求已下单成功，或之前你用过该outOrderId下过单
            //你自己要保证该outOrderId的唯一
            //00000表示下单成功，之前超时的请求没有成功。
        }
    }
}
