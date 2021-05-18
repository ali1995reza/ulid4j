package ulid4j.impl;

import ulid4j.api.ULID;
import ulid4j.api.ULIDGenerator;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;

public class SimpleULIDGenerator implements ULIDGenerator {

    private static final char[] VALUES = new char[32];
    private static final byte[] CHAR_TO_VALUES = new byte[256];
    static {

        VALUES[0] = '0';   CHAR_TO_VALUES['0'] = 0;
        VALUES[1] = '1';   CHAR_TO_VALUES['1'] = 1;
        VALUES[2] = '2';   CHAR_TO_VALUES['2'] = 2;
        VALUES[3] = '3';   CHAR_TO_VALUES['3'] = 3;
        VALUES[4] = '4';   CHAR_TO_VALUES['4'] = 4;
        VALUES[5] = '5';   CHAR_TO_VALUES['5'] = 5;
        VALUES[6] = '6';   CHAR_TO_VALUES['6'] = 6;
        VALUES[7] = '7';   CHAR_TO_VALUES['7'] = 7;
        VALUES[8] = '8';   CHAR_TO_VALUES['8'] = 8;
        VALUES[9] = '9';   CHAR_TO_VALUES['9'] = 9;
        VALUES[10] = 'a';  CHAR_TO_VALUES['a'] = 10;
        VALUES[11] = 'b';  CHAR_TO_VALUES['b'] = 11;
        VALUES[12] = 'c';  CHAR_TO_VALUES['c'] = 12;
        VALUES[13] = 'd';  CHAR_TO_VALUES['d'] = 13;
        VALUES[14] = 'e';  CHAR_TO_VALUES['e'] = 14;
        VALUES[15] = 'f';  CHAR_TO_VALUES['f'] = 15;
        VALUES[16] = 'g';  CHAR_TO_VALUES['g'] = 16;
        VALUES[17] = 'h';  CHAR_TO_VALUES['h'] = 17;
        VALUES[18] = 'i';  CHAR_TO_VALUES['i'] = 18;
        VALUES[19] = 'j';  CHAR_TO_VALUES['j'] = 19;
        VALUES[20] = 'k';  CHAR_TO_VALUES['k'] = 20;
        VALUES[21] = 'l';  CHAR_TO_VALUES['l'] = 21;
        VALUES[22] = 'm';  CHAR_TO_VALUES['m'] = 22;
        VALUES[23] = 'n';  CHAR_TO_VALUES['n'] = 23;
        VALUES[24] = 'o';  CHAR_TO_VALUES['o'] = 24;
        VALUES[25] = 'p';  CHAR_TO_VALUES['p'] = 25;
        VALUES[26] = 'q';  CHAR_TO_VALUES['q'] = 26;
        VALUES[27] = 'r';  CHAR_TO_VALUES['r'] = 27;
        VALUES[28] = 's';  CHAR_TO_VALUES['s'] = 28;
        VALUES[29] = 't';  CHAR_TO_VALUES['t'] = 29;
        VALUES[30] = 'u';  CHAR_TO_VALUES['u'] = 30;
        VALUES[31] = 'x';  CHAR_TO_VALUES['x'] = 31;
    }

    private final static byte MASK_5LBS = Byte.parseByte("00011111", 2);
    private final static byte MASK_4LBS = Byte.parseByte("00001111", 2);
    private final static byte MASK_3LBS = Byte.parseByte("00000111", 2);
    private final static byte MASK_2LBS = Byte.parseByte("00000011", 2);
    private final static byte MASK_1LBS = Byte.parseByte("00000001", 2);

    private static byte getValueOfChar(char c) {
        return CHAR_TO_VALUES[c];
    }

    private static long getLongFromStringId(String id, final int startIndex) {
        final int lastIndex = startIndex+13;
        long l = 0;
        for(int i=startIndex;i<lastIndex;i++) {
            if(i<lastIndex-1) {
                l = l<<5;
                l |= getValueOfChar(id.charAt(i));
            } else {
                l = l<<4;
                l |= getValueOfChar(id.charAt(i));
            }
        }
        return l;
    }

    private static int getIntFromStringId(String id, final int startIndex) {
        final int lastIndex = startIndex+7;
        int integer = 0;
        for(int i=startIndex;i<lastIndex;i++) {
            if(i<lastIndex-1) {
                integer = integer<<5;
                integer |= getValueOfChar(id.charAt(i));
            } else {
                integer = integer<<2;
                integer |= getValueOfChar(id.charAt(i));
            }
        }
        return integer;
    }

    private static int get5LBSValue(byte b) {
        return (b&MASK_5LBS)&0xff;
    }

    private static int get5LBSValue(int b) {
        return get5LBSValue((byte) b);
    }

    private static int get5LBSValue(long b) {
        return get5LBSValue((byte) b);
    }

