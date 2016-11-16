package android.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * THIS IS A SHADOW OF THE ANDROID SOURCE CLASS. IT EXISTS ONLY TO PROVIDE UNHIDDEN
 * REFERENCES TO COMPILE AGAINST.
 *
 * Denotes that the annotated element is a multi-user user ID. This is
 * <em>not</em> the same as a UID.
 *
 *
 */
@Retention(SOURCE)
@Target({METHOD, PARAMETER, FIELD})
public @interface UserIdInt {
}
