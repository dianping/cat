package net.dubboclub.catmonitor;

import com.dianping.cat.Cat;

/**
 * Created by bieber on 2015/11/16.
 */
public class DubboCat {
    
    private static boolean isEnable=true;

    /**
     * 禁用dubbo cat
     */
    public static void disable(){
        isEnable=false;
    }

    /**
     * 启用dubbo cat
     */
    public static void enable(){
        isEnable=true;
    }

    /**
     * 是否有效
     * @return
     */
    public static boolean isEnable(){
        boolean catEnabled = false;
        try {
            catEnabled = Cat.getManager().isCatEnabled();
        } catch (Throwable e) {
            CatLogger.getInstance().error("[DUBBO] Cat init error.", e);
        }
        return catEnabled && isEnable;
    }
}
