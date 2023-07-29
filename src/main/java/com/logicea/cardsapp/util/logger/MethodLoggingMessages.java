package com.logicea.cardsapp.util.logger;

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;

/**
 * {@literal final} class whose {@literal static} methods are used by {@link ComponentLogger} classes to
 * log their messages.
 *
 * @author jason
 * @see JoinPoint
 */
public final class MethodLoggingMessages {

  private MethodLoggingMessages() {}

  /**
   * Create a message of entrance or completion to / of a method.
   *
   * @param pointInMethod A {@link Loc} enum value signifying entrance to or exit from a method.
   * @param jp The method around which we are applying logging.
   * @return A {@link String} that informs us when we enter or exit a method.
   */
  public static String msg(Loc pointInMethod, JoinPoint jp) {
    String args = Arrays.toString(jp.getArgs());
    return ((pointInMethod == Loc.BEGIN) ? "Making" : "Completed")
        + " the call "
        + jp.getSignature().toShortString()
        + " with arguments: "
        + ((args.length() == 2) // args.length == 2 means that args = "[]", i.e no args
            ? "()"
            : args.substring(1, args.length() - 1));
  }

  /**
   * Create a message of entrance or completion to / of a method without including its last
   * argument. This method is useful for logging around some authentication methods that have the
   * user's password as the last argument, and we want to avoid logging that.
   *
   * @param pointInMethod A {@link Loc} enum value signifying entrance to or exit from a method.
   * @param jp The method around which we are applying logging.
   * @return A {@link String} that informs us when we enter or exit a method.
   */
  public static String msgWithoutLastArgument(Loc pointInMethod, JoinPoint jp) {
    String args =
        Arrays.toString(
            ArrayUtils.remove(
                jp.getArgs(), jp.getArgs().length - 1)); // Don't include the last argument.
    return ((pointInMethod == Loc.BEGIN) ? "Making" : "Completed")
        + " the call "
        + jp.getSignature().toShortString()
        + " with arguments (omitting last argument for security reasons): "
        + ((args.length() == 2) ? "()" : args.substring(1, args.length() - 1));
  }

  /**
   * Create a user-friendly message indicating that a certain {@link Throwable} was thrown by a
   * method.
   *
   * @param jp The method we are applying logging around.
   * @param throwable A {@link Throwable} that was thrown.
   * @return A user-friendly message that indicates that a certain {@link Throwable} was thrown by
   *     the method.
   */
  public static String msg(JoinPoint jp, Class<? extends Throwable> throwable) {
    return "Method "
        + jp.getSignature().toShortString()
        + " threw an instance of "
        + throwable.getSimpleName()
        + "!";
  }
}
