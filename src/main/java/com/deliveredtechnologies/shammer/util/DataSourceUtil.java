package com.deliveredtechnologies.shammer.util;

import java.lang.reflect.Constructor;

/**
 * Created by clong on 1/29/17.
 */
public class DataSourceUtil {
    public static <T> T createInstance(final String className, final Class<T> clazz, final Object... args)
    {
        if (className == null) {
            return null;
        }

        try {
            Class<?> loaded = DataSourceUtil.class.getClassLoader().loadClass(className);
            if (args.length == 0) {
                return clazz.cast(loaded.newInstance());
            }

            Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            Constructor<?> constructor = loaded.getConstructor(argClasses);
            return clazz.cast(constructor.newInstance(args));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
