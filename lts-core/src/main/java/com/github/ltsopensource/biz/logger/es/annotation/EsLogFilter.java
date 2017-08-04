package com.github.ltsopensource.biz.logger.es.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsLogFilter {

	String name();
	
	Optype opType() default Optype.Term;
	
	String extra() default "";
	
	public enum Optype {
		Term, Match, Range
	}
	
	
}
