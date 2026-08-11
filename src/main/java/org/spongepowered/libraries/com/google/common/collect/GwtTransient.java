package org.spongepowered.libraries.com.google.common.collect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@GwtCompatible
@interface GwtTransient {
}
