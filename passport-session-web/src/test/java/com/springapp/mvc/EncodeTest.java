package com.springapp.mvc;

import com.sogou.upd.passport.session.util.SessionSDKUtil;
import com.sogou.upd.passport.session.util.SessionServerUtil;
import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-28
 * Time: 下午9:04
 */
public class EncodeTest {

    @Test
    public void Base64LongEncode(){
        byte[] by=null;

        long a=System.currentTimeMillis();
        System.out.println(a+":"+long2bytes(a).length);
        String aB= new String(Base64.encodeBase64String(long2bytes(a)));
        System.out.println(aB.length()+":"+aB);
        long b= bytes2long(Base64.decodeBase64(aB.getBytes()));
        System.out.println(b+":"+(a==b));



        int dsdsd = 2113112300;

        int cdcdc=1385694797;

        byte[] by2=null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update((a + "asdasdas@sodsadasdas dasdasdasdas dadas das das da sdas d as das dgou.com" + 1111 + "dasdasd21^$%^%$").getBytes());
            by2 = md.digest();

            aB= new String(Base64.encodeBase64URLSafeString(by2));
            System.out.println(aB.length()+":"+aB);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        byte[] version={2};
        by=byteMerger(version,long2bytes(a));
        System.out.println(by.length);
        by=byteMerger(by,by2);


        System.out.println(by.length);
        aB=Base64.encodeBase64URLSafeString(by);
        System.out.println(aB.length()+":"+aB);
    }


    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     *
     * @param passportId
     * @return
     */
    public static String createUid(String passportId){

        return null;
    }

//    public String getDate(){
////        new Date()
//    }

    public static int bytes2int(byte[] b) {
        int temp = 0;
        int res = 0;
        for (int i=0;i<4;i++) {
            res <<= 4;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }

    public static byte[] int2bytes(int num) {
        byte[] b = new byte[4];
        for (int i=0;i<4;i++) {
            b[i] = (byte)(num>>>(56-(i*8)));
        }
        return b;
    }



    public static long bytes2long(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i=0;i<8;i++) {
            res <<= 8;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }

    public static byte[] long2bytes(long num) {
        byte[] b = new byte[8];
        for (int i=0;i<8;i++) {
            b[i] = (byte)(num>>>(56-(i*8)));
        }
        return b;
    }

    @Test
    public void testCheckSid() throws InterruptedException {
        String sid= SessionServerUtil.createSessionSid("ldasdahjkhbu@#@#%$^%^%@sogou.com");
        System.out.println(sid.length()+":"+sid);
        Date date= SessionServerUtil.getDate(sid);
        System.out.println("date:"+date);
        Thread.sleep(100l);
        System.out.println(SessionServerUtil.checkSid(sid));
    }


    @Test
    public void testCreateSid(){
        long startDate=System.currentTimeMillis();
        for(int i=0;i<1000000;i++){
            String sid= SessionServerUtil.createSessionSid("ldasdahjkhbu@#@#%$^%^%@sogou.com" + i);
            if(!SessionServerUtil.checkSid(sid)){
              System.out.println("sid:"+sid);
              System.out.println("passportid:"+"ldasdahjkhbu@#@#%$^%^%@sogou.com"+i);
            }
            Assert.assertTrue(SessionSDKUtil.checkSid(sid));
            if(i%10000==0){
                long endDate=System.currentTimeMillis();
                System.out.println(i+":"+(endDate-startDate));
            }
        }
        long endDate=System.currentTimeMillis();
        System.out.println("end:"+(endDate-startDate));
    }
}
