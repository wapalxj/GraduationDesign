package com.muguihai.rc1.view.face;

public interface Face_Const
{
    String KEY_FACE_ID = "face_id";
    String FACE_PREFIX = "face";  //  表情图像文件名的前缀
    /***
     * 插在EditText或TextView中的文本的前缀
     */
    String FACE_TEXT_PREFIX = "<:";  //  不要使用正则表达式中使用的符号
    /***
     * 插在EditText或TextView中的文本的后缀，和前缀以及face id一起使用
     * e.g.  (:4)
     */
    String FACE_TEXT_SUFFIX = ":>";
}
