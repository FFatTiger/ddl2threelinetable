package anno;

import java.lang.annotation.*;

/**
 * @author wzy
 * @date 2021/5/3
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {

    String value() default "";
}
