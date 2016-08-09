package factorio.calculator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A non-instantiable class for evaluating mathematical expressions in {@link String}s.
 * @author ricky3350
 */
public class Evaluator {

	private Evaluator() {}

	/**
	 * A {@link Pattern} to match a single token
	 */
	private static final Pattern TOKEN = Pattern.compile("[\\+\\-\\*\\/\\(\\)]|(?:\\-?(?:\\d*\\.)?\\d+)");

	/**
	 * <ul>
	 * <b><i>toPostfix</i></b><br>
	 * <pre> public static {@link List}&lt;{@link String}&gt; toPostfix(String infix)</pre>
	 * Converts the given infix string to postfix notation (reverse polish notation)
	 * @param infix - a mathmatical expression
	 * @return a list of the tokens in the given infix ordered for RPN
	 * </ul>
	 */
	public static List<String> toPostfix(String infix) {
		ArrayDeque<String> stack = new ArrayDeque<>(), oper = new ArrayDeque<>();

		Matcher m = TOKEN.matcher(infix.replaceAll("\\s", "").replaceAll("([\\+\\-\\*\\/])+(\\d)", "$1$2"));

		boolean lastOper = true;
		while (m.find()) {
			String token = m.group();

			try {
				Double.parseDouble(token);
				stack.push(token);
				lastOper = false;
			} catch (NumberFormatException e) {
				if (lastOper && token.equals("-")) {
					m.find();
					String num = m.group();
					stack.push("-" + num);
					lastOper = false;
				} else if (token.equals(")")) {
					lastOper = true;
					while (!oper.peek().equals("(")) {
						stack.push(oper.pop());
					}
					oper.pop();
				} else {
					lastOper = true;
					while (!(oper.isEmpty() || oper.peek().equals("(") || precedence(oper.peek().charAt(0)) < precedence(token.charAt(0)))) {
						stack.push(oper.pop());
					}
					oper.push(token);
				}
			}
		}

		while (!oper.isEmpty()) {
			stack.push(oper.pop());
		}

		return new ArrayList<>(stack);
	}

	/**
	 * <ul>
	 * <b><i>precedence</i></b><br>
	 * <pre> private static int precedence(char oper)</pre>
	 * @param oper - the character representing the mathematical operator, or a left parenthesis
	 * @return the operator precedence of the given operator (e.g. a higher value for multiplication than addition)
	 * </ul>
	 */
	private static int precedence(char oper) {
		return oper == '(' ? 2 : (oper == '/' || oper == '*' ? 1 : 0);
	}

	/**
	 * <ul>
	 * <b><i>evaluate</i></b><br>
	 * <pre> public static double evaluate({@link List}&lt;{@link String}&gt; postfix)</pre>
	 * Evaluates the given postfix.
	 * @param postfix - a list of tokens ordered in postfix notation
	 * @return the result of the given postfix expression
	 * @throws IllegalArgumentException if the given postfix could not be evaluated properly.
	 * @see {@link #evaluate(String)}
	 * </ul>
	 */
	public static double evaluate(List<String> postfix) {
		try {
			List<String> pfx = new ArrayList<>(postfix);
			Collections.reverse(pfx);

			ArrayDeque<Double> stack = new ArrayDeque<>();

			for (String token : pfx) {
				try {
					double d = Double.parseDouble(token);
					stack.push(d);
				} catch (NumberFormatException e) {
					switch (token) {
						case "+":
							stack.push(stack.pop() + stack.pop());
							break;
						case "-":
							stack.push(-stack.pop() + stack.pop());
							break;
						case "*":
							stack.push(stack.pop() * stack.pop());
							break;
						case "/":
							stack.push(1 / (stack.pop() / stack.pop()));
							break;
					}
				}
			}

			return stack.peek();
		} catch (Exception e) {
			IllegalArgumentException exception = new IllegalArgumentException("Could not evaluate postfix.");
			exception.initCause(e);
			throw exception;
		}
	}

	/**
	 * <ul>
	 * <b><i>evaluate</i></b><br>
	 * <pre> public static double evaluate({@link String} infix)</pre>
	 * A convenience method for converting the given infix expression to postix, and then evaluating it.
	 * @param infix - a mathmatical expression
	 * @return the result of the given infix expression
	 * @see {@link #toPostfix(String)}, {@link #evaluate(List)}
	 * </ul>
	 */
	public static double evaluate(String infix) {
		return evaluate(toPostfix(infix));
	}

}
