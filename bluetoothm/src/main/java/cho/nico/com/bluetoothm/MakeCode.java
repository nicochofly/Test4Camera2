package cho.nico.com.bluetoothm;

import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MakeCode {

    public static String makecard(String CardData) {
        int[] Card_array = new int[16];
        int nType;
        String cardhexstring = "";
        String cardType = "", cardno = "", cardbuild = "", cardfloor = "", cardroom = "", cardarea = "",
                cardauthorization = "";
        String cardflag = "", changkai = "", ZuHaoflag = "", issuetime = "", cardendtime = "", lostno = "",
                baojing = "", wurao = "", shiduan = "", DLL = "";
        String unlock = "";

        String[] temp;

        temp = CardData.split(",");

        for (String str : temp) {
            Log.e("caodongquan", "temp >> " + str);
        }

        for (int i = 0; i < temp.length; i++) {
            if (temp[i].substring(0, 1).equals("T")) {
                cardType = temp[i].substring(1);
                Log.e("caodongquan", "cardType >> " + cardType);
            }

            if (temp[i].substring(0, 1).equals("C")) {
                cardno = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("L")) {
                lostno = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("X")) {
                DLL = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("B")) {
                cardbuild = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("F")) {
                cardfloor = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("R")) {
                cardroom = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("A")) {
                cardarea = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("M")) {
                cardauthorization = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("Y")) {
                cardflag = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("S")) {
                issuetime = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("O")) {
                cardendtime = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("Q")) {
                baojing = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("W")) {
                wurao = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("D")) {
                shiduan = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("K")) {
                changkai = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("Z")) {
                ZuHaoflag = temp[i].substring(1);
            }

            if (temp[i].substring(0, 1).equals("U")) {
                unlock = temp[i].substring(1);
            }

        }

        if (TextUtils.isEmpty(DLL)) {
            DLL = "0";
        }
        if (TextUtils.isEmpty(unlock)) {
            unlock = "0";
        }
        if (TextUtils.isEmpty(cardflag)) {
            cardflag = "0";
        }
        if (TextUtils.isEmpty(baojing)) {
            baojing = "128";
        }
        if (TextUtils.isEmpty(wurao)) {
            wurao = "152";
        }
        if (TextUtils.isEmpty(changkai)) {
            changkai = "0";
        }

        nType = Integer.parseInt(cardType);

        int m = 0;
        Card_array[m++] = 0xC9;
        Card_array[m++] = Integer.valueOf(shiduan)/* 0xd1 */;// 0xff ;
        Card_array[m++] = (Integer.valueOf(cardauthorization) / 256)/*0xf8*/; // 酒店授权
        if (Integer.valueOf(changkai) > 0) {
            Card_array[m] = (byte) (Card_array[m] + 64);
        }
        Card_array[m++] = (Integer.valueOf(cardauthorization) % 256)/*0xfa*/;

        switch (nType) {

            case 0: {
                Card_array[m++] = (Integer.valueOf(Integer.valueOf(cardauthorization) / 255)) & 0xff;
                Card_array[m++] = (Integer.valueOf(cardauthorization) % 255) & 0xff;
                Card_array[m++] = Integer.valueOf(baojing);
                Card_array[m++] = Integer.valueOf(wurao);
                break;
            }

            case 1: {

                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = 0xff;
                break;
            }

            case 2: {
                Card_array[m++] = 0x00; // 房号 //m:=4; LLock需要设置
                Card_array[m++] = Integer.valueOf(cardroom); // 房号 //m:=5;
                Card_array[m++] = Integer.valueOf(cardfloor); // 楼层
                Card_array[m++] = Integer.valueOf(cardbuild); // 楼栋
                break;
            }

            case 3: {

                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                break;
            }

            case 4: {
                while (lostno.length() < 8) {
                    lostno = lostno + "F";
                }
                Card_array[m++] = Integer.valueOf(lostno.substring(0, 2), 16);
                Card_array[m++] = Integer.valueOf(lostno.substring(4, 6), 16);
                Card_array[m++] = Integer.valueOf(lostno.substring(6, 8), 16);
                Card_array[m++] = Integer.valueOf(lostno.substring(8, 10), 16);
                break;
            }

            case 5: {

                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = Integer.valueOf(cardarea);
                Card_array[m++] = Integer.valueOf(ZuHaoflag);
                break;
            }

            case 6: {
                Card_array[m++] = Integer.valueOf(unlock);
                Card_array[m++] = Integer.valueOf(cardroom);
                Card_array[m++] = Integer.valueOf(cardfloor);
                Card_array[m++] = Integer.valueOf(cardbuild);
                break;
            }

            case 7: {

                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                break;
            }

            case 8: {
                Card_array[m++] = 127;
                Card_array[m++] = (8 * Integer.valueOf(shiduan.substring(0, 2), 16)
                        + Integer.valueOf(Integer.valueOf(shiduan.substring(2, 4), 16) / 10));
                Card_array[m++] = Integer.valueOf(cardarea);
                Card_array[m++] = /* Random(255) */0;
                break;
            }

            case 10: {
                Card_array[m++] = 0xff;
                Card_array[m++] = (8 * Integer.valueOf(shiduan.substring(0, 2), 16)
                        + Integer.valueOf(Integer.valueOf(shiduan.substring(2, 4), 16) / 10));
                Card_array[m++] = /* Random(255) */0;
                Card_array[m++] = /* Random(255) */0;
                break;
            }

            case 11: {
                Card_array[m++] = 0xff;

                shiduan = "0" + shiduan;
                int value = (8 * Integer.valueOf(shiduan.substring(0, 2), 16)
                        + Integer.valueOf(Integer.valueOf(shiduan.substring(2, 4), 16) / 10));
                Log.e("caodongquan", "Card_array >>>  " + Integer.toHexString(value));
                Card_array[m++] = value;
                Random r = new Random();
                int random1 = r.nextInt(256);
                int random2 = r.nextInt(256);
                Log.e("caodongquan", "random1>>" + random1 + "  random2>>" + random2);
                Card_array[m++] = /* Random(255) */random1;
                Card_array[m++] = /* Random(255) */random2;
                break;
            }

            case 12: {
                Card_array[m++] = 127;
                Card_array[m++] = (8 * Integer.valueOf(shiduan.substring(0, 2), 16)
                        + Integer.valueOf(Integer.valueOf(shiduan.substring(2, 4), 16) / 10));
                Card_array[m++] = Integer.valueOf(cardfloor);
                Card_array[m++] = Integer.valueOf(cardbuild);
                break;
            }

            case 13: {
                Card_array[m++] = 127;
                Card_array[m++] = (8 * Integer.valueOf(shiduan.substring(0, 2), 16)
                        + Integer.valueOf(Integer.valueOf(shiduan.substring(2, 4), 16) / 10));
                Card_array[m++] = 0x00;
                Card_array[m++] = Integer.valueOf(cardbuild);
                break;
            }

            default: {
                Card_array[m++] = Integer.valueOf(unlock);
                Card_array[m++] = (Integer.valueOf(cardroom) + 0);
                Card_array[m++] = (Integer.valueOf(cardfloor) + 128);
                Card_array[m++] = Integer.valueOf(cardbuild);
                break;
            }

        }

        Card_array[m++] = Integer.valueOf(cardflag);
        Card_array[m++] = (nType * 16 + Integer.valueOf(cardno));

        Card_array[m++] = ((Integer.valueOf(issuetime.substring(0, 2)) - 9) * 16
                + Integer.valueOf(issuetime.substring(2, 4))); // 发卡时间
        Card_array[m++] = (Integer.valueOf(issuetime.substring(4, 6)) * 8
                + (Integer.valueOf(Integer.valueOf(issuetime.substring(6, 8)) / 4)));
        Card_array[m++] = ((Integer.valueOf(issuetime.substring(6, 8)) % 4) * 64
                + Integer.valueOf(issuetime.substring(8, 10)));

        Card_array[m++] = (16 * (Integer.valueOf(cardendtime.substring(0, 2)) - 9)
                + Integer.valueOf(cardendtime.substring(2, 4)));
        Card_array[m++] = (Integer.valueOf(cardendtime.substring(4, 6)) * 8
                + (Integer.valueOf(Integer.valueOf(cardendtime.substring(6, 8)) / 4))); // 退房时间
        Card_array[m++] = ((Integer.valueOf(cardendtime.substring(6, 8)) % 4) * 64
                + Integer.valueOf(cardendtime.substring(8, 10)));

        Card_array[2] ^= Card_array[12];
        Card_array[2] ^= 0xa1;
        Card_array[3] += Card_array[11];
        Card_array[3] ^= 0x7c;

        cardhexstring = "";
        for (int i = 0; i < 16; i++) {
            String ArrayString;
            ArrayString = Integer.toHexString(Card_array[i]);
            if (ArrayString.length() < 2) {
                ArrayString = "0" + ArrayString;
            }
            Log.e("caodongquan", "ArrayString " + ArrayString);
            cardhexstring = cardhexstring + ArrayString;
        }

        // console.log('Select makecard data:', cardhexstring.toUpperCase());

        Log.e("caodongquan", "Select makecard data: " + cardhexstring.toUpperCase());
        return cardhexstring.toUpperCase();

    }



    public static byte[]  getBtData() {
        // byte[] result =
        // String2Byte("576b0000000000000000000000000000000000");//576800041A0F31030061000000000000000000CB
        // //576b0000000000000000000000000000000000

        int time = 1;
        Date now = new Date();
        Date now_10 = new Date(now.getTime() + (7200000 * time)); //
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");// 可以方便地修改日期格式
        String nowTime_10 = dateFormat.format(now_10);
        /******************************************************/
        Date now_1 = new Date(now.getTime() + (3600000 * time));
        String nowTime_1 = dateFormat.format(now_1);

        String cardStr = "T06,C01,M13202,Y01,B01,F08,R08,S" + nowTime_1 + ",I" + nowTime_1 + ",O" + nowTime_10 + ",D209";
//		Log.e("caodongquan", "cardStr >>" + cardStr);
        String cardResult = MakeCode.makecard(cardStr);
        cardResult = cardResult.substring(2);
//		cardResult = "d1f8fa800808010161a7f46aa87300";

//		Log.e("caodongquan", "byte >> " + "576B" + cardResult + "0000");
        byte[] result = String2Byte("576B" + cardResult + "0000");

        ArrayList<byte[]> arrayList = new ArrayList<byte[]>();
        arrayList.add(result);
        byte[] result1 = sumMultiByteArray(arrayList);

        byte bb = 0x00;
        for (byte b : result1) {
            bb = (byte) (bb += b);
        }
        byte result2 = getXor(bb);
        byte[] r = new byte[20];
        for (int i = 0; i < r.length; i++) {
            if (i == 19) {
                r[i] = result2;
            } else {
                r[i] = result1[i];
            }
        }
        byte[] rr = enSecrect(r);
//        ArrayList<byte[]> arrayList1 = new ArrayList<byte[]>();
//        arrayList1.add(rr);
        return rr;
    }

    public static byte[] String2Byte(String s) {
        s = s.replace(" ", "");
        s = s.replace("#", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return baKeyword;
    }

    public static String byte2String(byte[] data) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[data.length * 2];

        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        String result = new String(hexChars);
        result = result.replace(" ", "");
        return result;
    }

    public static byte[] sumMultiByteArray(ArrayList<byte[]> arrayList) {
        int byteArrayLen = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            byteArrayLen += arrayList.get(i).length;
        }

        byte[] sumBtyeArray = new byte[byteArrayLen];
        int currentIndex = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            System.arraycopy(arrayList.get(i), 0, sumBtyeArray, currentIndex, arrayList.get(i).length);
            currentIndex += arrayList.get(i).length;
        }
        return sumBtyeArray;
    }

    public static byte getXor(byte data) {

        byte xor = 0x4a;
        xor ^= data;
        return xor;
    }

    private static byte[] mima = { 0x0E, 0x08, 0x09, 0x06 };

    private static byte[] enSecrect(byte[] r) {

        r[1] -= mima[0];
        r[2] ^= mima[3];
        r[3] += mima[2];
        r[4] ^= mima[1];

        r[5] ^= mima[1];
        r[6] -= mima[0];
        r[7] ^= mima[3];
        r[8] += mima[2];

        r[9] += mima[2];
        r[10] ^= mima[1];
        r[11] -= mima[0];
        r[12] ^= mima[3];

        r[13] ^= mima[3];
        r[14] -= mima[2];
        r[15] += mima[1];
        r[16] ^= mima[0];

        r[17] -= mima[0];
        r[18] ^= mima[3];
        r[19] += mima[2];

        return r;

    }
}
