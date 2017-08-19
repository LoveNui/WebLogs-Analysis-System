package software.hbase.hbase;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//对应的field一般是TreeMap<String,HbaseDO>
@Target(FIELD) 
@Retention(RUNTIME)
public @interface HbaseOneToOne {
	boolean changeKey() default false;		//是否更改原来的key为orderKey
	String orderKey() default "";	//根据关联对象的哪个列排序
	String joinField();		//关联依据的列
	Class<?> joinTableDao();	//连接表的Dao
	boolean lazy() default true;	//是否主动加载
}