    private int counter;
    private long lastTime;
    private long now;
    private final Random random = new SecureRandom();
    private final Supplier<Long> timestampSupplier;
    private final Supplier<Integer> secureSupplier;
    private final int afterIdIndex;

    final char[] chars;

    public SimpleULIDGenerator(byte[] id, Supplier<Long> timestampSupplier, Supplier<Integer> secureSupplier) {

        this.timestampSupplier = timestampSupplier;
        this.secureSupplier = secureSupplier;


        int idLen = (id.length*8)%5==0?(id.length*8)/5:(id.length*8)/5+1;

        chars = new char[13+idLen+7+7+2];
        chars[13] = '-';

        BigInteger idAsBigInt= new BigInteger(id);
        int remBits = id.length*8;

        int index = 14;

        while (remBits>0) {
            remBits -= 5;
            if(remBits>=0){
                byte[] shiftedArray = idAsBigInt.shiftRight(remBits).toByteArray();
                byte lastByte = shiftedArray[shiftedArray.length-1];
                chars[index++] = VALUES[get5LBSValue(lastByte)];
            }else {
                int shift = 5+remBits; // less than 5
                byte[] shiftedArray = idAsBigInt.toByteArray();
                byte lastByte = shiftedArray[shiftedArray.length-1];
                if(shift==4) {
                    chars[index++] = VALUES[(lastByte & MASK_4LBS) & 0xff];
                } else if(shift==3) {
                    chars[index++] = VALUES[(lastByte & MASK_3LBS) & 0xff];
                } else if(shift==2) {
                    chars[index++] = VALUES[(lastByte & MASK_2LBS) & 0xff];
                } else if(shift==1) {
                    chars[index++] = VALUES[(lastByte & MASK_1LBS) & 0xff];
                }
            }
        }

        chars[14+idLen] = '-';

        afterIdIndex = 14+idLen+1;
    }

    private void setTimeAndIncrementCounter() {
        now = timestampSupplier.get();
        if(now>lastTime) {
            lastTime = now;
            counter = random.nextInt(1000000); // counter start from a random point !
        }else {
            ++counter;
        }
        if(counter==Integer.MAX_VALUE) {
            //need to wait a millisecond to reset counter
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                while (timestampSupplier.get()<=lastTime);
            }
            setTimeAndIncrementCounter();
        }
    }

    @Override
    public synchronized ULID generate()
    {
        setTimeAndIncrementCounter();


        chars[0] = VALUES[get5LBSValue(now>>59)];
        chars[1] = VALUES[get5LBSValue(now>>54)];
        chars[2] = VALUES[get5LBSValue(now>>49)];
        chars[3] = VALUES[get5LBSValue(now>>44)];
        chars[4] = VALUES[get5LBSValue(now>>39)];
        chars[5] = VALUES[get5LBSValue(now>>34)];
        chars[6] = VALUES[get5LBSValue(now>>29)];
        chars[7] = VALUES[get5LBSValue(now>>24)];
        chars[8] = VALUES[get5LBSValue(now>>19)];
        chars[9] = VALUES[get5LBSValue(now>>14)];
        chars[10] = VALUES[get5LBSValue(now>>9)];
        chars[11] = VALUES[get5LBSValue(now>>4)];
        chars[12] = VALUES[(((byte)now&MASK_4LBS)&0xff)];

        //----------------------------------------

        int index = afterIdIndex;

        chars[index++] = VALUES[get5LBSValue(counter>>27)];
        chars[index++] = VALUES[get5LBSValue(counter>>22)];
        chars[index++] = VALUES[get5LBSValue(counter>>17)];
        chars[index++] = VALUES[get5LBSValue(counter>>12)];
        chars[index++] = VALUES[get5LBSValue(counter>>7)];
        chars[index++] = VALUES[get5LBSValue(counter>>2)];
        chars[index++] = VALUES[(((byte)counter&MASK_2LBS)&0xff)];

        //----------------------------------------

        final int secure = secureSupplier.get();

        chars[index++] = VALUES[get5LBSValue(secure>>27)];
        chars[index++] = VALUES[get5LBSValue(secure>>22)];
        chars[index++] = VALUES[get5LBSValue(secure>>17)];
        chars[index++] = VALUES[get5LBSValue(secure>>12)];
        chars[index++] = VALUES[get5LBSValue(secure>>7)];
        chars[index++] = VALUES[get5LBSValue(secure>>2)];
        chars[index++] = VALUES[(((byte)secure&MASK_2LBS)&0xff)];



        return new ULIDImpl(now, counter, secure, new String(chars));
    }

    @Override
    public ULID from(String from) {

        return new ULIDImpl(getLongFromStringId(from, 0),
                getIntFromStringId(from, from.length()-14),
                getIntFromStringId(from, from.length()-7), from);
    }


}
