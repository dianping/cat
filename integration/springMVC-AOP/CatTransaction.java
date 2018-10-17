import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CatTransaction {
    String type() default "Handler";//"URL MVC Service SQL" is reserved for Cat Transaction Type
    String name() default "";
}