package org.nw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一般情况，vo对应着数据库表，但是有些情况，vo可能扩展一些字段，使用这个注解可以表明这个字段不属于表的字段
 * 在设计统计行的查询时，如果模板中勾选了统计行，那么会对这个字段进行sum查询，实际上，如果这个字段不属于表，那么不应该进行合计
 * 
 * @author xuqc
 * @date 2014-12-5 上午09:27:34
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface NotDBField {

}
