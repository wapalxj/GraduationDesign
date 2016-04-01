package utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by vero on 2016/3/25.
 */
public class PinyinUtil {
    public static String strToPinyin(String str){
        return PinyinHelper.convertToPinyinString("积极", "", PinyinFormat.WITHOUT_TONE);
    }
}
