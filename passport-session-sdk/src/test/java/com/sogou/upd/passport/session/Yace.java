package com.sogou.upd.passport.session;

import com.sogou.upd.passport.session.sdk.model.RequestModel;
import com.sogou.upd.passport.session.sdk.param.HttpMethodEnum;
import com.sogou.upd.passport.session.sdk.util.CoderUtil;
import com.sogou.upd.passport.session.sdk.util.HttpClientUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-9
 * Time: 下午8:22
 */
public class Yace implements  Runnable{

    public String a;

    @Override
    public void run() {
        long startTime=System.currentTimeMillis();
        System.out.println("start:"+a);
        for(int i=0;i<1000;i++){
            String sgid=setSession(i);
            for(int j=0;j <10;j++){
                getSession(sgid);
            }
        }
        long endTime=System.currentTimeMillis();
        System.out.println("end:"+a+",time:"+(endTime-startTime));

    }

    public String getSession(String sgid){
        long ct=System.currentTimeMillis();
        RequestModel setRequestModel=new RequestModel("http://session.account.sogou.com.z.sogou-op.org/verify_sid");
        String code= CoderUtil.generatorCode(sgid, 1120, "4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY", ct);
        setRequestModel.addParam("ct",ct);
        setRequestModel.addParam("sgid",sgid);
        setRequestModel.addParam("code",code);
        setRequestModel.addParam("client_id",1120);
        setRequestModel.addParam("user_ip","127.0.0.1");
        setRequestModel.setHttpMethodEnum(HttpMethodEnum.POST);
        try {
            String result= HttpClientUtil.executeStr(setRequestModel);
            if(!result.contains("\"status\":\"0\"")){
               System.out.println(result);
            }
        } catch (RuntimeException re) {
        }
        return sgid;
    }


    public String setSession(int i){
        long ct=System.currentTimeMillis();
        RequestModel setRequestModel=new RequestModel("http://session.account.sogou.com.z.sogou-op.org/set_session");
        String sgid=SessionServerUtil.createSessionSid(a+i+"@test.com");
        String code= CoderUtil.generatorCode(sgid, 1120, "4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY", ct);
        setRequestModel.addParam("ct",ct);
        setRequestModel.addParam("sgid",sgid);
        setRequestModel.addParam("code",code);
        setRequestModel.addParam("client_id",1120);
        setRequestModel.addParam("user_info","{\"prssport_id\":\""+a+i+"@test.com"+"\"}");
        setRequestModel.setHttpMethodEnum(HttpMethodEnum.POST);
        try {
           HttpClientUtil.executeStr(setRequestModel);
        } catch (RuntimeException re) {
        }
        return sgid;
    }

    public static void main(String[] args){
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(50);
        for(int i=0;i<500;i++){
            Yace ya=new Yace();
            ya.a=i+"";
            scheduledThreadPool.execute(ya);
        }
        System.out.println("end");
    }
}
