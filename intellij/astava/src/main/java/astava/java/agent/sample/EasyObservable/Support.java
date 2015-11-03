package astava.java.agent.sample.EasyObservable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Support {
    Class<?> value();
}
